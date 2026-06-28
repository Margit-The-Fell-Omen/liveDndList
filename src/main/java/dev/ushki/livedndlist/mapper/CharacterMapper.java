package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.request.AbilityScoresRequest;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.request.SkillUpdateRequest;
import dev.ushki.livedndlist.dto.response.AbilityScoresResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.SkillResponse;
import dev.ushki.livedndlist.entity.dndCharacter.AbilityScores;
import dev.ushki.livedndlist.entity.dndCharacter.Archetype;
import dev.ushki.livedndlist.entity.dndCharacter.Background;
import dev.ushki.livedndlist.entity.dndCharacter.CharacterClass;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.DndClass;
import dev.ushki.livedndlist.entity.dndCharacter.DndCurrency;
import dev.ushki.livedndlist.entity.dndCharacter.Race;
import dev.ushki.livedndlist.entity.dndCharacter.Skill;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.SkillType;
import dev.ushki.livedndlist.repository.ArchetypeRepository;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.repository.RaceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CharacterMapper {

  private static final TreeMap<Integer, Integer> LEVEL_THRESHOLDS = new TreeMap<>();

  static {
    LEVEL_THRESHOLDS.put(0, 1);
    LEVEL_THRESHOLDS.put(300, 2);
    LEVEL_THRESHOLDS.put(900, 3);
    LEVEL_THRESHOLDS.put(2700, 4);
    LEVEL_THRESHOLDS.put(6500, 5);
    LEVEL_THRESHOLDS.put(14000, 6);
    LEVEL_THRESHOLDS.put(23000, 7);
    LEVEL_THRESHOLDS.put(34000, 8);
    LEVEL_THRESHOLDS.put(48000, 9);
    LEVEL_THRESHOLDS.put(64000, 10);
    LEVEL_THRESHOLDS.put(85000, 11);
    LEVEL_THRESHOLDS.put(100000, 12);
    LEVEL_THRESHOLDS.put(120000, 13);
    LEVEL_THRESHOLDS.put(140000, 14);
    LEVEL_THRESHOLDS.put(165000, 15);
    LEVEL_THRESHOLDS.put(195000, 16);
    LEVEL_THRESHOLDS.put(225000, 17);
    LEVEL_THRESHOLDS.put(265000, 18);
    LEVEL_THRESHOLDS.put(305000, 19);
    LEVEL_THRESHOLDS.put(355000, 20);
  }

  private int getLevelFromExperience(int experiencePoints) {
    Map.Entry<Integer, Integer> entry = LEVEL_THRESHOLDS.floorEntry(experiencePoints);
    return (entry != null) ? entry.getValue() : 1;
  }

  private final RaceRepository raceRepository;
  private final BackgroundRepository backgroundRepository;
  private final DndClassRepository dndClassRepository;
  private final ArchetypeRepository archetypeRepository;
  private final SpellMapper spellMapper;
  private final EquipmentMapper equipmentMapper;

  public CharacterResponse toResponse(DndCharacter character) {
    if (character == null) {
      return null;
    }

    return CharacterResponse.builder()
        .id(character.getId())
        .name(character.getName())
        .raceName(character.getRace().getName())
        .alignment(character.getAlignment())
        .backgroundKey(character.getBackground().getKey())
        .experiencePoints(character.getExperiencePoints())
        .portraitUrl(character.getPortraitUrl())
        .classesInfo(Hibernate.isInitialized(character.getClasses())
            ? character.getClasses().stream()
            .map(c -> c.getDndClass().getName())
            .toList() : null)
        .totalLevel(Hibernate.isInitialized(character.getClasses())
            ? character.getTotalLevel() : 0)
        .abilityScores(mapAbilityScores(character.getAbilityScores()))
        .maxHitPoints(character.getMaxHitPoints())
        .currentHitPoints(character.getCurrentHitPoints())
        .temporaryHitPoints(character.getTemporaryHitPoints())
        .armorClass(character.getArmorClass())
        .initiative(character.getInitiative())
        .speed(character.getSpeed())
        .proficiencyBonus(character.getProficiencyBonus())
        .hitDice(character.getHitDice())
        .deathSaveSuccesses(character.getDeathSaveSuccesses())
        .deathSaveFailures(character.getDeathSaveFailures())
        .skills(Hibernate.isInitialized(character.getSkills())
            ? mapSkills(character.getSkills(), character.getAbilityScores(),
            character.getProficiencyBonus()) : null)
        .savingThrowProficiencies(Hibernate.isInitialized(character.getSavingThrowProficiencies())
            ? character.getSavingThrowProficiencies() : null)
        .equipment(Hibernate.isInitialized(character.getEquipment())
            ? equipmentMapper.toResponseList(character.getEquipment()) : null)
        .currency(mapCurrency(character.getCurrency()))
        .spells(Hibernate.isInitialized(character.getSpells())
            ? spellMapper.toResponseSet(character.getSpells()) : null)
        .spellcastingAbility(character.getSpellcastingAbility())
        .featuresAndTraits(character.getFeaturesAndTraits())
        .backstory(character.getBackstory())
        .personalityTraits(character.getPersonalityTraits())
        .ideals(character.getIdeals())
        .bonds(character.getBonds())
        .flaws(character.getFlaws())
        .notes(character.getNotes())
        .createdAt(character.getCreatedAt())
        .updatedAt(character.getUpdatedAt())
        .build();
  }

  public CharacterSummaryResponse toSummaryResponse(DndCharacter character) {
    if (character == null) {
      return null;
    }

    String classDisplay = Hibernate.isInitialized(character.getClasses())
        ? character.getClasses().stream()
        .sorted(Comparator.comparing(CharacterClass::getLevel).reversed())
        .map(c -> c.getDndClass().getName() + " " + c.getLevel())
        .collect(Collectors.joining(" / "))
        : null;

    return CharacterSummaryResponse.builder()
        .id(character.getId())
        .name(character.getName())
        .raceName(character.getRace() != null ? character.getRace().getName() : null)
        .classDisplay(classDisplay)
        .totalLevel(Hibernate.isInitialized(character.getClasses())
            ? character.getTotalLevel() : 0)
        .currentHitPoints(character.getCurrentHitPoints())
        .maxHitPoints(character.getMaxHitPoints())
        .portraitUrl(character.getPortraitUrl())
        .updatedAt(character.getUpdatedAt())
        .build();
  }

  public List<CharacterSummaryResponse> toSummaryResponseList(List<DndCharacter> characters) {
    return characters.stream()
        .map(this::toSummaryResponse)
        .toList();
  }

  public DndCharacter toEntity(CharacterCreateRequest request) {
    if (request == null) {
      return null;
    }

    Race race = raceRepository.findBySlug(request.getRaceSlug())
        .orElseThrow(() -> new EntityNotFoundException(
            "Race not found with id: " + request.getRaceSlug()));

    DndClass dndClass = dndClassRepository.findBySlug(request.getClassSlug())
        .orElseThrow(() -> new EntityNotFoundException(
            "Class not found with id: " + request.getClassSlug()));

    Archetype archetype = null;
    if (request.getArchetypeSlug() != null) {
      archetype = archetypeRepository.findBySlug(request.getArchetypeSlug())
          .orElseThrow(() -> new EntityNotFoundException(
              "Archetype not found with id: " + request.getArchetypeSlug()));

      if (!archetype.getDndClass().getId().equals(dndClass.getId())) {
        throw new IllegalArgumentException(
            "Archetype " + archetype.getName() + " does not belong to class " + dndClass.getName());
      }
    }

    Background background = backgroundRepository.findByKey(request.getBackgroundKey())
        .orElseThrow(
            () -> new EntityNotFoundException(
                "Background not found with key: " + request.getBackgroundKey()));

    DndCharacter character = DndCharacter.builder()
        .name(request.getName())
        .race(race)
        .background(background)
        .alignment(request.getAlignment())
        .portraitUrl(request.getPortraitUrl())
        .build();

    CharacterClass characterClass = CharacterClass.builder()
        .dndClass(dndClass)
        .archetype(archetype)
        .level(1)
        .build();
    character.addClass(characterClass);

    if (request.getAbilityScores() != null) {
      character.setAbilityScores(mapAbilityScoresRequest(request.getAbilityScores()));
    }

    if (request.getMaxHitPoints() != null) {
      character.setMaxHitPoints(request.getMaxHitPoints());
      character.setCurrentHitPoints(request.getMaxHitPoints());
    }

    if (request.getSpellcastingAbility() != null) {
      character.setSpellcastingAbility(
          AbilityType.valueOf(request.getSpellcastingAbility()));
    }

    initializeSkills(character);

    return character;
  }

  public void updateEntity(DndCharacter character, CharacterUpdateRequest request) {
    updateIfPresent(request.getName(), character::setName);
    updateIfPresent(request.getAlignment(), character::setAlignment);
    updateIfPresent(request.getMaxHitPoints(), character::setMaxHitPoints);
    updateIfPresent(request.getCurrentHitPoints(), character::setCurrentHitPoints);
    updateIfPresent(request.getTemporaryHitPoints(), character::setTemporaryHitPoints);
    updateIfPresent(request.getArmorClass(), character::setArmorClass);
    updateIfPresent(request.getSpeed(), character::setSpeed);
    updateIfPresent(request.getPortraitUrl(), character::setPortraitUrl);
    updateIfPresent(request.getBackstory(), character::setBackstory);
    updateIfPresent(request.getPersonalityTraits(), character::setPersonalityTraits);
    updateIfPresent(request.getIdeals(), character::setIdeals);
    updateIfPresent(request.getBonds(), character::setBonds);
    updateIfPresent(request.getFlaws(), character::setFlaws);
    updateIfPresent(request.getNotes(), character::setNotes);
    updateIfPresent(request.getDeathSaveFailures(), character::setDeathSaveFailures);
    updateIfPresent(request.getDeathSaveSuccesses(), character::setDeathSaveSuccesses);
    updateIfPresent(request.getExperiencePoints(), character::setExperiencePoints);
    updateIfPresent(request.getFeaturesAndTraits(), character::setFeaturesAndTraits);

    if (request.getBackgroundKey() != null) {
      Background background = backgroundRepository.findByKey(request.getBackgroundKey())
          .orElseThrow(() -> new EntityNotFoundException(
              "Background not found with key: " + request.getBackgroundKey()));
      character.setBackground(background);
      log.info("Character update: background found: {}", background);
    } else {
      log.info("Character update: background key is null");
    }

    if (request.getExperiencePoints() != null) {
      int oldTotalLevel = character.getTotalLevel();
      int newExperience = request.getExperiencePoints();
      character.setExperiencePoints(newExperience);

      int newTotalLevel = getLevelFromExperience(newExperience);

      if (newTotalLevel > oldTotalLevel) {
        int levelsToAdd = newTotalLevel - oldTotalLevel;

        Optional<CharacterClass> classToLevelUp = character.getClasses().stream()
            .max(Comparator.comparing(CharacterClass::getLevel));

        classToLevelUp.ifPresent(primaryClass ->
            primaryClass.setLevel(primaryClass.getLevel() + levelsToAdd)
        );
      }
    }

    if (request.getSavingThrowProficiencies() != null) {
      Set<AbilityType> proficiencies = request.getSavingThrowProficiencies().stream()
          .map(AbilityType::valueOf)
          .collect(Collectors.toSet());
      character.setSavingThrowProficiencies(proficiencies);
    }

    if (request.getSkills() != null && character.getSkills() != null) {
      Map<Long, Skill> skillMap = character.getSkills().stream()
          .collect(Collectors.toMap(Skill::getId, s -> s));

      for (SkillUpdateRequest skillUpdate : request.getSkills()) {
        Skill skillToUpdate = skillMap.get(skillUpdate.getId());
        if (skillToUpdate != null) {
          updateIfPresent(skillUpdate.getProficient(), skillToUpdate::setProficiency);
          updateIfPresent(skillUpdate.getExpertise(), skillToUpdate::setExpertise);
        }
      }
    }

    if (request.getRaceId() != null) {
      Race race = raceRepository.findById(request.getRaceId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Race not found with id: " + request.getRaceId()));
      character.setRace(race);
    }

    if (request.getAbilityScores() != null) {
      character.setAbilityScores(mapAbilityScoresRequest(request.getAbilityScores()));
      character.setInitiative(character.getAbilityScores().getModifier(AbilityType.DEXTERITY));
    }

    if (request.getSpellcastingAbility() != null) {
      character.setSpellcastingAbility(
          AbilityType.valueOf(request.getSpellcastingAbility()));
    }
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }

  private AbilityScoresResponse mapAbilityScores(AbilityScores scores) {
    if (scores == null) {
      return null;
    }

    return AbilityScoresResponse.builder()
        .strength(scores.getStrength())
        .strengthModifier(scores.getModifier(AbilityType.STRENGTH))
        .dexterity(scores.getDexterity())
        .dexterityModifier(scores.getModifier(AbilityType.DEXTERITY))
        .constitution(scores.getConstitution())
        .constitutionModifier(scores.getModifier(AbilityType.CONSTITUTION))
        .intelligence(scores.getIntelligence())
        .intelligenceModifier(scores.getModifier(AbilityType.INTELLIGENCE))
        .wisdom(scores.getWisdom())
        .wisdomModifier(scores.getModifier(AbilityType.WISDOM))
        .charisma(scores.getCharisma())
        .charismaModifier(scores.getModifier(AbilityType.CHARISMA))
        .build();
  }

  private AbilityScores mapAbilityScoresRequest(@Valid AbilityScoresRequest request) {
    return AbilityScores.builder()
        .strength(request.getStrength())
        .dexterity(request.getDexterity())
        .constitution(request.getConstitution())
        .intelligence(request.getIntelligence())
        .wisdom(request.getWisdom())
        .charisma(request.getCharisma())
        .build();
  }

  private List<SkillResponse> mapSkills(Set<Skill> skills, AbilityScores abilityScores,
      int proficiencyBonus) {
    if (skills == null) {
      return List.of();
    }

    return skills.stream()
        .sorted(Comparator.comparing(s -> s.getSkillType().name()))
        .map(skill -> mapSkill(skill, abilityScores, proficiencyBonus))
        .toList();
  }

  private SkillResponse mapSkill(Skill skill, AbilityScores abilityScores, int proficiencyBonus) {
    AbilityType baseAbility = skill.getSkillType().getBaseAbility();
    int totalBonus = abilityScores.getModifier(baseAbility);

    if (skill.isExpertise()) {
      totalBonus += proficiencyBonus * 2;
    } else if (skill.isProficient()) {
      totalBonus += proficiencyBonus;
    }

    return SkillResponse.builder()
        .id(skill.getId())
        .skillType(skill.getSkillType())
        .abilityType(baseAbility)
        .proficient(skill.isProficient())
        .expertise(skill.isExpertise())
        .totalBonus(totalBonus)
        .build();
  }

  private CharacterResponse.DndCurrencyResponse mapCurrency(DndCurrency currency) {
    if (currency == null) {
      return null;
    }

    return CharacterResponse.DndCurrencyResponse.builder()
        .copper(currency.getCopper())
        .silver(currency.getSilver())
        .electrum(currency.getElectrum())
        .gold(currency.getGold())
        .platinum(currency.getPlatinum())
        .build();
  }

  private void initializeSkills(DndCharacter character) {
    Set<Skill> skills = new HashSet<>();
    for (SkillType skillType : SkillType.values()) {
      Skill skill = Skill.builder()
          .skillType(skillType)
          .proficiency(false)
          .expertise(false)
          .build();
      skill.setCharacter(character);
      skills.add(skill);
    }
    character.setSkills(skills);
  }
}
