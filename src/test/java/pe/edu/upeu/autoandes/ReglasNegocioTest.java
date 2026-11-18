package pe.edu.upeu.autoandes;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import pe.edu.upeu.autoandes.service.*;

@SpringBootTest
@Transactional
class ReglasNegocioTest {
  @Autowired ServicioService servicios;
  @Autowired VehiculoService vehiculos;
  @Autowired OrdenService ordenes;
  @Autowired OrdenServicioRepository ordenRepository;
  @Autowired DetalleOrdenRepository detalleRepository;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder passwords;
  @Autowired UsuarioRepository usuarios;

  private OrdenRequestDTO orden(int km, DetalleOrdenRequestDTO... detalles) {
    return new OrdenRequestDTO(1L, km, List.of(detalles));
  }

  @Test
  void rn01RechazaCodigoDuplicado() {
    assertThatThrownBy(() ->
            servicios.crear(
                new ServicioRequestDTO("SRV-001", "Duplicado", BigDecimal.TEN, 30, true)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("RN-01");
  }

  @Test
  void rn01NoEliminaServicioUsado() {
    assertThatThrownBy(() -> servicios.eliminar(1L)).hasMessageContaining("RN-01");
  }

  @Test
  void rn01PermiteBajaLogica() {
    assertThat(servicios.estado(1L, new EstadoRequestDTO(false)).activo()).isFalse();
  }

  @Test
  void rn02RechazaPlacaDuplicada() {
    assertThatThrownBy(
            () -> vehiculos.crear(new VehiculoRequestDTO("AAA-111", "Toyota", "Corolla", 2020, 1L)))
        .hasMessageContaining("RN-02");
  }

  @Test
  void rn02ClienteDebeExistir() {
    assertThatThrownBy(() ->
            vehiculos.crear(new VehiculoRequestDTO("ZZZ-999", "Toyota", "Corolla", 2020, 999999L)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Cliente");
  }

  @Test
  void rn02NoEliminaVehiculoConOrdenes() {
    assertThatThrownBy(() -> vehiculos.eliminar(1L)).hasMessageContaining("RN-02");
  }

  @Test
  void rn03RechazaRepetidos() {
    assertThatThrownBy(() ->
            ordenes.crear(
                orden(45000, new DetalleOrdenRequestDTO(1L, 1), new DetalleOrdenRequestDTO(1L, 2)),
                "asesor"))
        .hasMessageContaining("repetir");
  }

  @Test
  void rn03CantidadYLimiteSeValidanEnServicio() {
    assertThatThrownBy(() -> ordenes.crear(orden(45000), "asesor")).hasMessageContaining("1 a 10");
    assertThatThrownBy(
            () -> ordenes.crear(orden(45000, new DetalleOrdenRequestDTO(1L, 6)), "asesor"))
        .hasMessageContaining("1 a 5");
  }

  @Test
  void rn03TercerDetalleInactivoNoDejaNadaGuardado() {
    long cabeceras = ordenRepository.count();
    long detalles = detalleRepository.count();
    assertThatThrownBy(() ->
            ordenes.crear(
                orden(
                    45000,
                    new DetalleOrdenRequestDTO(1L, 1),
                    new DetalleOrdenRequestDTO(2L, 1),
                    new DetalleOrdenRequestDTO(8L, 1)),
                "asesor"))
        .hasMessageContaining("inactivo");
    assertThat(ordenRepository.count()).isEqualTo(cabeceras);
    assertThat(detalleRepository.count()).isEqualTo(detalles);
  }

  @Test
  void rn04ConservaUltimoKilometraje() {
    assertThatThrownBy(
            () -> ordenes.crear(orden(44999, new DetalleOrdenRequestDTO(1L, 1)), "asesor"))
        .hasMessageContaining("45000");
  }

  @Test
  void totalSeCalculaConPreciosDeOracle() {
    var guardada =
        ordenes.crear(
            orden(45000, new DetalleOrdenRequestDTO(1L, 2), new DetalleOrdenRequestDTO(2L, 1)),
            "asesor");
    assertThat(guardada.total()).isEqualByComparingTo("220.00");
    assertThat(guardada.numero()).matches("OS-\\d{6}");
    assertThat(guardada.detalles()).hasSize(2);
    assertThat(guardada.estado()).isEqualTo("REGISTRADA");
  }

  @Test
  void rn05SoloAnulaEnElDiaYConMotivo() {
    var o = ordenes.crear(orden(45000, new DetalleOrdenRequestDTO(1L, 1)), "asesor");
    assertThatThrownBy(() -> ordenes.anular(o.id(), new AnularRequestDTO("corto")))
        .hasMessageContaining("10 a 200");
    // Cada rechazo transaccional se verifica en su propia prueba para evitar continuar un
    // rollback-only.
  }

  @Test
  void rn05AnulacionCorrecta() {
    var o = ordenes.crear(orden(45000, new DetalleOrdenRequestDTO(1L, 1)), "asesor");
    var anulada =
        ordenes.anular(o.id(), new AnularRequestDTO("Cliente solicita cancelar la atencion"));
    assertThat(anulada.estado()).isEqualTo("ANULADA");
  }

  @Test
  void rn05OrdenAnteriorNoSeAnula() {
    assertThatThrownBy(
            () -> ordenes.anular(1L, new AnularRequestDTO("Cliente solicita cancelar la atencion")))
        .hasMessageContaining("mismo día");
  }

  @Test
  void rn05NoSeAnulaUnaAnulada() {
    assertThatThrownBy(
            () -> ordenes.anular(4L, new AnularRequestDTO("Cliente solicita cancelar la atencion")))
        .hasMessageContaining("REGISTRADA");
  }

  @Test
  void reporteExcluyeAnuladasConAgregacionEnOracle() {
    var reporte =
        ordenes.ingresos(
            java.time.LocalDate.of(2026, 10, 29), java.time.LocalDate.of(2026, 10, 29));
    BigDecimal total =
        reporte.stream()
            .map(IngresoServicioProjection::getImporteTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertThat(total).isEqualByComparingTo("560.00");
    assertThat(reporte).noneMatch(r -> r.getCodigo().equals("SRV-007"));
  }

  @Test
  void permisosSonUnionYNoComparacionesEnControladores() {
    var union = PermisosPorRol.de(List.of("ASESOR", "JEFE_TALLER"));
    assertThat(union).contains("ORDEN_REGISTRAR", "ORDEN_ANULAR", "REPORTE_VER");
    assertThat(PermisosPorRol.de(List.of("ASESOR"))).doesNotContain("ORDEN_ANULAR", "REPORTE_VER");
    assertThat(PermisosPorRol.de(List.of("JEFE_TALLER"))).doesNotContain("ORDEN_REGISTRAR");
  }

  @Test
  void semillaTienePasswordBCrypt() {
    var u = usuarios.findByUsername("admin").orElseThrow();
    assertThat(u.getPassword()).startsWith("$2");
    assertThat(passwords.matches("Auto2026*", u.getPassword())).isTrue();
  }

  @Test
  void tokenVencidoSeRechazaAunqueSuFirmaSeaValida() {
    String secret = java.util.Base64.getEncoder().encodeToString(new byte[48]);
    var emisorAnterior =
        new pe.edu.upeu.autoandes.config.JwtService(
            secret,
            3600,
            java.time.Clock.fixed(
                java.time.Instant.now().minusSeconds(7200), java.time.ZoneOffset.UTC));
    var actual =
        new pe.edu.upeu.autoandes.config.JwtService(secret, 3600, java.time.Clock.systemUTC());
    String vencido =
        emisorAnterior.crear("asesor", java.util.Set.of("ASESOR"), java.util.Set.of("ORDEN_LEER"));
    assertThatThrownBy(() -> actual.validar(vencido))
        .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
  }
}
