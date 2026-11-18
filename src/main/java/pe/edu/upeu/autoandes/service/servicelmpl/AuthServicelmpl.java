package pe.edu.upeu.autoandes.service.servicelmpl;

import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.exception.ApiException;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.security.PermisosPorRol;
import pe.edu.upeu.autoandes.service.AuthService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServicelmpl implements AuthService {
  private final UsuarioRepository repository;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private static final String HASH_FICTICIO =
      "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

  private UsuarioResponseDTO dto(Usuario u) {
    Set<String> roles = new TreeSet<>();
    u.getRoles().forEach(r -> roles.add(r.getNombre()));
    return new UsuarioResponseDTO(
        u.getId(), u.getUsername(), u.getNombreCompleto(), roles, PermisosPorRol.de(roles));
  }

  private ApiException rechazado() {
    return new ApiException(
        org.springframework.http.HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
  }

  public LoginResponseDTO login(LoginRequestDTO d) {
    var opt = repository.findByUsername(d.username());
    boolean coincide =
        encoder.matches(d.password(), opt.map(Usuario::getPassword).orElse(HASH_FICTICIO));
    if (opt.isEmpty() || !coincide || !Boolean.TRUE.equals(opt.get().getActivo()))
      throw rechazado();
    var u = dto(opt.get());
    return new LoginResponseDTO(
        jwt.crear(u.username(), u.roles(), u.permisos()),
        "Bearer",
        jwt.expiracion(),
        u.username(),
        u.nombreCompleto(),
        u.roles(),
        u.permisos());
  }

  public UsuarioResponseDTO actual(String username) {
    return dto(
        repository
            .findByUsername(username)
            .filter(u -> Boolean.TRUE.equals(u.getActivo()))
            .orElseThrow(this::rechazado));
  }
}
