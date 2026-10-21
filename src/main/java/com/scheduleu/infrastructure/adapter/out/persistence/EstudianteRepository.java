package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Estudiante;
import com.scheduleu.domain.port.out.EstudiantePort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer>, EstudiantePort {
    Optional<Estudiante> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    Optional<Estudiante> findByUsuarioIdUsuario(Integer idUsuario);
}
