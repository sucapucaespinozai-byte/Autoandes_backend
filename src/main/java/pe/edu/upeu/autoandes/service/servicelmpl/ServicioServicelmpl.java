package pe.edu.upeu.autoandes.service.servicelmpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.ServicioService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioServicelmpl implements ServicioService {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(ServicioServicelmpl.class);
  private final ServicioRepository repository;
  private final DetalleOrdenRepository detalles;
  private final AutoAndesMapper mapper;

  private Servicio obtener(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.noEncontrado("Servicio"));
  }

  public java.util.List<ServicioResponseDTO> listar() {
    return repository.findAll(org.springframework.data.domain.Sort.by("codigo")).stream()
        .map(mapper::servicio)
        .toList();
  }

  public ServicioResponseDTO buscar(Long id) {
    return mapper.servicio(obtener(id));
  }

  private void copiar(Servicio s, ServicioRequestDTO d) {
    s.setCodigo(d.codigo());
    s.setNombre(d.nombre().trim());
    s.setPrecio(d.precio());
    s.setDuracionMin(d.duracionMin());
    s.setActivo(d.activo());
  }

  @Transactional
  public ServicioResponseDTO crear(ServicioRequestDTO d) {
    if (repository.existsByCodigo(d.codigo()))
      throw ApiException.conflicto("RN-01: el código de servicio ya existe");
    var s = new Servicio();
    copiar(s, d);
    repository.saveAndFlush(s);
    log.info("Servicio creado id={}", s.getId());
    return mapper.servicio(s);
  }

  @Transactional
  public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO d) {
    var s = obtener(id);
    if (repository.existsByCodigoAndIdNot(d.codigo(), id))
      throw ApiException.conflicto("RN-01: el código de servicio ya existe");
    copiar(s, d);
    repository.flush();
    log.info("Servicio actualizado id={}", id);
    return mapper.servicio(s);
  }

  @Transactional
  public void eliminar(Long id) {
    var s = obtener(id);
    if (detalles.existsByServicioId(id))
      throw ApiException.conflicto(
          "RN-01: servicio usado en órdenes; utilice PATCH /servicios/"
              + id
              + "/estado para darlo de baja");
    repository.delete(s);
    repository.flush();
    log.info("Servicio eliminado id={}", id);
  }

  @Transactional
  public ServicioResponseDTO estado(Long id, EstadoRequestDTO d) {
    var s = obtener(id);
    s.setActivo(d.activo());
    repository.flush();
    log.info("Estado de servicio actualizado id={} activo={}", id, d.activo());
    return mapper.servicio(s);
  }
}
