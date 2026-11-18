package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
  private final UsuarioRepository repository;

  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    var u =
        repository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    var roles = u.getRoles().stream().map(r -> r.getNombre()).toList();
    return User.withUsername(u.getUsername())
        .password(u.getPassword() == null ? "" : u.getPassword())
        .disabled(!Boolean.TRUE.equals(u.getActivo()))
        .authorities(PermisosPorRol.de(roles).toArray(String[]::new))
        .build();
  }
}
