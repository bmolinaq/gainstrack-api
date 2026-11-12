package com.molina.gainstrack.api.service;

import com.molina.gainstrack.api.dto.routine.*;
import com.molina.gainstrack.api.dto.session.TrainingSessionSummaryResponse;
import com.molina.gainstrack.api.exception.NotFoundException;
import com.molina.gainstrack.api.model.User;
import com.molina.gainstrack.api.repository.ExerciseRepository;
import com.molina.gainstrack.api.repository.RoutineRepository;
import com.molina.gainstrack.api.repository.TrainingSessionRepository;
import com.molina.gainstrack.api.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio que maneja la lógica de negocio para la gestión de rutinas.
 * Obtiene el usuario autenticado desde AuthUtils para garantizar
 * que cada operación esté acotada al usuario en sesión.
 */
@Service
public class RoutineService {

    private static final Logger LOG = LoggerFactory.getLogger(RoutineService.class);

    private final RoutineRepository routineRepository;
    private final ExerciseRepository exerciseRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final AuthUtils authUtils;

    /**
     * @param routineRepository         repositorio de acceso a datos de rutinas
     * @param exerciseRepository        repositorio de acceso a datos de ejercicios
     * @param trainingSessionRepository repositorio de acceso a datos de sesiones
     * @param authUtils                 utilidad para obtener el usuario autenticado
     */
    public RoutineService(RoutineRepository routineRepository,
                          ExerciseRepository exerciseRepository,
                          TrainingSessionRepository trainingSessionRepository,
                          AuthUtils authUtils) {
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.authUtils = authUtils;
    }

    /**
     * Retorna el resumen de todas las rutinas del usuario autenticado.
     * No incluye ejercicios ni sets.
     *
     * @return lista de rutinas del usuario en sesión
     */
    public List<RoutineSummaryResponse> findAll() {
        User user = this.authUtils.getAuthenticatedUser();
        return this.routineRepository.findAll(user.getId());
    }

    /**
     * Retorna el detalle completo de una rutina del usuario autenticado.
     * Incluye ejercicios con sus sets de referencia.
     *
     * @param id id de la rutina a consultar
     * @return RoutineDetailResponse con ejercicios y sets anidados
     */
    public RoutineDetailResponse findById(Long id) {
        User user = this.authUtils.getAuthenticatedUser();
        return this.routineRepository.findById(id,
                                               user.getId());
    }

    /**
     * Crea una nueva rutina para el usuario autenticado.
     *
     * @param request datos de la rutina — nombre obligatorio, notas opcionales
     * @return RoutineSummaryResponse con los datos de la rutina creada
     */
    public RoutineSummaryResponse save(RoutineRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineSummaryResponse routine = this.routineRepository.save(user.getId(),
                                                                     request.name(),
                                                                     request.notes());
        LOG.info("Nueva rutina creada — routineId: {}, userId: {}, name: {}",
                 routine.id(),
                 user.getId(),
                 request.name());
        return routine;
    }

    /**
     * Actualiza nombre y/o notas de una rutina del usuario autenticado.
     * Solo actualiza los campos enviados — campos null conservan su valor actual.
     *
     * @param id      id de la rutina a actualizar
     * @param request campos a actualizar — name y/o notes, ambos opcionales
     */
    public void update(Long id, RoutineRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        this.routineRepository.update(id,
                                      user.getId(),
                                      request.name(),
                                      request.notes());
    }

    /**
     * Elimina una rutina del usuario autenticado.
     * Por el CASCADE del modelo relacional, se eliminan también
     * todos los ejercicios, sets y sesiones asociadas a esta rutina.
     *
     * @param id id de la rutina a eliminar
     * @throws NotFoundException si la rutina no existe o no pertenece al usuario
     */
    @Transactional
    public void deleteById(Long id) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(id,
                                                                        user.getId());

