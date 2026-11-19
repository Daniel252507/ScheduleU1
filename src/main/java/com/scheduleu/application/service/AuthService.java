package com.scheduleu.application.service;

import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.in.AuthUseCase;
import com.scheduleu.domain.port.out.UsuarioPort;
import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.LoginRequest;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistrarUsuarioRequest;
import com.scheduleu.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {

    private final UsuarioPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioPort usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse registrarUsuario(RegistrarUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        usuario.setRol(Usuario.RolUsuario.valueOf(request.getRol().toUpperCase()));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        String token = jwtTokenProvider.generateToken(usuarioGuardado.getCorreo(), usuarioGuardado.getRol().name());

        return new AuthResponse(token, "Bearer", usuarioGuardado.getIdUsuario(),
                usuarioGuardado.getNombre(), usuarioGuardado.getCorreo(), usuarioGuardado.getRol(), false);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtTokenProvider.generateToken(usuario.getCorreo(), usuario.getRol().name());

        return new AuthResponse(token, "Bearer", usuario.getIdUsuario(),
                usuario.getNombre(), usuario.getCorreo(), usuario.getRol(), false);
    }
}
