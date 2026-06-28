package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eSpellDto;
import dev.ushki.livedndlist.dto.request.SpellRequest;
import dev.ushki.livedndlist.dto.response.SpellResponse;
import dev.ushki.livedndlist.entity.dndCharacter.Spell;
import dev.ushki.livedndlist.enums.SpellSchool;
import java.util.ArrayList;
import java.util.List;
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
    if (spells == null) {
      return Set.of();
    }

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

  public Spell fromOpen5eDto(Open5eSpellDto dto) {
    if (dto == null) {
      return null;
    }

    return Spell.builder()
        .name(dto.getName())
        .level(dto.getLevel())
        .school(mapSchool(dto))
        .castingTime(dto.getCastingTime())
        .range(dto.getRangeText())
        .components(buildComponents(dto))
        .duration(buildDuration(dto))
        .concentration(Boolean.TRUE.equals(dto.getConcentration()))
        .ritual(Boolean.TRUE.equals(dto.getRitual()))
        .description(dto.getDesc())
        .higherLevels(dto.getHigherLevel())
        .build();
  }

  public SpellRequest toSpellRequest(Open5eSpellDto dto) {
    if (dto == null) {
      return null;
    }

    return SpellRequest.builder()
        .name(dto.getName())
        .level(dto.getLevel())
        .school(mapSchool(dto))
        .castingTime(dto.getCastingTime())
        .range(dto.getRangeText())
        .components(buildComponents(dto))
        .duration(buildDuration(dto))
        .concentration(Boolean.TRUE.equals(dto.getConcentration()))
        .ritual(Boolean.TRUE.equals(dto.getRitual()))
        .description(dto.getDesc())
        .higherLevels(dto.getHigherLevel())
        .build();
  }

  public void updateEntityFromOpen5eDto(Spell spell, Open5eSpellDto dto) {
    updateEntity(spell, toSpellRequest(dto));
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

    spell.setConcentration(request.isConcentration());
    spell.setRitual(request.isRitual());
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }

  private SpellSchool mapSchool(Open5eSpellDto dto) {
    if (dto.getSchool() == null || dto.getSchool().getKey() == null) {
      return null;
    }

    try {
      return SpellSchool.valueOf(dto.getSchool().getKey().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private String buildComponents(Open5eSpellDto dto) {
    List<String> parts = new ArrayList<>();

    if (Boolean.TRUE.equals(dto.getVerbal())) {
      parts.add("V");
    }
    if (Boolean.TRUE.equals(dto.getSomatic())) {
      parts.add("S");
    }
    if (Boolean.TRUE.equals(dto.getMaterial())) {
      String materialSpecified = dto.getMaterialSpecified();
      if (materialSpecified != null && !materialSpecified.isBlank()) {
        parts.add("M (" + materialSpecified + ")");
      } else {
        parts.add("M");
      }
    }

    return parts.isEmpty() ? null : String.join(", ", parts);
  }

  private String buildDuration(Open5eSpellDto dto) {
    if (dto.getDuration() == null) {
      return null;
    }

    if (Boolean.TRUE.equals(dto.getConcentration())) {
      return "Concentration, up to " + dto.getDuration();
    }

    return dto.getDuration();
  }
}