package pe.edu.upeu.autoandes.dto;

public record LoginResponseDTO(
    String token,
    String tipo,
    long expiraEnSegundos,
    String username,
    String nombreCompleto,
    java.util.Set<String> roles,
    java.util.Set<String> permisos) {}
