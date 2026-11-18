package pe.edu.upeu.autoandes.dto;

public record ServicioRequestDTO(
    @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Pattern(
            regexp = "SRV-[0-9]{3}",
            message = "El código debe tener formato SRV-000")
        String codigo,
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 3, max = 80)
        String nombre,
    @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.DecimalMin(value = "0", inclusive = false)
        @jakarta.validation.constraints.Digits(integer = 8, fraction = 2)
        java.math.BigDecimal precio,
    @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Min(15)
        @jakarta.validation.constraints.Max(480)
        Integer duracionMin,
    @jakarta.validation.constraints.NotNull Boolean activo) {}
