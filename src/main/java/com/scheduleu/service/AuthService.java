package com.scheduleu.service;

import com.scheduleu.dto.AuthResponse;
import com.scheduleu.dto.LoginRequest;
import com.scheduleu.dto.RegistrarUsuarioRequest;
import com.scheduleu.model.Usuario;
import com.scheduleu.repository.UsuarioRepository;
import com.scheduleu.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

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
                usuarioGuardado.getNombre(), usuarioGuardado.getCorreo(), usuarioGuardado.getRol());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getContrasena())
        );

        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtTokenProvider.generateToken(usuario.getCorreo(), usuario.getRol().name());

        return new AuthResponse(token, "Bearer", usuario.getIdUsuario(),
                usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }
}
