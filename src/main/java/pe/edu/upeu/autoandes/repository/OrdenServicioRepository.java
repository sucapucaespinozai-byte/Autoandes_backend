package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.*;

public interface OrdenServicioRepository
    extends org.springframework.data.jpa.repository.JpaRepository<OrdenServicio, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<OrdenServicio> {
  boolean existsByVehiculoId(Long vehiculoId);

  java.util.Optional<OrdenServicio> findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(
      Long vehiculoId, EstadoOrden estado);

  @org.springframework.data.jpa.repository.Query(
      value = "SELECT orden_numero_seq.NEXTVAL FROM dual",
      nativeQuery = true)
  Long siguienteNumero();

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select o from OrdenServicio o where o.id=:id")
  java.util.Optional<OrdenServicio> buscarParaActualizar(
      @org.springframework.data.repository.query.Param("id") Long id);

  @org.springframework.data.jpa.repository.Query(
      """
      select s.codigo as codigo, s.nombre as servicio, sum(d.cantidad) as cantidadTotal, sum(d.subtotal) as importeTotal
      from DetalleOrden d join d.servicio s join d.orden o
      where o.estado=pe.edu.upeu.autoandes.entity.EstadoOrden.REGISTRADA
      and (:desde is null or o.fecha>=:desde) and (:hasta is null or o.fecha<:hasta)
      group by s.codigo,s.nombre order by sum(d.subtotal) desc
      """)
  java.util.List<pe.edu.upeu.autoandes.dto.IngresoServicioProjection> ingresos(
      @org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,
      @org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta);
}
