package pe.edu.upeu.autoandes.dto;

public record LoginRequestDTO(
    @jakarta.validation.constraints.NotBlank String username,
    @jakarta.validation.constraints.NotBlank String password) {}
