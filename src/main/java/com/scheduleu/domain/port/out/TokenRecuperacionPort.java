package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.TokenRecuperacion;

import java.util.Optional;

public interface TokenRecuperacionPort {
    Optional<TokenRecuperacion> findByToken(String token);
    <S extends TokenRecuperacion> S save(S entity);
    void deleteById(Integer id);
    void eliminarPorUsuario(Integer idUsuario);
}
