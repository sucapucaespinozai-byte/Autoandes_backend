package pe.edu.upeu.autoandes.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.IngresoServicioProjection;
import pe.edu.upeu.autoandes.service.OrdenService;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {
  private final OrdenService service;

  @GetMapping("/ingresos-por-servicio")
  public java.util.List<IngresoServicioProjection> ingresos(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate desde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate hasta) {
    return service.ingresos(desde, hasta);
  }
}
