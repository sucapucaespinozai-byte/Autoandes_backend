package pe.edu.upeu.autoandes.dto;

public record DetalleOrdenResponseDTO(
    Long id,
    Long servicioId,
    String servicioCodigo,
    String servicioNombre,
    Integer cantidad,
    java.math.BigDecimal precioUnitario,
    java.math.BigDecimal subtotal) {}