        this.routineRepository.deleteById(id,
                                          user.getId());
        LOG.warn("Rutina eliminada — routineId: {}, userId: {}, name: {}",
                 id, user.getId(),
                 routine.name());
    }

    /**
     * Agrega un ejercicio a una rutina del usuario autenticado.
     * Valida que el ejercicio exista en el catálogo antes de insertarlo.
     *
     * @param id      id de la rutina
     * @param request datos del ejercicio a agregar — exerciseId y orderIndex
     * @return RoutineExerciseResponse con el ejercicio agregado
     * @throws NotFoundException si el ejercicio no existe en el catálogo
     */
    @Transactional
    public RoutineExerciseResponse saveExercise(Long id, RoutineExerciseRequest request) {
        User user = this.authUtils.getAuthenticatedUser();

        if (!exerciseRepository.existsById(request.exerciseId()))
            throw new NotFoundException("Ejercicio no encontrado");

        RoutineExerciseResponse routineExercise = this.routineRepository.saveExercise(id,
                                                                              request.exerciseId(),
                                                                              request.orderIndex(),
                                                                              user.getId());
        LOG.info("Ejercicio agregado a rutina — routineId: {}, exerciseId: {}",
                 id,
                 request.exerciseId());
        return routineExercise;
    }

    /**
     * Elimina un ejercicio de una rutina del usuario autenticado.
     * Valida que el ejercicio pertenezca a la rutina antes de eliminar.
     * Por el CASCADE del modelo relacional, se eliminan también
     * todos los sets asociados al ejercicio eliminado.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises a eliminar
     * @return RoutineDetailResponse con la rutina actualizada
     * @throws NotFoundException si el routineExerciseId no pertenece a la rutina
     */
    @Transactional
    public RoutineDetailResponse deleteExerciseById(Long id, Long routineExerciseId) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(id,
                                                                        user.getId());

        boolean isExerciseFromRoutine = routine.exercises()
                                               .stream()
                                               .anyMatch(re -> re.id().equals(routineExerciseId));

        if (!isExerciseFromRoutine)
            throw new NotFoundException("Ejercicio no encontrado para rutina especificada");

        RoutineDetailResponse updated = this.routineRepository.deleteExerciseById(id,
                                                                                  routineExerciseId,
                                                                                  user.getId());
        LOG.warn("Ejercicio eliminado de rutina — routineId: {}, routineExerciseId: {}",
                 id,
                 routineExerciseId);
        return updated;
    }

    /**
     * Agrega un set a un ejercicio de una rutina del usuario autenticado.
     * Valida que el routineExerciseId pertenezca a la rutina antes de insertar.
     * weight, reps y notes se insertan con el valor recibido en el request.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises al que agregar el set
     * @param request           datos del set — setNumber obligatorio, weight/reps/notes opcionales
     * @return RoutineDetailResponse con la rutina actualizada
     * @throws NotFoundException si el routineExerciseId no pertenece a la rutina
     */
    @Transactional
    public RoutineDetailResponse saveExerciseSet(Long id,
                                                 Long routineExerciseId,
                                                 RoutineSetRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(id,
                                                                        user.getId());

        boolean isExerciseFromRoutine = routine.exercises()
                                               .stream()
                                               .anyMatch(re -> re.id().equals(routineExerciseId));

        if (!isExerciseFromRoutine)
            throw new NotFoundException("Ejercicio no encontrado para rutina especificada");

        RoutineDetailResponse updated = this.routineRepository.saveExerciseSet(id,
                                                                               routineExerciseId,
                                                                               request.setNumber(),
                                                                               request.weight(),
                                                                               request.reps(),
                                                                               request.notes(),
                                                                               user.getId());
        LOG.info("Set agregado a rutina — routineId: {}, routineExerciseId: {}, setNumber: {}, weight: {}, reps: {}, notes: {}",
                 id,
                 routineExerciseId,
                 request.setNumber(),
                 request.weight(),
                 request.reps(),
                 request.notes());
        return updated;
    }

    /**
     * Elimina un set de un ejercicio de una rutina del usuario autenticado.
     * Valida que el routineExerciseId pertenezca a la rutina antes de eliminar.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises al que pertenece el set
     * @param setId             id del set a eliminar
     * @return RoutineDetailResponse con la rutina actualizada
     * @throws NotFoundException si el routineExerciseId no pertenece a la rutina
     */
    @Transactional
    public RoutineDetailResponse deleteExerciseSetById(Long id,
                                                       Long routineExerciseId,
                                                       Long setId) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(id,
                                                                        user.getId());

        boolean isExerciseFromRoutine = routine.exercises()
                                               .stream()
                                               .anyMatch(re -> re.id().equals(routineExerciseId));

        if (!isExerciseFromRoutine)
            throw new NotFoundException("Ejercicio no encontrado para rutina especificada");

        RoutineDetailResponse updated = this.routineRepository.deleteExerciseSetById(id,
                                                                                     setId,
                                                                                     routineExerciseId,
                                                                                     user.getId());
        LOG.warn("Set eliminado de rutina — routineId: {}, routineExerciseId: {}, setId: {}",
                 id,
                 routineExerciseId,
                 setId);
        return updated;
    }

    /**
     * Actualiza los datos de un ejercicio dentro de una rutina del usuario autenticado.
     * Solo actualiza los campos enviados — campos null conservan su valor actual.
     * Si se cambia el exerciseId, elimina automáticamente los sets existentes
     * ya que pertenecían al ejercicio anterior.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises a actualizar
     * @param request           campos a actualizar — exerciseId, orderIndex y/o notes
     * @return RoutineDetailResponse con la rutina actualizada
     * @throws NotFoundException si el routineExerciseId no pertenece a la rutina
     */
    @Transactional
    public RoutineDetailResponse updateExercise(Long id,
                                                Long routineExerciseId,
                                                RoutineExerciseRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(id,
                                                                        user.getId());

        Long currentExerciseId = routine.exercises()
                                        .stream()
                                        .filter(re -> re.id().equals(routineExerciseId))
                                        .map(re -> re.exercise().id())
                                        .findFirst()
                                        .orElseThrow(() -> new NotFoundException("Ejercicio no encontrado para rutina especificada"));

        if (request.exerciseId() != null && !currentExerciseId.equals(request.exerciseId())) {
            this.routineRepository.deleteSetsFromRoutineExercise(routineExerciseId);
            LOG.warn("Sets eliminados por cambio de ejercicio — routineExerciseId: {}",
                     routineExerciseId);
        }

        return this.routineRepository.updateExercise(id,
                                                     routineExerciseId,
                                                     request.exerciseId(),
                                                     request.orderIndex(),
                                                     request.notes(),
                                                     user.getId());
    }

    /**
     * Actualiza los datos de un set de un ejercicio de una rutina del usuario autenticado.
     * Solo actualiza los campos enviados — campos null conservan su valor actual.
     *
     * @param id                id de la rutina
     * @param routineExerciseId id del registro en routine_exercises al que pertenece el set
     * @param setId             id del set a actualizar
     * @param request           campos a actualizar — setNumber, weight, reps y/o notes
     * @return RoutineDetailResponse con la rutina actualizada
     * @throws NotFoundException si el routineExerciseId no pertenece a la rutina
     */
    public RoutineDetailResponse updateExerciseSet(Long id,
                                                   Long routineExerciseId,
                                                   Long setId,
                                                   RoutineSetRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(id,
                                                                        user.getId());

        routine.exercises()
               .stream()
               .filter(re -> re.id().equals(routineExerciseId))
               .map(re -> re.exercise().id())
               .findFirst()
               .orElseThrow(() -> new NotFoundException("Ejercicio no encontrado para rutina especificada"));

        return this.routineRepository.updateExerciseSet(id,
                                                        setId,
                                                        routineExerciseId,
                                                        request.setNumber(),
                                                        request.weight(),
                                                        request.reps(),
                                                        request.notes(),
                                                        user.getId());
    }

    /**
     * Retorna todas las sesiones asociadas a una rutina del usuario autenticado.
     * Valida que la rutina exista y pertenezca al usuario antes de consultar.
     * Las sesiones se retornan ordenadas de más reciente a más antigua.
     *
     * @param id id de la rutina cuyas sesiones se consultan
     * @return lista de sesiones con sus datos de cabecera y gimnasio
     * @throws NotFoundException si la rutina no existe o no pertenece al usuario
     */
    public List<TrainingSessionSummaryResponse> findSessionsByRoutineId(Long id) {
        User user = this.authUtils.getAuthenticatedUser();
        this.routineRepository.findById(id,
                                        user.getId());
        return this.trainingSessionRepository.findAllByRoutineId(id,
                                                                 user.getId());
    }
}