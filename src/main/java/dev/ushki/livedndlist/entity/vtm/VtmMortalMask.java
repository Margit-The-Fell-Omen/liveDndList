package dev.ushki.livedndlist.entity.vtm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The "mortal mask" of a vampire - a human identity maintained to observe the Masquerade.
 * There is at most one mask per character (1:1).
 */
@Entity
@Table(name = "vtm_mortal_masks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VtmMortalMask {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "character_id", unique = true, nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private VtmCharacter character;

  @Column(name = "mortal_name", nullable = false)
  private String mortalName;

  @Column(name = "occupation")
  private String occupation;

  @Column(columnDefinition = "TEXT")
  private String coverStory;

  @Column(columnDefinition = "TEXT")
  private String description;
}