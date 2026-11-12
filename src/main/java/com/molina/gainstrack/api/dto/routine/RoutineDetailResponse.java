package com.molina.gainstrack.api.dto.routine;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de salida con el detalle completo de una rutina de entrenamiento.
 * Incluye los ejercicios que la componen con sus sets de referencia.
 *
 * @param id        identificador único de la rutina
 * @param name      nombre de la rutina
 * @param createdAt fecha de creación de la rutina
 * @param notes     notas opcionales de la rutina
 * @param exercises lista de ejercicios con sus sets de referencia
 */
public record RoutineDetailResponse(Long id,
                                    String name,
                                    LocalDate createdAt,
                                    String notes,
                                    List<RoutineExerciseResponse> exercises) {}
