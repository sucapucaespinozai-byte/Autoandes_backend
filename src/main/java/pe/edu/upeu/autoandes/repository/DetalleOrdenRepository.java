package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.*;

public interface DetalleOrdenRepository
    extends org.springframework.data.jpa.repository.JpaRepository<DetalleOrden, Long> {
  boolean existsByServicioId(Long servicioId);
}
