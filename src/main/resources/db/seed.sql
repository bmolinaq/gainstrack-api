-- ============================================================
-- GainsTrack — Seed Data v2
-- Usuario de prueba: John Doe
-- ============================================================


-- ------------------------------------------------------------
-- 1. muscle_groups
-- ------------------------------------------------------------
INSERT INTO muscle_groups (name) VALUES
('Pecho'),        -- id: 1
('Espalda'),      -- id: 2
('Piernas'),      -- id: 3
('Hombros'),      -- id: 4
('Glúteos'),      -- id: 5
('Core'),         -- id: 6
('Bíceps'),       -- id: 7
('Tríceps'),      -- id: 8
('Antebrazos'),   -- id: 9
('Cuádriceps'),   -- id: 10
('Isquiotibiales'), -- id: 11
('Pantorrillas'), -- id: 12
('Dorsal'),       -- id: 13
('Trapecio'),     -- id: 14
('Lumbares');     -- id: 15

-- ------------------------------------------------------------
-- 3. gyms
-- John entrena en tres sucursales distintas
-- ------------------------------------------------------------
INSERT INTO gyms (user_id, name) VALUES
(1, 'Smart Fit Providencia'),
(1, 'Smart Fit Las Condes'),
(1, 'Smart Fit Maipú');


-- ------------------------------------------------------------
-- 4. exercises — globales (user_id = NULL, is_predefined = TRUE)
-- ------------------------------------------------------------
INSERT INTO exercises (name, muscle_group_id, user_id, is_predefined, image_url, video_url) VALUES

update exercises set image_url = 'exercises/images/apertura-de-pecho-con-mancuernas.jpeg', video_url = 'exercises/videos/apertura-de-pecho-con-mancuernas.mp4' where id = 6;
-- Pecho (id: 1)
('Press de Banca con Barra', 1, NULL, TRUE, NULL, NULL),                                                                                                                                          -- id: 1
('Press de Banca Inclinado con Barra', 1, NULL, TRUE, NULL, NULL),                                                                                                                                -- id: 2
('Press de Banca Declinado con Barra', 1, NULL, TRUE, NULL, NULL),                                                                                                                                -- id: 3
('Press de Banca con Mancuernas', 1, NULL, TRUE, 'exercises/images/press-de-banca-con-mancuernas.jpeg', 'exercises/videos/press-de-banca-con-mancuernas.mp4'),                                                                                                                                     -- id: 4
('Press de Banca Inclinado con Mancuernas', 1, NULL, TRUE, 'exercises/images/press-de-banca-inclinado-con-mancuernas.jpeg', 'exercises/videos/press-de-banca-inclinado-con-mancuernas.mp4'),      -- id: 5
('Aperturas con Mancuernas en Banco Plano', 1, NULL, TRUE, 'exercises/images/apertura-de-pecho-con-mancuernas.jpeg', 'exercises/videos/apertura-de-pecho-con-mancuernas.mp4'),                                                                                                                           -- id: 6
('Aperturas con Mancuernas en Banco Inclinado', 1, NULL, TRUE, NULL, NULL),                                                                                                                       -- id: 7
('Cruce de Poleas en Polea Alta', 1, NULL, TRUE, NULL, NULL),                                                                                                                                     -- id: 8
('Cruce de Poleas en Polea Baja', 1, NULL, TRUE, NULL, NULL),                                                                                                                                     -- id: 9
('Fondos en Paralelas', 1, NULL, TRUE, NULL, NULL),                                                                                                                                               -- id: 10
('Press de Pecho en Máquina', 1, NULL, TRUE, NULL, NULL),                                                                                                                                         -- id: 11
('Pullover con Mancuerna', 1, NULL, TRUE, NULL, NULL),                                                                                                                                            -- id: 12

