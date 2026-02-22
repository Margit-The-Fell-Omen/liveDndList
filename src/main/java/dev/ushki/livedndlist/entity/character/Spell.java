package dev.ushki.livedndlist.entity.character;

import dev.ushki.livedndlist.enums.SpellSchool;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a spell in D&D 5th Edition. Contains all spell information as defined in the
 * Player's Handbook and other sourcebooks.
 *
 * <p>Spell levels range from 0 (cantrips) to 9 (most powerful spells).
 * Cantrips can be cast at will without expending spell slots.
 *
 * <p>The eight schools of magic are:
 * <ul>
 *   <li>Abjuration - protective spells</li>
 *   <li>Conjuration - summoning and transportation</li>
 *   <li>Divination - revealing information</li>
 *   <li>Enchantment - affecting minds</li>
 *   <li>Evocation - elemental damage effects</li>
 *   <li>Illusion - deceiving the senses</li>
 *   <li>Necromancy - manipulating life force</li>
 *   <li>Transmutation - changing properties</li>
 * </ul>
 *
 * <p>Spells are shared entities - multiple characters can know the same spell.
 */
@Entity
@Table(name = "spells")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spell {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private Integer level;

  @Enumerated(EnumType.STRING)
  private SpellSchool school;

  private String castingTime;

  private String range;

  private String components; // V, S, M (materials)

  private String duration;

  @Builder.Default
  private boolean concentration = false;

  @Builder.Default
  private boolean ritual = false;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String higherLevels;
}
