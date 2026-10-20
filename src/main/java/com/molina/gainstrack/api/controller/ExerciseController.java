package com.molina.gainstrack.api.controller;

import com.molina.gainstrack.api.dto.exercise.ExerciseRequest;
import com.molina.gainstrack.api.dto.exercise.ExerciseResponse;
import com.molina.gainstrack.api.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller que expone los endpoints REST para la gestión de ejercicios.
 * Todas las operaciones requieren autenticación JWT.
 * El usuario autenticado se resuelve internamente en el Service
 * desde el contexto de seguridad — no se recibe como parámetro.
 */
@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {

    /**
     * @param exerciseService servicio con la lógica de negocio de ejercicios.
     */
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    /**
     * Retorna ejercicios privados y genericos de usuario autenticado.
     *
     * @return 200 OK con la lista de gimnasios del usuario.
     */
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> findAll() {
        return ResponseEntity.ok(exerciseService.findAll());
    }

    /**
     * Crea un nuevo ejercicio  para usuario autenticado.
     *
     * @param request body con el nombre del ejercicio.
     * @return 201 Created con los datos del ejercicio creado.
     */
    @PostMapping
    public ResponseEntity<ExerciseResponse> save(@Valid @RequestBody ExerciseRequest request) {
        return ResponseEntity.status(201).body(exerciseService.save(request));
    }

    /**
     * Actualiza la información de un ejercicio privado para el usuario autenticado.
     * Solo puede actualizar ejercicios propios del usuario.
     *
     * @param id id del ejercicio a editar.
     * @return 200 OK sin body.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody ExerciseRequest request) {
        exerciseService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina un ejercicio por su id.
     * Se hace un soft delete, por lo que el ejercicio no se borra, si no que se le agrega una fecha de eliminación.
     *
     * @param id id del gimnasio a eliminar.
     * @return 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        exerciseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getImage(@PathVariable("id") Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(exerciseService.getImage(id));
    }

    @GetMapping("/{id}/video")
    public ResponseEntity<Resource> getVideo(@PathVariable("id") Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(exerciseService.getVideo(id));
    }
}