-- Espalda (id: 2)
('Dominadas Agarre Prono', 2, NULL, TRUE, NULL, NULL),                                                                                                                                            -- id: 13
('Dominadas Agarre Supino', 2, NULL, TRUE, NULL, NULL),                                                                                                                                           -- id: 14
('Remo con Barra Agarre Prono', 2, NULL, TRUE, NULL, NULL),                                                                                                                                       -- id: 15
('Remo con Mancuerna a Una Mano', 2, NULL, TRUE, NULL, NULL),                                                                                                                                     -- id: 16
('Remo en Polea Baja Sentado', 2, NULL, TRUE, NULL, NULL),                                                                                                                                        -- id: 17
('Jalón al Pecho en Polea Alta', 2, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 18
('Jalón al Pecho Agarre Neutro', 2, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 19
('Jalón Trasero en Polea Alta', 2, NULL, TRUE, NULL, NULL),                                                                                                                                       -- id: 20
('Remo en Máquina', 2, NULL, TRUE, NULL, NULL),                                                                                                                                                   -- id: 21
('Pull-Over en Polea Alta', 2, NULL, TRUE, NULL, NULL),                                                                                                                                           -- id: 22
('Encogimientos de Hombros con Barra', 2, NULL, TRUE, NULL, NULL),                                                                                                                                -- id: 23

-- Piernas (id: 3)
('Sentadilla con Barra Libre', 3, NULL, TRUE, NULL, NULL),                                                                                                                                        -- id: 24
('Sentadilla Frontal con Barra', 3, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 25
('Sentadilla Hack con Barra', 3, NULL, TRUE, NULL, NULL),                                                                                                                                         -- id: 26
('Prensa de Piernas 45 Grados', 3, NULL, TRUE, NULL, NULL),                                                                                                                                       -- id: 27
('Zancada con Mancuernas', 3, NULL, TRUE, NULL, NULL),                                                                                                                                            -- id: 28
('Zancada Búlgara con Mancuernas', 3, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 29
('Peso Muerto Convencional con Barra', 3, NULL, TRUE, NULL, NULL),                                                                                                                                -- id: 30
('Peso Muerto Sumo con Barra', 3, NULL, TRUE, NULL, NULL),                                                                                                                                        -- id: 31

-- Hombros (id: 4)
('Press Militar con Barra de Pie', 4, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 32
('Press Militar con Barra Sentado', 4, NULL, TRUE, NULL, NULL),                                                                                                                                   -- id: 33
('Press Arnold con Mancuernas', 4, NULL, TRUE, NULL, NULL),                                                                                                                                       -- id: 34
('Press de Hombros con Mancuernas Sentado', 4, NULL, TRUE, NULL, NULL),                                                                                                                           -- id: 35
('Elevaciones Laterales con Mancuernas', 4, NULL, TRUE, NULL, NULL),                                                                                                                              -- id: 36
('Elevaciones Frontales con Mancuernas', 4, NULL, TRUE, NULL, NULL),                                                                                                                              -- id: 37
('Elevaciones Laterales en Polea Baja', 4, NULL, TRUE, NULL, NULL),                                                                                                                               -- id: 38
('Pájaros con Mancuernas en Banco Inclinado', 4, NULL, TRUE, NULL, NULL),                                                                                                                         -- id: 39
('Face Pull en Polea Alta', 4, NULL, TRUE, NULL, NULL),                                                                                                                                           -- id: 40
('Press de Hombros en Máquina', 4, NULL, TRUE, NULL, NULL),                                                                                                                                       -- id: 41

-- Glúteos (id: 5)
('Hip Thrust con Barra', 5, NULL, TRUE, NULL, NULL),                                                                                                                                              -- id: 42
('Hip Thrust en Máquina', 5, NULL, TRUE, NULL, NULL),                                                                                                                                             -- id: 43
('Patada de Glúteo en Polea Baja', 5, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 44
('Abducción de Cadera en Máquina', 5, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 45
('Aducción de Cadera en Máquina', 5, NULL, TRUE, NULL, NULL),                                                                                                                                     -- id: 46
('Buenos Días con Barra', 5, NULL, TRUE, NULL, NULL),                                                                                                                                             -- id: 47

-- Core (id: 6)
('Plancha Isométrica', 6, NULL, TRUE, NULL, NULL),                                                                                                                                                -- id: 48
('Plancha Lateral Isométrica', 6, NULL, TRUE, NULL, NULL),                                                                                                                                        -- id: 49
('Crunch Abdominal', 6, NULL, TRUE, NULL, NULL),                                                                                                                                                  -- id: 50
('Crunch Abdominal en Polea Alta', 6, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 51
('Elevación de Piernas en Barra', 6, NULL, TRUE, NULL, NULL),                                                                                                                                     -- id: 52
('Rueda Abdominal', 6, NULL, TRUE, NULL, NULL),                                                                                                                                                   -- id: 53
('Rotación de Torso en Polea', 6, NULL, TRUE, NULL, NULL),                                                                                                                                        -- id: 54

-- Bíceps (id: 7)
('Curl de Bíceps con Barra Recta', 7, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 55
('Curl de Bíceps con Barra Z', 7, NULL, TRUE, NULL, NULL),                                                                                                                                        -- id: 56
('Curl de Bíceps con Mancuernas Alternado', 7, NULL, TRUE, NULL, NULL),                                                                                                                           -- id: 57
('Curl Martillo con Mancuernas', 7, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 58
('Curl de Bíceps en Polea Baja', 7, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 59
('Curl de Bíceps en Banco Inclinado', 7, NULL, TRUE, NULL, NULL),                                                                                                                                 -- id: 60
('Curl de Bíceps Concentrado con Mancuerna', 7, NULL, TRUE, NULL, NULL),                                                                                                                          -- id: 61
('Curl de Bíceps en Máquina', 7, NULL, TRUE, NULL, NULL),                                                                                                                                         -- id: 62

-- Tríceps (id: 8)
('Press Francés con Barra Z', 8, NULL, TRUE, NULL, NULL),                                                                                                                                         -- id: 63
('Press Francés con Mancuernas', 8, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 64
('Extensión de Tríceps en Polea Alta con Cuerda', 8, NULL, TRUE, NULL, NULL),                                                                                                                     -- id: 65
('Extensión de Tríceps en Polea Alta con Barra', 8, NULL, TRUE, NULL, NULL),                                                                                                                      -- id: 66
('Extensión de Tríceps sobre la Cabeza en Polea', 8, NULL, TRUE, NULL, NULL),                                                                                                                     -- id: 67
('Fondos en Banco para Tríceps', 8, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 68
('Press Cerrado con Barra', 8, NULL, TRUE, NULL, NULL),                                                                                                                                           -- id: 69
('Patada de Tríceps con Mancuerna', 8, NULL, TRUE, NULL, NULL),                                                                                                                                   -- id: 70

-- Antebrazos (id: 9)
('Curl de Muñeca con Barra', 9, NULL, TRUE, NULL, NULL),                                                                                                                                          -- id: 71
('Curl de Muñeca Inverso con Barra', 9, NULL, TRUE, NULL, NULL),                                                                                                                                  -- id: 72
('Curl de Muñeca con Mancuerna', 9, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 73

-- Cuádriceps (id: 10)
('Extensión de Cuádriceps en Máquina', 10, NULL, TRUE, NULL, NULL),                                                                                                                               -- id: 74
('Sentadilla Sissy', 10, NULL, TRUE, NULL, NULL),                                                                                                                                                 -- id: 75
('Prensa de Piernas Pies Juntos', 10, NULL, TRUE, NULL, NULL),                                                                                                                                    -- id: 76

-- Isquiotibiales (id: 11)
('Curl de Isquiotibiales Tumbado en Máquina', 11, NULL, TRUE, NULL, NULL),                                                                                                                        -- id: 77
('Curl de Isquiotibiales Sentado en Máquina', 11, NULL, TRUE, NULL, NULL),                                                                                                                        -- id: 78
('Peso Muerto Rumano con Barra', 11, NULL, TRUE, NULL, NULL),                                                                                                                                     -- id: 79
('Peso Muerto Rumano con Mancuernas', 11, NULL, TRUE, NULL, NULL),                                                                                                                                -- id: 80

-- Pantorrillas (id: 12)
('Elevación de Pantorrillas de Pie en Máquina', 12, NULL, TRUE, NULL, NULL),                                                                                                                      -- id: 81
('Elevación de Pantorrillas Sentado en Máquina', 12, NULL, TRUE, NULL, NULL),                                                                                                                     -- id: 82
('Elevación de Pantorrillas en Prensa 45 Grados', 12, NULL, TRUE, NULL, NULL),                                                                                                                    -- id: 83

-- Dorsal (id: 13)
('Jalón en Polea Alta Agarre Abierto', 13, NULL, TRUE, NULL, NULL),                                                                                                                               -- id: 84
('Remo en Polea Baja Agarre Neutro', 13, NULL, TRUE, NULL, NULL),                                                                                                                                 -- id: 85

-- Trapecio (id: 14)
('Encogimientos de Hombros con Mancuernas', 14, NULL, TRUE, NULL, NULL),                                                                                                                          -- id: 86
('Encogimientos de Hombros en Máquina', 14, NULL, TRUE, NULL, NULL),                                                                                                                              -- id: 87
('Remo al Mentón con Barra', 14, NULL, TRUE, NULL, NULL),                                                                                                                                         -- id: 88

-- Lumbares (id: 15)
('Extensión Lumbar en Máquina', 15, NULL, TRUE, NULL, NULL),                                                                                                                                      -- id: 89
('Hiperextensión en Banco Romano', 15, NULL, TRUE, NULL, NULL),                                                                                                                                   -- id: 90
('Peso Muerto con Piernas Rígidas', 15, NULL, TRUE, NULL, NULL);                                                                                                                                  -- id: 91


-- ------------------------------------------------------------
-- 5. routines
-- Rutinas de John Doe — incluye rutina libre automática
-- ------------------------------------------------------------
INSERT INTO routines (user_id, name, notes, is_free) VALUES
(1, 'Sesión Libre', NULL, TRUE),                                        -- id: 1
(1, 'Espalda / Bíceps', 'Día de tirón — enfoque en dorsal y bíceps', FALSE),   -- id: 2
(1, 'Pecho / Tríceps', 'Día de empuje — enfoque en pectoral y tríceps', FALSE), -- id: 3
(1, 'Piernas', 'Día de pierna completo — cuádriceps, isquios y pantorrillas', FALSE), -- id: 4
(1, 'Hombros', 'Día de hombros — deltoides y trapecio', FALSE);         -- id: 5


-- ------------------------------------------------------------
-- 6. routine_exercises
-- Ejercicios de cada rutina con su orden
-- ------------------------------------------------------------
INSERT INTO routine_exercises (routine_id, exercise_id, order_index, notes) VALUES

-- Rutina Espalda/Bíceps (routine_id: 2)
(2, 13, 1, 'Agarre prono — ancho de hombros'),           -- id: 1  Dominadas Agarre Prono
(2, 18, 2, 'Bajar hasta el pecho — no soltar escápulas'), -- id: 2  Jalón al Pecho en Polea Alta
(2, 15, 3, 'Espalda recta — no redondear lumbar'),        -- id: 3  Remo con Barra Agarre Prono
(2, 16, 4, 'Codo pegado al cuerpo — no girar torso'),     -- id: 4  Remo con Mancuerna a Una Mano
(2, 55, 5, 'Codos fijos — no balancear'),                 -- id: 5  Curl de Bíceps con Barra Recta
(2, 58, 6, 'Pulgar alineado con el índice'),              -- id: 6  Curl Martillo con Mancuernas

-- Rutina Pecho/Tríceps (routine_id: 3)
(3, 1,  1, 'Agarre a la anchura de los hombros'),         -- id: 7  Press de Banca con Barra
(3, 2,  2, 'Banco a 30 grados — no más'),                 -- id: 8  Press de Banca Inclinado con Barra
(3, 6,  3, 'Codos ligeramente flexionados'),              -- id: 9  Aperturas con Mancuernas en Banco Plano
(3, 8,  4, 'Polea a la altura del pecho'),                -- id: 10 Cruce de Poleas en Polea Alta
(3, 63, 5, 'Codos apuntando al techo — no abrir'),        -- id: 11 Press Francés con Barra Z
(3, 65, 6, 'Cuerda al costado del cuello'),               -- id: 12 Extensión de Tríceps en Polea Alta con Cuerda

-- Rutina Piernas (routine_id: 4)
(4, 24, 1, 'Profundidad paralela al suelo mínimo'),       -- id: 13 Sentadilla con Barra Libre
(4, 27, 2, 'Pies a la anchura de cadera'),                -- id: 14 Prensa de Piernas 45 Grados
(4, 74, 3, 'Rango completo de movimiento'),               -- id: 15 Extensión de Cuádriceps en Máquina
(4, 77, 4, 'Movimiento lento en negativa'),               -- id: 16 Curl de Isquiotibiales Tumbado en Máquina
(4, 79, 5, 'Espalda neutra — no redondear'),              -- id: 17 Peso Muerto Rumano con Barra
(4, 81, 6, 'Rango completo — talón abajo del todo'),      -- id: 18 Elevación de Pantorrillas de Pie en Máquina

-- Rutina Hombros (routine_id: 5)
(5, 32, 1, 'Core apretado — no arquear lumbar'),          -- id: 19 Press Militar con Barra de Pie
(5, 36, 2, 'Codos ligeramente flexionados — no subir más del hombro'), -- id: 20 Elevaciones Laterales con Mancuernas
(5, 37, 3, 'Pulgar arriba — no rotar muñeca'),            -- id: 21 Elevaciones Frontales con Mancuernas
(5, 40, 4, 'Polea a la altura de los ojos'),              -- id: 22 Face Pull en Polea Alta
(5, 86, 5, 'Movimiento vertical puro — no rotar'),        -- id: 23 Encogimientos de Hombros con Mancuernas
(5, 39, 6, 'Pecho apoyado — apretar escápulas al bajar'); -- id: 24 Pájaros con Mancuernas en Banco Inclinado


-- ------------------------------------------------------------
-- 7. routine_sets
-- Sets de referencia por ejercicio de rutina
-- Representan los pesos y reps actuales de John en cada ejercicio
-- ------------------------------------------------------------
INSERT INTO routine_sets (routine_exercise_id, set_number, weight, reps, notes) VALUES

-- Dominadas Agarre Prono (routine_exercise_id: 1)
(1, 1, 0.00, 8,  'Peso corporal'),
(1, 2, 0.00, 7,  'Peso corporal'),
(1, 3, 0.00, 6,  'Peso corporal'),

-- Jalón al Pecho en Polea Alta (routine_exercise_id: 2)
(2, 1, 65.00, 10, NULL),
(2, 2, 65.00, 10, NULL),
(2, 3, 65.00, 8,  NULL),

-- Remo con Barra Agarre Prono (routine_exercise_id: 3)
(3, 1, 80.00, 8, NULL),
(3, 2, 80.00, 8, NULL),
(3, 3, 80.00, 6, NULL),

-- Remo con Mancuerna a Una Mano (routine_exercise_id: 4)
(4, 1, 32.00, 10, 'Cada lado'),
(4, 2, 32.00, 10, 'Cada lado'),
(4, 3, 32.00, 8,  'Cada lado'),

-- Curl de Bíceps con Barra Recta (routine_exercise_id: 5)
(5, 1, 40.00, 10, NULL),
(5, 2, 40.00, 10, NULL),
(5, 3, 40.00, 8,  NULL),

-- Curl Martillo con Mancuernas (routine_exercise_id: 6)
(6, 1, 18.00, 12, 'Cada lado'),
(6, 2, 18.00, 12, 'Cada lado'),
(6, 3, 18.00, 10, 'Cada lado'),

-- Press de Banca con Barra (routine_exercise_id: 7)
(7, 1, 90.00, 8,  NULL),
(7, 2, 90.00, 8,  NULL),
(7, 3, 85.00, 10, NULL),

-- Press de Banca Inclinado con Barra (routine_exercise_id: 8)
(8, 1, 70.00, 10, NULL),
(8, 2, 70.00, 10, NULL),
(8, 3, 70.00, 8,  NULL),

-- Aperturas con Mancuernas en Banco Plano (routine_exercise_id: 9)
(9, 1, 20.00, 12, 'Cada lado'),
(9, 2, 20.00, 12, 'Cada lado'),
(9, 3, 20.00, 10, 'Cada lado'),

-- Cruce de Poleas en Polea Alta (routine_exercise_id: 10)
(10, 1, 15.00, 15, 'Cada lado'),
(10, 2, 15.00, 15, 'Cada lado'),
(10, 3, 15.00, 12, 'Cada lado'),

-- Press Francés con Barra Z (routine_exercise_id: 11)
(11, 1, 40.00, 10, NULL),
(11, 2, 40.00, 10, NULL),
(11, 3, 40.00, 8,  NULL),

-- Extensión de Tríceps en Polea Alta con Cuerda (routine_exercise_id: 12)
(12, 1, 25.00, 12, NULL),
(12, 2, 25.00, 12, NULL),
(12, 3, 25.00, 10, NULL),

-- Sentadilla con Barra Libre (routine_exercise_id: 13)
(13, 1, 100.00, 8, NULL),
(13, 2, 100.00, 8, NULL),
(13, 3, 95.00, 10, NULL),

-- Prensa de Piernas 45 Grados (routine_exercise_id: 14)
(14, 1, 160.00, 10, NULL),
(14, 2, 160.00, 10, NULL),
(14, 3, 160.00, 8,  NULL),

-- Extensión de Cuádriceps en Máquina (routine_exercise_id: 15)
(15, 1, 70.00, 12, NULL),
(15, 2, 70.00, 12, NULL),
(15, 3, 70.00, 10, NULL),

-- Curl de Isquiotibiales Tumbado en Máquina (routine_exercise_id: 16)
(16, 1, 45.00, 12, NULL),
(16, 2, 45.00, 12, NULL),
(16, 3, 45.00, 10, NULL),

-- Peso Muerto Rumano con Barra (routine_exercise_id: 17)
(17, 1, 90.00, 10, NULL),
(17, 2, 90.00, 10, NULL),
(17, 3, 90.00, 8,  NULL),

-- Elevación de Pantorrillas de Pie en Máquina (routine_exercise_id: 18)
(18, 1, 80.00, 15, NULL),
(18, 2, 80.00, 15, NULL),
(18, 3, 80.00, 12, NULL),

-- Press Militar con Barra de Pie (routine_exercise_id: 19)
(19, 1, 60.00, 8,  NULL),
(19, 2, 60.00, 8,  NULL),
(19, 3, 55.00, 10, NULL),

-- Elevaciones Laterales con Mancuernas (routine_exercise_id: 20)
(20, 1, 12.00, 15, 'Cada lado'),
(20, 2, 12.00, 15, 'Cada lado'),
(20, 3, 12.00, 12, 'Cada lado'),

-- Elevaciones Frontales con Mancuernas (routine_exercise_id: 21)
(21, 1, 10.00, 12, 'Cada lado'),
(21, 2, 10.00, 12, 'Cada lado'),
(21, 3, 10.00, 10, 'Cada lado'),

-- Face Pull en Polea Alta (routine_exercise_id: 22)
(22, 1, 20.00, 15, NULL),
(22, 2, 20.00, 15, NULL),
(22, 3, 20.00, 12, NULL),

-- Encogimientos de Hombros con Mancuernas (routine_exercise_id: 23)
(23, 1, 28.00, 15, 'Cada lado'),
(23, 2, 28.00, 15, 'Cada lado'),
(23, 3, 28.00, 12, 'Cada lado'),

-- Pájaros con Mancuernas en Banco Inclinado (routine_exercise_id: 24)
(24, 1, 10.00, 15, 'Cada lado'),
(24, 2, 10.00, 15, 'Cada lado'),
(24, 3, 10.00, 12, 'Cada lado');


-- ------------------------------------------------------------
-- 8. training_sessions
-- Sesiones ejecutadas por John — últimas 4 semanas
-- ------------------------------------------------------------
INSERT INTO training_sessions (user_id, routine_id, gym_id, session_date, duration, notes) VALUES

-- Semana 1
(1, 2, 1, '2026-04-01', 65, 'Subí peso en dominadas con lastre'),  -- id: 1 Espalda/Bíceps
(1, 3, 1, '2026-04-02', 70, 'Press banca llegué a 90kg x 8'),      -- id: 2 Pecho/Tríceps
(1, 4, 2, '2026-04-04', 80, 'Sentadilla bien — rodillas sin dolor'), -- id: 3 Piernas
(1, 5, 2, '2026-04-05', 55, 'Hombros cargados — buena sesión'),     -- id: 4 Hombros

-- Semana 2
(1, 2, 1, '2026-04-08', 60, 'Jalón con 70kg por primera vez'),     -- id: 5 Espalda/Bíceps
(1, 3, 3, '2026-04-09', 68, 'Pecho inclinado mejoró bastante'),    -- id: 6 Pecho/Tríceps
(1, 4, 1, '2026-04-11', 75, 'Prensa con 170kg — nuevo récord'),    -- id: 7 Piernas
(1, 5, 1, '2026-04-12', 50, 'Elevaciones laterales con 14kg'),     -- id: 8 Hombros

-- Semana 3
(1, 2, 2, '2026-04-15', 62, 'Remo con barra técnica mejorada'),    -- id: 9  Espalda/Bíceps
(1, 3, 2, '2026-04-16', 72, 'Aperturas con 22kg cada lado'),       -- id: 10 Pecho/Tríceps
(1, 4, 3, '2026-04-18', 85, 'Rumano con 95kg — espalda perfecta'), -- id: 11 Piernas
(1, 5, 3, '2026-04-19', 58, 'Face pull con más control'),          -- id: 12 Hombros

-- Sesión libre
(1, 1, 1, '2026-04-21', 45, 'Sesión improvisada — solo pecho');    -- id: 13 Sesión Libre


-- ------------------------------------------------------------
-- 9. session_exercises
-- Ejercicios realizados en cada sesión
-- Copiados desde routine_exercises como punto de partida
-- ------------------------------------------------------------
INSERT INTO session_exercises (session_id, exercise_id, order_index, notes) VALUES

-- Sesión 1 — Espalda/Bíceps (04/04/01)
(1, 13, 1, 'Agarre prono — ancho de hombros'),
(1, 18, 2, 'Bajar hasta el pecho'),
(1, 15, 3, 'Espalda recta'),
(1, 16, 4, 'Codo pegado al cuerpo'),
(1, 55, 5, 'Codos fijos'),
(1, 58, 6, NULL),

-- Sesión 2 — Pecho/Tríceps (2026-04-02)
(2, 1,  1, NULL),
(2, 2,  2, NULL),
(2, 6,  3, NULL),
(2, 8,  4, NULL),
(2, 63, 5, NULL),
(2, 65, 6, NULL),

-- Sesión 3 — Piernas (2026-04-04)
(3, 24, 1, NULL),
(3, 27, 2, NULL),
(3, 74, 3, NULL),
(3, 77, 4, NULL),
(3, 79, 5, NULL),
(3, 81, 6, NULL),

-- Sesión 4 — Hombros (2026-04-05)
(4, 32, 1, NULL),
(4, 36, 2, NULL),
(4, 37, 3, NULL),
(4, 40, 4, NULL),
(4, 86, 5, NULL),
(4, 39, 6, NULL),

-- Sesión libre 13 (2026-04-21) — solo pecho improvisado
(13, 1,  1, NULL),
(13, 6,  2, NULL),
(13, 11, 3, NULL);


-- ------------------------------------------------------------
-- 10. sets
-- Series registradas por John en cada sesión
-- Los pesos reflejan progresión realista a lo largo de las semanas
-- ------------------------------------------------------------
INSERT INTO sets (session_exercise_id, set_number, weight, reps, notes) VALUES

-- ── Sesión 1 — Espalda/Bíceps (session_exercise_ids: 1-6) ──

-- Dominadas (se_id: 1)
(1, 1, 0.00, 8,  NULL),
(1, 2, 0.00, 7,  NULL),
(1, 3, 0.00, 6,  NULL),

-- Jalón al Pecho (se_id: 2)
(2, 1, 65.00, 10, NULL),
(2, 2, 65.00, 10, NULL),
(2, 3, 65.00, 8,  NULL),

-- Remo con Barra (se_id: 3)
(3, 1, 80.00, 8, NULL),
(3, 2, 80.00, 8, NULL),
(3, 3, 80.00, 6, NULL),

-- Remo con Mancuerna (se_id: 4)
(4, 1, 32.00, 10, NULL),
(4, 2, 32.00, 10, NULL),
(4, 3, 32.00, 8,  NULL),

-- Curl Bíceps Barra Recta (se_id: 5)
(5, 1, 40.00, 10, NULL),
(5, 2, 40.00, 10, NULL),
(5, 3, 40.00, 8,  NULL),

-- Curl Martillo (se_id: 6)
(6, 1, 18.00, 12, NULL),
(6, 2, 18.00, 12, NULL),
(6, 3, 18.00, 10, NULL),

-- ── Sesión 2 — Pecho/Tríceps (session_exercise_ids: 7-12) ──

-- Press Banca con Barra (se_id: 7)
(7, 1, 90.00, 8,  NULL),
(7, 2, 90.00, 8,  NULL),
(7, 3, 85.00, 10, NULL),

-- Press Banca Inclinado (se_id: 8)
(8, 1, 70.00, 10, NULL),
(8, 2, 70.00, 10, NULL),
(8, 3, 70.00, 8,  NULL),

-- Aperturas Banco Plano (se_id: 9)
(9, 1, 20.00, 12, NULL),
(9, 2, 20.00, 12, NULL),
(9, 3, 20.00, 10, NULL),

-- Cruce Poleas Alta (se_id: 10)
(10, 1, 15.00, 15, NULL),
(10, 2, 15.00, 15, NULL),
(10, 3, 15.00, 12, NULL),

-- Press Francés Barra Z (se_id: 11)
(11, 1, 40.00, 10, NULL),
(11, 2, 40.00, 10, NULL),
(11, 3, 40.00, 8,  NULL),

-- Extensión Tríceps Cuerda (se_id: 12)
(12, 1, 25.00, 12, NULL),
(12, 2, 25.00, 12, NULL),
(12, 3, 25.00, 10, NULL),

-- ── Sesión 3 — Piernas (session_exercise_ids: 13-18) ──

-- Sentadilla Barra Libre (se_id: 13)
(13, 1, 100.00, 8,  NULL),
(13, 2, 100.00, 8,  NULL),
(13, 3, 95.00,  10, NULL),

-- Prensa 45 Grados (se_id: 14)
(14, 1, 160.00, 10, NULL),
(14, 2, 160.00, 10, NULL),
(14, 3, 160.00, 8,  NULL),

-- Extensión Cuádriceps (se_id: 15)
(15, 1, 70.00, 12, NULL),
(15, 2, 70.00, 12, NULL),
(15, 3, 70.00, 10, NULL),

-- Curl Isquiotibiales (se_id: 16)
(16, 1, 45.00, 12, NULL),
(16, 2, 45.00, 12, NULL),
(16, 3, 45.00, 10, NULL),

-- Peso Muerto Rumano (se_id: 17)
(17, 1, 90.00, 10, NULL),
(17, 2, 90.00, 10, NULL),
(17, 3, 90.00, 8,  NULL),

-- Elevación Pantorrillas (se_id: 18)
(18, 1, 80.00, 15, NULL),
(18, 2, 80.00, 15, NULL),
(18, 3, 80.00, 12, NULL),

-- ── Sesión 4 — Hombros (session_exercise_ids: 19-24) ──

-- Press Militar (se_id: 19)
(19, 1, 60.00, 8,  NULL),
(19, 2, 60.00, 8,  NULL),
(19, 3, 55.00, 10, NULL),

-- Elevaciones Laterales (se_id: 20)
(20, 1, 12.00, 15, NULL),
(20, 2, 12.00, 15, NULL),
(20, 3, 12.00, 12, NULL),

-- Elevaciones Frontales (se_id: 21)
(21, 1, 10.00, 12, NULL),
(21, 2, 10.00, 12, NULL),
(21, 3, 10.00, 10, NULL),

-- Face Pull (se_id: 22)
(22, 1, 20.00, 15, NULL),
(22, 2, 20.00, 15, NULL),
(22, 3, 20.00, 12, NULL),

-- Encogimientos Mancuernas (se_id: 23)
(23, 1, 28.00, 15, NULL),
(23, 2, 28.00, 15, NULL),
(23, 3, 28.00, 12, NULL),

-- Pájaros Banco Inclinado (se_id: 24)
(24, 1, 10.00, 15, NULL),
(24, 2, 10.00, 15, NULL),
(24, 3, 10.00, 12, NULL),

-- ── Sesión Libre 13 (session_exercise_ids: 25-27) ──

-- Press Banca con Barra (se_id: 25)
(25, 1, 85.00, 10, NULL),
(25, 2, 85.00, 10, NULL),
(25, 3, 85.00, 8,  NULL),

-- Aperturas Banco Plano (se_id: 26)
(26, 1, 18.00, 12, NULL),
(26, 2, 18.00, 12, NULL),
(26, 3, 18.00, 10, NULL),

-- Press Pecho en Máquina (se_id: 27)
(27, 1, 60.00, 12, NULL),
(27, 2, 60.00, 12, NULL),
(27, 3, 60.00, 10, NULL);