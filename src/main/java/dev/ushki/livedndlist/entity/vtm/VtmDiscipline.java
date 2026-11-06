package dev.ushki.livedndlist.entity.vtm;

import dev.ushki.livedndlist.enums.DisciplineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Supernatural discipline of a vampire (e.g. Potence, Obfuscate, Dominate).
 */
@Entity
@Table(name = "vtm_disciplines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmDiscipline {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "discipline_type", nullable = false, columnDefinition = "discipline_type")
  private DisciplineType disciplineType;

  @Column(name = "sourcebook")
  private String sourcebook;
}