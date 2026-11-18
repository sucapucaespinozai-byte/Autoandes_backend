package pe.edu.upeu.autoandes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.VehiculoService;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {
  private final VehiculoService service;

  @GetMapping
  public java.util.List<VehiculoResponseDTO> listar(
      @RequestParam(required = false) Long clienteId) {
    return service.listar(clienteId);
  }

  @GetMapping("/{id}")
  public VehiculoResponseDTO buscar(@PathVariable Long id) {
    return service.buscar(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public VehiculoResponseDTO crear(@jakarta.validation.Valid @RequestBody VehiculoRequestDTO d) {
    return service.crear(d);
  }

  @PutMapping("/{id}")
  public VehiculoResponseDTO actualizar(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody VehiculoRequestDTO d) {
    return service.actualizar(id, d);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }
}
