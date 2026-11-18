package pe.edu.upeu.autoandes.dto;

public record OrdenRequestDTO(
    @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
        Long vehiculoId,
    @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Min(0)
        @jakarta.validation.constraints.Max(9999999)
        Integer kilometraje,
    @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Size(min = 1, max = 10)
        java.util.List<
                @jakarta.validation.constraints.NotNull @jakarta.validation.Valid
                DetalleOrdenRequestDTO>
            detalles) {}
