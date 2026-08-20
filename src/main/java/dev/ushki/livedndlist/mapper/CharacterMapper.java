package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.DndClassLevelDto;
import dev.ushki.livedndlist.dto.request.AbilityScoresRequest;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.response.AbilityScoresResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.ActionResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.AttackModifierResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.CharacterFeatureResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.ClassSpellcastingResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.CustomFeatureResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.FeatureChoiceAnswerResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.HitDiceEntryResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.PendingChoiceResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.ProficienciesResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.ResourceResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.SenseResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse.SpellcastingResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.DndCurrencyResponse;
import dev.ushki.livedndlist.dto.response.SkillResponse;
import dev.ushki.livedndlist.entity.dndCharacter.AbilityScores;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.DndCurrency;
import dev.ushki.livedndlist.entity.dndCharacter.Skill;
import dev.ushki.livedndlist.entity.dndCharacter.background.Background;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndCharacterClassLevel;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClass;
import dev.ushki.livedndlist.entity.dndCharacter.race.Race;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.SkillType;
import dev.ushki.livedndlist.exceptions.ResourceNotFoundException;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

  private final RaceRepository raceRepository;
  private final BackgroundRepository backgroundRepository;
  private final DndClassRepository dndClassRepository;
  private final SpellMapper spellMapper;
  private final EquipmentMapper equipmentMapper;

  public int getLevelFromExperience(int experiencePoints) {
    Map.Entry<Integer, Integer> entry = LEVEL_THRESHOLDS.floorEntry(experiencePoints);
    return (entry != null) ? entry.getValue() : 1;
  }

  public CharacterResponse toResponse(DndCharacter character, ComputedCharacterState state) {
    if (character == null) {
      return null;
    }

    CharacterResponse.CharacterResponseBuilder builder = baseResponseBuilder(character);

    if (state == null) {
      return builder.build();
    }

    builder
        .totalLevel(state.getTotalLevel())
        .proficiencyBonus(state.getProficiencyBonus())
        .abilityScores(mapAbilityScoresFromState(state))
        .armorClass(state.getArmorClass())
        .initiative(state.getInitiative())
        .speeds(state.getSpeeds())
        .size(state.getSize() != null ? state.getSize().name() : null)
        .creatureType(state.getCreatureType() != null ? state.getCreatureType().name() : null)
        .hitDice(mapHitDice(state))
        .skills(mapSkillsFromState(state, character.getSkills()))
        .savingThrowProficiencies(mapSavingThrowProficiencies(state))
        .proficiencies(mapProficiencies(state))
        .senses(mapSenses(state))
        .damageResistances(state.getDamageResistances())
        .damageImmunities(state.getDamageImmunities())
        .damageVulnerabilities(state.getDamageVulnerabilities())
        .conditionImmunities(state.getConditionImmunities())
        .spellcasting(mapSpellcasting(state))
        .resources(mapResources(state))
        .actions(mapActions(state))
        .attackModifiers(mapAttackModifiers(state))
        .features(mapFeatures(state))
        .customFeatures(mapCustomFeatures(state))
        .pendingChoices(mapPendingChoices(state));

    return builder.build();
  }

  public CharacterResponse toResponse(DndCharacter character) {
    return toResponse(character, null);
  }

  private CharacterResponse.CharacterResponseBuilder baseResponseBuilder(DndCharacter character) {
    return CharacterResponse.builder()
        .id(character.getId())
        .name(character.getName())
        .raceKey(character.getRace() != null ? character.getRace().getKey() : null)
        .alignment(character.getAlignment())
        .backgroundKey(
            character.getBackground() != null ? character.getBackground().getKey() : null)
        .experiencePoints(character.getExperiencePoints())
        .portraitUrl(character.getPortraitUrl())
        .classesInfo(mapClasses(character.getClasses()))
        .maxHitPoints(character.getMaxHitPoints())
        .currentHitPoints(character.getCurrentHitPoints())
        .temporaryHitPoints(character.getTemporaryHitPoints())
        .armorClassBonus(character.getArmorClassBonus())
        .deathSaveSuccesses(character.getDeathSaveSuccesses())
        .deathSaveFailures(character.getDeathSaveFailures())
        .equipment(Hibernate.isInitialized(character.getEquipment())
            ? equipmentMapper.toResponseList(character.getEquipment()) : null)
        .currency(mapCurrency(character.getCurrency()))
        .spells(Hibernate.isInitialized(character.getSpells())
            ? spellMapper.toResponseSet(character.getSpells()) : null)
        .spellcastingAbility(character.getSpellcastingAbility())
        .backstory(character.getBackstory())
        .personalityTraits(character.getPersonalityTraits())
        .ideals(character.getIdeals())
        .bonds(character.getBonds())
        .flaws(character.getFlaws())
        .notes(character.getNotes())
        .createdAt(character.getCreatedAt())
        .updatedAt(character.getUpdatedAt());
  }

  private AbilityScoresResponse mapAbilityScoresFromState(ComputedCharacterState state) {
    Map<String, Integer> scores = state.getFinalAbilityScores();
    Map<String, Integer> mods = state.getAbilityModifiers();

    return AbilityScoresResponse.builder()
        .strength(scores.getOrDefault("STR", 10))
        .strengthModifier(mods.getOrDefault("STR", 0))
        .dexterity(scores.getOrDefault("DEX", 10))
        .dexterityModifier(mods.getOrDefault("DEX", 0))
        .constitution(scores.getOrDefault("CON", 10))
        .constitutionModifier(mods.getOrDefault("CON", 0))
        .intelligence(scores.getOrDefault("INT", 10))
        .intelligenceModifier(mods.getOrDefault("INT", 0))
        .wisdom(scores.getOrDefault("WIS", 10))
        .wisdomModifier(mods.getOrDefault("WIS", 0))
        .charisma(scores.getOrDefault("CHA", 10))
        .charismaModifier(mods.getOrDefault("CHA", 0))
        .build();
  }

  private Map<String, HitDiceEntryResponse> mapHitDice(ComputedCharacterState state) {
    Map<String, HitDiceEntryResponse> result = new HashMap<>();
    state.getHitDice().forEach((key, entry) ->
        result.put(key, HitDiceEntryResponse.builder()
            .die(entry.getDie())
            .count(entry.getCount())
            .build()));
    return result;
  }

  private List<SkillResponse> mapSkillsFromState(ComputedCharacterState state,
      Set<Skill> characterSkills) {
    if (characterSkills == null || !Hibernate.isInitialized(characterSkills)) {
      return Collections.emptyList();
    }

    Map<SkillType, Skill> skillMap = characterSkills.stream()
        .collect(Collectors.toMap(Skill::getSkillType, s -> s));

    List<SkillResponse> result = new ArrayList<>();
    for (SkillType skillType : SkillType.values()) {
      Skill entity = skillMap.get(skillType);
      if (entity == null) {
        continue;
      }

      boolean isProficient = state.getSkillProficiencies().contains(skillType.name());
      boolean hasExpertise = state.getSkillExpertise().contains(skillType.name());
      int totalBonus = state.getSkillTotals().getOrDefault(skillType.name(), 0);

      result.add(SkillResponse.builder()
          .id(entity.getId())
          .skillType(skillType)
          .abilityType(skillType.getBaseAbility())
          .proficient(isProficient)
          .expertise(hasExpertise)
          .totalBonus(totalBonus)
          .build());
    }
    result.sort(Comparator.comparing(s -> s.getSkillType().name()));
    return result;
  }

  private Set<AbilityType> mapSavingThrowProficiencies(ComputedCharacterState state) {
    Set<AbilityType> result = new HashSet<>();
    for (String key : state.getSavingThrowProficiencies()) {
      try {
        AbilityType type = matchAbility(key);
        if (type != null) {
          result.add(type);
        }
      } catch (IllegalArgumentException ignored) {
        log.info("Illegal argument in saving throws profs of characterState");
      }
    }
    return result;
  }

  private AbilityType matchAbility(String key) {
    if (key == null) {
      return null;
    }
    return Arrays.stream(AbilityType.values())
        .filter(a -> a.name().equalsIgnoreCase(key) || a.name().startsWith(key.toUpperCase()))
        .findFirst()
        .orElse(null);
  }

  private ProficienciesResponse mapProficiencies(ComputedCharacterState state) {
    return ProficienciesResponse.builder()
        .armor(state.getArmorProficiencies())
        .weapons(state.getWeaponProficiencies())
        .tools(state.getToolProficiencies())
        .languages(state.getLanguages())
        .build();
  }

  private List<SenseResponse> mapSenses(ComputedCharacterState state) {
    return state.getSenses().stream()
        .map(s -> SenseResponse.builder()
            .senseType(s.getSenseType())
            .range(s.getRange())
            .build())
        .toList();
  }

  private SpellcastingResponse mapSpellcasting(ComputedCharacterState state) {
    if (state.getSpellcasting() == null || state.getSpellcasting().getClasses().isEmpty()) {
      return null;
    }
    List<ClassSpellcastingResponse> classes = state.getSpellcasting().getClasses().stream()
        .map(c -> ClassSpellcastingResponse.builder()
            .classKey(c.getClassKey())
            .ability(c.getAbility())
            .casterType(c.getCasterType())
            .spellSaveDc(c.getSpellSaveDc())
            .spellAttackBonus(c.getSpellAttackBonus())
            .spellSlotsTotal(c.getSpellSlotsTotal())
            .spellSlotsUsed(Collections.emptyMap())
            .preparedSpellsCount(c.getPreparedSpellsCount())
            .spellList(c.getSpellList())
            .ritualCasting(c.isRitualCasting())
            .build())
        .toList();
    return SpellcastingResponse.builder().classes(classes).build();
  }

  private List<ResourceResponse> mapResources(ComputedCharacterState state) {
    return state.getResources().stream()
        .map(r -> ResourceResponse.builder()
            .resourceKey(r.getResourceKey())
            .displayName(r.getDisplayName())
            .currentUses(r.getCurrentUses())
            .maxUses(r.getMaxUses())
            .refreshOn(r.getRefreshOn())
            .sourceFeatureId(r.getSourceFeatureId())
            .build())
        .toList();
  }

  private List<ActionResponse> mapActions(ComputedCharacterState state) {
    return state.getActions().stream()
        .map(a -> ActionResponse.builder()
            .kind(a.getKind())
            .name(a.getName())
            .description(a.getDescription())
            .resourceKey(a.getResourceKey())
            .uses(a.getUses())
            .refresh(a.getRefresh())
            .build())
        .toList();
  }

  private List<AttackModifierResponse> mapAttackModifiers(ComputedCharacterState state) {
    return state.getAttackModifiers().stream()
        .map(m -> AttackModifierResponse.builder()
            .amount(m.getAmount())
            .dice(m.getDice())
            .filter(m.getFilter())
            .build())
        .toList();
  }

  private List<CharacterFeatureResponse> mapFeatures(ComputedCharacterState state) {
    return state.getDisplayFeatures().stream()
        .map(f -> CharacterFeatureResponse.builder()
            .id(f.getCharacterFeatureId())
            .name(f.getName())
            .description(f.getDescription())
            .source(f.getSource())
            .sourceLabel(f.getSourceLabel())
            .sourceContext(f.getSourceContext())
            .choices(f.getChoices().stream()
                .map(c -> FeatureChoiceAnswerResponse.builder()
                    .choiceKey(c.getChoiceKey())
                    .name(c.getName())
                    .selectedValues(c.getSelectedValues())
                    .build())
                .toList())
            .build())
        .toList();
  }

  private List<CustomFeatureResponse> mapCustomFeatures(ComputedCharacterState state) {
    return state.getDisplayCustomFeatures().stream()
        .map(cf -> CustomFeatureResponse.builder()
            .id(cf.getId())
            .name(cf.getName())
            .description(cf.getDescription())
            .build())
        .toList();
  }

  private List<PendingChoiceResponse> mapPendingChoices(ComputedCharacterState state) {
    return state.getPendingChoices().stream()
        .map(pc -> PendingChoiceResponse.builder()
            .characterFeatureId(pc.getCharacterFeatureId())
            .choiceKey(pc.getChoiceKey())
            .name(pc.getName())
            .description(pc.getDescription())
            .chooseCount(pc.getChooseCount())
            .optionsSource(pc.getOptionsSource() != null ? pc.getOptionsSource().name() : null)
            .optionsFilter(pc.getOptionsFilter())
            .currentSelection(pc.getCurrentSelection())
            .build())
        .toList();
  }

  private List<DndClassLevelDto> mapClasses(Set<DndCharacterClassLevel> classes) {
    if (classes == null || !Hibernate.isInitialized(classes) || classes.isEmpty()) {
      return List.of();
    }
    return classes.stream()
        .map(cc -> DndClassLevelDto.builder()
            .classKey(cc.getDndClass().getKey())
            .level(cc.getLevel())
            .build())
        .toList();
  }

  public CharacterSummaryResponse toSummaryResponse(DndCharacter character) {
    if (character == null) {
      return null;
    }

    String classDisplay = Hibernate.isInitialized(character.getClasses())
        ? character.getClasses().stream()
        .sorted(Comparator.comparing(DndCharacterClassLevel::getLevel).reversed())
        .map(c -> c.getDndClass().getName() + " " + c.getLevel())
        .collect(Collectors.joining(" / "))
        : null;

    int totalLevel = Hibernate.isInitialized(character.getClasses())
        ? character.getClasses().stream().mapToInt(DndCharacterClassLevel::getLevel).sum()
        : 0;

    return CharacterSummaryResponse.builder()
        .id(character.getId())
        .name(character.getName())
        .raceKey(character.getRace() != null ? character.getRace().getKey() : null)
        .classDisplay(classDisplay)
        .totalLevel(totalLevel)
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

    Race race = raceRepository.findByKey(request.getRaceKey())
        .orElseThrow(() -> new EntityNotFoundException(
            "Race not found with key: " + request.getRaceKey()));

    DndClass dndClass = dndClassRepository.findByKey(request.getClassKey())
        .orElseThrow(() -> new EntityNotFoundException(
            "Class not found with key: " + request.getClassKey()));

    Background background = backgroundRepository.findByKey(request.getBackgroundKey())
        .orElseThrow(() -> new EntityNotFoundException(
            "Background not found with key: " + request.getBackgroundKey()));

    DndCharacter character = DndCharacter.builder()
        .name(request.getName())
        .race(race)
        .background(background)
        .alignment(request.getAlignment())
        .portraitUrl(request.getPortraitUrl())
        .build();

    DndCharacterClassLevel characterClass = DndCharacterClassLevel.builder()
        .dndClass(dndClass)
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
      character.setSpellcastingAbility(AbilityType.valueOf(request.getSpellcastingAbility()));
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
    updateIfPresent(request.getArmorClassBonus(), character::setArmorClassBonus);
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

    if (request.getCurrency() != null) {
      character.setCurrency(mapCurrencyResponse(request.getCurrency()));
    }

    if (request.getBackgroundKey() != null) {
      Background background = backgroundRepository.findByKey(request.getBackgroundKey())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Background not found with key: " + request.getBackgroundKey()));
      character.setBackground(background);
    }

    if (request.getDndClassLevels() != null) {
      Map<String, DndClassLevelDto> desired = request.getDndClassLevels().stream()
          .collect(Collectors.toMap(DndClassLevelDto::getClassKey, dto -> dto));

      character.getClasses().removeIf(existing ->
          !desired.containsKey(existing.getDndClass().getKey())
      );

      for (DndCharacterClassLevel existing : character.getClasses()) {
        DndClassLevelDto dto = desired.remove(existing.getDndClass().getKey());
        if (dto != null) {
          existing.setLevel(dto.getLevel());
        }
      }

      for (DndClassLevelDto dto : desired.values()) {
        DndClass dndClass = dndClassRepository.findByKey(dto.getClassKey())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Class not found by key: " + dto.getClassKey()));
        character.addClass(
            DndCharacterClassLevel.builder()
                .character(character)
                .dndClass(dndClass)
                .level(dto.getLevel())
                .build()
        );
      }
    }

    if (request.getRaceKey() != null) {
      Race race = raceRepository.findByKey(request.getRaceKey())
          .orElseThrow(() -> new EntityNotFoundException(
              "Race not found with key: " + request.getRaceKey()));
      character.setRace(race);
    }

    if (request.getAbilityScores() != null) {
      character.setAbilityScores(mapAbilityScoresRequest(request.getAbilityScores()));
    }

    if (request.getSpellcastingAbility() != null) {
      character.setSpellcastingAbility(AbilityType.valueOf(request.getSpellcastingAbility()));
    }
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
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

  private DndCurrencyResponse mapCurrency(DndCurrency currency) {
    if (currency == null) {
      return null;
    }
    return DndCurrencyResponse.builder()
        .copper(currency.getCopper())
        .silver(currency.getSilver())
        .electrum(currency.getElectrum())
        .gold(currency.getGold())
        .platinum(currency.getPlatinum())
        .build();
  }

  private DndCurrency mapCurrencyResponse(DndCurrencyResponse dto) {
    if (dto == null) {
      return null;
    }

    return DndCurrency.builder()
        .copper(dto.getCopper())
        .silver(dto.getSilver())
        .electrum(dto.getElectrum())
        .gold(dto.getGold())
        .platinum(dto.getPlatinum())
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
