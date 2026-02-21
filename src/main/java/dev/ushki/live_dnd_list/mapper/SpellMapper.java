package dev.ushki.live_dnd_list.mapper;

import dev.ushki.live_dnd_list.dto.request.SpellRequest;
import dev.ushki.live_dnd_list.dto.response.SpellResponse;
import dev.ushki.live_dnd_list.entity.character.Spell;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SpellMapper {

    public SpellResponse toResponse(Spell spell) {
        if (spell == null) return null;

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

    public List<SpellResponse> toResponseList(List<Spell> spells) {
        return spells.stream()
                .map(this::toResponse)
                .toList();
    }

    public Set<SpellResponse> toResponseSet(Set<Spell> spells) {
        return spells.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    public Spell toEntity(SpellRequest request) {
        if (request == null) return null;

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
        if (request.getName() != null) spell.setName(request.getName());
        if (request.getLevel() != null) spell.setLevel(request.getLevel());
        if (request.getSchool() != null) spell.setSchool(request.getSchool());
        if (request.getCastingTime() != null) spell.setCastingTime(request.getCastingTime());
        if (request.getRange() != null) spell.setRange(request.getRange());
        if (request.getComponents() != null) spell.setComponents(request.getComponents());
        if (request.getDuration() != null) spell.setDuration(request.getDuration());
        spell.setConcentration(request.isConcentration());
        spell.setRitual(request.isRitual());
        if (request.getDescription() != null) spell.setDescription(request.getDescription());
        if (request.getHigherLevels() != null) spell.setHigherLevels(request.getHigherLevels());
    }
}
