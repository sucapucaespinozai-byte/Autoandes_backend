package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;

public interface OrdenService {
  OrdenResponseDTO crear(OrdenRequestDTO dto, String usuario);

  OrdenResponseDTO buscar(Long id);

  java.util.List<OrdenResponseDTO> listar(
      String placa,
      pe.edu.upeu.autoandes.entity.EstadoOrden estado,
      java.time.LocalDate desde,
      java.time.LocalDate hasta);

  OrdenResponseDTO anular(Long id, AnularRequestDTO dto);

  java.util.List<IngresoServicioProjection> ingresos(
      java.time.LocalDate desde, java.time.LocalDate hasta);
}
