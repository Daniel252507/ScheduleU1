package com.scheduleu.repository;

import com.scheduleu.model.GrupoPreferido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GrupoPreferidoRepository extends JpaRepository<GrupoPreferido, Long> {
    List<GrupoPreferido> findByPreferenciaIdPreferencia(Long idPreferencia);

    @Modifying
    @Transactional
    @Query("DELETE FROM GrupoPreferido gp WHERE gp.preferencia.idPreferencia = :idPref")
    void deleteByPreferenciaId(@Param("idPref") Long idPref);
}
