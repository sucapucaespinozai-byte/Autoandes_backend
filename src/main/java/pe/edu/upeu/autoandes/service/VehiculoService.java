package pe.edu.upeu.autoandes.service;

import pe.edu.upeu.autoandes.dto.*;

public interface VehiculoService {
  java.util.List<VehiculoResponseDTO> listar(Long clienteId);

  VehiculoResponseDTO buscar(Long id);

  VehiculoResponseDTO crear(VehiculoRequestDTO dto);

  VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO dto);

  void eliminar(Long id);
}
