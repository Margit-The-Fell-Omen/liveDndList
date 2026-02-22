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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Character entities and DTOs. Handles mapping for character
 * creation, updates, and responses.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Converting entities to full response DTOs</li>
 *   <li>Converting entities to summary response DTOs</li>
 *   <li>Converting creation requests to entities</li>
 *   <li>Updating existing entities from update requests</li>
 *   <li>Calculating skill bonuses based on ability scores and proficiencies</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CharacterMapper {

  private final SpellMapper spellMapper;
  private final EquipmentMapper equipmentMapper;

  /**
   * Converts a DndCharacter entity to a full CharacterResponse DTO. Includes all character details
   * including equipment, spells, and skills.
   *
   * @param character the character entity to convert
   * @return the character response DTO, or null if character is null
   */
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

  /**
   * Converts a DndCharacter entity to a CharacterSummaryResponse DTO. Includes only essential
   * information for list views.
   *
   * @param character the character entity to convert
   * @return the character summary DTO, or null if character is null
   */
  public CharacterSummaryResponse toSummaryResponse(DndCharacter character) {
    if (character == null) {
      return null;
    }

    String classDisplay = character.getClasses().stream()
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

  /**
   * Converts a list of DndCharacter entities to CharacterSummaryResponse DTOs.
   *
   * @param characters the list of character entities
   * @return list of character summary DTOs
   */
  public List<CharacterSummaryResponse> toSummaryResponseList(List<DndCharacter> characters) {
    return characters.stream()
        .map(this::toSummaryResponse)
        .toList();
  }

  /**
   * Converts a CharacterCreateRequest to a new DndCharacter entity. Initializes all necessary
   * fields including starting class, ability scores, and skills.
   *
   * @param request the character creation request
   * @return the new character entity, or null if request is null
   */
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

  /**
   * Updates an existing DndCharacter entity from a CharacterUpdateRequest. Only updates fields that
   * are present (non-null) in the request.
   *
   * @param character the character entity to update
   * @param request   the update request containing new values
   */
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

  /**
   * Maps character class entities to response DTOs.
   *
   * @param classes the list of character class entities
   * @return list of character class response DTOs
   */
  private List<CharacterResponse.CharacterClassResponse> mapClasses(
      List<CharacterClass> classes) {
    return classes.stream()
        .map(c -> CharacterResponse.CharacterClassResponse.builder()
            .id(c.getId())
            .className(c.getClassName())
            .subclass(c.getSubClass())
            .level(c.getLevel())
            .build())
        .toList();
  }

  /**
   * Maps ability scores entity to response DTO. Calculates and includes modifiers for each ability
   * score.
   *
   * @param scores the ability scores entity
   * @return the ability scores response DTO, or null if scores is null
   */
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

  /**
   * Maps ability scores request DTO to entity.
   *
   * @param request the ability scores request
   * @return the ability scores entity
   */
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
   * Maps skill entities to response DTOs. Calculates total bonuses including ability modifiers,
   * proficiency, and expertise.
   *
   * <p>Bonus calculation:
   * <ul>
   *   <li>Base: ability modifier</li>
   *   <li>Proficient: + proficiency bonus</li>
   *   <li>Expertise: + proficiency bonus × 2 (replaces proficiency bonus)</li>
   * </ul>
   *
   * @param skills           the list of skill entities
   * @param abilityScores    the character's ability scores
   * @param proficiencyBonus the character's proficiency bonus
   * @return list of skill response DTOs with calculated bonuses
   */
  private List<SkillResponse> mapSkills(List<Skill> skills, AbilityScores abilityScores,
      int proficiencyBonus) {
    return skills.stream()
        .map(skill -> {
          AbilityType baseAbility = skill.getSkillType().getBaseAbility();
          int abilityMod = abilityScores.getModifier(baseAbility);
          int totalBonus = abilityMod;

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

  /**
   * Maps currency entity to response DTO.
   *
   * @param currency the currency entity
   * @return the currency response DTO, or null if currency is null
   */
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
   * Initializes all 18 skills for a new character. All skills start with no proficiency or
   * expertise.
   *
   * @param character the character to initialize skills for
   */
  private void initializeSkills(DndCharacter character) {
    List<Skill> skills = new ArrayList<>();
    for (SkillType skillType : SkillType.values()) {
      skills.add(Skill.builder()
          .skillType(skillType)
          .proficiency(false)
          .expertise(false)
          .build());
    }
    character.setSkills(skills);
  }
}
