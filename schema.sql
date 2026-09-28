-- ============================================================
-- ScheduleU: Script de inicializacion de la base de datos
-- Base de datos: MySQL
-- Nombre de la base: horario_uni1
-- ============================================================

-- Eliminar la base de datos si existe (opcional)
-- DROP DATABASE IF EXISTS horario_uni1;

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS horario_uni1
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE horario_uni1;

-- ============================================================
-- TABLA: usuarios
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: estudiantes
-- ============================================================
CREATE TABLE IF NOT EXISTS estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    semestre INT NOT NULL,
    correo VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: asignaturas
-- ============================================================
CREATE TABLE IF NOT EXISTS asignaturas (
    id_asignatura INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    creditos INT NOT NULL,
    codigo VARCHAR(20),
    semestre_recomendado INT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: docentes
-- ============================================================
CREATE TABLE IF NOT EXISTS docentes (
    id_docente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100),
    identificacion VARCHAR(20)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: salones
-- ============================================================
CREATE TABLE IF NOT EXISTS salones (
    id_salon INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    capacidad INT NOT NULL,
    ubicacion VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: grupos
-- ============================================================
CREATE TABLE IF NOT EXISTS grupos (
    id_grupo INT AUTO_INCREMENT PRIMARY KEY,
    id_asignatura INT NOT NULL,
    id_docente INT,
    id_salon INT,
    cupo INT NOT NULL,
    nombre VARCHAR(10),
    FOREIGN KEY (id_asignatura) REFERENCES asignaturas(id_asignatura) ON DELETE CASCADE,
    FOREIGN KEY (id_docente) REFERENCES docentes(id_docente) ON DELETE SET NULL,
    FOREIGN KEY (id_salon) REFERENCES salones(id_salon) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: horarios
-- ============================================================
CREATE TABLE IF NOT EXISTS horarios (
    id_horario INT AUTO_INCREMENT PRIMARY KEY,
    id_grupo INT NOT NULL,
    dia VARCHAR(15) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: inscripciones
-- ============================================================
CREATE TABLE IF NOT EXISTS inscripciones (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_asignatura INT NOT NULL,
    estado VARCHAR(30),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_asignatura) REFERENCES asignaturas(id_asignatura) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- TABLA: asignaciones
-- ============================================================
CREATE TABLE IF NOT EXISTS asignaciones (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grupo INT,
    estado VARCHAR(30),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grupo) REFERENCES grupos(id_grupo) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- INDICES PARA MEJORAR RENDIMIENTO
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_grupos_asignatura ON grupos(id_asignatura);
CREATE INDEX IF NOT EXISTS idx_grupos_docente ON grupos(id_docente);
CREATE INDEX IF NOT EXISTS idx_horarios_grupo ON horarios(id_grupo);
CREATE INDEX IF NOT EXISTS idx_inscripciones_estudiante ON inscripciones(id_estudiante);
CREATE INDEX IF NOT EXISTS idx_inscripciones_asignatura ON inscripciones(id_asignatura);
CREATE INDEX IF NOT EXISTS idx_asignaciones_estudiante ON asignaciones(id_estudiante);
CREATE INDEX IF NOT EXISTS idx_asignaciones_grupo ON asignaciones(id_grupo);
CREATE INDEX IF NOT EXISTS idx_asignaciones_estado ON asignaciones(estado);

-- ============================================================
-- DATOS DE EJEMPLO (OPCIONALES - para pruebas)
-- ============================================================

-- Asignaturas de ejemplo (programa de Sistemas de Informacion)
INSERT INTO asignaturas (nombre, creditos, codigo, semestre_recomendado) VALUES
('Programacion I', 4, 'SIS101', 1),
('Matematicas Discretas', 3, 'SIS102', 1),
('Algebra Lineal', 3, 'SIS103', 1),
('Programacion II', 4, 'SIS201', 2),
('Calculo Diferencial', 3, 'SIS202', 2),
('Bases de Datos I', 4, 'SIS301', 3),
('Ingenieria de Software I', 4, 'SIS401', 4),
('Sistemas Operativos', 3, 'SIS402', 4);

-- Docentes de ejemplo
INSERT INTO docentes (nombre, correo, identificacion) VALUES
('Carlos Alberto Rodriguez', 'crodriguez@mail.ucundinamarca.edu.co', 'D001'),
('Maria Fernanda Lopez', 'mlopez@mail.ucundinamarca.edu.co', 'D002'),
('Juan Carlos Perez', 'jperez@mail.ucundinamarca.edu.co', 'D003'),
('Ana Maria Gomez', 'agomez@mail.ucundinamarca.edu.co', 'D004'),
('Luis Eduardo Torres', 'ltorres@mail.ucundinamarca.edu.co', 'D005');

-- Salones de ejemplo
INSERT INTO salones (nombre, capacidad, ubicacion) VALUES
('A-101', 40, 'Bloque A, Piso 1'),
('A-102', 35, 'Bloque A, Piso 1'),
('B-201', 45, 'Bloque B, Piso 2'),
('B-202', 30, 'Bloque B, Piso 2'),
('C-301', 50, 'Bloque C, Piso 3'),
('Lab-SI-01', 30, 'Laboratorio Sistemas, Piso 2');

-- Grupos de ejemplo
INSERT INTO grupos (id_asignatura, id_docente, id_salon, cupo, nombre) VALUES
(1, 1, 6, 30, 'G1'),
(1, 2, 1, 35, 'G2'),
(2, 3, 2, 40, 'G1'),
(3, 4, 3, 45, 'G1'),
(4, 1, 6, 30, 'G1'),
(5, 5, 4, 30, 'G1'),
(6, 2, 6, 25, 'G1'),
(7, 3, 5, 40, 'G1'),
(8, 4, 3, 35, 'G1');

-- Horarios de ejemplo
INSERT INTO horarios (id_grupo, dia, hora_inicio, hora_fin) VALUES
(1, 'LUNES', '08:00:00', '10:00:00'),
(1, 'MIERCOLES', '08:00:00', '10:00:00'),
(2, 'MARTES', '10:00:00', '12:00:00'),
(2, 'JUEVES', '10:00:00', '12:00:00'),
(3, 'LUNES', '10:00:00', '12:00:00'),
(3, 'VIERNES', '08:00:00', '10:00:00'),
(4, 'MARTES', '08:00:00', '10:00:00'),
(4, 'JUEVES', '08:00:00', '10:00:00'),
(5, 'MIERCOLES', '14:00:00', '16:00:00'),
(5, 'VIERNES', '14:00:00', '16:00:00'),
(6, 'LUNES', '14:00:00', '16:00:00'),
(6, 'MIERCOLES', '10:00:00', '12:00:00'),
(7, 'MARTES', '14:00:00', '16:00:00'),
(7, 'JUEVES', '14:00:00', '16:00:00'),
(8, 'LUNES', '16:00:00', '18:00:00'),
(8, 'JUEVES', '16:00:00', '18:00:00'),
(9, 'MIERCOLES', '16:00:00', '18:00:00'),
(9, 'VIERNES', '16:00:00', '18:00:00');

-- Estudiantes de ejemplo
INSERT INTO estudiantes (nombre, codigo, semestre, correo) VALUES
('Juan Esteban Martinez', '20211001', 3, 'jemartinez@mail.ucundinamarca.edu.co'),
('Laura Valentina Sanchez', '20211002', 3, 'lvsanchez@mail.ucundinamarca.edu.co'),
('Andres Felipe Ramirez', '20211003', 2, 'aframirez@mail.ucundinamarca.edu.co'),
('Sofia Natalia Ortiz', '20211004', 4, 'snortiz@mail.ucundinamarca.edu.co'),
('Diego Alejandro Castro', '20211005', 1, 'dacastro@mail.ucundinamarca.edu.co');

-- Inscripciones de ejemplo
INSERT INTO inscripciones (id_estudiante, id_asignatura, estado) VALUES
(1, 1, 'PENDIENTE'),
(1, 2, 'PENDIENTE'),
(1, 6, 'PENDIENTE'),
(2, 1, 'PENDIENTE'),
(2, 3, 'PENDIENTE'),
(2, 6, 'PENDIENTE'),
(3, 1, 'PENDIENTE'),
(3, 4, 'PENDIENTE'),
(4, 7, 'PENDIENTE'),
(4, 8, 'PENDIENTE'),
(5, 1, 'PENDIENTE'),
(5, 2, 'PENDIENTE'),
(5, 3, 'PENDIENTE');
