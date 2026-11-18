package pe.edu.upeu.autoandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ErrorResponseDTO> error(
      HttpStatus status, String mensaje, HttpServletRequest r, Map<String, String> campos) {
    return ResponseEntity.status(status)
        .body(
            new ErrorResponseDTO(
                java.time.LocalDateTime.now(java.time.ZoneId.of("America/Lima")),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                r.getRequestURI(),
                campos));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponseDTO> dominio(ApiException e, HttpServletRequest r) {
    log.warn("Regla rechazada {}: {}", r.getRequestURI(), e.getMessage());
    return error(e.getStatus(), e.getMessage(), r, null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> validacion(
      MethodArgumentNotValidException e, HttpServletRequest r) {
    Map<String, String> campos = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> campos.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return error(HttpStatus.BAD_REQUEST, "Datos inválidos", r, campos);
  }

  @ExceptionHandler({
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  public ResponseEntity<ErrorResponseDTO> formato(Exception e, HttpServletRequest r) {
    return error(HttpStatus.BAD_REQUEST, "Solicitud o parámetros inválidos", r, null);
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDTO> integridad(Exception e, HttpServletRequest r) {
    log.warn("Conflicto de integridad en {}", r.getRequestURI());
    return error(
        HttpStatus.CONFLICT,
        "El registro está duplicado o está relacionado con otros registros",
        r,
        null);
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public ResponseEntity<ErrorResponseDTO> ruta(Exception e, HttpServletRequest r) {
    return error(HttpStatus.NOT_FOUND, "Ruta no encontrada", r, null);
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDTO> permiso(Exception e, HttpServletRequest r) {
    return error(HttpStatus.FORBIDDEN, "No tiene el permiso requerido", r, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> inesperado(Exception e, HttpServletRequest r) {
    log.error("Error en {}", r.getRequestURI(), e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", r, null);
  }
}
