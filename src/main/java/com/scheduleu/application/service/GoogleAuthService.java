package com.scheduleu.application.service;

import com.scheduleu.domain.model.IdentidadExterna;
import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.in.GoogleAuthUseCase;
import com.scheduleu.domain.port.out.ProveedorIdentidadPort;
import com.scheduleu.domain.port.out.UsuarioPort;
import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

/**
 * Caso de uso de inicio de sesion con Google.
 *
 * La verificacion criptografica del token la realiza el adaptador del puerto
 * {@link ProveedorIdentidadPort}; aqui solo se resuelve la identidad acreditada
 * contra los usuarios de ScheduleU y se emite la sesion propia de la aplicacion.
 */
@Service
public class GoogleAuthService implements GoogleAuthUseCase {

    private final ProveedorIdentidadPort proveedorIdentidad;
    private final UsuarioPort usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public GoogleAuthService(ProveedorIdentidadPort proveedorIdentidad,
                             UsuarioPort usuarioRepository,
                             JwtTokenProvider jwtTokenProvider) {
        this.proveedorIdentidad = proveedorIdentidad;
        this.usuarioRepository = usuarioRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse loginConGoogle(String idToken) {
        IdentidadExterna identidad = proveedorIdentidad.verificarIdToken(idToken);

        // Solo pueden entrar cuentas ya registradas: el administrador es quien
        // crea a los estudiantes y quien define su rol dentro del sistema.
        Usuario usuario = usuarioRepository.findByCorreo(identidad.getCorreo())
                .orElseThrow(() -> new RuntimeException(
                        "No hay una cuenta registrada con el correo " + identidad.getCorreo()));

        String token = jwtTokenProvider.generateToken(usuario.getCorreo(), usuario.getRol().name());

        return new AuthResponse(token, "Bearer", usuario.getIdUsuario(),
                usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }
}
