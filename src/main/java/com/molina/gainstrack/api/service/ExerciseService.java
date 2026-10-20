package com.molina.gainstrack.api.service;

import com.molina.gainstrack.api.dto.exercise.ExerciseRequest;
import com.molina.gainstrack.api.dto.exercise.ExerciseResponse;
import com.molina.gainstrack.api.exception.NotFoundException;
import com.molina.gainstrack.api.model.User;
import com.molina.gainstrack.api.repository.ExerciseRepository;
import com.molina.gainstrack.api.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Servicio que maneja la lógica de negocio para la gestión de ejercicios.
 * Obtiene el usuario autenticado desde el contexto de seguridad de Spring
 * para garantizar que cada operación esté acotada al usuario en sesión.
 * Distingue entre ejercicios globales (visibles para todos) y
 * ejercicios privados (creados y gestionados por el usuario).
 */
@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final AuthUtils authUtils;
    private final String uploadsDir;

    public ExerciseService(ExerciseRepository exerciseRepository,
                           AuthUtils authUtils,
                           @Value("${app.uploads-dir}") String uploadsDir) {
        this.exerciseRepository = exerciseRepository;
        this.authUtils = authUtils;
        this.uploadsDir = uploadsDir;
    }

    /**
     * Retorna todos los ejercicios visibles para el usuario autenticado.
     * Incluye ejercicios globales del sistema y ejercicios privados del usuario.
     * Excluye ejercicios eliminados mediante soft delete.
     *
     * @return lista de ejercicios visibles para el usuario en sesión
     */
    public List<ExerciseResponse> findAll() {
        User user = this.authUtils.getAuthenticatedUser();
        return this.exerciseRepository.findAll(user.getId());
    }

    /**
     * Crea un nuevo ejercicio privado para el usuario autenticado.
     * El ejercicio quedará asociado al usuario y no será visible para otros.
     *
     * @param request datos del ejercicio a crear — nombre y grupo muscular
     * @return ExerciseResponse con los datos del ejercicio recién creado
     */
    public ExerciseResponse save(ExerciseRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        return this.exerciseRepository.save(request.name(), request.muscleGroupId(), user.getId());
    }

    /**
     * Actualiza el nombre y grupo muscular de un ejercicio privado del usuario.
     * No permite editar ejercicios globales ni ejercicios de otros usuarios.
     *
     * @param id      id del ejercicio a editar
     * @param request nuevos datos del ejercicio — nombre y grupo muscular
     */
    public void update(Long id, ExerciseRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        this.exerciseRepository.update(id, request.name(), request.muscleGroupId(), user.getId());
    }

    /**
     * Realiza un soft delete del ejercicio del usuario autenticado.
     * El ejercicio no se elimina físicamente — se marca con deleted_at
     * para preservar la integridad del historial de sesiones asociadas.
     * No permite eliminar ejercicios globales ni ejercicios de otros usuarios.
     *
     * @param id id del ejercicio a eliminar
     */
    public void deleteById(Long id) {
        User user = this.authUtils.getAuthenticatedUser();
        this.exerciseRepository.deleteById(id, user.getId());
    }

    public Resource getImage(Long id) {
        String rawPath = exerciseRepository.findRawImageUrl(id);
        if (rawPath != null) {
            Resource resource = new FileSystemResource(Paths.get(uploadsDir).resolve(rawPath));
            if (resource.exists()) return resource;
        }
        Resource fallback = new FileSystemResource(Paths.get(uploadsDir).resolve("exercises/images/no-available.jpeg"));
        if (!fallback.exists()) throw new NotFoundException("Imagen no encontrada");
        return fallback;
    }

    public Resource getVideo(Long id) {
        String rawPath = exerciseRepository.findRawVideoUrl(id);
        if (rawPath != null) {
            Resource resource = new FileSystemResource(Paths.get(uploadsDir).resolve(rawPath));
            if (resource.exists()) return resource;
        }
        Resource fallback = new FileSystemResource(Paths.get(uploadsDir).resolve("exercises/images/no-available.jpeg"));
        if (!fallback.exists()) throw new NotFoundException("Video no encontrado");
        return fallback;
    }
}
