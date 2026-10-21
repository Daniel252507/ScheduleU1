package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.TokenRecuperacion;
import com.scheduleu.domain.port.out.TokenRecuperacionPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenRecuperacionRepository
        extends JpaRepository<TokenRecuperacion, Integer>, TokenRecuperacionPort {

    @Override
    @Modifying
    @Query("delete from TokenRecuperacion t where t.usuario.idUsuario = :idUsuario")
    void eliminarPorUsuario(@Param("idUsuario") Integer idUsuario);
}
