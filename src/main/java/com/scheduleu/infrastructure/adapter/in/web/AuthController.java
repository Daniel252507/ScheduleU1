package com.scheduleu.infrastructure.adapter.in.web;

import com.scheduleu.domain.port.in.AuthUseCase;
import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.LoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RestablecerContrasenaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authService;

    public AuthController(AuthUseCase authService) {
        this.authService = authService;
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
