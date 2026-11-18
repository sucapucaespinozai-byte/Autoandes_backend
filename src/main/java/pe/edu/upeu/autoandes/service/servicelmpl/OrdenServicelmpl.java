package pe.edu.upeu.autoandes.service.servicelmpl;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.OrdenService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenServicelmpl implements OrdenService {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(OrdenServicelmpl.class);
  private final OrdenServicioRepository repository;
  private final VehiculoRepository vehiculos;
  private final ServicioRepository servicios;
  private final AutoAndesMapper mapper;
  private final Clock clock;

  @Transactional
  public OrdenResponseDTO crear(OrdenRequestDTO d, String usuario) {
    validarDetalles(d);
    // Serializa el kilometraje de órdenes concurrentes del mismo vehículo.
    var vehiculo =
        vehiculos
            .buscarParaActualizar(d.vehiculoId())
            .orElseThrow(() -> ApiException.noEncontrado("Vehículo"));
    var ultima =
        repository.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(
            vehiculo.getId(), EstadoOrden.REGISTRADA);
    if (ultima.isPresent() && d.kilometraje() < ultima.get().getKilometraje())
      throw ApiException.conflicto(
          "RN-04: el kilometraje no puede ser menor al último kilometraje registrado: "
              + ultima.get().getKilometraje());
    var orden = new OrdenServicio();
    orden.setVehiculo(vehiculo);
    orden.setKilometraje(d.kilometraje());
    orden.setFecha(LocalDateTime.now(clock));
    orden.setEstado(EstadoOrden.REGISTRADA);
    orden.setUsuarioRegistro(usuario);
    BigDecimal total = BigDecimal.ZERO;
    for (var item : d.detalles()) {
      var servicio =
          servicios
              .findById(item.servicioId())
              .orElseThrow(() -> ApiException.noEncontrado("Servicio"));
      if (!Boolean.TRUE.equals(servicio.getActivo()))
        throw ApiException.conflicto(
            "RN-03: el servicio " + servicio.getCodigo() + " está inactivo");
      var detalle = new DetalleOrden();
      detalle.setServicio(servicio);
      detalle.setCantidad(item.cantidad());
      detalle.setPrecioUnitario(servicio.getPrecio());
      detalle.setSubtotal(servicio.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
      orden.agregar(detalle);
      total = total.add(detalle.getSubtotal());
    }
    long correlativo = repository.siguienteNumero();
    if (correlativo > 999999)
      throw ApiException.conflicto("Se agotó la numeración OS de seis dígitos");
    orden.setNumero(String.format(java.util.Locale.ROOT, "OS-%06d", correlativo));
    orden.setTotal(total);
    repository.saveAndFlush(orden);
    log.info(
        "Orden registrada numero={} detalles={} total={}",
        orden.getNumero(),
        orden.getDetalles().size(),
        total);
    return mapper.orden(orden);
  }

  private void validarDetalles(OrdenRequestDTO d) {
    if (d.vehiculoId() == null
        || d.vehiculoId() < 1
        || d.kilometraje() == null
        || d.kilometraje() < 0
        || d.kilometraje() > 9999999)
      throw ApiException.invalido("Vehículo y kilometraje inválidos");
    if (d.detalles() == null || d.detalles().isEmpty() || d.detalles().size() > 10)
      throw ApiException.invalido("RN-03: la orden debe tener de 1 a 10 detalles");
    Set<Long> ids = new HashSet<>();
    for (var item : d.detalles()) {
      if (item == null
          || item.servicioId() == null
          || item.servicioId() < 1
          || item.cantidad() == null
          || item.cantidad() < 1
          || item.cantidad() > 5)
        throw ApiException.invalido("RN-03: cada detalle requiere servicio y cantidad de 1 a 5");
      if (!ids.add(item.servicioId()))
        throw ApiException.invalido("RN-03: no se permite repetir un servicio");
    }
  }

  public OrdenResponseDTO buscar(Long id) {
    return mapper.orden(
        repository.findById(id).orElseThrow(() -> ApiException.noEncontrado("Orden")));
  }

  public List<OrdenResponseDTO> listar(
      String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta) {
    validarFechas(desde, hasta);
    return repository
        .findAll(
            (r, q, cb) -> {
              var filtros = new ArrayList<jakarta.persistence.criteria.Predicate>();
              if (placa != null && !placa.isBlank())
                filtros.add(
                    cb.equal(
                        cb.upper(r.get("vehiculo").get("placa")),
                        placa.trim().toUpperCase(Locale.ROOT)));
              if (estado != null) filtros.add(cb.equal(r.get("estado"), estado));
              if (desde != null)
                filtros.add(cb.greaterThanOrEqualTo(r.get("fecha"), desde.atStartOfDay()));
              if (hasta != null)
                filtros.add(cb.lessThan(r.get("fecha"), hasta.plusDays(1).atStartOfDay()));
              return cb.and(filtros.toArray(jakarta.persistence.criteria.Predicate[]::new));
            },
            org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("fecha"),
                org.springframework.data.domain.Sort.Order.desc("id")))
        .stream()
        .map(mapper::orden)
        .toList();
  }

  @Transactional
  public OrdenResponseDTO anular(Long id, AnularRequestDTO d) {
    String motivo = d.motivo() == null ? "" : d.motivo().trim();
    if (motivo.length() < 10 || motivo.length() > 200)
      throw ApiException.invalido("RN-05: el motivo debe tener de 10 a 200 caracteres");
    var o =
        repository.buscarParaActualizar(id).orElseThrow(() -> ApiException.noEncontrado("Orden"));
    if (o.getEstado() != EstadoOrden.REGISTRADA)
      throw ApiException.conflicto("RN-05: solo se anula una orden REGISTRADA");
    if (!o.getFecha().toLocalDate().equals(LocalDate.now(clock)))
      throw ApiException.conflicto("RN-05: solo se anula una orden el mismo día de su registro");
    o.setEstado(EstadoOrden.ANULADA);
    o.setMotivoAnulacion(motivo);
    repository.flush();
    log.info("Orden anulada numero={}", o.getNumero());
    return mapper.orden(o);
  }

  public List<IngresoServicioProjection> ingresos(LocalDate desde, LocalDate hasta) {
    validarFechas(desde, hasta);
    return repository.ingresos(
        desde == null ? null : desde.atStartOfDay(),
        hasta == null ? null : hasta.plusDays(1).atStartOfDay());
  }

  private void validarFechas(LocalDate desde, LocalDate hasta) {
    if (desde != null && hasta != null && desde.isAfter(hasta))
      throw ApiException.invalido("La fecha desde no puede superar hasta");
  }
}
