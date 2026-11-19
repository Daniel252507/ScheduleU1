package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.port.in.AuthUseCase;
import com.scheduleu.domain.port.in.GoogleAuthUseCase;
import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.GoogleLoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.LoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistroGoogleRequest;
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
            // Si el correo no esta registrado, la respuesta trae
            // requiereRegistro = true con los datos para completar el alta.
            return new ResponseEntity<>(googleAuthService.loginConGoogle(request.getIdToken()), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", motivo(e)), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Completa el alta del estudiante que ingresa por primera vez con Google.
     */
    @PostMapping("/google/registro")
    public ResponseEntity<?> registrarConGoogle(@RequestBody RegistroGoogleRequest request) {
        try {
            return new ResponseEntity<>(googleAuthService.registrarEstudianteConGoogle(request), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("error", motivo(e)), HttpStatus.BAD_REQUEST);
        }
    }

    /** Motivo legible del fallo para mostrarlo en el frontend. */
    private String motivo(RuntimeException e) {
        return (e.getMessage() == null || e.getMessage().isBlank())
                ? "No se pudo completar la operacion con Google"
                : e.getMessage();
    }
}
