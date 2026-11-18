package pe.edu.upeu.autoandes.dto;

@com.fasterxml.jackson.annotation.JsonInclude(
    com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(
    java.time.LocalDateTime timestamp,
    int status,
    String error,
    String mensaje,
    String ruta,
    java.util.Map<String, String> campos) {}
