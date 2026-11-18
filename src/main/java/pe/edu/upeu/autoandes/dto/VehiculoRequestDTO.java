package pe.edu.upeu.autoandes.dto;

public record VehiculoRequestDTO(
    @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Pattern(
            regexp = "[A-Z]{3}-[0-9]{3}",
            message = "La placa debe tener el formato ABC-123")
        String placa,
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 40)
        String marca,
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 40)
        String modelo,
    @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Min(1990)
        @jakarta.validation.constraints.Max(2027)
        Integer anio,
    @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
        Long clienteId) {}
