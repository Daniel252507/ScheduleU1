package com.scheduleu.infrastructure.config;

import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.out.UsuarioPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioPort usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String BCRYPT_PREFIX = "$2a$";
    private static final String TMP_PREFIX = "TMP_NO_ENCRYPTED_";

    private void upsertUsuario(String correo, String nombre, String rawPassword, Usuario.RolUsuario rol) {
        Usuario u = usuarioRepository.findByCorreo(correo).orElse(null);
        boolean created = (u == null);
        if (created) {
            u = new Usuario();
            u.setCorreo(correo);
            System.out.println("=== [INIT] Creando usuario nuevo: " + correo + " ===");
        }
        u.setNombre(nombre);
        u.setRol(rol);

        boolean needsReencrypt = false;
        String storedPass = u.getContrasena();

        if (storedPass == null) {
            needsReencrypt = true;
            System.out.println("=== [FIX] " + correo + ": contraseña nula, encriptando... ===");
        } else if (storedPass.startsWith(TMP_PREFIX)) {
            needsReencrypt = true;
            System.out.println("=== [FIX] " + correo + ": detectado prefijo TMP_NO_ENCRYPTED_, re-encriptando contraseña... ===");
        } else if (!storedPass.startsWith(BCRYPT_PREFIX)) {
            needsReencrypt = true;
            System.out.println("=== [FIX] " + correo + ": contraseña no está en formato BCrypt, re-encriptando... ===");
        } else if (!passwordEncoder.matches(rawPassword, storedPass)) {
            needsReencrypt = true;
            System.out.println("=== [FIX] " + correo + ": contraseña almacenada no coincide con la esperada, re-encriptando... ===");
        }

        if (needsReencrypt) {
            u.setContrasena(passwordEncoder.encode(rawPassword));
            System.out.println("=== [OK] " + correo + ": contraseña encriptada correctamente ===");
        } else {
            System.out.println("=== [SKIP] " + correo + ": contraseña ya es válida, sin cambios ===");
        }

        usuarioRepository.save(u);
    }

    @Override
    public void run(String... args) {
        System.out.println("======================================================");
        System.out.println("=== DataInitializer: Inicializando usuarios del sistema ===");
        System.out.println("======================================================");

        upsertUsuario("admin@scheduleu.com",
                "Administrador ScheduleU",
                "admin123",
                Usuario.RolUsuario.ADMINISTRADOR);

        upsertUsuario("coordinador@scheduleu.com",
                "Coordinador Academico",
                "coord123",
                Usuario.RolUsuario.COORDINADOR_ACADEMICO);

        upsertUsuario("estudiante@scheduleu.com",
                "Estudiante Prueba Semestre 5",
                "estudiante123",
                Usuario.RolUsuario.ESTUDIANTE);

        System.out.println("======================================================");
        System.out.println("=== DataInitializer: Finalizado. Credenciales por defecto:");
        System.out.println("===   ADMIN:      admin@scheduleu.com      / admin123");
        System.out.println("===   COORDINADOR: coordinador@scheduleu.com / coord123");
        System.out.println("===   ESTUDIANTE: estudiante@scheduleu.com  / estudiante123");
        System.out.println("======================================================");
    }
}
