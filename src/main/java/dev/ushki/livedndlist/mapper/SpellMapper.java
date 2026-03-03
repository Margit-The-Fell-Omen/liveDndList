package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.character.Spell;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SpellMapper {

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

  public Set<SpellResponse> toResponseSet(Set<Spell> spells) {
    return spells.stream()
        .map(this::toResponse)
        .collect(Collectors.toSet());
  }

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

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
