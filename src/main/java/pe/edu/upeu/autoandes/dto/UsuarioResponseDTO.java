package pe.edu.upeu.autoandes.dto;

public record UsuarioResponseDTO(
    Long id,
    String username,
    String nombreCompleto,
    java.util.Set<String> roles,
    java.util.Set<String> permisos) {}
