package pe.edu.upeu.autoandes.security;

import java.util.*;
import java.util.stream.Collectors;

public final class PermisosPorRol {
  private PermisosPorRol() {}

  private static final Map<String, Set<Permiso>> MATRIZ =
      Map.of(
          "ADMIN", EnumSet.allOf(Permiso.class),
          "ASESOR",
              EnumSet.of(
                  Permiso.SERVICIO_LEER,
                  Permiso.VEHICULO_LEER,
                  Permiso.VEHICULO_ESCRIBIR,
                  Permiso.ORDEN_LEER,
                  Permiso.ORDEN_REGISTRAR),
          "JEFE_TALLER",
              EnumSet.of(
                  Permiso.SERVICIO_LEER,
                  Permiso.SERVICIO_ESCRIBIR,
                  Permiso.VEHICULO_LEER,
                  Permiso.ORDEN_LEER,
                  Permiso.ORDEN_ANULAR,
                  Permiso.REPORTE_VER));

  public static Set<String> de(Collection<String> roles) {
    return roles.stream()
        .flatMap(r -> MATRIZ.getOrDefault(r, Set.of()).stream())
        .map(Enum::name)
        .collect(Collectors.toCollection(TreeSet::new));
  }
}
