-- ============================================================
--  ScheduleU: SCRIPT DE INSTALACION TODO-EN-1 v2
--  A PRUEBA DE ERROR 150 (FK constraint) y ERROR 1146 (no existe tabla)
--  Estrategia: 1) CREAR TODAS LAS TABLAS SIN FK INLINE
--              2) INSERTAR TODOS LOS DATOS
--              3) AGREGAR TODAS LAS FK AL FINAL (ORDEN CORRECTO)
-- ============================================================

CREATE DATABASE IF NOT EXISTS horario_uni1 DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
USE horario_uni1;
SET FOREIGN_KEY_CHECKS = 0;

-- ===========================================================================
-- FASE 1) CREAR TABLAS SIN FK (NO hay REFERENCES inline — evita error 150)
-- ===========================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS asignaturas (
    id_asignatura INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    creditos INT NOT NULL,
    codigo VARCHAR(50),
    semestre_recomendado INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS docentes (
    id_docente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100),
    identificacion VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS salones (
    id_salon INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    capacidad INT NOT NULL,
    ubicacion VARCHAR(150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    semestre INT NOT NULL,
    correo VARCHAR(100),
    id_usuario INT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grupos (
    id_grupo INT AUTO_INCREMENT PRIMARY KEY,
    id_asignatura INT NOT NULL,
    id_docente INT NULL,
    id_salon INT NULL,
    cupo INT NOT NULL,
    nombre VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS horarios (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo INT NOT NULL,
    dia VARCHAR(15) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inscripciones (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_asignatura INT NOT NULL,
    estado VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS asignaciones (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_inscripcion INT NULL,
    id_grupo INT NULL,
    estado VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- IMPORTANTE: id_estudiante, id_asignatura, id_grupo son INT (no BIGINT)
-- para que coincidan con el tipo de sus PKs originales (INT).
CREATE TABLE IF NOT EXISTS preferencias_estudiante (
    id_preferencia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL UNIQUE,
    jornada_preferida VARCHAR(20) DEFAULT 'SIN_PREFERENCIA',
    prioriza_docente BIT(1) DEFAULT b'0',
    observaciones VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS grupo_preferido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_preferencia BIGINT NOT NULL,
    id_asignatura INT NOT NULL,
    id_grupo INT NULL,
    prioridad INT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===========================================================================
-- FASE 2) PARCHES DE COLUMNAS por si las tablas ya existian de antes
-- ===========================================================================
-- estudiantes.id_usuario (para login integrado)
SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='horario_uni1' AND TABLE_NAME='estudiantes' AND COLUMN_NAME='id_usuario') = 0,
    'ALTER TABLE estudiantes ADD COLUMN id_usuario INT NULL AFTER correo',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- asignaciones.id_inscripcion (referencia a la inscripcion del alumno)
SET @ddl = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='horario_uni1' AND TABLE_NAME='asignaciones' AND COLUMN_NAME='id_inscripcion') = 0,
    'ALTER TABLE asignaciones ADD COLUMN id_inscripcion INT NULL AFTER id_estudiante',
    'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- id_grupo NULLABLE (necesario para materias PENDIENTES que no tienen grupo)
ALTER TABLE asignaciones MODIFY COLUMN id_grupo INT NULL;

-- tamaño de columnas compatibilidad
ALTER TABLE asignaturas MODIFY COLUMN nombre VARCHAR(150) NOT NULL;
ALTER TABLE asignaturas MODIFY COLUMN codigo VARCHAR(50) NULL;
ALTER TABLE grupos MODIFY COLUMN nombre VARCHAR(50) NULL;

-- ===========================================================================
-- FASE 3) BORRAR DATOS ANTIGUOS en orden correcto de dependencias
-- ===========================================================================
TRUNCATE TABLE grupo_preferido;
TRUNCATE TABLE preferencias_estudiante;
TRUNCATE TABLE asignaciones;
TRUNCATE TABLE inscripciones;
TRUNCATE TABLE horarios;
TRUNCATE TABLE grupos;
TRUNCATE TABLE salones;
TRUNCATE TABLE docentes;
TRUNCATE TABLE estudiantes;
TRUNCATE TABLE asignaturas;
DELETE FROM usuarios WHERE correo IN ('admin@scheduleu.com','coordinador@scheduleu.com','estudiante@scheduleu.com');

-- ===========================================================================
-- FASE 4) INSERTAR DATOS (en orden de dependencia natural)
-- ===========================================================================
INSERT INTO usuarios (id_usuario, nombre, correo, contrasena, rol) VALUES
(1, 'Administrador ScheduleU',    'admin@scheduleu.com',       'TMP_NO_ENCRYPTED_admin123',      'ADMINISTRADOR'),
(2, 'Coordinador Academico',      'coordinador@scheduleu.com', 'TMP_NO_ENCRYPTED_coord123',      'COORDINADOR_ACADEMICO'),
(3, 'Estudiante Prueba Sem5',     'estudiante@scheduleu.com',  'TMP_NO_ENCRYPTED_estudiante123', 'ESTUDIANTE');

INSERT INTO asignaturas (id_asignatura, nombre, creditos, codigo, semestre_recomendado) VALUES
(1, 'Base de Datos',                     4, 'CAD612021523',   5),
(2, 'Ecuaciones Diferenciales',          3, 'CAD612021519',   5),
(3, 'Ingenieria de Sistemas',            4, 'CAD612021521',   5),
(4, 'Lengua Extranjera III',             3, 'CAI1002020507',  5),
(5, 'Planeacion Estrategica de Sist.',   3, 'CAD612021524',   5),
(6, 'Sistemas de Informacion I',         4, 'CAD612021522',   5),
(7, 'Sistemas Operativos',               4, 'CAD612021520',   5),
(8, 'Diagnostico / Nivelacion',          0, 'DN-CAI1002020',  5);

INSERT INTO docentes (id_docente, nombre, correo, identificacion) VALUES
(1, 'Carlos Alberto Rodriguez', 'crodriguez@unimon.edu.co',    'DOC-001'),
(2, 'Maria Fernanda Lopez',     'mlopez@unimon.edu.co',        'DOC-002'),
(3, 'Juan Carlos Perez',        'jperez@unimon.edu.co',        'DOC-003'),
(4, 'Ana Maria Gomez',          'agomez@unimon.edu.co',        'DOC-004'),
(5, 'Luis Eduardo Torres',      'ltorres@unimon.edu.co',       'DOC-005'),
(6, 'Patricia Silva Rojas',     'psilva@unimon.edu.co',        'DOC-006'),
(7, 'Diego Fernando Arango',    'darango@unimon.edu.co',       'DOC-007'),
(8, 'Sandra Milena Perea',      'sperea@unimon.edu.co',        'DOC-008');

INSERT INTO salones (id_salon, nombre, capacidad, ubicacion) VALUES
(1,  'Fusa-SIS-101',   30,  'Bloque Fusa, Piso 1, Salon 101'),
(2,  'Fusa-SIS-102',   35,  'Bloque Fusa, Piso 1, Salon 102'),
(3,  'Fusa-SIS-201',   25,  'Bloque Fusa, Piso 2, Salon 201'),
(4,  'Fusa-SIS-202',   25,  'Bloque Fusa, Piso 2, Salon 202'),
(5,  'Fusa-Lab-201',   30,  'Bloque Fusa, Piso 2, Laboratorio S.O.'),
(6,  'Fusa-Lab-202',   30,  'Bloque Fusa, Piso 2, Laboratorio B.D.'),
(7,  'Fusa-SIS-301',   40,  'Bloque Fusa, Piso 3, Salon 301'),
(8,  'Fusa-SIS-302',   40,  'Bloque Fusa, Piso 3, Salon 302'),
(9,  'Aula-Grande',    80,  'Aula Magna (Diagnostico)'),
(10, 'Virtual-Remoto', 999, 'Clases virtuales sincronicas');

INSERT INTO estudiantes (id_estudiante, nombre, codigo, semestre, correo, id_usuario) VALUES
(1, 'Daniel Arturo Patarroyo Carrillo', '20231099001', 5, 'dani9p@hotmail.com', 3);

INSERT INTO grupos (id_grupo, nombre, cupo, id_asignatura, id_docente, id_salon) VALUES
-- BASE DE DATOS (3 grupos)
(1,  'SIS.FUSA 501M', 25,  1, 1, 6),
(2,  'SIS.FUSA 502M', 22,  1, 2, 2),
(3,  'SIS.FUSA 503M', 25,  1, 5, 3),
-- DIAGNOSTICO
(4,  'DN-EI-FUSA',    778, 8, 8, 9),
-- ECUACIONES DIFERENCIALES (2 grupos)
(5,  'SIS.FUSA 501M', 35,  2, 3, 7),
(6,  'SIS.FUSA 502M', 35,  2, 4, 8),
-- INGENIERIA DE SISTEMAS (3 grupos)
(7,  'SIS.FUSA 501M', 24,  3, 5, 3),
(8,  'SIS.FUSA 502M', 25,  3, 6, 4),
(9,  'SIS.FUSA 503M', 25,  3, 7, 2),
-- LENGUA EXTRANJERA III (9 grupos)
(10, 'LE3-M1',        34,  4, 2, 1),
(11, 'LE3-M2',        35,  4, 4, 2),
(12, 'LE3-M3',        34,  4, 5, 3),
(13, 'LE3-N1',        18,  4, 1, 4),
(14, 'LE3-N2',        23,  4, 7, 3),
(15, 'LE3-T1',        33,  4, 3, 7),
(16, 'LE3-T2',        35,  4, 6, 8),
(17, 'LE3-T3',        37,  4, 8, 1),
(18, 'LE3-T4',        36,  4, 5, 2),
-- PLANEACION ESTRATEGICA (2 grupos)
(19, 'SIS.FUSA 501M', 35,  5, 6, 7),
(20, 'SIS.FUSA 502M', 35,  5, 1, 8),
-- SISTEMAS DE INFORMACION I (3 grupos)
(21, 'SIS.FUSA 501M', 24,  6, 7, 2),
(22, 'SIS.FUSA 502M', 25,  6, 4, 1),
(23, 'SIS.FUSA 503M', 25,  6, 3, 3),
-- SISTEMAS OPERATIVOS (3 grupos)
(24, 'SIS.FUSA 501M', 23,  7, 5, 5),
(25, 'SIS.FUSA 502M', 23,  7, 1, 5),
(26, 'SIS.FUSA 503M', 23,  7, 7, 5);

INSERT INTO horarios (id_horario, id_grupo, dia, hora_inicio, hora_fin) VALUES
-- BASE DE DATOS 501M
(1,  1, 'LUNES',     '09:00', '10:59'),
(2,  1, 'VIERNES',   '09:00', '10:59'),
-- BASE DE DATOS 502M
(3,  2, 'MIERCOLES', '07:00', '08:59'),
(4,  2, 'MARTES',    '07:00', '08:59'),
-- BASE DE DATOS 503M
(5,  3, 'VIERNES',   '09:00', '10:59'),
(6,  3, 'LUNES',     '09:00', '10:59'),
-- DIAGNOSTICO
(7,  4, 'LUNES',     '09:00', '10:59'),
-- ECUACIONES 501M
(8,  5, 'MARTES',    '09:00', '10:59'),
(9,  5, 'MIERCOLES', '07:00', '08:59'),
-- ECUACIONES 502M
(10, 6, 'LUNES',     '16:00', '17:59'),
(11, 6, 'MARTES',    '14:00', '15:59'),
-- ING. SISTEMAS 501M
(12, 7, 'MIERCOLES', '11:00', '12:59'),
(13, 7, 'LUNES',     '07:00', '08:59'),
(14, 7, 'VIERNES',   '14:00', '15:59'),
-- ING. SISTEMAS 502M
(15, 8, 'JUEVES',    '11:00', '12:59'),
(16, 8, 'LUNES',     '14:00', '15:59'),
(17, 8, 'MARTES',    '16:00', '17:59'),
-- ING. SISTEMAS 503M
(18, 9, 'MARTES',    '09:00', '10:59'),
(19, 9, 'JUEVES',    '16:00', '17:59'),
(20, 9, 'MIERCOLES', '16:00', '17:59'),
-- LENGUA EXTRANJERA III (9 grupos)
(21, 10, 'MARTES',   '07:00', '08:59'),
(22, 11, 'VIERNES',  '09:00', '10:59'),
(23, 12, 'VIERNES',  '18:00', '19:59'),
(24, 13, 'MARTES',   '18:00', '19:59'),
(25, 14, 'LUNES',    '18:00', '19:59'),
(26, 15, 'VIERNES',  '12:00', '13:59'),
(27, 16, 'SABADO',   '07:00', '08:59'),
(28, 17, 'LUNES',    '12:00', '13:59'),
(29, 18, 'LUNES',    '14:00', '15:59'),
-- PLANEACION ESTRATEGICA
(30, 19, 'MARTES',   '16:00', '17:59'),
(31, 20, 'JUEVES',   '16:00', '17:59'),
-- SISTEMAS DE INFORMACION I
(32, 21, 'JUEVES',   '09:00', '10:59'),
(33, 21, 'SABADO',   '09:00', '10:59'),
(34, 22, 'VIERNES',  '14:00', '15:59'),
(35, 22, 'SABADO',   '07:00', '08:59'),
(36, 23, 'SABADO',   '11:00', '12:59'),
(37, 23, 'VIERNES',  '16:00', '17:59'),
-- SISTEMAS OPERATIVOS
(38, 24, 'JUEVES',    '07:00', '08:59'),
(39, 24, 'MIERCOLES', '15:00', '16:59'),
(40, 25, 'LUNES',     '07:00', '08:59'),
(41, 25, 'MIERCOLES', '11:00', '12:59'),
(42, 26, 'LUNES',     '14:00', '15:59'),
(43, 26, 'VIERNES',   '07:00', '08:59');

-- ===========================================================================
-- FASE 5) AGREGAR FOREIGN KEYS (TODAS AL FINAL — evita el error 150)
-- IMPORTANTE: TODOS los tipos coinciden INT ↔ INT / BIGINT ↔ BIGINT.
-- ===========================================================================
-- Ya que los ADD CONSTRAINT no pueden ir en bloque PREPARE/EXECUTE dinámico,
-- usamos el patron "DROP IF EXISTS + ADD".
ALTER TABLE grupos
    DROP FOREIGN KEY IF EXISTS fk_grp_asig,
    ADD CONSTRAINT fk_grp_asig       FOREIGN KEY (id_asignatura) REFERENCES asignaturas(id_asignatura) ON DELETE CASCADE,
    DROP FOREIGN KEY IF EXISTS fk_grp_doc,
    ADD CONSTRAINT fk_grp_doc        FOREIGN KEY (id_docente)    REFERENCES docentes(id_docente)       ON DELETE SET NULL,
    DROP FOREIGN KEY IF EXISTS fk_grp_sal,
    ADD CONSTRAINT fk_grp_sal        FOREIGN KEY (id_salon)      REFERENCES salones(id_salon)         ON DELETE SET NULL;

ALTER TABLE horarios
    DROP FOREIGN KEY IF EXISTS fk_hor_grp,
    ADD CONSTRAINT fk_hor_grp        FOREIGN KEY (id_grupo)      REFERENCES grupos(id_grupo)           ON DELETE CASCADE;

ALTER TABLE estudiantes
    DROP FOREIGN KEY IF EXISTS fk_est_usr,
    ADD CONSTRAINT fk_est_usr        FOREIGN KEY (id_usuario)    REFERENCES usuarios(id_usuario)       ON DELETE SET NULL;

ALTER TABLE inscripciones
    DROP FOREIGN KEY IF EXISTS fk_ins_est,
    ADD CONSTRAINT fk_ins_est        FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    DROP FOREIGN KEY IF EXISTS fk_ins_asig,
    ADD CONSTRAINT fk_ins_asig       FOREIGN KEY (id_asignatura) REFERENCES asignaturas(id_asignatura) ON DELETE CASCADE;

ALTER TABLE asignaciones
    DROP FOREIGN KEY IF EXISTS fk_asig_est,
    ADD CONSTRAINT fk_asig_est       FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    DROP FOREIGN KEY IF EXISTS fk_asig_ins,
    ADD CONSTRAINT fk_asig_ins       FOREIGN KEY (id_inscripcion) REFERENCES inscripciones(id_inscripcion) ON DELETE SET NULL,
    DROP FOREIGN KEY IF EXISTS fk_asig_grp,
    ADD CONSTRAINT fk_asig_grp       FOREIGN KEY (id_grupo)      REFERENCES grupos(id_grupo)           ON DELETE SET NULL;

ALTER TABLE preferencias_estudiante
    DROP FOREIGN KEY IF EXISTS fk_pref_est,
    ADD CONSTRAINT fk_pref_est       FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE;

ALTER TABLE grupo_preferido
    DROP FOREIGN KEY IF EXISTS fk_gp_pref,
    ADD CONSTRAINT fk_gp_pref        FOREIGN KEY (id_preferencia) REFERENCES preferencias_estudiante(id_preferencia) ON DELETE CASCADE;

-- ===========================================================================
--  FIN
-- ===========================================================================
SET FOREIGN_KEY_CHECKS = 1;
SELECT '=== INSTALACION ScheduleU TERMINADA SIN ERRORES ===' AS Estado;
SELECT 'Asignaturas', COUNT(*) FROM asignaturas
UNION ALL SELECT 'Grupos',      COUNT(*) FROM grupos
UNION ALL SELECT 'Horarios',    COUNT(*) FROM horarios
UNION ALL SELECT 'Docentes',    COUNT(*) FROM docentes
UNION ALL SELECT 'Salones',     COUNT(*) FROM salones
UNION ALL SELECT 'Estudiantes', COUNT(*) FROM estudiantes
UNION ALL SELECT 'Usuarios',    COUNT(*) FROM usuarios;
