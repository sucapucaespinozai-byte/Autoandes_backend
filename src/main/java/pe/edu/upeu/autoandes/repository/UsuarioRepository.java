package pe.edu.upeu.autoandes.repository;

import pe.edu.upeu.autoandes.entity.*;

public interface UsuarioRepository
    extends org.springframework.data.jpa.repository.JpaRepository<Usuario, Long> {
  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "roles")
  java.util.Optional<Usuario> findByUsername(String username);
}
