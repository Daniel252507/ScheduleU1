/**
 * Composition root (contenedor de dependencias).
 *
 * Instancia el adaptador HTTP, lo inyecta en los servicios de aplicación y
 * exporta los servicios ya cableados y listos para usar. La capa de
 * infraestructura (componentes y vistas) consume estos servicios.
 *
 * @module infrastructure/config/container
 */

import { HttpScheduleuGateway } from '../adapter/http/HttpScheduleuGateway.js'
import { createAuthService } from '../../application/service/authService.js'
import { createAsignaturasService } from '../../application/service/asignaturasService.js'
import { createEstudiantesService } from '../../application/service/estudiantesService.js'
import { createDocentesService } from '../../application/service/docentesService.js'
import { createSalonesService } from '../../application/service/salonesService.js'
import { createGruposService } from '../../application/service/gruposService.js'
import { createHorariosService } from '../../application/service/horariosService.js'
import { createAsignacionesService } from '../../application/service/asignacionesService.js'
import { createMisHorariosService } from '../../application/service/misHorariosService.js'

/**
 * Adaptador único de acceso a datos (inyectado en todos los servicios).
 * En local y en el contenedor nginx se usa `/api` (mismo origen).
 * Al desplegar el frontend como sitio estático separado, se define
 * VITE_API_URL con la URL pública del backend.
 */
const gateway = new HttpScheduleuGateway(import.meta.env.VITE_API_URL || '/api')

export const authService = createAuthService(gateway)
export const asignaturasService = createAsignaturasService(gateway)
export const estudiantesService = createEstudiantesService(gateway)
export const docentesService = createDocentesService(gateway)
export const salonesService = createSalonesService(gateway)
export const gruposService = createGruposService(gateway)
export const horariosService = createHorariosService(gateway)
export const asignacionesService = createAsignacionesService(gateway)
export const misHorariosService = createMisHorariosService(gateway)

export const services = {
  auth: authService,
  asignaturas: asignaturasService,
  estudiantes: estudiantesService,
  docentes: docentesService,
  salones: salonesService,
  grupos: gruposService,
  horarios: horariosService,
  asignaciones: asignacionesService,
  misHorarios: misHorariosService,
}

export default services
