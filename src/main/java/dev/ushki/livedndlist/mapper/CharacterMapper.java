package dev.ushki.livedndlist.mapper;

import dev.ushki.livedndlist.dto.request.AbilityScoresRequest;
import dev.ushki.livedndlist.dto.request.CharacterCreateRequest;
import dev.ushki.livedndlist.dto.request.CharacterUpdateRequest;
import dev.ushki.livedndlist.dto.response.AbilityScoresResponse;
import dev.ushki.livedndlist.dto.response.CharacterResponse;
import dev.ushki.livedndlist.dto.response.CharacterSummaryResponse;
import dev.ushki.livedndlist.dto.response.SkillResponse;
import dev.ushki.livedndlist.entity.character.AbilityScores;
import dev.ushki.livedndlist.entity.character.CharacterClass;
import dev.ushki.livedndlist.entity.character.DndCharacter;
import dev.ushki.livedndlist.entity.character.DndCurrency;
import dev.ushki.livedndlist.entity.character.Skill;
import dev.ushki.livedndlist.enums.AbilityType;
import dev.ushki.livedndlist.enums.SkillType;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CharacterMapper {

  private final SpellMapper spellMapper;
  private final EquipmentMapper equipmentMapper;

  public CharacterResponse toResponse(DndCharacter character) {
    if (character == null) {
      return null;
    }

    return CharacterResponse.builder()
        .id(character.getId())
        .name(character.getName())
        .race(character.getRace())
        .subrace(character.getSubrace())
        .alignment(character.getAlignment())
        .background(character.getBackground())
        .experiencePoints(character.getExperiencePoints())
        .portraitUrl(character.getPortraitUrl())
        .classes(mapClasses(character.getClasses()))
        .totalLevel(character.getTotalLevel())
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
        .skills(mapSkills(character.getSkills(), character.getAbilityScores(),
            character.getProficiencyBonus()))
        .savingThrowProficiencies(character.getSavingThrowProficiencies())
        .equipment(equipmentMapper.toResponseList(character.getEquipment()))
        .currency(mapCurrency(character.getCurrency()))
        .spells(spellMapper.toResponseSet(character.getSpells()))
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

    String classDisplay = character.getClasses().stream()
        .sorted(Comparator.comparing(CharacterClass::getLevel).reversed())
        .map(c -> c.getClassName() + " " + c.getLevel())
        .collect(Collectors.joining(" / "));

    return CharacterSummaryResponse.builder()
        .id(character.getId())
        .name(character.getName())
        .race(character.getRace())
        .classDisplay(classDisplay)
        .totalLevel(character.getTotalLevel())
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

    DndCharacter character = DndCharacter.builder()
        .name(request.getName())
        .race(request.getRace())
        .subrace(request.getSubrace())
        .alignment(request.getAlignment())
        .background(request.getBackground())
        .portraitUrl(request.getPortraitUrl())
        .build();

    // Add initial class at level 1
    CharacterClass characterClass = CharacterClass.builder()
        .className(request.getClassName())
        .subClass(request.getSubclass())
        .level(1)
        .build();
    character.getClasses().add(characterClass);

    // Set ability scores (or use defaults if not provided)
    if (request.getAbilityScores() != null) {
      character.setAbilityScores(mapAbilityScoresRequest(request.getAbilityScores()));
    }

    // Set hit points
    if (request.getMaxHitPoints() != null) {
      character.setMaxHitPoints(request.getMaxHitPoints());
      character.setCurrentHitPoints(request.getMaxHitPoints());
    }

    // Initialize all 18 skills with no proficiencies
    initializeSkills(character);

    return character;
  }

  public void updateEntity(DndCharacter character, CharacterUpdateRequest request) {
    updateIfPresent(request.getName(), character::setName);
    updateIfPresent(request.getRace(), character::setRace);
    updateIfPresent(request.getSubrace(), character::setSubrace);
    updateIfPresent(request.getAlignment(), character::setAlignment);
    updateIfPresent(request.getBackground(), character::setBackground);
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

    // Ability scores require mapping transformation
    updateIfPresent(request.getAbilityScores(),
        scores -> character.setAbilityScores(mapAbilityScoresRequest(scores)));
  }

  private <T> void updateIfPresent(T value, Consumer<T> setter) {
    Optional.ofNullable(value).ifPresent(setter);
  }

  /**
   * Maps Set of CharacterClass to List of CharacterClassResponse. Sorted by level descending
   * (highest level class first).
   */
  private List<CharacterResponse.CharacterClassResponse> mapClasses(
      Set<CharacterClass> classes) {
    if (classes == null) {
      return List.of();
    }

    return classes.stream()
        .sorted(Comparator.comparing(CharacterClass::getLevel).reversed()
            .thenComparing(CharacterClass::getClassName))
        .map(c -> CharacterResponse.CharacterClassResponse.builder()
            .id(c.getId())
            .className(c.getClassName())
            .subclass(c.getSubClass())
            .level(c.getLevel())
            .build())
        .toList();
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

  /**
   * Maps Set of Skills to List of SkillResponse. Sorted by skill type name for consistent
   * ordering.
   */
  private List<SkillResponse> mapSkills(Set<Skill> skills, AbilityScores abilityScores,
      int proficiencyBonus) {
    if (skills == null) {
      return List.of();
    }

    return skills.stream()
        .sorted(Comparator.comparing(s -> s.getSkillType().name()))
        .map(skill -> {
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
        })
        .toList();
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

  /**
   * Initializes all 18 D&D 5e skills with no proficiencies. Uses Set instead of List for Hibernate
   * optimization.
   */
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
