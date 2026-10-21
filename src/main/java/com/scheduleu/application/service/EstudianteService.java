package com.scheduleu.application.service;

import com.scheduleu.domain.model.Estudiante;
import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.in.EnlaceContrasenaUseCase;
import com.scheduleu.domain.port.in.EstudianteUseCase;
import com.scheduleu.domain.port.out.EstudiantePort;
import com.scheduleu.domain.port.out.TokenRecuperacionPort;
import com.scheduleu.domain.port.out.UsuarioPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EstudianteService implements EstudianteUseCase {

    private final EstudiantePort estudianteRepository;
    private final UsuarioPort usuarioRepository;
    private final TokenRecuperacionPort tokenRecuperacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnlaceContrasenaUseCase enlaceContrasenaUseCase;

    public EstudianteService(EstudiantePort estudianteRepository,
                             UsuarioPort usuarioRepository,
                             TokenRecuperacionPort tokenRecuperacionRepository,
                             PasswordEncoder passwordEncoder,
                             EnlaceContrasenaUseCase enlaceContrasenaUseCase) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.enlaceContrasenaUseCase = enlaceContrasenaUseCase;
    }

    @Override
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    @Override
    public Optional<Estudiante> buscarPorId(Integer id) {
        return estudianteRepository.findById(id);
    }

    @Override
    public Optional<Estudiante> buscarPorCodigo(String codigo) {
        return estudianteRepository.findByCodigo(codigo);
    }

    @Override
    public Estudiante guardar(Estudiante estudiante) {
        Usuario usuario = sincronizarUsuario(estudiante, true);
        Estudiante guardado = estudianteRepository.save(estudiante);
        if (usuario != null) {
            enlaceContrasenaUseCase.enviarEnlaceEstablecimiento(usuario);
        }
        return guardado;
    }

    @Override
    public Estudiante actualizar(Integer id, Estudiante estudianteActualizado) {
        return estudianteRepository.findById(id)
                .map(estudiante -> {
                    estudiante.setNombre(estudianteActualizado.getNombre());
                    estudiante.setCodigo(estudianteActualizado.getCodigo());
                    estudiante.setSemestre(estudianteActualizado.getSemestre());
                    estudiante.setCorreo(estudianteActualizado.getCorreo());
                    sincronizarUsuario(estudiante, false);
                    return estudianteRepository.save(estudiante);
                })
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con id: " + id));
        Usuario usuario = estudiante.getUsuario();
        estudianteRepository.deleteById(id);
        if (usuario != null) {
            tokenRecuperacionRepository.eliminarPorUsuario(usuario.getIdUsuario());
            usuarioRepository.deleteById(usuario.getIdUsuario());
        }
    }

    /**
     * Crea o actualiza el usuario de acceso del estudiante (rol ESTUDIANTE).
     * Al crear se asigna una contrasena aleatoria que nadie conoce: el estudiante
     * debe establecerla desde el enlace que recibe por correo.
     */
    private Usuario sincronizarUsuario(Estudiante estudiante, boolean reiniciarContrasena) {
        String correo = estudiante.getCorreo();
        if (correo == null || correo.isBlank()) {
            return null;
        }

        Usuario usuario = estudiante.getUsuario();
        if (usuario == null) {
            usuario = usuarioRepository.findByCorreo(correo).orElseGet(Usuario::new);
        }
        usuario.setCorreo(correo);
        usuario.setNombre(estudiante.getNombre());
        usuario.setRol(Usuario.RolUsuario.ESTUDIANTE);

        if (reiniciarContrasena) {
            usuario.setContrasena(passwordEncoder.encode(UUID.randomUUID().toString()));
        }

        estudiante.setUsuario(usuarioRepository.save(usuario));
        return estudiante.getUsuario();
    }
}
