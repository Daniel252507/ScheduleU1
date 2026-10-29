package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.port.in.AuthUseCase;
import com.scheduleu.domain.port.in.GoogleAuthUseCase;
import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.GoogleLoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.LoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RestablecerContrasenaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authService;
    private final GoogleAuthUseCase googleAuthService;

    public AuthController(AuthUseCase authService, GoogleAuthUseCase googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> registrarUsuario(@RequestBody RegistrarUsuarioRequest request) {
        try {
            AuthResponse response = authService.registrarUsuario(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginConGoogle(@RequestBody GoogleLoginRequest request) {
        try {
            return new ResponseEntity<>(googleAuthService.loginConGoogle(request.getIdToken()), HttpStatus.OK);
        } catch (RuntimeException e) {
            // Se devuelve el motivo para que el usuario sepa si su correo no
            // esta registrado o si el token de Google resulto invalido.
            String motivo = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "No se pudo iniciar sesion con Google"
                    : e.getMessage();
            return new ResponseEntity<>(Map.of("error", motivo), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<Void> restablecerContrasena(@RequestBody RestablecerContrasenaRequest request) {
        try {
            authService.restablecerContrasena(request.getToken(), request.getContrasena());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
