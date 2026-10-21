package com.scheduleu.domain.port.in;

import com.scheduleu.infrastructure.adapter.in.web.dto.HorarioGeneradoResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.PreferenciasRequest;

import java.util.List;
import java.util.Map;

public interface EstudianteHorarioUseCase {
    Map<String, Object> obtenerPerfil(String correo);
    List<Map<String, Object>> listarAsignaturasDisponibles(String correo);
    Map<String, Object> obtenerPreferencias(String correo);
    Map<String, Object> guardarPreferencias(String correo, PreferenciasRequest request);
    HorarioGeneradoResponse generarHorario(String correo);
    Map<String, Object> confirmarHorario(String correo, HorarioGeneradoResponse.OpcionHorario opcion);
    List<HorarioGeneradoResponse.FilaAsignacion> listarMisAsignaciones(String correo);
}
