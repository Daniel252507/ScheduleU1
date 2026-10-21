package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Usuario;
import com.scheduleu.domain.port.out.UsuarioPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>, UsuarioPort {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
}
