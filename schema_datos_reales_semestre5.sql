-- ============================================================
--  ScheduleU: DATOS REALES DEL SEMESTRE 5 (Sistemas - Unisimón)
--  Basados en la información oficial del horario de semestre 5
--  Instrucciones:
--    1. Asegurate de haber creado la BD con schema.sql primero
--    2. Ejecuta este script EN PHPMYADMIN (o Workbench) dentro
--       de la base de datos `horario_uni1`
--    3. Elimina los datos antiguos ANTES (las 3 primeras sentencias
--       ya lo hacen por ti en orden)
-- ============================================================

-- Paso 1: Limpiar datos antiguos en ORDEN correcto (por claves foráneas)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE asignaciones;
TRUNCATE TABLE horarios;
TRUNCATE TABLE grupos;
TRUNCATE TABLE salones;
TRUNCATE TABLE docentes;
TRUNCATE TABLE inscripciones;
TRUNCATE TABLE estudiantes;
TRUNCATE TABLE asignaturas;
TRUNCATE TABLE preferencias_estudiante;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. ASIGNATURAS DEL SEMESTRE 5
-- ============================================================
INSERT INTO asignaturas (id_asignatura, nombre, creditos, codigo, semestre_recomendado) VALUES
(1,  'Base de Datos',                     4, 'CAD612021523',     5),
(2,  'Ecuaciones Diferenciales',          3, 'CAD612021519',     5),
(3,  'Ingenieria de Sistemas',            4, 'CAD612021521',     5),
(4,  'Lengua Extranjera III',             3, 'CAI1002020507',    5),
(5,  'Planeacion Estrategica de Sist.',   3, 'CAD612021524',     5),
(6,  'Sistemas de Informacion I',         4, 'CAD612021522',     5),
(7,  'Sistemas Operativos',               4, 'CAD612021520',     5),
(8,  'Diagnostico / Nivelacion',          0, 'DN-CAI1002020',    5);

-- ============================================================
-- 2. DOCENTES (datos de ejemplo - el administrador los completa)
-- ============================================================
INSERT INTO docentes (id_docente, nombre, correo, identificacion) VALUES
(1, 'Carlos Alberto Rodriguez', 'crodriguez@unimon.edu.co',    'DOC-001'),
(2, 'Maria Fernanda Lopez',     'mlopez@unimon.edu.co',        'DOC-002'),
(3, 'Juan Carlos Perez',        'jperez@unimon.edu.co',        'DOC-003'),
(4, 'Ana Maria Gomez',          'agomez@unimon.edu.co',        'DOC-004'),
(5, 'Luis Eduardo Torres',      'ltorres@unimon.edu.co',       'DOC-005'),
(6, 'Patricia Silva Rojas',     'psilva@unimon.edu.co',        'DOC-006'),
(7, 'Diego Fernando Arango',    'darango@unimon.edu.co',       'DOC-007'),
(8, 'Sandra Milena Perea',      'sperea@unimon.edu.co',        'DOC-008');

-- ============================================================
-- 3. SALONES (Campus Unisimón - edificio Sistemas/Fusa)
-- ============================================================
INSERT INTO salones (id_salon, nombre, capacidad, ubicacion) VALUES
(1,  'Fusa-SIS-101',  30, 'Bloque Fusa, Piso 1, Salon 101'),
(2,  'Fusa-SIS-102',  35, 'Bloque Fusa, Piso 1, Salon 102'),
(3,  'Fusa-SIS-201',  25, 'Bloque Fusa, Piso 2, Salon 201'),
(4,  'Fusa-SIS-202',  25, 'Bloque Fusa, Piso 2, Salon 202'),
(5,  'Fusa-Lab-201',  30, 'Bloque Fusa, Piso 2, Laboratorio S.O.'),
(6,  'Fusa-Lab-202',  30, 'Bloque Fusa, Piso 2, Laboratorio B.D.'),
(7,  'Fusa-SIS-301',  40, 'Bloque Fusa, Piso 3, Salon 301'),
(8,  'Fusa-SIS-302',  40, 'Bloque Fusa, Piso 3, Salon 302'),
(9,  'Aula-Grande',   80, 'Aula Magna (Diagnostico)'),
(10, 'Virtual-Remoto',999,'Clases virtuales sincronicas');

