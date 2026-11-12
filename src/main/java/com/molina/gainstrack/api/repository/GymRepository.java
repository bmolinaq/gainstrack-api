package com.molina.gainstrack.api.repository;

import com.molina.gainstrack.api.dto.gym.GymResponse;
import com.molina.gainstrack.api.exception.NotFoundException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la tabla gyms.
 * Ejecuta SQL puro mediante JdbcClient, sin ORM.
 * Todas las operaciones están acotadas al usuario propietario
 * para garantizar aislamiento de datos entre usuarios.
 */
@Repository
public class GymRepository {

    /**
     * @param jdbcClient cliente JDBC para ejecutar consultas SQL
     */
    private final JdbcClient jdbcClient;

    GymRepository(JdbcClient jdbcClient) { this.jdbcClient = jdbcClient; }

    /**
     * Inserta un nuevo gimnasio y retorna el registro creado.
     * El gimnasio se crea con is_primary en FALSE por defecto.
     * Se recupera el registro usando ORDER BY id DESC LIMIT 1
     * ya que MySQL no soporta RETURNING como PostgreSQL.
     *
     * @param userId id del usuario propietario del gimnasio
     * @param name   nombre o apodo del gimnasio
     * @return GymResponse con los datos del gimnasio recién creado
     */
    public GymResponse save(Long userId, String name) {
        jdbcClient.sql("INSERT INTO gyms (user_id, name) VALUES (:userId, :name)")
                  .param("userId", userId)
                  .param("name", name)
                  .update();

        return jdbcClient.sql("SELECT id, name FROM gyms WHERE user_id = :userId ORDER BY id DESC LIMIT 1")
                         .param("userId", userId)
                         .query(GymResponse.class)
                         .optional()
                         .orElseThrow(() -> new NotFoundException("Sesión no encontrada"));
    }

    /**
     * Retorna todos los gimnasios registrados por un usuario.
     *
     * @param userId id del usuario propietario
     * @return lista de gimnasios del usuario
     */
    public List<GymResponse> findAll(Long userId) {
        return jdbcClient.sql("SELECT id, name FROM gyms WHERE user_id = :userId")
                         .param("userId", userId)
                         .query(GymResponse.class)
                         .list();
    }

    /**
     * Retorna un gimnasio del usuario a partir de su id.
     * El userId garantiza que el usuario solo pueda consultar sus propios gimnasios.
     *
     * @param id     id del gimnasio a consultar
     * @param userId id del usuario propietario — previene acceso a gimnasios ajenos
     * @return GymResponse con los datos del gimnasio
     * @throws NotFoundException si el gimnasio no existe o no pertenece al usuario
     */
    public GymResponse findById(Long id, Long userId) {
        return jdbcClient.sql("SELECT id, name FROM gyms WHERE id = :id AND user_id = :userId")
                         .param("id", id)
                         .param("userId", userId)
                         .query(GymResponse.class)
                         .optional()
                         .orElseThrow(() -> new NotFoundException("Gimnasio no encontrado"));
    }

    /**
     * Actualiza el nombre de un gimnasio del usuario autenticado.
     * El userId garantiza que el usuario solo pueda editar sus propios gimnasios.
     *
     * @param id     id del gimnasio a editar
     * @param name   nuevo nombre del gimnasio
     * @param userId id del usuario propietario — previene edición de gimnasios ajenos
     */
    public void update(Long id, String name, Long userId) {
        int affectedRows = jdbcClient.sql("UPDATE gyms SET name = :name WHERE id = :id AND user_id = :userId")
                                     .param("name", name)
                                     .param("id", id)
                                     .param("userId", userId)
                                     .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Gimnasio no encontrado");
        }
    }

    /**
     * Elimina un gimnasio por su id.
     * Por el CASCADE definido en el DDL, se eliminarán también
     * todas las sesiones de entrenamiento asociadas a este gimnasio.
     *
     * @param id id del gimnasio a eliminar
     * @param userId id del usuario propietario
     */
    public void deleteById(Long id, Long userId) {
        int affectedRows = jdbcClient.sql("DELETE FROM gyms WHERE id = :id AND user_id = :userId")
                                     .param("id", id)
                                     .param("userId", userId)
                                     .update();

        if (affectedRows == 0) {
            throw new NotFoundException("Gimnasio no encontrado");
        }
    }
}
