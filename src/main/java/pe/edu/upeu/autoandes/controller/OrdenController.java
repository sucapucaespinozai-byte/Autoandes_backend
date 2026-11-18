package pe.edu.upeu.autoandes.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.service.OrdenService;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {
  private final OrdenService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OrdenResponseDTO crear(
      @jakarta.validation.Valid @RequestBody OrdenRequestDTO d, java.security.Principal principal) {
    return service.crear(d, principal.getName());
  }

  @GetMapping("/{id}")
  public OrdenResponseDTO buscar(@PathVariable Long id) {
    return service.buscar(id);
  }

  @GetMapping
  public java.util.List<OrdenResponseDTO> listar(
      @RequestParam(required = false) String placa,
      @RequestParam(required = false) EstadoOrden estado,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate hasta) {
    return service.listar(placa, estado, desde, hasta);
  }

  @PatchMapping("/{id}/anular")
  public OrdenResponseDTO anular(
      @PathVariable Long id, @jakarta.validation.Valid @RequestBody AnularRequestDTO d) {
    return service.anular(id, d);
  }
}
