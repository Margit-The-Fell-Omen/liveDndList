package dev.ushki.livedndlist.dto.request;

import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.CharacterRace;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing D&D character. All fields are optional - only provided
 * fields will be updated.
 *
 * <p>This request supports partial updates. Fields set to {@code null}
 * will not modify the existing character data.
 *
 * <p>Includes fields for:
 * <ul>
 *   <li>Basic information (name, race, alignment)</li>
 *   <li>Combat stats (HP, AC, speed)</li>
 *   <li>Ability scores</li>
 *   <li>Roleplay elements (backstory, personality traits, etc.)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterUpdateRequest {

  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  private CharacterRace race;

  private String subrace;

  private CharacterAlignment alignment;

  private String background;

  @Valid
  private AbilityScoresRequest abilityScores;

  private Integer maxHitPoints;

  private Integer currentHitPoints;

  private Integer temporaryHitPoints;

  private Integer armorClass;

  private Integer speed;

  private String portraitUrl;

  private String backstory;

  private String personalityTraits;

  private String ideals;

  private String bonds;

  private String flaws;

  private String notes;
}
