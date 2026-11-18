package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 7)
  private String codigo;

  @Column(nullable = false, length = 80)
  private String nombre;

  @Column(nullable = false, precision = 10, scale = 2)
  private java.math.BigDecimal precio;

  @Column(name = "duracion_min", nullable = false)
  private Integer duracionMin;

  @Column(nullable = false)
  @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
  private Boolean activo;
}
