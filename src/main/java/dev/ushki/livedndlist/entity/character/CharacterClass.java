package dev.ushki.livedndlist.entity.character;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a character's class and level. Supports multiclassing - a character can have
 * multiple CharacterClass entries.
 *
 * <p>In D&D 5th Edition, there are 13 base classes:
 * Barbarian, Bard, Cleric, Druid, Fighter, Monk, Paladin, Ranger, Rogue, Sorcerer, Warlock, Wizard,
 * and Artificer.
 *
 * <p>Each class has subclasses (archetypes) chosen at specific levels,
 * typically between level 1 and 3 depending on the class.
 */
@Entity
@Table(name = "character_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterClass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String className;

  private String subClass;

  @Builder.Default
  private Integer level = 1;
}
