package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A single power inside a discipline, with a power level (dots) from 1 to 5.
 */
@Entity
@Table(name = "vtm_powers", uniqueConstraints = {
    @UniqueConstraint(name = "uq_vtm_powers_discipline_name", columnNames = {"discipline_id", "name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmPower {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "level", nullable = false)
  private Integer level;

  @Column(name = "amalgam")
  private String amalgam;

  @Column(columnDefinition = "TEXT")
  private String prerequisite;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "discipline_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private VtmDiscipline discipline;
}