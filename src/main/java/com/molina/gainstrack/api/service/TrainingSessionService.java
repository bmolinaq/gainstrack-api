package com.molina.gainstrack.api.service;

import com.molina.gainstrack.api.dto.gym.GymResponse;
import com.molina.gainstrack.api.dto.routine.RoutineDetailResponse;
import com.molina.gainstrack.api.dto.session.*;
import com.molina.gainstrack.api.exception.NotFoundException;
import com.molina.gainstrack.api.model.User;
import com.molina.gainstrack.api.repository.ExerciseRepository;
import com.molina.gainstrack.api.repository.GymRepository;
import com.molina.gainstrack.api.repository.RoutineRepository;
import com.molina.gainstrack.api.repository.TrainingSessionRepository;
import com.molina.gainstrack.api.utils.AuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio que maneja la lógica de negocio para las sesiones de entrenamiento.
 * Obtiene el usuario autenticado desde AuthUtils para garantizar
 * que cada operación esté acotada al usuario en sesión.
 */
@Service
public class TrainingSessionService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainingSessionService.class);

    private final TrainingSessionRepository trainingSessionRepository;
    private final ExerciseRepository exerciseRepository;
    private final RoutineRepository routineRepository;
    private final GymRepository gymRepository;
    private final AuthUtils authUtils;

    /**
     * @param trainingSessionRepository repositorio de acceso a datos de sesiones
     * @param exerciseRepository        repositorio de acceso a datos de ejercicios
     * @param routineRepository         repositorio de acceso a datos de rutinas
     * @param gymRepository             repositorio de acceso a datos de gimnasios
     * @param authUtils                 utilidad para obtener el usuario autenticado
     */
    public TrainingSessionService(TrainingSessionRepository trainingSessionRepository,
                                  ExerciseRepository exerciseRepository,
                                  RoutineRepository routineRepository,
                                  GymRepository gymRepository,
                                  AuthUtils authUtils) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.exerciseRepository = exerciseRepository;
        this.routineRepository = routineRepository;
        this.gymRepository = gymRepository;
        this.authUtils = authUtils;
    }

    /**
     * Retorna el resumen de todas las sesiones del usuario autenticado.
     * Cada sesión incluye su gimnasio asociado pero no el detalle
     * de ejercicios ni sets.
     *
     * @return lista de sesiones de entrenamiento del usuario en sesión
     */
    public List<TrainingSessionSummaryResponse> findAll() {
        User user = this.authUtils.getAuthenticatedUser();
        return this.trainingSessionRepository.findAll(user.getId());
    }

    /**
     * Construye el molde de una nueva sesión de entrenamiento a partir de la rutina,
     * sin persistir nada. Ejercicios, orden, sets, pesos, reps y notas se copian tal
     * cual desde la plantilla de la rutina. Todos los ids (sesión, ejercicios y sets)
     * se retornan en null, ya que no representan registros reales — el frontend
     * puede construir el body de save() directamente desde esta respuesta y editarlo
     * durante el entrenamiento. Evita dejar sesiones huérfanas cuando el usuario
     * abandona el entrenamiento antes de terminarlo.
     *
     * @param routineId id de la rutina a ejecutar — obligatorio
     * @param gymId     id del gimnasio — opcional, null representa sesión sin gimnasio
     * @return TrainingSessionDetailResponse simulado, con ids null y duration 0
     * @throws NotFoundException si la rutina o el gimnasio no existen o no pertenecen al usuario
     */
    public TrainingSessionDetailResponse preview(Long routineId, Long gymId) {
        User user = this.authUtils.getAuthenticatedUser();
        RoutineDetailResponse routine = this.routineRepository.findById(routineId, user.getId());
        GymResponse gym = gymId != null
                ? this.gymRepository.findById(gymId, user.getId())
                : new GymResponse(null, "No especificado");

        List<TrainingSessionExerciseResponse> exercises = routine.exercises()
                .stream()
                .map(re -> new TrainingSessionExerciseResponse(null,
                                                               re.orderIndex(),
                                                               re.notes(),
                                                               re.exercise(),
                                                               re.sets()
                                                                 .stream()
                                                                 .map(s -> new SetResponse(null,
                                                                                           s.setNumber(),
                                                                                           s.weight(),
                                                                                           s.reps(),
                                                                                           s.notes()))
                                                                 .toList()))
                .toList();

        return new TrainingSessionDetailResponse(null, gym, LocalDate.now(), 0, routine.notes(), exercises);
    }

    /**
     * Persiste una sesión de entrenamiento completa tal como la envía el cliente —
     * rutina, gimnasio, duración, notas, ejercicios y sets ya decididos, típicamente
     * a partir de lo que devolvió preview() y luego editado por el usuario durante el
     * entrenamiento. No aplica ninguna lógica de fusión ni de copia: inserta
     * exactamente lo recibido en el request. Valida que la rutina y el gimnasio
     * pertenezcan al usuario antes de insertar.
     * Si viene routineExercises, en la misma transacción se reemplazan los
     * ejercicios y sets de la rutina por los recibidos — el cliente decide su
     * contenido (solo pesos/reps actualizados, o también la estructura).
     *
     * @param request datos completos de la sesión — routineId y duration obligatorios,
     *                gymId/notes opcionales, exercises con sus sets anidados,
     *                routineExercises opcional con la nueva plantilla de la rutina
     * @return TrainingSessionDetailResponse con el detalle completo de la sesión creada
     * @throws NotFoundException si la rutina o el gimnasio no existen o no pertenecen al usuario
     */
    @Transactional
    public TrainingSessionDetailResponse save(TrainingSessionRequest request) {
        User user = this.authUtils.getAuthenticatedUser();

        if (!this.routineRepository.existsById(request.routineId(), user.getId()))
            throw new NotFoundException("Rutina no encontrada");

        if (request.gymId() != null)
            this.gymRepository.findById(request.gymId(), user.getId());

        List<SessionExerciseRequest> exercises = request.exercises() != null ? request.exercises() : List.of();
        TrainingSessionDetailResponse session = this.trainingSessionRepository.save(user.getId(),
                                                                                    request.routineId(),
                                                                                    request.gymId(),
                                                                                    request.duration(),
                                                                                    request.notes(),
                                                                                    exercises);

        int exerciseCount = exercises.size();
        int setCount = exercises.stream()
                                .mapToInt(e -> e.sets() != null ? e.sets().size() : 0)
                                .sum();
        LOG.info("Nueva sesión creada — sessionId: {}, userId: {}, routineId: {}, exercises: {}, sets: {}",
                session.id(), user.getId(), request.routineId(), exerciseCount, setCount);

        if (request.routineExercises() != null) {
            this.routineRepository.replaceExercises(request.routineId(), request.routineExercises());
            LOG.info("Rutina actualizada desde sesión — routineId: {}, sessionId: {}, exercises: {}",
                     request.routineId(), session.id(), request.routineExercises().size());
        }

        return session;
    }

    /**
     * Retorna el detalle completo de una sesión del usuario autenticado.
     * Incluye ejercicios realizados con sus sets y pesos registrados.
     *
     * @param id id de la sesión a consultar
     * @return TrainingSessionDetailResponse con ejercicios y sets anidados
     */
    public TrainingSessionDetailResponse findById(Long id) {
        User user = this.authUtils.getAuthenticatedUser();
        return this.trainingSessionRepository.findById(id, user.getId());
    }

    /**
     * Retorna el resumen de todas las sesiones del usuario autenticado para una
     * rutina y gimnasio específicos, ordenadas de más reciente a más antigua.
     * Se usa para consultar el historial completo de entrenamientos de una rutina
     * en un gimnasio puntual — por ejemplo, todas las sesiones de la rutina de
     * piernas realizadas en un gimnasio determinado.
     * gymId puede ser null para consultar sesiones sin gimnasio asociado
     * (sesión sin gimnasio) — se trata como un gimnasio más.
     *
     * @param routineId id de la rutina consultada
     * @param gymId     id del gimnasio consultado — null representa sin gimnasio
     * @return lista de sesiones ordenadas por fecha descendente — vacía si no
     *         existe historial para esa combinación de rutina y gimnasio
     */
    public List<TrainingSessionSummaryResponse> findAllByRoutineAndGym(Long routineId, Long gymId) {
        User user = this.authUtils.getAuthenticatedUser();
        return this.trainingSessionRepository.findAllByRoutineAndGym(routineId, gymId, user.getId());
    }

    /**
     * Elimina una sesión de entrenamiento del usuario autenticado.
     * Por el CASCADE del modelo relacional, se eliminan también
     * todos los ejercicios y sets asociados a esta sesión.
     *
     * @param id id de la sesión a eliminar
     */
    public void deleteById(Long id) {
        User user = this.authUtils.getAuthenticatedUser();
        trainingSessionRepository.deleteById(id, user.getId());
        LOG.warn("Sesión eliminada — sessionId: {}, userId: {}", id, user.getId());
    }

    /**
     * Actualiza las notas y/o la duración de una sesión del usuario autenticado.
     * Solo modifica los campos enviados — la fecha y rutina son inmutables.
     *
     * @param id      id de la sesión a actualizar
     * @param request datos a actualizar — notes y/o duration, ambos opcionales
     * @throws NotFoundException si la sesión no existe
     */
    public void update(Long id, TrainingSessionNotesRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        this.trainingSessionRepository.update(id, user.getId(), request.notes(), request.duration());
    }

    /**
     * Agrega un ejercicio a una sesión del usuario autenticado.
     * Valida que el ejercicio exista en el catálogo antes de insertarlo.
     *
     * @param id      id de la sesión
     * @param request datos del ejercicio a agregar — exerciseId y orderIndex
     * @return TrainingSessionExerciseResponse con el ejercicio agregado
     * @throws NotFoundException si el ejercicio no existe en el catálogo
     */
    @Transactional
    public TrainingSessionExerciseResponse saveExercise(Long id,
                                                        TrainingSessionExerciseRequest request) {
        User user = this.authUtils.getAuthenticatedUser();

        if (!exerciseRepository.existsById(request.exerciseId()))
            throw new NotFoundException("Ejercicio no encontrado");

        TrainingSessionExerciseResponse sessionExercise = this.trainingSessionRepository.saveExercise(id,
                                                                                                       request.exerciseId(),
                                                                                                       request.orderIndex(),
                                                                                                       user.getId());
        LOG.info("Ejercicio agregado a sesión — sessionId: {}, exerciseId: {}",
                 id, request.exerciseId());
        return sessionExercise;
    }

    /**
     * Elimina un ejercicio de una sesión del usuario autenticado.
     * Valida que el ejercicio pertenezca a la sesión antes de eliminar.
     * Por el CASCADE del modelo relacional, se eliminan también sus sets.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises a eliminar
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el sessionExerciseId no pertenece a la sesión
     */
    @Transactional
    public TrainingSessionDetailResponse deleteExerciseById(Long id,
                                                            Long sessionExerciseId) {
        User user = this.authUtils.getAuthenticatedUser();
        TrainingSessionDetailResponse session = this.trainingSessionRepository.findById(id,
                                                                                        user.getId());

        boolean isExerciseFromSession = session.exercises()
                                               .stream()
                                               .anyMatch(se -> se.id().equals(sessionExerciseId));

        if (!isExerciseFromSession)
            throw new NotFoundException("Ejercicio no encontrado para sesión especificada");

        TrainingSessionDetailResponse updated = this.trainingSessionRepository.deleteExerciseById(id,
                                                                                                  sessionExerciseId,
                                                                                                  user.getId());
        LOG.warn("Ejercicio eliminado de sesión — sessionId: {}, sessionExerciseId: {}",
                 id, sessionExerciseId);
        return updated;
    }

    /**
     * Actualiza los datos de un ejercicio dentro de una sesión del usuario autenticado.
     * Solo actualiza los campos enviados — campos null conservan su valor actual.
     * Si se cambia el exerciseId, elimina automáticamente los sets existentes
     * ya que pertenecían al ejercicio anterior.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises a actualizar
     * @param request           campos a actualizar — exerciseId, orderIndex y/o notes
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el sessionExerciseId no pertenece a la sesión
     */
    @Transactional
    public TrainingSessionDetailResponse updateExercise(Long id,
                                                        Long sessionExerciseId,
                                                        TrainingSessionExerciseRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        TrainingSessionDetailResponse session = this.trainingSessionRepository.findById(id,
                                                                                        user.getId());

        Long currentExerciseId = session.exercises()
                                        .stream()
                                        .filter(se -> se.id().equals(sessionExerciseId))
                                        .map(se -> se.exercise().id())
                                        .findFirst()
                                        .orElseThrow(() -> new NotFoundException("Ejercicio no encontrado para sesión especificada"));

        if (request.exerciseId() != null && !currentExerciseId.equals(request.exerciseId())) {
            this.trainingSessionRepository.deleteSetsFromSessionExercise(sessionExerciseId);
            LOG.warn("Sets eliminados por cambio de ejercicio — sessionExerciseId: {}",
                     sessionExerciseId);
        }

        return this.trainingSessionRepository.updateExercise(id,
                                                             sessionExerciseId,
                                                             request.exerciseId(),
                                                             request.orderIndex(),
                                                             request.notes(),
                                                             user.getId());
    }

    /**
     * Agrega un set a un ejercicio de una sesión del usuario autenticado.
     * Valida que el sessionExerciseId pertenezca a la sesión antes de insertar.
     * weight, reps y notes se insertan con el valor recibido en el request.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises al que agregar el set
     * @param request           datos del set — setNumber obligatorio, weight/reps/notes opcionales
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el sessionExerciseId no pertenece a la sesión
     */
    @Transactional
    public TrainingSessionDetailResponse saveExerciseSet(Long id,
                                                         Long sessionExerciseId,
                                                         TrainingSessionSetRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        TrainingSessionDetailResponse session = this.trainingSessionRepository.findById(id,
                                                                                        user.getId());

        boolean isExerciseFromSession = session.exercises()
                                               .stream()
                                               .anyMatch(se -> se.id().equals(sessionExerciseId));

        if (!isExerciseFromSession)
            throw new NotFoundException("Ejercicio no encontrado para sesión especificada");

        TrainingSessionDetailResponse updated = this.trainingSessionRepository.saveExerciseSet(id,
                                                                                               sessionExerciseId,
                                                                                               request.setNumber(),
                                                                                               request.weight(),
                                                                                               request.reps(),
                                                                                               request.notes(),
                                                                                               user.getId());
        LOG.info("Set agregado — sessionId: {}, sessionExerciseId: {}, setNumber: {}, weight: {}, reps: {}, notes: {}",
                 id,
                 sessionExerciseId,
                 request.setNumber(),
                 request.weight(),
                 request.reps(),
                 request.notes());
        return updated;
    }

    /**
     * Elimina un set de un ejercicio de una sesión del usuario autenticado.
     * Valida que el sessionExerciseId pertenezca a la sesión antes de eliminar.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises al que pertenece el set
     * @param setId             id del set a eliminar
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el sessionExerciseId no pertenece a la sesión
     */
    @Transactional
    public TrainingSessionDetailResponse deleteExerciseSetById(Long id,
                                                               Long sessionExerciseId,
                                                               Long setId) {
        User user = this.authUtils.getAuthenticatedUser();
        TrainingSessionDetailResponse session = this.trainingSessionRepository.findById(id,
                                                                                        user.getId());

        boolean isExerciseFromSession = session.exercises()
                                               .stream()
                                               .anyMatch(se -> se.id().equals(sessionExerciseId));

        if (!isExerciseFromSession)
            throw new NotFoundException("Ejercicio no encontrado para sesión especificada");

        TrainingSessionDetailResponse updated = this.trainingSessionRepository.deleteExerciseSetById(id,
                                                                                                     setId,
                                                                                                     sessionExerciseId,
                                                                                                     user.getId());
        LOG.warn("Set eliminado — sessionId: {}, sessionExerciseId: {}, setId: {}",
                 id,
                 sessionExerciseId,
                 setId);
        return updated;
    }

    /**
     * Actualiza los datos de un set de un ejercicio de una sesión del usuario autenticado.
     * Solo actualiza los campos enviados — campos null conservan su valor actual.
     *
     * @param id                id de la sesión
     * @param sessionExerciseId id del registro en session_exercises al que pertenece el set
     * @param setId             id del set a actualizar
     * @param request           campos a actualizar — setNumber, weight, reps y/o notes
     * @return TrainingSessionDetailResponse con la sesión actualizada
     * @throws NotFoundException si el sessionExerciseId no pertenece a la sesión
     */
    public TrainingSessionDetailResponse updateExerciseSet(Long id,
                                                           Long sessionExerciseId,
                                                           Long setId,
                                                           TrainingSessionSetRequest request) {
        User user = this.authUtils.getAuthenticatedUser();
        TrainingSessionDetailResponse session = this.trainingSessionRepository.findById(id,
                                                                                        user.getId());

        session.exercises()
                .stream()
                .filter(se -> se.id().equals(sessionExerciseId))
                .map(se -> se.exercise().id())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Ejercicio no encontrado para sesión especificada"));

        return this.trainingSessionRepository.updateExerciseSet(id,
                                                                setId,
                                                                sessionExerciseId,
                                                                request.setNumber(),
                                                                request.weight(),
                                                                request.reps(),
                                                                request.notes(),
                                                                user.getId());
    }
}