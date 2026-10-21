package com.scheduleu.domain.port.out;

import com.scheduleu.domain.model.GrupoPreferido;

import java.util.List;

public interface GrupoPreferidoPort {
    List<GrupoPreferido> findByPreferenciaIdPreferencia(Long idPreferencia);
    void eliminarPorPreferencia(Long idPreferencia);
    <S extends GrupoPreferido> S save(S entity);
}
