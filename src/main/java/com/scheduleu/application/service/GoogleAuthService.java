package com.scheduleu.application.service;

import com.scheduleu.domain.model.Estudiante;
import com.scheduleu.domain.model.IdentidadExterna;
import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.in.EstudianteUseCase;
import com.scheduleu.domain.port.in.GoogleAuthUseCase;
import com.scheduleu.domain.port.out.ProveedorIdentidadPort;
import com.scheduleu.domain.port.out.UsuarioPort;
import com.scheduleu.infrastructure.adapter.in.web.dto.AuthResponse;
import com.scheduleu.infrastructure.adapter.in.web.dto.RegistroGoogleRequest;
import com.scheduleu.infrastructure.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

/**
 * Caso de uso de inicio de sesion con Google.
 *
 * La verificacion criptografica del token la realiza el adaptador del puerto
 * {@link ProveedorIdentidadPort}; aqui se resuelve la identidad acreditada
 * contra los usuarios de ScheduleU y se emite la sesion propia de la aplicacion.
 *
 * Si el correo no existe todavia, el usuario completa su alta como estudiante
 * aportando su codigo academico y semestre (no se envian correos).
 */
@Service
public class GoogleAuthService implements GoogleAuthUseCase {

    private final ProveedorIdentidadPort proveedorIdentidad;
    private final UsuarioPort usuarioRepository;
    private final EstudianteUseCase estudianteUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    public GoogleAuthService(ProveedorIdentidadPort proveedorIdentidad,
                             UsuarioPort usuarioRepository,
                             EstudianteUseCase estudianteUseCase,
                             JwtTokenProvider jwtTokenProvider) {
        this.proveedorIdentidad = proveedorIdentidad;
        this.usuarioRepository = usuarioRepository;
        this.estudianteUseCase = estudianteUseCase;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse loginConGoogle(String idToken) {
        IdentidadExterna identidad = proveedorIdentidad.verificarIdToken(idToken);

        return usuarioRepository.findByCorreo(identidad.getCorreo())
                .map(this::crearSesion)
                // Correo desconocido: se le pide crear su cuenta de estudiante.
                .orElseGet(() -> solicitudDeRegistro(identidad));
    }

    @Override
    public AuthResponse registrarEstudianteConGoogle(RegistroGoogleRequest request) {
        IdentidadExterna identidad = proveedorIdentidad.verificarIdToken(request.getIdToken());

        // Si otra peticion ya lo registro (o el administrador lo creo antes),
        // simplemente se inicia sesion.
        Usuario existente = usuarioRepository.findByCorreo(identidad.getCorreo()).orElse(null);
        if (existente != null) {
            return crearSesion(existente);
        }

        String codigo = request.getCodigo() == null ? "" : request.getCodigo().trim();
        if (codigo.isEmpty()) {
            throw new RuntimeException("El codigo academico es obligatorio");
        }
        if (request.getSemestre() == null || request.getSemestre() < 1) {
            throw new RuntimeException("El semestre es obligatorio");
        }
        if (estudianteUseCase.buscarPorCodigo(codigo).isPresent()) {
            throw new RuntimeException("El codigo " + codigo + " ya esta registrado");
        }

        // El nombre lo aporta el formulario; si no llega, se usa el del perfil
        // de Google verificado en el token.
        String nombre = request.getNombre() == null ? "" : request.getNombre().trim();
        if (nombre.isEmpty()) {
            nombre = identidad.getNombre();
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(nombre);
        estudiante.setCodigo(codigo);
        estudiante.setSemestre(request.getSemestre());
        estudiante.setCorreo(identidad.getCorreo());
        // El servicio de estudiantes crea el usuario de acceso con rol ESTUDIANTE.
        estudianteUseCase.guardar(estudiante);

        Usuario usuario = usuarioRepository.findByCorreo(identidad.getCorreo())
                .orElseThrow(() -> new RuntimeException("No se pudo crear el usuario"));
        return crearSesion(usuario);
    }

    private AuthResponse crearSesion(Usuario usuario) {
        String token = jwtTokenProvider.generateToken(usuario.getCorreo(), usuario.getRol().name());
        return new AuthResponse(token, "Bearer", usuario.getIdUsuario(),
                usuario.getNombre(), usuario.getCorreo(), usuario.getRol(), false);
    }

    private AuthResponse solicitudDeRegistro(IdentidadExterna identidad) {
        return new AuthResponse(null, "Bearer", null,
                identidad.getNombre(), identidad.getCorreo(), null, true);
    }
}
