package pe.edu.upeu.autoandes.service.servicelmpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.Vehiculo;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.VehiculoService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehiculoServicelmpl implements VehiculoService {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(VehiculoServicelmpl.class);
  private final VehiculoRepository repository;
  private final ClienteRepository clientes;
  private final OrdenServicioRepository ordenes;
  private final AutoAndesMapper mapper;

  private Vehiculo obtener(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.noEncontrado("Vehículo"));
  }

  public java.util.List<VehiculoResponseDTO> listar(Long clienteId) {
    if (clienteId != null && !clientes.existsById(clienteId))
      throw ApiException.noEncontrado("Cliente");
    return (clienteId == null ? repository.findAll() : repository.findByClienteId(clienteId))
        .stream().map(mapper::vehiculo).toList();
  }

  public VehiculoResponseDTO buscar(Long id) {
    return mapper.vehiculo(obtener(id));
  }

  private void copiar(Vehiculo v, VehiculoRequestDTO d) {
    v.setPlaca(d.placa());
    v.setMarca(d.marca().trim());
    v.setModelo(d.modelo().trim());
    v.setAnio(d.anio());
    v.setCliente(
        clientes.findById(d.clienteId()).orElseThrow(() -> ApiException.noEncontrado("Cliente")));
  }

  @Transactional
  public VehiculoResponseDTO crear(VehiculoRequestDTO d) {
    if (repository.existsByPlaca(d.placa()))
      throw ApiException.conflicto("RN-02: la placa ya existe");
    var v = new Vehiculo();
    copiar(v, d);
    repository.saveAndFlush(v);
    log.info("Vehículo creado id={}", v.getId());
    return mapper.vehiculo(v);
  }

  @Transactional
  public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO d) {
    var v =
        repository
            .buscarParaActualizar(id)
            .orElseThrow(() -> ApiException.noEncontrado("Vehículo"));
    if (repository.existsByPlacaAndIdNot(d.placa(), id))
      throw ApiException.conflicto("RN-02: la placa ya existe");
    copiar(v, d);
    repository.flush();
    log.info("Vehículo actualizado id={}", id);
    return mapper.vehiculo(v);
  }

  @Transactional
  public void eliminar(Long id) {
    var v =
        repository
            .buscarParaActualizar(id)
            .orElseThrow(() -> ApiException.noEncontrado("Vehículo"));
    if (ordenes.existsByVehiculoId(id))
      throw ApiException.conflicto("RN-02: no se puede eliminar un vehículo con órdenes");
    repository.delete(v);
    repository.flush();
    log.info("Vehículo eliminado id={}", id);
  }
}
