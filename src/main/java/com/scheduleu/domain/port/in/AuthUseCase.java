package com.scheduleu.domain.port.in;

import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.LoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;

public interface AuthUseCase {
    AuthResponse registrarUsuario(RegistrarUsuarioRequest request);
    AuthResponse login(LoginRequest request);
    void restablecerContrasena(String token, String nuevaContrasena);
}
