package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 30)
  private String username;

  @Column(length = 100)
  private String password;

  @Column(name = "nombre_completo", nullable = false, length = 120)
  private String nombreCompleto;

  @Column(nullable = false)
  @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
  private Boolean activo;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "rol_id"))
  private java.util.Set<Rol> roles = new java.util.HashSet<>();
}
