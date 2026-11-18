package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;

public interface ServicioService {
  java.util.List<ServicioResponseDTO> listar();

  ServicioResponseDTO buscar(Long id);

  ServicioResponseDTO crear(ServicioRequestDTO dto);

  ServicioResponseDTO actualizar(Long id, ServicioRequestDTO dto);

  void eliminar(Long id);

  ServicioResponseDTO estado(Long id, EstadoRequestDTO dto);
}
