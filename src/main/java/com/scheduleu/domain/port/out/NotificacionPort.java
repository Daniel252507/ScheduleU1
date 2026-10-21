package com.scheduleu.domain.port.out;

public interface NotificacionPort {
    void enviarEnlaceEstablecimientoContrasena(String destinatario, String nombre, String enlace);
}
