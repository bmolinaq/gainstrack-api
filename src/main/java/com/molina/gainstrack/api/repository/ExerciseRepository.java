package com.molina.gainstrack.api.repository;

import com.molina.gainstrack.api.dto.exercise.ExerciseResponse;
import com.molina.gainstrack.api.dto.shared.MuscleGroupResponse;
import com.molina.gainstrack.api.exception.NotFoundException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la tabla exercises.
 * Ejecuta SQL puro mediante JdbcClient, sin ORM.
 * Gestiona tanto ejercicios globales (user_id = NULL) como
 * ejercicios privados por usuario (user_id = valor).
 */
@Repository
public class ExerciseRepository {

    private final JdbcClient jdbcClient;

    public ExerciseRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Lista todos los ejercicios visibles para un usuario:
     * ejercicios globales (user_id IS NULL) y ejercicios privados del usuario.
     * Excluye ejercicios eliminados mediante soft delete (deleted_at IS NOT NULL).
     *
     * @param userId id del usuario autenticado
     * @return lista de ejercicios visibles para el usuario
     */
    public List<ExerciseResponse> findAll(Long userId) {
        return jdbcClient.sql("SELECT e.id AS exercise_id, " +
                                     "e.name AS exercise_name, " +
                                     "e.muscle_group_id AS muscle_group_id, " +
                                     "mg.name AS muscle_group_name, " +
                                     "e.user_id AS user_id, " +
                                     "e.is_predefined AS is_predefined " +
                              "FROM exercises e " +
                              "JOIN muscle_groups mg ON e.muscle_group_id = mg.id " +
                              "WHERE (e.user_id = :userId OR e.user_id IS NULL) AND e.deleted_at IS NULL")
                         .param("userId", userId)
                         .query((rs, rowNum) -> new ExerciseResponse(
                                 rs.getLong("exercise_id"),
                                 rs.getString("exercise_name"),
                                 new MuscleGroupResponse(
                                         rs.getLong("muscle_group_id"),
                                         rs.getString("muscle_group_name")
                                 ),
                                 rs.getLong("user_id"),
                                 rs.getBoolean("is_predefined")
                         ))
                         .list();
    }

    /**
     * Inserta un nuevo ejercicio privado y retorna el registro creado.
     * El ejercicio queda asociado al usuario — no será visible para otros.
     *
     * @param name          nombre del nuevo ejercicio
     * @param muscleGroupId id del grupo muscular al que pertenece
     * @param userId        id del usuario que crea el ejercicio
     * @return ExerciseResponse con los datos del ejercicio recién insertado
     */
    public ExerciseResponse save(String name, Long muscleGroupId, Long userId) {
        jdbcClient.sql("INSERT INTO exercises (name, muscle_group_id, user_id) " +
                       "VALUES (:name, :muscleGroupId, :userId)")
                .param("name", name)
                .param("muscleGroupId", muscleGroupId)
                .param("userId", userId)
                .update();

        return jdbcClient.sql("SELECT e.id AS exercise_id, " +
                                     "e.name AS exercise_name, " +
                                     "e.muscle_group_id AS muscle_group_id, " +
                                     "mg.name AS muscle_group_name, " +
                                     "e.user_id AS user_id, " +
                                     "e.is_predefined AS is_predefined " +
                              "FROM exercises e " +
                              "JOIN muscle_groups mg " +
                              "ON e.muscle_group_id = mg.id " +
                              "WHERE e.user_id = :userId ORDER BY e.id DESC LIMIT 1")
                         .param("userId", userId)
                         .query((rs, rowNum) -> new ExerciseResponse(
                                 rs.getLong("exercise_id"),
                                 rs.getString("exercise_name"),
                                 new MuscleGroupResponse(
                                         rs.getLong("muscle_group_id"),
                                         rs.getString("muscle_group_name")
                                 ),
                                 rs.getLong("user_id"),
                                 rs.getBoolean("is_predefined")
                         ))
                         .single();
    }

    /**
     * Actualiza el nombre y grupo muscular de un ejercicio privado.
     * El userId garantiza que el usuario solo pueda editar sus propios ejercicios.
     *
     * @param id            id del ejercicio a editar
     * @param name          nuevo nombre del ejercicio
     * @param muscleGroupId id del nuevo grupo muscular
     * @param userId        id del usuario propietario — previene edición de ejercicios ajenos
     */
    public void update(Long id, String name, Long muscleGroupId, Long userId) {
        int affectedRows =  jdbcClient.sql("UPDATE exercises " +
                                           "SET muscle_group_id = :muscleGroupId, name = :name " +
                                           "WHERE id = :id " +
                                           "AND user_id = :userId")
                                      .param("muscleGroupId", muscleGroupId)
                                      .param("name", name)
                                      .param("id", id)
                                      .param("userId", userId)
                                      .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Ejercicio no encontrado");
        }
    }

    /**
     * Realiza un soft delete del ejercicio asignando la fecha actual a deleted_at.
     * El registro persiste en la base de datos para mantener la integridad
     * del historial de sesiones que referencian este ejercicio.
     * El userId garantiza que el usuario solo pueda eliminar sus propios ejercicios.
     *
     * @param id     id del ejercicio a eliminar
     * @param userId id del usuario propietario — previene eliminación de ejercicios ajenos
     */
    public void deleteById(Long id, Long userId) {
        int affectedRows = jdbcClient.sql("UPDATE exercises " +
                                          "SET deleted_at = NOW() " +
                                          "WHERE id = :id AND user_id = :userId")
                                     .param("id", id)
                                     .param("userId", userId)
                                     .update();

        if  (affectedRows == 0) {
            throw new NotFoundException("Ejercicio no encontrado");
        }
    }

    public String findRawImageUrl(Long id) {
        List<String> result = jdbcClient.sql("SELECT image_url FROM exercises WHERE id = :id AND deleted_at IS NULL")
                                        .param("id", id)
                                        .query((rs, rowNum) -> rs.getString("image_url"))
                                        .list();
        if (result.isEmpty()) throw new NotFoundException("Ejercicio no encontrado");
        return result.get(0);
    }

    public String findRawVideoUrl(Long id) {
        List<String> result = jdbcClient.sql("SELECT video_url FROM exercises WHERE id = :id AND deleted_at IS NULL")
                                        .param("id", id)
                                        .query((rs, rowNum) -> rs.getString("video_url"))
                                        .list();
        if (result.isEmpty()) throw new NotFoundException("Ejercicio no encontrado");
        return result.get(0);
    }

    public boolean existsById(Long id) {
        return jdbcClient.sql("SELECT COUNT(*) " +
                              "FROM exercises " +
                              "WHERE id = :id AND deleted_at IS NULL")
                         .param("id", id)
                         .query(Integer.class)
                         .single() > 0;
    }
}