-- ============================================================
-- 4. GRUPOS (con cupo real extraido del horario oficial)
--    asignatura + docente + salon + nombre_grupo + cupo
-- ============================================================
INSERT INTO grupos (id_grupo, nombre, cupo, id_asignatura, id_docente, id_salon) VALUES
-- BASE DE DATOS (4 grupos: 501M, 502M, 503M)
(1,  'SIS.FUSA 501M', 25, 1, 1, 6),   -- BD, Carlos R., Lab Bases
(2,  'SIS.FUSA 502M', 22, 1, 2, 2),   -- BD, Maria L., Salon 102
(3,  'SIS.FUSA 503M', 25, 1, 5, 3),   -- BD, Luis T., Salon 201
-- DIAGNOSTICO (grupo grande)
(4,  'DN-EI-FUSA',    500,8, 8, 9),   -- Diagnostico, Sandra, Aula Grande
-- ECUACIONES DIFERENCIALES (2 grupos: 501M, 502M)
(5,  'SIS.FUSA 501M', 35, 2, 3, 7),   -- ED, Juan P., Salon 301
(6,  'SIS.FUSA 502M', 35, 2, 4, 8),   -- ED, Ana G.,  Salon 302
-- INGENIERIA DE SISTEMAS (3 grupos: 501M, 502M, 503M)
(7,  'SIS.FUSA 501M', 24, 3, 5, 3),   -- IS, Luis T., Salon 201
(8,  'SIS.FUSA 502M', 25, 3, 6, 4),   -- IS, Paty,    Salon 202
(9,  'SIS.FUSA 503M', 25, 3, 7, 2),   -- IS, Diego A., Salon 102
-- LENGUA EXTRANJERA III (9 grupos, modalidades M mañana, N noche, T tarde)
(10, 'LE3-M1',       34, 4, 2, 1),    -- Maria Fernanda, Salon 101
(11, 'LE3-M2',       35, 4, 4, 2),    -- Ana Maria,      Salon 102
(12, 'LE3-M3',       34, 4, 5, 3),    -- Luis,           Salon 201
(13, 'LE3-N1',       18, 4, 1, 4),    -- Carlos,         Salon 202 (noche)
(14, 'LE3-N2',       23, 4, 7, 3),    -- Diego,          Salon 201 (noche)
(15, 'LE3-T1',       33, 4, 3, 7),    -- Juan,           Salon 301 (tarde)
(16, 'LE3-T2',       35, 4, 6, 8),    -- Patricia,       Salon 302 (Sabado)
(17, 'LE3-T3',       37, 4, 8, 1),    -- Sandra,         Salon 101 (tarde)
(18, 'LE3-T4',       36, 4, 5, 2),    -- Luis,           Salon 102 (tarde)
-- PLANEACION ESTRATEGICA (2 grupos: 501M, 502M)
(19, 'SIS.FUSA 501M', 35, 5, 6, 7),   -- Patricia, Salon 301
(20, 'SIS.FUSA 502M', 35, 5, 1, 8),   -- Carlos,   Salon 302
-- SISTEMAS DE INFORMACION I (3 grupos: 501M, 502M, 503M)
(21, 'SIS.FUSA 501M', 24, 6, 7, 2),   -- Diego, Salon 102
(22, 'SIS.FUSA 502M', 25, 6, 4, 1),   -- Ana,   Salon 101
(23, 'SIS.FUSA 503M', 25, 6, 3, 3),   -- Juan,  Salon 201
-- SISTEMAS OPERATIVOS (3 grupos: 501M, 502M, 503M)
(24, 'SIS.FUSA 501M', 23, 7, 5, 5),   -- Luis,   Lab SO
(25, 'SIS.FUSA 502M', 23, 7, 1, 5),   -- Carlos, Lab SO
(26, 'SIS.FUSA 503M', 23, 7, 7, 5);   -- Diego,  Lab SO

