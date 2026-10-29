package com.scheduleu.infrastructure.adapter.out.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduleu.domain.model.IdentidadExterna;
import com.scheduleu.domain.port.out.ProveedorIdentidadPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Adaptador del puerto {@link ProveedorIdentidadPort} para Google Sign-In.
 *
 * Google entrega un ID token (un JWT firmado con RS256) al frontend. Este
 * adaptador comprueba tres cosas antes de aceptar la identidad:
 *
 *   1. Que la cabecera declare RS256 y traiga el identificador de clave (kid),
 *      lo que descarta los ataques de confusion de algoritmo.
 *   2. Que la firma corresponda a una de las claves publicas que Google
 *      publica en su endpoint JWKS.
 *   3. Que las claims sean coherentes: emisor de Google, audiencia igual al
 *      Client ID de la aplicacion y token no expirado.
 *
 * Se implementa con las utilidades de la plataforma (java.net.http + Jackson)
 * en lugar de la libreria google-api-client para no cargar el contenedor: el
 * plan gratuito de despliegue dispone de muy poca memoria.
 */
@Component
public class GoogleIdentityAdapter implements ProveedorIdentidadPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleIdentityAdapter.class);

    private static final String URL_CLAVES_PUBLICAS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> EMISORES_VALIDOS =
            Set.of("accounts.google.com", "https://accounts.google.com");
    private static final Duration VIGENCIA_CACHE_CLAVES = Duration.ofHours(6);
    private static final long TOLERANCIA_RELOJ_SEGUNDOS = 60;

    private final ObjectMapper objectMapper;
    private final String clientId;
    private final HttpClient clienteHttp;

    /** Claves publicas de Google indexadas por `kid`. */
    private volatile Map<String, PublicKey> clavesPublicas = Map.of();
    private volatile Instant clavesDescargadasEn = Instant.EPOCH;

    public GoogleIdentityAdapter(ObjectMapper objectMapper,
                                 @Value("${google.client-id:}") String clientId) {
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.clienteHttp = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public IdentidadExterna verificarIdToken(String idToken) {
        if (clientId == null || clientId.isBlank()) {
            throw new RuntimeException("El inicio de sesion con Google no esta configurado");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("No se recibio el token de Google");
        }

        String[] partes = idToken.split("\\.");
        if (partes.length != 3) {
            throw new RuntimeException("El token de Google tiene un formato invalido");
        }

        // 1. Cabecera: solo se admite RS256 y se exige el identificador de clave.
        JsonNode cabecera = decodificarSegmento(partes[0]);
        if (!"RS256".equals(cabecera.path("alg").asText())) {
            throw new RuntimeException("El token de Google usa un algoritmo de firma no permitido");
        }
        String kid = cabecera.path("kid").asText("");
        if (kid.isBlank()) {
            throw new RuntimeException("El token de Google no indica la clave de firma");
        }

        // 2. Firma: se calcula sobre el contenido "cabecera.cuerpo" sin decodificar.
        verificarFirma(obtenerClavePublica(kid), partes[0] + "." + partes[1], partes[2]);

        // 3. Claims: emisor, audiencia, vigencia y correo verificado.
        JsonNode claims = decodificarSegmento(partes[1]);
        validarClaims(claims);

        String correo = claims.path("email").asText("");
        if (correo.isBlank()) {
            throw new RuntimeException("El token de Google no incluye un correo");
        }

        return new IdentidadExterna(correo, claims.path("name").asText(correo), true);
    }

    /**
     * Devuelve la clave publica del `kid` indicado, refrescando la cache si
     * esta caduco o si Google ha rotado sus claves y el `kid` es nuevo.
     */
    private PublicKey obtenerClavePublica(String kid) {
        PublicKey clave = clavesPublicas.get(kid);
        if (clave == null) {
            descargarClavesPublicas(true);
            clave = clavesPublicas.get(kid);
        } else if (Instant.now().isAfter(clavesDescargadasEn.plus(VIGENCIA_CACHE_CLAVES))) {
            descargarClavesPublicas(false);
            clave = clavesPublicas.get(kid);
        }
        if (clave == null) {
            throw new RuntimeException("Google no publica una clave con el identificador del token");
        }
        return clave;
    }

    private synchronized void descargarClavesPublicas(boolean forzar) {
        // Otra hebra pudo refrescar mientras esta esperaba el monitor.
        boolean cacheUtil = !clavesPublicas.isEmpty()
                && Instant.now().isBefore(clavesDescargadasEn.plus(VIGENCIA_CACHE_CLAVES));
        if (!forzar && cacheUtil) {
            return;
        }

        try {
            HttpRequest peticion = HttpRequest.newBuilder(URI.create(URL_CLAVES_PUBLICAS))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new RuntimeException("Google respondio " + respuesta.statusCode()
                        + " al publicar sus claves");
            }

            Map<String, PublicKey> descargadas = new HashMap<>();
            for (JsonNode clave : objectMapper.readTree(respuesta.body()).path("keys")) {
                if (!"RSA".equals(clave.path("kty").asText())) {
                    continue;
                }
                BigInteger modulo = new BigInteger(1, Base64.getUrlDecoder().decode(clave.path("n").asText()));
                BigInteger exponente = new BigInteger(1, Base64.getUrlDecoder().decode(clave.path("e").asText()));
                PublicKey publica = KeyFactory.getInstance("RSA")
                        .generatePublic(new RSAPublicKeySpec(modulo, exponente));
                descargadas.put(clave.path("kid").asText(), publica);
            }

            if (descargadas.isEmpty()) {
                throw new RuntimeException("Google no publico ninguna clave RSA");
            }

            clavesPublicas = descargadas;
            clavesDescargadasEn = Instant.now();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Se interrumpio la validacion del token de Google");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("No se pudieron obtener las claves publicas de Google", e);
            throw new RuntimeException("No fue posible validar el token de Google");
        }
    }

    private void verificarFirma(PublicKey clave, String contenidoFirmado, String firmaBase64Url) {
        try {
            Signature verificador = Signature.getInstance("SHA256withRSA");
            verificador.initVerify(clave);
            verificador.update(contenidoFirmado.getBytes(StandardCharsets.US_ASCII));
            if (!verificador.verify(Base64.getUrlDecoder().decode(firmaBase64Url))) {
                throw new RuntimeException("La firma del token de Google no es valida");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No fue posible verificar la firma del token de Google");
        }
    }

    private void validarClaims(JsonNode claims) {
        if (!EMISORES_VALIDOS.contains(claims.path("iss").asText(""))) {
            throw new RuntimeException("El token de Google fue emitido por un emisor desconocido");
        }

        boolean audienciaValida = false;
        JsonNode audiencia = claims.path("aud");
        if (audiencia.isArray()) {
            for (JsonNode valor : audiencia) {
                if (clientId.equals(valor.asText())) {
                    audienciaValida = true;
                }
            }
        } else {
            audienciaValida = clientId.equals(audiencia.asText());
        }
        if (!audienciaValida) {
            throw new RuntimeException("El token de Google no fue emitido para esta aplicacion");
        }

        long expiracion = claims.path("exp").asLong(0);
        if (expiracion == 0 || Instant.now().getEpochSecond() > expiracion + TOLERANCIA_RELOJ_SEGUNDOS) {
            throw new RuntimeException("El token de Google esta expirado");
        }

        if (!claims.path("email_verified").asBoolean(false)) {
            throw new RuntimeException("Google no ha verificado ese correo");
        }
    }

    private JsonNode decodificarSegmento(String segmentoBase64Url) {
        try {
            return objectMapper.readTree(Base64.getUrlDecoder().decode(segmentoBase64Url));
        } catch (Exception e) {
            throw new RuntimeException("El token de Google tiene un formato invalido");
        }
    }
}
