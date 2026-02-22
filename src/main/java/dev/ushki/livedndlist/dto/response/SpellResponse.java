package dev.ushki.livedndlist.dto.response;

import dev.ushki.livedndlist.enums.SpellSchool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing spell information. Represents a spell as defined in D&D 5th Edition.
 *
 * <p>Spell levels range from 0 (cantrips) to 9 (most powerful spells).
 * Cantrips can be cast at will without expending spell slots.
 *
 * <p>Spells belong to one of eight schools of magic:
 * Abjuration, Conjuration, Divination, Enchantment, Evocation, Illusion, Necromancy, and
 * Transmutation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpellResponse {

  private Long id;
  private String name;
  private Integer level;
  private SpellSchool school;
  private String castingTime;
  private String range;
  private String components;
  private String duration;
  private boolean concentration;
  private boolean ritual;
  private String description;
  private String higherLevels;
}
