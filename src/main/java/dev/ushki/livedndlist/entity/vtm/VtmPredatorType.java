package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A predator type describes how a vampire hunts and feeds, granting a specialty.
 */
@Entity
@Table(name = "vtm_predator_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmPredatorType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "granted_specialty")
  private String grantedSpecialty;

  @Column(name = "sourcebook")
  private String sourcebook;
}