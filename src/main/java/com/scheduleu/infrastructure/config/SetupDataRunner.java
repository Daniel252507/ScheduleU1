package com.scheduleu.infrastructure.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SetupDataRunner {
    @Bean
    public CommandLineRunner setupParcheSQL() {
        return (args) -> {
            // No-op: los datos se cargan via schema_datos_reales_semestre5.sql en phpMyAdmin
            System.out.println("=== Recuerda importar schema_datos_reales_semestre5.sql y parche_nuevo_modelo.sql en MySQL ===");
        };
    }
}
