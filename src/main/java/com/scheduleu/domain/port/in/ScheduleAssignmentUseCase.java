package com.scheduleu.domain.port.in;

import java.util.Map;

public interface ScheduleAssignmentUseCase {
    Map<String, Object> ejecutarPrimeraAsignacion();
    Map<String, Object> ejecutarSegundaAsignacion();
}
