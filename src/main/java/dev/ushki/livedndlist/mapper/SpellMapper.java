package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.character.Spell;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Spell entities and DTOs. Handles mapping for spell creation,
 * updates, and responses.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Converting entities to response DTOs</li>
 *   <li>Converting creation requests to entities</li>
 *   <li>Updating existing entities from update requests</li>
 *   <li>Batch conversion for lists and sets of spells</li>
 * </ul>
 */
@Component
public class SpellMapper {

  /**
   * Converts a Spell entity to a SpellResponse DTO.
   *
   * @param spell the spell entity to convert
   * @return the spell response DTO, or null if spell is null
   */
  public SpellResponse toResponse(Spell spell) {
    if (spell == null) {
      return null;
    }

    return SpellResponse.builder()
        .id(spell.getId())
        .name(spell.getName())
        .level(spell.getLevel())
        .school(spell.getSchool())
        .castingTime(spell.getCastingTime())
        .range(spell.getRange())
        .components(spell.getComponents())
        .duration(spell.getDuration())
        .concentration(spell.isConcentration())
        .ritual(spell.isRitual())
        .description(spell.getDescription())
        .higherLevels(spell.getHigherLevels())
        .build();
  }

  /**
   * Converts a set of Spell entities to SpellResponse DTOs. Useful for character spell lists which
   * are stored as sets.
   *
   * @param spells the set of spell entities
   * @return set of spell response DTOs
   */
  public Set<SpellResponse> toResponseSet(Set<Spell> spells) {
    return spells.stream()
        .map(this::toResponse)
        .collect(Collectors.toSet());
  }

  /**
   * Converts a SpellRequest to a new Spell entity.
   *
   * @param request the spell creation request
   * @return the new spell entity, or null if request is null
   */
  public Spell toEntity(SpellRequest request) {
    if (request == null) {
      return null;
    }

    return Spell.builder()
        .name(request.getName())
        .level(request.getLevel())
        .school(request.getSchool())
        .castingTime(request.getCastingTime())
        .range(request.getRange())
        .components(request.getComponents())
        .duration(request.getDuration())
        .concentration(request.isConcentration())
        .ritual(request.isRitual())
        .description(request.getDescription())
        .higherLevels(request.getHigherLevels())
        .build();
  }

  /**
   * Updates an existing Spell entity from a SpellRequest. Only updates fields that are present
   * (non-null) in the request.
   *
   * <p>Note: Boolean fields (concentration, ritual) are always updated
   * since they cannot be null in the request.
   *
   * @param spell   the spell entity to update
   * @param request the update request containing new values
   */
  public void updateEntity(Spell spell, SpellRequest request) {
    updateIfPresent(request.getName(), spell::setName);
    updateIfPresent(request.getLevel(), spell::setLevel);
    updateIfPresent(request.getSchool(), spell::setSchool);
    updateIfPresent(request.getCastingTime(), spell::setCastingTime);
    updateIfPresent(request.getRange(), spell::setRange);
    updateIfPresent(request.getComponents(), spell::setComponents);
    updateIfPresent(request.getDuration(), spell::setDuration);
    updateIfPresent(request.getDescription(), spell::setDescription);
    updateIfPresent(request.getHigherLevels(), spell::setHigherLevels);

    // Boolean values are always set (primitive booleans can't be null)
    spell.setConcentration(request.isConcentration());
    spell.setRitual(request.isRitual());
  }

  /**
   * Helper method for partial updates. Only applies the setter if the value is not null.
   *
   * @param value  the value to set (if not null)
   * @param setter the setter method to call
   * @param <T>    the type of the value
   */
  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
