# Guía de despliegue de ScheduleU (Vercel + AWS EC2 + Supabase)

Esta guía deja la aplicación publicada en internet usando **solo servicios gratuitos**:

| Capa | Plataforma | Costo |
|------|-----------|-------|
| Frontend (React + Vite) | **Vercel** (sitio estático, HTTPS) | Gratis |
| Backend (Spring Boot) | **AWS EC2** (Docker, t2/t3.micro free tier) | Gratis 12 meses |
| Base de datos (PostgreSQL) | **Supabase** | Gratis |

## Arquitectura del despliegue

```
Navegador  ──HTTPS──►  Vercel (frontend)  ──rewrite /api/*──►  EC2 :8080 (backend)  ──►  Supabase (PostgreSQL)
                        vuevang.vercel.app   (HTTP interno)      http://IP_ELASTICA:8080
```

El navegador **nunca** llama directo al backend: entra por Vercel y Vercel reenvía las
peticiones `/api/*` a la EC2. Así se evita el bloqueo de **mixed content** (página HTTPS
llamando a un backend HTTP) y también el CORS.

---

## Cambios que ya se hicieron en el repositorio

- **Backend migrado a PostgreSQL** (`pom.xml` + `src/main/resources/application.properties`).
  Se conserva el driver de MySQL para el desarrollo local; el driver y el dialecto ahora
  se detectan solos a partir de la URL JDBC.
- **`frontend/vercel.json`** con el `rewrite` de `/api/*` hacia la EC2 y el fallback SPA.
- **`docker-compose.prod.yml`** actualizado con las variables `GOOGLE_CLIENT_ID` y `GEMINI_*`.
- **Eliminadas** las carpetas duplicadas y desactualizadas `docker_files/` y `docker_compose/`.

> Falta **una acción manual**: reemplazar `REEMPLAZA_CON_TU_ELASTIC_IP` en
> `frontend/vercel.json` por la IP elástica de la EC2 (Paso 3).

---

## Paso 1 — Base de datos en Supabase (PostgreSQL)

> ✅ Este proyecto de Supabase **ya está creado**. Estos son los datos de conexión reales
> (obtenidos del **Session pooler** en *Project Settings → Database*):
>
> | Variable de entorno | Valor |
> |---------------------|-------|
> | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require` |
> | `SPRING_DATASOURCE_USERNAME` | `postgres.skpvoqrddxqfhvtrpbyx` |
> | `SPRING_DATASOURCE_PASSWORD` | `D@n1002460579` |
>
> El archivo `scheduleu.env` ya tiene estos valores listos para subir a la EC2.

**Sobre los datos:**

- El **esquema de tablas lo crea Hibernate solo** al arrancar (`ddl-auto=update`).
- Los **usuarios por defecto** los crea `DataInitializer` al iniciar el backend:

  | Rol | Correo | Contraseña |
  |-----|--------|-----------|
  | ADMINISTRADOR | `admin@scheduleu.com` | `admin123` |
  | COORDINADOR_ACADEMICO | `coordinador@scheduleu.com` | `coord123` |
  | ESTUDIANTE | `estudiante@scheduleu.com` | `estudiante123` |

- El script `INSTALAR_TODO.sql` (asignaturas, docentes, salones, grupos…) usa sintaxis
  **MySQL** y **no se puede ejecutar tal cual** en PostgreSQL. Esos datos hay que
  crearlos desde la aplicación con el rol ADMINISTRADOR, o convertir el script si los
  necesitas de ejemplo.

> ⚠️ Los proyectos gratuitos de Supabase se **pausan tras ~1 semana de inactividad**.
> Despiértalos desde el panel antes de usarlos.

---

## Paso 2 — Backend en AWS EC2 (Docker)

### 2.1 Crear la instancia

1. Consola AWS → **EC2 → Launch instance**.
   - **Name**: `scheduleu-backend`
   - **AMI**: Ubuntu Server 24.04 LTS (free tier eligible)
   - **Instance type**: `t2.micro` o `t3.micro`
   - **Key pair**: crea uno nuevo y **descarga el archivo `.pem`** (guárdalo seguro).
   - **Network settings → Edit → Security group rules**:
     - `SSH` (22) → *My IP*
     - `Custom TCP` (8080) → `0.0.0.0/0`  ← la necesita Vercel para reenviar `/api`
   - **Storage**: 20 GB gp3.
2. **Elastic IP (obligatorio):** EC2 → **Elastic IPs → Allocate Elastic IP address** y luego
   **Associate** con la instancia. Sin esto, la IP cambia al reiniciar y el proxy de Vercel
   dejaría de funcionar. **Anota esa IP**, la usarás en los pasos 2.5 y 3.

### 2.2 Conectarte por SSH (Windows PowerShell)

```powershell
# Da permisos solo al archivo de clave (si usas OpenSSH en Windows)
icacls "C:\ruta\clave.pem" /inheritance:r /grant:r "$($env:USERNAME):(R)"

