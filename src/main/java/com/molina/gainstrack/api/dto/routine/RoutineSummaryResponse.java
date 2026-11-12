package com.molina.gainstrack.api.dto.routine;

import java.time.LocalDate;

/**
 * DTO de salida con el resumen de una rutina de entrenamiento.
 * Se usa en el listado general — no incluye ejercicios ni sets.
 *
 * @param id        identificador único de la rutina
 * @param name      nombre de la rutina
 * @param createdAt fecha de creación de la rutina
 * @param notes     notas opcionales de la rutina
 */
public record RoutineSummaryResponse(Long id,
                                     String name,
                                     LocalDate createdAt,
                                     String notes) {}
