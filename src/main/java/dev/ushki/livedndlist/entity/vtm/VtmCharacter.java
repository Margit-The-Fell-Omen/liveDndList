package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * A vampire character belonging to a chronicle, a clan and a predator type.
 */
@Entity
@Table(name = "vtm_characters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmCharacter {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chronicle_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private VtmChronicle chronicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clan_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private VtmClan clan;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "predator_type_id", nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private VtmPredatorType predatorType;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String concept;

  @Column(columnDefinition = "TEXT")
  private String ambition;

  @Column(columnDefinition = "TEXT")
  private String desire;

  @Column(name = "generation")
  private Integer generation;

  @Column(name = "humanity")
  private Integer humanity;

  @Column(name = "blood_potency")
  private Integer bloodPotency;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}