-- ============================================================
-- 5. HORARIOS (asignados SEGUN DATOS OFICIALES del semestre 5)
-- ============================================================
INSERT INTO horarios (id_horario, id_grupo, dia, hora_inicio, hora_fin) VALUES
-- === BASE DE DATOS 501M (id_grupo=1, 2 dias/semana) ===
(1,  1, 'LUNES',     '09:00', '10:59'),
(2,  1, 'VIERNES',   '09:00', '10:59'),
-- === BASE DE DATOS 502M (id_grupo=2, 2 dias/semana) ===
(3,  2, 'MIERCOLES', '07:00', '08:59'),
(4,  2, 'MARTES',    '07:00', '08:59'),
-- === BASE DE DATOS 503M (id_grupo=3, 2 dias/semana) ===
(5,  3, 'VIERNES',   '09:00', '10:59'),
(6,  3, 'LUNES',     '09:00', '10:59'),
-- === DIAGNOSTICO DN-EI-FUSA (id_grupo=4, se marca sin horario fijo) ===
(7,  4, 'LUNES',     '07:00', '08:59'),
-- === ECUACIONES DIFERENCIALES 501M (id_grupo=5) ===
(8,  5, 'MARTES',    '09:00', '10:59'),
(9,  5, 'MIERCOLES', '07:00', '08:59'),
-- === ECUACIONES DIFERENCIALES 502M (id_grupo=6) ===
(10, 6, 'LUNES',     '16:00', '17:59'),
(11, 6, 'MARTES',    '14:00', '15:59'),
-- === INGENIERIA DE SISTEMAS 501M (id_grupo=7) ===
(12, 7, 'MIERCOLES', '11:00', '12:59'),
(13, 7, 'LUNES',     '07:00', '08:59'),
(14, 7, 'VIERNES',   '14:00', '15:59'),
-- === INGENIERIA DE SISTEMAS 502M (id_grupo=8) ===
(15, 8, 'JUEVES',    '11:00', '12:59'),
(16, 8, 'LUNES',     '14:00', '15:59'),
(17, 8, 'MARTES',    '16:00', '17:59'),
-- === INGENIERIA DE SISTEMAS 503M (id_grupo=9) ===
(18, 9, 'MARTES',    '09:00', '10:59'),
(19, 9, 'JUEVES',    '16:00', '17:59'),
(20, 9, 'MIERCOLES', '16:00', '17:59'),
-- === LENGUA EXTRANJERA III - 9 grupos ===
(21, 10, 'MARTES',   '07:00', '08:59'), -- LE3-M1
(22, 11, 'VIERNES',  '09:00', '10:59'), -- LE3-M2
(23, 12, 'VIERNES',  '18:00', '19:59'), -- LE3-M3 (tarde-noche)
(24, 13, 'MARTES',   '18:00', '19:59'), -- LE3-N1 (noche)
(25, 14, 'LUNES',    '18:00', '19:59'), -- LE3-N2 (noche)
(26, 15, 'VIERNES',  '12:00', '13:59'), -- LE3-T1 (tarde)
(27, 16, 'SABADO',   '07:00', '08:59'), -- LE3-T2 (sabado)
(28, 17, 'LUNES',    '12:00', '13:59'), -- LE3-T3 (tarde)
(29, 18, 'LUNES',    '14:00', '15:59'), -- LE3-T4 (tarde)
-- === PLANEACION ESTRATEGICA ===
(30, 19, 'MARTES',   '16:00', '17:59'), -- 501M
(31, 20, 'JUEVES',   '16:00', '17:59'), -- 502M
-- === SISTEMAS DE INFORMACION I ===
(32, 21, 'JUEVES',   '09:00', '10:59'), -- 501M
(33, 21, 'SABADO',   '09:00', '10:59'), -- 501M
(34, 22, 'VIERNES',  '14:00', '15:59'), -- 502M
(35, 22, 'SABADO',   '07:00', '08:59'), -- 502M
(36, 23, 'SABADO',   '11:00', '12:59'), -- 503M
(37, 23, 'VIERNES',  '16:00', '17:59'), -- 503M
-- === SISTEMAS OPERATIVOS ===
(38, 24, 'JUEVES',   '07:00', '08:59'), -- 501M
(39, 24, 'MIERCOLES','15:00', '16:59'), -- 501M (laboratorio 2h)
(40, 25, 'LUNES',    '07:00', '08:59'), -- 502M
(41, 25, 'MIERCOLES','11:00', '12:59'), -- 502M
(42, 26, 'LUNES',    '14:00', '15:59'), -- 503M
(43, 26, 'VIERNES',  '07:00', '08:59'); -- 503M

-- ============================================================
-- 6. ESTUDIANTE DE PRUEBA (Rol: ESTUDIANTE, semestre 5)
-- ============================================================
-- Usuario estudiante real (ya existe en `usuarios` creado por el DataInitializer)
-- Ahora creamos su registro ESTUDIANTE asociado a el (id_usuario=3)
INSERT INTO estudiantes (id_estudiante, nombre, codigo, semestre, correo, id_usuario) VALUES
(1, 'Daniel Arturo Patarroyo Carrillo', '20231099001', 5, 'dani9p@hotmail.com', 3);

-- ============================================================
-- 7. INSCRIPCIONES (seran creadas POR EL ESTUDIANTE desde la UI)
--    Por defecto NINGUNA - el estudiante escoge las suyas.
-- ============================================================
-- (Aqui inserta el sistema cuando el estudiante envía su formulario)

-- ============================================================
-- 8. PREFERENCIAS_ESTUDIANTE (seran creadas POR EL ESTUDIANTE)
-- ============================================================
-- (Tabla nueva creada abajo o en parche de BD)
