package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.*;

public interface VehiculoRepository
    extends org.springframework.data.jpa.repository.JpaRepository<Vehiculo, Long> {
  boolean existsByPlaca(String placa);

  boolean existsByPlacaAndIdNot(String placa, Long id);

  java.util.List<Vehiculo> findByClienteId(Long clienteId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select v from Vehiculo v where v.id=:id")
  java.util.Optional<Vehiculo> buscarParaActualizar(
      @org.springframework.data.repository.query.Param("id") Long id);
}
