package com.scheduleu.repository;

import com.scheduleu.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
    Optional<Estudiante> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    Optional<Estudiante> findByUsuarioIdUsuario(Integer idUsuario);
}
