package pe.edu.upeu.autoandes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.ServicioService;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {
  private final ServicioService service;

  @GetMapping
  public java.util.List<ServicioResponseDTO> listar() {
    return service.listar();
  }

  @GetMapping("/{id}")
  public ServicioResponseDTO buscar(@PathVariable Long id) {
    return service.buscar(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ServicioResponseDTO crear(@jakarta.validation.Valid @RequestBody ServicioRequestDTO d) {
    return service.crear(d);
  }

  @PutMapping("/{id}")
  public ServicioResponseDTO actualizar(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody ServicioRequestDTO d) {
    return service.actualizar(id, d);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }

  @PatchMapping("/{id}/estado")
  public ServicioResponseDTO estado(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody EstadoRequestDTO d) {
    return service.estado(id, d);
  }
}