ssh -i "C:\ruta\clave.pem" ubuntu@IP_ELASTICA
```

### 2.3 Instalar Docker en la instancia

```bash
sudo apt-get update
sudo apt-get install -y docker.io
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
# Cierra la sesión SSH y vuelve a entrar para aplicar el grupo docker
```

### 2.4 Construir y subir la imagen (desde tu PC, no en la EC2)

> La instancia `t2/t3.micro` tiene solo 1 GB de RAM y una compilación Maven podría
> quedarse sin memoria. Por eso se construye en local y se sube a Docker Hub.

```powershell
# En la raíz del proyecto, en tu PC:
docker build -f Dockerfile.backend -t danielp2507/scheduleu-backend:latest .
docker login
docker push danielp2507/scheduleu-backend:latest
```

### 2.5 Ejecutar el contenedor en la EC2

Crea un archivo `scheduleu.env` en la EC2 con tus valores reales. Este archivo **ya existe
en el repo** (raíz del proyecto) listo para subir — solo edítalo si quieres cambiar algo:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-us-west-2.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.skpvoqrddxqfhvtrpbyx
SPRING_DATASOURCE_PASSWORD=D@n1002460579
SPRING_JPA_HIBERNATE_DDL_AUTO=update
JWT_SECRET=cambia-esta-clave-por-una-larga-y-aleatoria
JWT_EXPIRATION=86400000
GOOGLE_CLIENT_ID=599416100081-n80andjlulc6i2cb79lcvo5cacuer5fo.apps.googleusercontent.com
GEMINI_API_KEY=tu-api-key-de-google-ai-studio
GEMINI_MODEL=gemini-3.8-flash
GEMINI_MODEL_RESPALDO=gemini-3.7-flash
CORS_ALLOWED_ORIGINS=https://TU-APP.vercel.app
```

Luego descarga y arranca:

```bash
sudo docker pull danielp2507/scheduleu-backend:latest
sudo docker rm -f scheduleu-backend 2>/dev/null

sudo docker run -d --restart unless-stopped --name scheduleu-backend \
  -p 8080:8080 --env-file ~/scheduleu.env \
  danielp2507/scheduleu-backend:latest
```

### 2.6 Verificar

```bash
# Dentro de la EC2
curl http://localhost:8080/api/health      # -> {"status":"ok"}
sudo docker logs -f scheduleu-backend      # revisa que conectó a Supabase y creó usuarios
```

Desde tu PC: abre `http://IP_ELASTICA:8080/api/health` en el navegador.

### 2.7 Actualizar el backend (cada vez que cambies código)

```powershell
# En tu PC
docker build -f Dockerfile.backend -t danielp2507/scheduleu-backend:latest .
docker push danielp2507/scheduleu-backend:latest
```

```bash
# En la EC2
sudo docker pull danielp2507/scheduleu-backend:latest
sudo docker rm -f scheduleu-backend
sudo docker run -d --restart unless-stopped --name scheduleu-backend \
  -p 8080:8080 --env-file ~/scheduleu.env \
  danielp2507/scheduleu-backend:latest
```

---

## Paso 3 — Frontend en Vercel

1. Antes de desplegar, edita `frontend/vercel.json` y reemplaza
   `REEMPLAZA_CON_TU_ELASTIC_IP` por la IP elástica del Paso 2.1:

   ```json
   "destination": "http://TU_IP_ELASTICA:8080/api/:path*"
   ```

2. <https://vercel.com> → **Add New → Project → Import Git Repository** → elige
   `Daniel252507/ScheduleU1`.
3. Configuración:
   - **Root Directory**: `frontend`   ← importante
   - **Framework Preset**: `Vite` (se detecta solo)
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. **Environment Variables** → añade solo esta:
   - `VITE_GOOGLE_CLIENT_ID` = `599416100081-n80andjlulc6i2cb79lcvo5cacuer5fo.apps.googleusercontent.com`

   > **No** definas `VITE_API_URL`: debe quedar sin valor para que el frontend use rutas
   > relativas `/api` y sea Vercel quien las reenvíe a la EC2.
5. **Deploy**. Al terminar obtendrás una URL tipo `https://scheduleu-xxxx.vercel.app`.

---

## Paso 4 — Autorizar el dominio en Google Cloud Console

Para que funcione el botón "Continuar con Google" y la exportación a Google Calendar:

1. <https://console.cloud.google.com> → **APIs & Services → Credentials**.
2. Abre tu **OAuth 2.0 Client ID** (tipo *Web application*).
3. En **Authorized JavaScript origins** agrega la URL de Vercel:
   - `https://scheduleu-xxxx.vercel.app`
   - (deja también `http://localhost:5173` y `http://localhost:3000` para el desarrollo local)
4. **Save**. Los cambios pueden tardar unos minutos.

---

## Paso 5 — Verificación final

1. Abre `https://TU-APP.vercel.app/api/health` → debe responder `{"status":"ok"}`.
   Esto confirma que el proxy de Vercel llega al backend.
2. Abre `https://TU-APP.vercel.app` e inicia sesión con `admin@scheduleu.com` / `admin123`.
3. Prueba: crear una asignatura/grupo, el login con Google y el asistente de IA.

---

## Problemas frecuentes

| Síntoma | Causa / solución |
|---------|------------------|
| `{"status":"ok"}` en Vercel no responde o da 500 | La EC2 no está corriendo, cambiaron su IP o el puerto 8080 está cerrado en el Security Group. Revisa `sudo docker logs scheduleu-backend`. |
| El navegador bloquea llamadas "mixed content" | Estás apuntando el frontend directo a la IP por HTTP. Usa el proxy de Vercel (`VITE_API_URL` vacío + `rewrites`). |
| Cambió la IP de la EC2 | Asóciale una **Elastic IP** y actualiza `frontend/vercel.json`; vuelve a desplegar en Vercel. |
| El backend no arranca y el log dice "connection refused" a la BD | Revisa la cadena de Supabase: usa el **Session pooler** (:5432) y `?sslmode=require`. |
| Supabase responde pero sin tablas | Espera a que el backend termine de arrancar (`ddl-auto=update` crea el esquema). Confirma en el **Table Editor** de Supabase que aparece `usuarios`, `asignaturas`, etc. |
| El proyecto de Supabase se pausó | Reactívalo desde el panel de Supabase (free tier se pausa por inactividad). |

> 🔐 **Seguridad:** el `JWT_SECRET` del repositorio es público. Cámbialo por uno largo y
> aleatorio en producción (variable `JWT_SECRET` del Paso 2.5).
