package com.molina.gainstrack.api.controller;

import com.molina.gainstrack.api.dto.routine.*;
import com.molina.gainstrack.api.dto.session.TrainingSessionSummaryResponse;
import com.molina.gainstrack.api.service.RoutineService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller que expone los endpoints REST para la gestión de rutinas.
 * Todas las operaciones requieren autenticación JWT.
 */
@RestController
@RequestMapping("/api/v1/routines")
public class RoutineController {

    private final RoutineService routineService;

    /**
     * @param routineService servicio con la lógica de negocio de rutinas
     */
    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    /**
     * Retorna el resumen de todas las rutinas del usuario autenticado.
     * No incluye ejercicios ni sets — usar GET /{id} para el detalle completo.
     *
     * @return 200 OK con la lista de rutinas del usuario
     */
    @GetMapping
    public ResponseEntity<List<RoutineSummaryResponse>> findAll() {
        return ResponseEntity.ok(this.routineService.findAll());
    }

    /**
     * Retorna el detalle completo de una rutina del usuario autenticado.
     * Incluye ejercicios con sus sets de referencia.
     *
     * @param id id de la rutina a consultar
     * @return 200 OK con ejercicios y sets anidados
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoutineDetailResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.routineService.findById(id));
    }

    /**
     * Crea una nueva rutina para el usuario autenticado.
     *
     * @param request body con nombre obligatorio y notas opcionales
     * @return 201 Created con los datos de la rutina creada
     */
    @PostMapping
    public ResponseEntity<RoutineSummaryResponse> save(@Valid @RequestBody RoutineRequest request) {
        return ResponseEntity.status(201).body(this.routineService.save(request));
    }

    /**
     * Actualiza nombre y/o notas de una rutina del usuario autenticado.
     * Solo modifica los campos enviados en el body.
     *
     * @param id      id de la rutina a actualizar
     * @param request body con name y/o notes — ambos opcionales
     * @return 204 No Content
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id,
                                       @RequestBody RoutineRequest request) {
        this.routineService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina una rutina del usuario autenticado.
     * Advertencia: elimina también todos los ejercicios, sets y sesiones asociadas.
     *
     * @param id id de la rutina a eliminar
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        this.routineService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agrega un ejercicio a una rutina del usuario autenticado.
     *
     * @param id      id de la rutina
     * @param request body con exerciseId y orderIndex
     * @return 201 Created con el ejercicio agregado a la rutina
     */
    @PostMapping("/{id}/exercises")
    public ResponseEntity<RoutineExerciseResponse> saveExercise(@PathVariable("id") Long id,
                                                                @Valid @RequestBody RoutineExerciseRequest request) {
        return ResponseEntity.status(201).body(this.routineService.saveExercise(id,
                                                                                request));
    }

    /**
     * Elimina un ejercicio de una rutina del usuario autenticado.
     * Advertencia: elimina también todos los sets asociados al ejercicio.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises a eliminar
     * @return 200 OK con la rutina actualizada
     */
    @DeleteMapping("/{id}/exercises/{routineExerciseId}")
    public ResponseEntity<RoutineDetailResponse> deleteExerciseById(@PathVariable("id") Long id,
                                                                    @PathVariable("routineExerciseId") Long routineExerciseId) {
        return ResponseEntity.ok(this.routineService.deleteExerciseById(id,
                                                                        routineExerciseId));
    }

    /**
     * Agrega un set a un ejercicio de una rutina del usuario autenticado.
     * weight, reps y notes se insertan con el valor recibido en el body.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises al que agregar el set
     * @param request           body con setNumber obligatorio y weight/reps/notes opcionales
     * @return 201 Created con la rutina actualizada
     */
    @PostMapping("/{id}/exercises/{routineExerciseId}/sets")
    public ResponseEntity<RoutineDetailResponse> saveExerciseSet(@PathVariable("id") Long id,
                                                                 @PathVariable("routineExerciseId") Long routineExerciseId,
                                                                 @Valid @RequestBody RoutineSetRequest request) {
        return ResponseEntity.status(201).body(this.routineService.saveExerciseSet(id,
                                                                                   routineExerciseId,
                                                                                   request));
    }

    /**
     * Elimina un set de un ejercicio de una rutina del usuario autenticado.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises al que pertenece el set
     * @param setId             id del set a eliminar
     * @return 200 OK con la rutina actualizada
     */
    @DeleteMapping("/{id}/exercises/{routineExerciseId}/sets/{setId}")
    public ResponseEntity<RoutineDetailResponse> deleteExerciseSetById(@PathVariable("id") Long id,
                                                                       @PathVariable("routineExerciseId") Long routineExerciseId,
                                                                       @PathVariable("setId") Long setId) {
        return ResponseEntity.ok(this.routineService.deleteExerciseSetById(id,
                                                                           routineExerciseId,
                                                                           setId));
    }

    /**
     * Actualiza los datos de un ejercicio dentro de una rutina del usuario autenticado.
     * Solo modifica los campos enviados en el body.
     * Si se cambia el exerciseId, elimina automáticamente los sets del ejercicio anterior.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises a actualizar
     * @param request           body con exerciseId, orderIndex y/o notes — todos opcionales
     * @return 200 OK con la rutina actualizada
     * No aplica validación de campos — todos son opcionales para actualización parcial.
     */
    @PatchMapping("/{id}/exercises/{routineExerciseId}")
    public ResponseEntity<RoutineDetailResponse> updateExercise(@PathVariable("id") Long id,
                                                                @PathVariable("routineExerciseId") Long routineExerciseId,
                                                                @RequestBody RoutineExerciseRequest request) {
        return ResponseEntity.ok(this.routineService.updateExercise(id,
                                                                    routineExerciseId,
                                                                    request));
    }

    /**
     * Actualiza los datos de un set de un ejercicio de una rutina del usuario autenticado.
     * Solo modifica los campos enviados en el body.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises al que pertenece el set
     * @param setId             id del set a actualizar
     * @param request           body con setNumber, weight, reps y/o notes — todos opcionales
     * @return 200 OK con la rutina actualizada
     */
    @PatchMapping("/{id}/exercises/{routineExerciseId}/sets/{setId}")
    public ResponseEntity<RoutineDetailResponse> updateExerciseSet(@PathVariable("id") Long id,
                                                                   @PathVariable("routineExerciseId") Long routineExerciseId,
                                                                   @PathVariable("setId") Long setId,
                                                                   @RequestBody RoutineSetRequest request) {
        return ResponseEntity.ok(this.routineService.updateExerciseSet(id,
                                                                       routineExerciseId,
                                                                       setId,
                                                                       request));
    }

    /**
     * Retorna todas las sesiones de una rutina del usuario autenticado.
     * Las sesiones se ordenan de más reciente a más antigua.
     * Usar GET /sessions/{id} para el detalle completo de una sesión específica.
     *
     * @param id id de la rutina cuyas sesiones se consultan
     * @return 200 OK con la lista de sesiones de la rutina
     */
    @GetMapping("/{id}/sessions")
    public ResponseEntity<List<TrainingSessionSummaryResponse>> findSessionsByRoutineId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.routineService.findSessionsByRoutineId(id));
    }
}
