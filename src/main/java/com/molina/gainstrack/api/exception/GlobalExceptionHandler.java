package com.molina.gainstrack.api.exception;

import com.molina.gainstrack.api.dto.shared.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Intercepta excepciones lanzadas en cualquier capa y las convierte
 * en respuestas JSON estructuradas con status code apropiado.
 * Orden: de más específico a más genérico para garantizar
 * que cada excepción sea capturada por el handler correcto.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG =  LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja credenciales incorrectas en el proceso de login.
     * Se lanza cuando el email o contraseña no coinciden.
     *
     * @return 401 Unauthorized con mensaje genérico de credenciales
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handlerBadCredentialsException(BadCredentialsException badCredentialsException) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    "Usuario y/o contraseña incorrectos",
                                                                    null,
                                                                    LocalDateTime.now()));
    }

    /**
     * Maneja errores generales de autenticación de Spring Security.
     * Cubre casos de autenticación fallida no capturados por handlers más específicos.
     *
     * @return 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handlerAuthenticationException(AuthenticationException authenticationException) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    "No autorizado",
                                                                    null,
                                                                    LocalDateTime.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handlerHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    "Formato de datos inválido — verifique los tipos de los campos enviados",
                                                                    null,
                                                                    LocalDateTime.now()
        ));
    }

    /**
     * Maneja errores de validación de campos en el body del request.
     * Extrae todos los errores por campo y los retorna en un mapa
     * para que el frontend pueda mostrarlos junto al campo correspondiente.
     *
     * @param ex excepción con el detalle de los campos que fallaron la validación
     * @return 400 Bad Request con mapa de errores por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        Map<String, String> errors = new HashMap<>();

        fieldErrors.forEach(error -> errors.put(error.getField(),
                                                         error.getDefaultMessage()));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    "Error de validación",
                                                                    errors,
                                                                    LocalDateTime.now()));
    }

    /**
     * Maneja violaciones de constraints de la base de datos.
     * Inspecciona el mensaje de la excepción para identificar la constraint
     * violada y devolver un mensaje de negocio apropiado.
     * Si la constraint no es reconocida, devuelve un mensaje genérico de conflicto.
     *
     * @return 409 Conflict con mensaje descriptivo del conflicto
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handlerDataIntegrityViolationException(DataIntegrityViolationException dataIntegrityViolationException) {
        LOG.error("Violación de integridad de datos: ", dataIntegrityViolationException);

        HttpStatus status = HttpStatus.CONFLICT;
        String message = "Error de integridad de datos — verifique la información enviada";

        if (dataIntegrityViolationException.getMessage().contains("users.uq_users_email")) {
            message = "Email ya se encuentra registrado";
        } else if (dataIntegrityViolationException.getMessage().contains("routine_exercises.fk_re_routine")) {
            message = "Rutina no encontrada - verifique la información enviada";
        } else if (dataIntegrityViolationException.getMessage().contains("routine_exercises.fk_re_exercise")) {
            message = "Ejercicio no encontrado - verifique la información enviada";
        } else if (dataIntegrityViolationException.getMessage().contains("routine_sets.fk_rs_routine_exercise")) {
            message = "Ejercicio no encontrado para rutina especificada - verifique la información enviada";
        } else if (dataIntegrityViolationException.getMessage().contains("training_sessions.fk_ts_routine")) {
            message = "Rutina no encontrada - verifique la información enviada";
        } else if (dataIntegrityViolationException.getMessage().contains("training_sessions.fk_ts_gym")) {
            message = "Gimnasio no encontrado - verifique la información enviada";
        } else if (dataIntegrityViolationException.getMessage().contains("session_exercises.fk_se_exercise")) {
            message = "Ejercicio no encontrado - verifique la información enviada";
        }

        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    message,
                                                                    null,
                                                                    LocalDateTime.now()));
    }

    /**
     * Maneja errores generales de acceso a la base de datos.
     * Loguea el error internamente sin exponer detalles técnicos al cliente.
     *
     * @return 503 Service Unavailable
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handlerDataAccessException(DataAccessException dataAccessException) {
        LOG.error("Error inesperado: ",  dataAccessException);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    "Error de base de datos - intente nuevamente",
                                                                    null,
                                                                    LocalDateTime.now()));
    }

    /**
     * Maneja recursos no encontrados en la base de datos.
     * Se lanza explícitamente desde los services cuando un recurso no existe.
     *
     * @return 404 Not Found con el mensaje de negocio definido al lanzar la excepción
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerNotFoundException(NotFoundException notFoundException) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    notFoundException.getMessage(),
                                                                    null,
                                                                    LocalDateTime.now()));
    }

    /**
     * Maneja operaciones no permitidas por reglas de negocio.
     * Ejemplo: iniciar sesión con contraseña en una cuenta registrada con Google.
     *
     * @return 403 Forbidden con el mensaje de negocio definido al lanzar la excepción
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse>  handlerForbiddenException(ForbiddenException forbiddenException) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    forbiddenException.getMessage(),
                                                                    null,
                                                                    LocalDateTime.now()));
    }

    /**
     * Handler de último recurso para cualquier excepción no capturada.
     * Loguea el error internamente sin exponer detalles técnicos al cliente.
     * Nunca debe recibir excepciones de negocio — si lo hace, falta un handler específico.
     *
     * @return 500 Internal Server Error con mensaje genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>  handlerException(Exception exception) {
        LOG.error("Error inesperado: ",  exception);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(),
                                                                    status.name(),
                                                                    "Error inesperado - contacte al administrador",
                                                                    null,
                                                                    LocalDateTime.now()));
    }
}
