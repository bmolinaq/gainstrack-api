package com.molina.gainstrack.api.repository;

import com.molina.gainstrack.api.dto.exercise.ExerciseResponse;
import com.molina.gainstrack.api.dto.gym.GymResponse;
import com.molina.gainstrack.api.dto.session.*;
import com.molina.gainstrack.api.dto.shared.MuscleGroupResponse;
import com.molina.gainstrack.api.exception.NotFoundException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Repositorio de acceso a datos para la tabla training_sessions.
 * Ejecuta SQL puro mediante JdbcClient, sin ORM.
 * Todas las operaciones están acotadas al usuario propietario
 * para garantizar aislamiento de datos entre usuarios.
 */
@Repository
public class TrainingSessionRepository {

    /**
     * @param jdbcClient cliente JDBC para ejecutar consultas SQL
     */
    private final JdbcClient jdbcClient;

    public TrainingSessionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Retorna el resumen de todas las sesiones de entrenamiento del usuario.
     * Incluye los datos del gimnasio asociado mediante LEFT JOIN —
     * sesiones sin gym muestran "No especificado".
     * No incluye ejercicios ni sets — usar findById para el detalle completo.
     *
     * @param userId id del usuario autenticado
     * @return lista de sesiones con sus datos de cabecera, gimnasio y duración
     */
    public List<TrainingSessionSummaryResponse> findAll(Long userId) {
        return jdbcClient.sql("SELECT ts.id AS training_session_id, " +
                                      "g.id AS gym_id, " +
                                      "g.name AS gym_name, " +
                                      "ts.session_date AS training_session_date, " +
                                      "ts.duration AS training_session_duration, " +
                                      "ts.notes AS training_session_notes " +
                              "FROM training_sessions ts " +
                              "LEFT JOIN gyms g ON ts.gym_id = g.id " +
                              "WHERE ts.user_id = :userId " +
                              "ORDER BY ts.session_date DESC")
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    Long gymId = rs.getObject("gym_id", Long.class);
                    GymResponse gym = gymId != null
                            ? new GymResponse(gymId, rs.getString("gym_name"))
                            : new GymResponse(null, "No especificado");

                    return new TrainingSessionSummaryResponse(
                            rs.getLong("training_session_id"),
                            gym,
                            rs.getDate("training_session_date")
                                    .toLocalDate(),
                            rs.getInt("training_session_duration"),
                            rs.getString("training_session_notes")
                    );
                })
                .list();
    }

    /**
     * Retorna el resumen de todas las sesiones asociadas a una rutina del usuario.
     * Incluye los datos del gimnasio mediante LEFT JOIN.
     * Ordenadas de más reciente a más antigua.
     *
     * @param routineId id de la rutina cuyas sesiones se consultan
     * @param userId    id del usuario propietario — previene acceso a sesiones ajenas
     * @return lista de sesiones ordenadas por fecha descendente
     */
    public List<TrainingSessionSummaryResponse> findAllByRoutineId(Long routineId,
                                                                   Long userId) {
        return jdbcClient.sql("SELECT ts.id AS training_session_id, " +
                              "g.id AS gym_id, " +
                              "g.name AS gym_name, " +
                              "ts.session_date AS training_session_date, " +
                              "ts.duration AS training_session_duration, " +
                              "ts.notes AS training_session_notes " +
                              "FROM training_sessions ts " +
                              "LEFT JOIN gyms g ON ts.gym_id = g.id " +
                              "WHERE ts.routine_id = :routineId AND ts.user_id = :userId " +
                              "ORDER BY ts.session_date DESC")
                .param("routineId", routineId)
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    Long gymId = rs.getObject("gym_id", Long.class);
                    GymResponse gym = gymId != null
                            ? new GymResponse(gymId, rs.getString("gym_name"))
                            : new GymResponse(null, "No especificado");

                    return new TrainingSessionSummaryResponse(
                            rs.getLong("training_session_id"),
                            gym,
                            rs.getDate("training_session_date")
                                    .toLocalDate(),
                            rs.getInt("training_session_duration"),
                            rs.getString("training_session_notes")
                    );
                })
                .list();
    }

    /**
     * Retorna el resumen de todas las sesiones del usuario para una rutina y
     * gimnasio específicos, ordenadas de más reciente a más antigua.
     * Se usa para consultar el historial completo de entrenamientos de una
     * rutina en un gimnasio puntual — por ejemplo, todas las sesiones de la
     * rutina de piernas realizadas en un gimnasio determinado.
     * gymId puede ser null — se usa el operador NULL-safe <=> de MySQL para incluir
     * sesiones sin gimnasio asociado (sesión libre de gimnasio) como un grupo
     * válido de búsqueda, tratando null como un gimnasio más.
     * Incluye los datos del gimnasio asociado mediante LEFT JOIN.
     *
     * @param routineId id de la rutina consultada
     * @param gymId     id del gimnasio consultado — null representa sin gimnasio
     * @param userId    id del usuario propietario — previene acceso a sesiones ajenas
     * @return lista de sesiones ordenadas por fecha descendente — vacía si no
     *         existe historial para esa combinación de rutina y gimnasio
     */
    public List<TrainingSessionSummaryResponse> findAllByRoutineAndGym(Long routineId,
                                                                       Long gymId,
                                                                       Long userId) {
        return jdbcClient.sql("SELECT ts.id AS training_session_id, " +
                              "g.id AS gym_id, " +
                              "g.name AS gym_name, " +
                              "ts.session_date AS training_session_date, " +
                              "ts.duration AS training_session_duration, " +
                              "ts.notes AS training_session_notes " +
                              "FROM training_sessions ts " +
                              "LEFT JOIN gyms g ON ts.gym_id = g.id " +
                              "WHERE ts.routine_id = :routineId AND ts.user_id = :userId " +
                              "AND ts.gym_id <=> :gymId " +
                              "ORDER BY ts.session_date DESC, ts.id DESC")
                .param("routineId", routineId)
                .param("userId", userId)
                .param("gymId", gymId)
                .query((rs, rowNum) -> {
                    Long gymIdQuery = rs.getObject("gym_id", Long.class);
                    GymResponse gym = gymIdQuery != null
                            ? new GymResponse(gymIdQuery, rs.getString("gym_name"))
                            : new GymResponse(null, "No especificado");

                    return new TrainingSessionSummaryResponse(
                            rs.getLong("training_session_id"),
                            gym,
                            rs.getDate("training_session_date")
                                    .toLocalDate(),
                            rs.getInt("training_session_duration"),
                            rs.getString("training_session_notes")
                    );
                })
                .list();
    }

    /**
     * Crea una nueva sesión de entrenamiento persistiendo exactamente lo recibido —
     * rutina, gimnasio, notas, ejercicios y sets ya decididos por el cliente,
     * típicamente a partir de lo que devolvió preview() y editado por el usuario
     * durante el entrenamiento. No aplica ninguna fusión ni copia: inserta la
     * sesión, luego cada ejercicio, luego cada set de cada ejercicio, en ese orden.
     * Delega a findById para retornar el detalle completo de la sesión creada.
     *
     * @param userId    id del usuario autenticado
     * @param routineId id de la rutina ejecutada — obligatorio
     * @param gymId     id del gimnasio donde se realiza la sesión — puede ser null
     * @param duration  duración de la sesión en minutos — obligatorio
     * @param notes     notas de la sesión — puede ser null
     * @param exercises ejercicios de la sesión con sus sets, ya definidos por el cliente
     * @return TrainingSessionDetailResponse con la sesión completa tal como fue recibida
     */
    @Transactional
    public TrainingSessionDetailResponse save(Long userId, Long routineId, Long gymId, Integer duration,
                                              String notes, List<SessionExerciseRequest> exercises) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcClient.sql("INSERT INTO training_sessions (user_id, routine_id, gym_id, session_date, duration, notes) " +
                            "VALUES (:userId, :routineId, :gymId, NOW(), :duration, :notes)")
                       .param("userId", userId)
                       .param("routineId", routineId)
                       .param("gymId", gymId)
                       .param("duration", duration)
                       .param("notes", notes)
                       .update(keyHolder);
        Long sessionId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        for (SessionExerciseRequest exercise : exercises) {
            KeyHolder exerciseKeyHolder = new GeneratedKeyHolder();
            this.jdbcClient.sql("INSERT INTO session_exercises (session_id, exercise_id, order_index, notes) " +
                                "VALUES (:sessionId, :exerciseId, :orderIndex, :notes)")
                           .param("sessionId", sessionId)
                           .param("exerciseId", exercise.exerciseId())
                           .param("orderIndex", exercise.orderIndex())
                           .param("notes", exercise.notes())
                           .update(exerciseKeyHolder);
            Long sessionExerciseId = Objects.requireNonNull(exerciseKeyHolder.getKey()).longValue();

            List<SessionSetRequest> sets = exercise.sets() != null ? exercise.sets() : List.of();
            for (SessionSetRequest set : sets) {
                this.jdbcClient.sql("INSERT INTO sets (session_exercise_id, set_number, weight, reps, notes) " +
                                    "VALUES (:sessionExerciseId, :setNumber, :weight, :reps, :notes)")
                               .param("sessionExerciseId", sessionExerciseId)
                               .param("setNumber", set.setNumber())
                               .param("weight", set.weight())
                               .param("reps", set.reps())
                               .param("notes", set.notes())
                               .update();
            }
        }

        return this.findById(sessionId, userId);
    }

    /**
     * Retorna el detalle completo de una sesión de entrenamiento.
     * Ejecuta tres queries anidadas para construir la respuesta:
     * 1. Datos de la sesión y gimnasio
     * 2. Ejercicios de la sesión con sus datos del catálogo
     * 3. Sets de cada ejercicio
     * El userId garantiza que el usuario solo pueda ver sus propias sesiones.
     *
     * @param id     id de la sesión a consultar
     * @param userId id del usuario propietario — previene acceso a sesiones ajenas
     * @return TrainingSessionDetailResponse con ejercicios y sets anidados
     */
    public TrainingSessionDetailResponse findById(Long id, Long userId) {
        return jdbcClient.sql("SELECT ts.id AS session_id, " +
                              "g.id AS gym_id, " +
                              "g.name AS gym_name, " +
                              "ts.session_date AS session_date, " +
                              "ts.duration AS session_duration, " +
                              "ts.notes AS session_notes " +
                              "FROM training_sessions ts " +
                              "LEFT JOIN gyms AS g ON ts.gym_id = g.id " +
                              "WHERE ts.id = :sessionId AND ts.user_id = :userId")
                .param("sessionId", id)
                .param("userId", userId)
                .query((rs, rowNum) -> {
                    Long gymIdQuery = rs.getObject("gym_id", Long.class);
                    GymResponse gym = gymIdQuery != null
                            ? new GymResponse(gymIdQuery, rs.getString("gym_name"))
                            : new GymResponse(null, "No especificado");

                    // Query 2: ejercicios de la sesión
                    List<TrainingSessionExerciseResponse> sessionExercises =
                            jdbcClient.sql("SELECT se.id AS session_exercise_id, " +
                                                  "se.order_index AS session_exercise_order_index, " +
                                                  "se.notes AS session_exercise_notes, " +
                                                  "e.id AS exercise_id, " +
                                                  "e.name AS exercise_name, " +
                                                  "mg.id AS muscle_group_id, " +
                                                  "mg.name AS muscle_group_name, " +
                                                  "e.user_id AS exercise_user_id, " +
                                                  "e.is_predefined AS exercise_is_predefined " +
                                           "FROM session_exercises se " +
                                           "JOIN exercises e ON se.exercise_id = e.id " +
                                           "JOIN muscle_groups mg ON e.muscle_group_id = mg.id " +
                                           "WHERE se.session_id = :sessionId " +
                                           "ORDER BY se.order_index ASC")
                                    .param("sessionId", id)
                                    .query((rs2, rowNum2) -> {
                                        // Query 3: sets de cada ejercicio
                                        List<SetResponse> sessionExerciseSets =
                                                jdbcClient.sql("SELECT id, set_number, weight, reps, notes " +
                                                                "FROM sets " +
                                                                "WHERE session_exercise_id = :seId")
                                                        .param("seId", rs2.getLong("session_exercise_id"))
                                                        .query((rs3, rowNum3) -> new SetResponse(
                                                                rs3.getLong("id"),
                                                                rs3.getInt("set_number"),
                                                                rs3.getObject("weight", Double.class),
                                                                rs3.getObject("reps", Integer.class),
                                                                rs3.getString("notes")
                                                        ))
                                                        .list();

                                        return new TrainingSessionExerciseResponse(
                                                rs2.getLong("session_exercise_id"),
                                                rs2.getInt("session_exercise_order_index"),
                                                rs2.getString("session_exercise_notes"),
                                                new ExerciseResponse(
                                                        rs2.getLong("exercise_id"),
                                                        rs2.getString("exercise_name"),
                                                        new MuscleGroupResponse(
                                                                rs2.getLong("muscle_group_id"),
                                                                rs2.getString("muscle_group_name")
                                                        ),
                                                        rs2.getLong("exercise_user_id"),
                                                        rs2.getBoolean("exercise_is_predefined")
                                                ),
                                                sessionExerciseSets
                                        );
                                    })
                                    .list();
                    return new TrainingSessionDetailResponse(
                            rs.getLong("session_id"),
                            gym,
                            rs.getDate("session_date")
                              .toLocalDate(),
                            rs.getInt("session_duration"),
                            rs.getString("session_notes"),
                            sessionExercises
                    );
                })
                .optional()
                .orElseThrow(() -> new NotFoundException("Sesión de entrenamiento no encontrada"));
    }

    /**
     * Elimina una sesión de entrenamiento por su id.
     * El userId garantiza que el usuario solo pueda eliminar sus propias sesiones.
     * Por el CASCADE del modelo relacional, se eliminan también
     * todos los session_exercises y sets asociados.
     *
     * @param id     id de la sesión a eliminar
     * @param userId id del usuario propietario — previene eliminación de sesiones ajenas
     */
    public void deleteById(Long id, Long userId) {
        int affectedRows = jdbcClient.sql("DELETE FROM training_sessions " +
                                          "WHERE id = :id AND user_id = :userId")
                                     .param("id", id)
                                     .param("userId", userId)
                                     .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Sesión de entrenamiento no encontrada");
        }
    }

    /**
     * Actualiza las notas y/o la duración de una sesión de entrenamiento.
     * Usa COALESCE para preservar el valor actual de cada campo si llega null.
     * El userId garantiza que el usuario solo pueda editar sus propias sesiones.
     *
     * @param id       id de la sesión a actualizar
     * @param userId   id del usuario propietario — previene edición de sesiones ajenas
     * @param notes    nuevas notas — puede ser null para mantener las actuales
     * @param duration nueva duración en minutos — puede ser null para mantener la actual
     * @throws NotFoundException si la sesión no existe o no pertenece al usuario
     */
    public void update(Long id, Long userId, String notes, Integer duration) {
        int affectedRows = this.jdbcClient.sql("UPDATE training_sessions " +
                                               "SET notes = COALESCE(:notes, notes), " +
                                                   "duration = COALESCE(:duration, duration) " +
                                               "WHERE id = :id " +
                                               "AND user_id = :userId")
                                          .param("notes", notes)
                                          .param("duration", duration)
                                          .param("id", id)
                                          .param("userId", userId)
                                          .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Sesión no encontrada");
        }
    }

    /**
     * Agrega un ejercicio a una sesión de entrenamiento.
     * El ejercicio se inserta con notas nulas — se editan posteriormente.
     *
     * @param id         id de la sesión
     * @param exerciseId id del ejercicio del catálogo a agregar
     * @param orderIndex posición del ejercicio dentro de la sesión
     * @param userId     id del usuario propietario
     * @return TrainingSessionExerciseResponse con el ejercicio agregado
     */
    public TrainingSessionExerciseResponse saveExercise(Long id,
                                                        Long exerciseId,
                                                        Integer orderIndex,
                                                        Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        this.jdbcClient.sql("INSERT INTO session_exercises (session_id, exercise_id, order_index, notes) " +
                            "VALUES (:id, :exerciseId, :orderIndex, NULL)")
                       .param("id", id)
                       .param("exerciseId", exerciseId)
                       .param("orderIndex", orderIndex)
                       .update(keyHolder);
        Long sessionExerciseId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        TrainingSessionDetailResponse sessionDetailResponse = this.findById(id, userId);

        return sessionDetailResponse.exercises()
                                    .stream()
                                    .filter(se -> se.id().equals(sessionExerciseId))
                                    .findFirst()
                                    .orElseThrow(() -> new NotFoundException("Ejercicio no encontrado"));
    }

    /**
     * Elimina un ejercicio de una sesión de entrenamiento.
     * Por el CASCADE del modelo relacional, se eliminan también
     * todos los sets asociados al ejercicio eliminado.
     * Retorna el detalle completo de la sesión actualizada.
     *
     * @param id                 id de la sesión
     * @param sessionExerciseId  id del registro en session_exercises a eliminar
     * @param userId             id del usuario propietario
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el ejercicio no existe en la sesión
     */
    public TrainingSessionDetailResponse deleteExerciseById(Long id,
                                                            Long sessionExerciseId,
                                                            Long userId) {
        int affectedRows = this.jdbcClient.sql("DELETE FROM session_exercises " +
                                               "WHERE session_id = :id " +
                                               "AND id = :sessionExerciseId")
                                          .param("id", id)
                                          .param("sessionExerciseId", sessionExerciseId)
                                          .update();

        if  (affectedRows == 0) {
            throw new NotFoundException("Ejercicio no encontrado para sesión especificada");
        }

        return this.findById(id,
                             userId);
    }

    /**
     * Actualiza los datos de un ejercicio dentro de una sesión.
     * Usa COALESCE para actualizar solo los campos enviados.
     * Si se cambia el exerciseId, los sets deben eliminarse previamente
     * desde el service antes de llamar este método.
     * Retorna el detalle completo de la sesión actualizada.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises a actualizar
     * @param exerciseId        nuevo ejercicio — puede ser null para mantener el actual
     * @param orderIndex        nueva posición — puede ser null para mantener la actual
     * @param notes             nuevas notas — puede ser null para mantener las actuales
     * @param userId            id del usuario propietario
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el ejercicio no existe en la sesión
     */
    public TrainingSessionDetailResponse updateExercise(Long id,
                                                        Long sessionExerciseId,
                                                        Long exerciseId,
                                                        Integer orderIndex,
                                                        String notes,
                                                        Long userId) {
        int affectedRows = this.jdbcClient.sql("UPDATE session_exercises " +
                                               "SET exercise_id = COALESCE(:exerciseId, exercise_id), " +
                                                   "order_index = COALESCE(:orderIndex, order_index), " +
                                                   "notes = COALESCE(:notes, notes) " +
                                               "WHERE id = :sessionExerciseId")
                .param("exerciseId", exerciseId)
                .param("orderIndex", orderIndex)
                .param("notes", notes)
                .param("sessionExerciseId", sessionExerciseId)
                .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Ejercicio no encontrado para sesión especificada");
        }

        return this.findById(id,
                             userId);
    }

    /**
     * Agrega un set a un ejercicio de una sesión.
     * weight, reps y notes son opcionales — se insertan con el valor
     * recibido en el request, incluyendo null si el usuario aún no los conoce.
     * Retorna el detalle completo de la sesión actualizada.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises
     * @param setNumber         número de serie dentro del ejercicio
     * @param weight            peso en kg — puede ser null
     * @param reps              repeticiones — puede ser null
     * @param notes             notas del set — puede ser null
     * @param userId            id del usuario propietario
     * @return TrainingSessionDetailResponse con la sesión actualizada
     */
    public TrainingSessionDetailResponse saveExerciseSet(Long id,
                                                         Long sessionExerciseId,
                                                         Integer setNumber,
                                                         Double weight,
                                                         Integer reps,
                                                         String notes,
                                                         Long userId) {
        this.jdbcClient.sql("INSERT INTO sets (session_exercise_id, set_number, weight, reps, notes) " +
                            "VALUES (:sessionExerciseId, :setNumber, :weight, :reps, :notes)")
                .param("sessionExerciseId", sessionExerciseId)
                .param("setNumber", setNumber)
                .param("weight", weight)
                .param("reps", reps)
                .param("notes", notes)
                .update();

        return this.findById(id,
                             userId);
    }

    /**
     * Elimina un set de un ejercicio de una sesión.
     * Retorna el detalle completo de la sesión actualizada.
     *
     * @param id                id de la sesión
     * @param setId             id del set a eliminar
     * @param sessionExerciseId id del registro en session_exercises
     * @param userId            id del usuario propietario
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el set no existe para el ejercicio especificado
     */
    public TrainingSessionDetailResponse deleteExerciseSetById(Long id,
                                                               Long setId,
                                                               Long sessionExerciseId,
                                                               Long userId) {
        int affectedRows = this.jdbcClient.sql("DELETE FROM sets " +
                                               "WHERE id = :setId " +
                                               "AND session_exercise_id = :sessionExerciseId")
                                          .param("setId", setId)
                                          .param("sessionExerciseId", sessionExerciseId)
                                          .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Set no encontrado para ejercicio especificado");
        }

        return this.findById(id,
                             userId);
    }

    /**
     * Actualiza los datos de un set de un ejercicio de una sesión.
     * setNumber usa COALESCE y se preserva si no se envía. weight, reps y
     * notes se sobrescriben directamente con el valor recibido — enviar
     * null los deja vacíos intencionalmente.
     * Retorna el detalle completo de la sesión actualizada.
     *
     * @param id                id de la sesión
     * @param setId             id del set a actualizar
     * @param sessionExerciseId id del registro en session_exercises
     * @param setNumber         nuevo número de serie — null preserva el actual
     * @param weight            nuevo peso en kg — null lo deja sin definir
     * @param reps              nuevas repeticiones — null las deja sin definir
     * @param notes             nuevas notas — null las deja sin definir
     * @param userId            id del usuario propietario
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el set no existe para el ejercicio especificado
     */
    public TrainingSessionDetailResponse updateExerciseSet(Long id,
                                                           Long setId,
                                                           Long sessionExerciseId,
                                                           Integer setNumber,
                                                           Double weight,
                                                           Integer reps,
                                                           String notes,
                                                           Long userId) {
        int affectedRows = this.jdbcClient.sql("UPDATE sets " +
                                               "SET set_number = COALESCE(:setNumber, set_number), " +
                                                   "weight = :weight, " +
                                                   "reps = :reps, " +
                                                   "notes = :notes " +
                                               "WHERE id = :setId " +
                                               "AND session_exercise_id = :sessionExerciseId")
                                          .param("setNumber", setNumber)
                                          .param("weight", weight)
                                          .param("reps", reps)
                                          .param("notes", notes)
                                          .param("setId", setId)
                                          .param("sessionExerciseId", sessionExerciseId)
                                          .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Set no encontrado para ejercicio especificado");
        }

        return this.findById(id,
                             userId);
    }

    /**
     * Elimina todos los sets asociados a un ejercicio de una sesión.
     * Se utiliza cuando el ejercicio de una sesión es reemplazado por otro,
     * ya que los sets del ejercicio anterior no son relevantes para el nuevo.
     *
     * @param sessionExerciseId id del registro en session_exercises
     *                          cuyos sets serán eliminados
     */
    public void deleteSetsFromSessionExercise(Long sessionExerciseId) {
        this.jdbcClient.sql("DELETE FROM sets " +
                            "WHERE session_exercise_id = :sessionExerciseId")
                       .param("sessionExerciseId", sessionExerciseId)
                       .update();
    }
}
