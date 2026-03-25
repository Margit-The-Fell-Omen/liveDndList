package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "character_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"character", "dndClass"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CharacterClass {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "class_seq")
  @SequenceGenerator(name = "class_seq", sequenceName = "class_sequence", allocationSize = 50)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "character_id", nullable = false)
  private DndCharacter character;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_id", nullable = false)
  private DndClass dndClass;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "archetype_id")
  private Archetype archetype;

  @Builder.Default
  private Integer level = 1;
}
