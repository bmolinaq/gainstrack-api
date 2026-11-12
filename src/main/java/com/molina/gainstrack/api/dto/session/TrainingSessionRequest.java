package com.molina.gainstrack.api.dto.session;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * DTO de entrada para la creación de una sesión de entrenamiento.
 * Representa el estado final de la sesión — típicamente construido por el
 * cliente a partir de la respuesta de GET /sessions/preview y editado
 * libremente por el usuario durante el entrenamiento. save() persiste
 * exactamente lo recibido, sin ninguna fusión ni copia adicional.
 * @Valid en exercises es necesario para que la validación baje también
 * a cada SessionExerciseRequest de la lista.
 *
 * @param routineId id de la rutina ejecutada — obligatorio
 * @param gymId     id del gimnasio donde se realiza la sesión — opcional, puede ser null
 * @param duration  duración de la sesión en minutos — obligatorio
 * @param notes     notas de la sesión — opcional
 * @param exercises        ejercicios realizados con sus sets — opcional, null se trata como lista vacía
 * @param routineExercises nueva plantilla de la rutina (ejercicios y sets) — opcional.
 *                         null no modifica la rutina; con valor, reemplaza por completo
 *                         los ejercicios y sets de la rutina (una lista vacía la deja vacía).
 *                         Se ignora si la rutina es la rutina libre
 */
public record TrainingSessionRequest(
        @NotNull(message = "{field.required}")
        @Positive(message = "{field.positive}")
        Long routineId,
        Long gymId,
        @NotNull(message = "{field.required}")
        @Positive(message = "{field.positive}")
        Integer duration,
        String notes,
        @Valid
        List<SessionExerciseRequest> exercises,
        @Valid
        List<SessionExerciseRequest> routineExercises
) {}
