package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.Usuario;

import java.util.Optional;

public interface UsuarioPort {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    <S extends Usuario> S save(S entity);
    void deleteById(Integer id);
}
