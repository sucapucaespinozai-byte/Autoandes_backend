package pe.edu.upeu.autoandes.dto;

public record DetalleOrdenRequestDTO(
    @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
        Long servicioId,
    @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Min(1)
        @jakarta.validation.constraints.Max(5)
        Integer cantidad) {}
