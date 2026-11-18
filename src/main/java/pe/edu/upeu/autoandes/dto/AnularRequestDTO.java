package pe.edu.upeu.autoandes.dto;

public record AnularRequestDTO(
    @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Size(min = 10, max = 200)
        String motivo) {}
