package com.scheduleu.infrastructure.adapter.out.email;

import com.scheduleu.domain.port.out.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpNotificacionAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(SmtpNotificacionAdapter.class);

    private final JavaMailSender mailSender;
    private final String remitente;
    private final boolean habilitado;

    public SmtpNotificacionAdapter(JavaMailSender mailSender,
                                   @Value("${spring.mail.username:}") String remitente,
                                   @Value("${app.mail.enabled:true}") boolean habilitado) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.habilitado = habilitado;
    }

    @Override
    public void enviarEnlaceEstablecimientoContrasena(String destinatario, String nombre, String enlace) {
        if (!habilitado || remitente == null || remitente.isBlank()) {
            log.warn("[MAIL no configurado] Enlace para establecer contrasena de {} ({}): {}",
                    nombre, destinatario, enlace);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("ScheduleU - Establece tu contrasena");
            mensaje.setText("Hola " + nombre + ",\n\n"
                    + "Se creo tu cuenta en ScheduleU. Para establecer tu contrasena abre este enlace:\n\n"
                    + enlace + "\n\n"
                    + "El enlace es valido por 48 horas. Si no solicitaste esto, ignora este mensaje.");
            mailSender.send(mensaje);
            log.info("Enlace de establecimiento de contrasena enviado a {}", destinatario);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
            log.warn("[MAIL fallback] Enlace para establecer contrasena de {}: {}", destinatario, enlace);
        }
    }
}
