package pe.edu.upeu.autoandes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class PasswordSeedConfig implements CommandLineRunner {
  private final UsuarioRepository repository;
  private final PasswordEncoder encoder;

  @Transactional
  public void run(String... args) {
    for (String username : java.util.List.of("admin", "asesor", "jefe", "inactivo"))
      repository
          .findByUsername(username)
          .filter(u -> u.getPassword() == null)
          .ifPresent(u -> u.setPassword(encoder.encode("Auto2026*")));
  }
}
