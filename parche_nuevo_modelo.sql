ALTER TABLE asignaturas MODIFY COLUMN nombre VARCHAR(150) NOT NULL;
ALTER TABLE asignaturas MODIFY COLUMN codigo VARCHAR(50) NOT NULL;
ALTER TABLE grupos MODIFY COLUMN nombre VARCHAR(50) NOT NULL;

CREATE TABLE IF NOT EXISTS preferencias_estudiante (
    id_preferencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante BIGINT NOT NULL UNIQUE,
    jornada_preferida VARCHAR(20) DEFAULT 'SIN_PREFERENCIA',
    prioriza_docente BIT(1) DEFAULT b'0',
    observaciones VARCHAR(500),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grupo_preferido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_preferencia BIGINT NOT NULL,
    id_asignatura BIGINT NOT NULL,
    id_grupo BIGINT,
    prioridad INT DEFAULT 1,
    FOREIGN KEY (id_preferencia) REFERENCES preferencias_estudiante(id_preferencia) ON DELETE CASCADE,
    FOREIGN KEY (id_asignatura) REFERENCES asignaturas(id_asignatura) ON DELETE CASCADE,
    FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE asignaciones MODIFY COLUMN id_grupo INT NULL;
ALTER TABLE asignaciones ADD COLUMN IF NOT EXISTS id_inscripcion INT NULL AFTER id_estudiante;
ALTER TABLE asignaciones ADD CONSTRAINT fk_asig_inscripcion
    FOREIGN KEY IF NOT EXISTS (id_inscripcion) REFERENCES inscripciones(id_inscripcion) ON DELETE SET NULL;
