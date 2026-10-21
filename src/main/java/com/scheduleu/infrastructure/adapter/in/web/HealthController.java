package com.scheduleu.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de salud público.
 *
 * Lo usan las plataformas de despliegue (Render, etc.) para verificar que el
 * servicio está arriba y, opcionalmente, para mantenerlo despierto con un cron.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
