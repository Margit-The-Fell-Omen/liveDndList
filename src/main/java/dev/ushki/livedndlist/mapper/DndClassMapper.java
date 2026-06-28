package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.open5e.Open5eArchetypeDto;
import dev.ushki.livedndlist.dto.open5e.Open5eClassDto;
import dev.ushki.livedndlist.entity.dndCharacter.Archetype;
import dev.ushki.livedndlist.entity.dndCharacter.DndClass;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class DndClassMapper {

  public DndClass toEntity(Open5eClassDto dto) {
    if (dto == null) {
      return null;
    }

    DndClass dndClass = DndClass.builder()
        .name(dto.getName())
        .slug(dto.getSlug())
        .description(dto.getDesc())
        .hitDice(dto.getHitDice())
        .hpAt1stLevel(dto.getHpAt1stLevel())
        .hpAtHigherLevels(dto.getHpAtHigherLevels())
        .profArmor(dto.getProfArmor())
        .profWeapons(dto.getProfWeapons())
        .profTools(dto.getProfTools())
        .profSavingThrows(dto.getProfSavingThrows())
        .profSkills(dto.getProfSkills())
        .equipment(dto.getEquipment())
        .levelTable(dto.getLevelTable())
        .spellcastingAbility(dto.getSpellcastingAbility())
        .subtypesName(dto.getSubtypesName())
        .documentSlug(dto.getDocumentSlug())
        .documentTitle(dto.getDocumentTitle())
        .documentLicenseUrl(dto.getDocumentLicenseUrl())
        .documentUrl(dto.getDocumentUrl())
        .archetypes(new ArrayList<>())
        .build();

    if (dto.getArchetypes() != null) {
      dto.getArchetypes().forEach(archetypeDto -> {
        Archetype archetype = toArchetypeEntity(archetypeDto);
        dndClass.addArchetype(archetype);
      });
    }

    return dndClass;
  }

  public Archetype toArchetypeEntity(Open5eArchetypeDto dto) {
    if (dto == null) {
      return null;
    }

    return Archetype.builder()
        .name(dto.getName())
        .slug(dto.getSlug())
        .description(dto.getDesc())
        .documentSlug(dto.getDocumentSlug())
        .documentTitle(dto.getDocumentTitle())
        .documentLicenseUrl(dto.getDocumentLicenseUrl())
        .documentUrl(dto.getDocumentUrl())
        .build();
  }

  public void updateEntity(DndClass entity, Open5eClassDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getDesc(), entity::setDescription);
    updateIfPresent(dto.getHitDice(), entity::setHitDice);
    updateIfPresent(dto.getHpAt1stLevel(), entity::setHpAt1stLevel);
    updateIfPresent(dto.getHpAtHigherLevels(), entity::setHpAtHigherLevels);
    updateIfPresent(dto.getProfArmor(), entity::setProfArmor);
    updateIfPresent(dto.getProfWeapons(), entity::setProfWeapons);
    updateIfPresent(dto.getProfTools(), entity::setProfTools);
    updateIfPresent(dto.getProfSavingThrows(), entity::setProfSavingThrows);
    updateIfPresent(dto.getProfSkills(), entity::setProfSkills);
    updateIfPresent(dto.getEquipment(), entity::setEquipment);
    updateIfPresent(dto.getLevelTable(), entity::setLevelTable);
    updateIfPresent(dto.getSpellcastingAbility(), entity::setSpellcastingAbility);
    updateIfPresent(dto.getSubtypesName(), entity::setSubtypesName);
    updateIfPresent(dto.getDocumentSlug(), entity::setDocumentSlug);
    updateIfPresent(dto.getDocumentTitle(), entity::setDocumentTitle);
    updateIfPresent(dto.getDocumentLicenseUrl(), entity::setDocumentLicenseUrl);
    updateIfPresent(dto.getDocumentUrl(), entity::setDocumentUrl);

    updateArchetypes(entity, dto);
  }

  private void updateArchetypes(DndClass entity, Open5eClassDto dto) {
    entity.getArchetypes().clear();

    if (dto.getArchetypes() == null || dto.getArchetypes().isEmpty()) {
      return;
    }

    dto.getArchetypes().forEach(archetypeDto -> {
      Archetype archetype = toArchetypeEntity(archetypeDto);
      entity.addArchetype(archetype);
    });
  }

  public void updateArchetypeEntity(Archetype entity, Open5eArchetypeDto dto) {
    if (dto == null || entity == null) {
      return;
    }

    updateIfPresent(dto.getName(), entity::setName);
    updateIfPresent(dto.getSlug(), entity::setSlug);
    updateIfPresent(dto.getDesc(), entity::setDescription);
    updateIfPresent(dto.getDocumentSlug(), entity::setDocumentSlug);
    updateIfPresent(dto.getDocumentTitle(), entity::setDocumentTitle);
    updateIfPresent(dto.getDocumentLicenseUrl(), entity::setDocumentLicenseUrl);
    updateIfPresent(dto.getDocumentUrl(), entity::setDocumentUrl);
  }

  public Open5eClassDto toDto(DndClass entity) {
    if (entity == null) {
      return null;
    }

    Open5eClassDto dto = new Open5eClassDto();
    dto.setName(entity.getName());
    dto.setSlug(entity.getSlug());
    dto.setDesc(entity.getDescription());
    dto.setHitDice(entity.getHitDice());
    dto.setHpAt1stLevel(entity.getHpAt1stLevel());
    dto.setHpAtHigherLevels(entity.getHpAtHigherLevels());
    dto.setProfArmor(entity.getProfArmor());
    dto.setProfWeapons(entity.getProfWeapons());
    dto.setProfTools(entity.getProfTools());
    dto.setProfSavingThrows(entity.getProfSavingThrows());
    dto.setProfSkills(entity.getProfSkills());
    dto.setEquipment(entity.getEquipment());
    dto.setLevelTable(entity.getLevelTable());
    dto.setSpellcastingAbility(entity.getSpellcastingAbility());
    dto.setSubtypesName(entity.getSubtypesName());
    dto.setDocumentSlug(entity.getDocumentSlug());
    dto.setDocumentTitle(entity.getDocumentTitle());
    dto.setDocumentLicenseUrl(entity.getDocumentLicenseUrl());
    dto.setDocumentUrl(entity.getDocumentUrl());

    if (entity.getArchetypes() != null) {
      dto.setArchetypes(
          entity.getArchetypes().stream()
              .map(this::toArchetypeDto)
              .toList()
      );
    }

    return dto;
  }

  public Open5eArchetypeDto toArchetypeDto(Archetype entity) {
    if (entity == null) {
      return null;
    }

    Open5eArchetypeDto dto = new Open5eArchetypeDto();
    dto.setName(entity.getName());
    dto.setSlug(entity.getSlug());
    dto.setDesc(entity.getDescription());
    dto.setDocumentSlug(entity.getDocumentSlug());
    dto.setDocumentTitle(entity.getDocumentTitle());
    dto.setDocumentLicenseUrl(entity.getDocumentLicenseUrl());
    dto.setDocumentUrl(entity.getDocumentUrl());

    return dto;
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }
}
