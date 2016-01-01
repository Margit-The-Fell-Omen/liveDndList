package dev.ushki.live_dnd_list.mapper;

import dev.ushki.live_dnd_list.dto.request.AbilityScoresRequest;
import dev.ushki.live_dnd_list.dto.request.CharacterCreateRequest;
import dev.ushki.live_dnd_list.dto.request.CharacterUpdateRequest;
import dev.ushki.live_dnd_list.dto.response.AbilityScoresResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterResponse;
import dev.ushki.live_dnd_list.dto.response.CharacterSummaryResponse;
import dev.ushki.live_dnd_list.dto.response.SkillResponse;
import dev.ushki.live_dnd_list.entity.character.*;
import dev.ushki.live_dnd_list.enums.AbilityType;
import dev.ushki.live_dnd_list.enums.SkillType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CharacterMapper {

    private final SpellMapper spellMapper;
    private final EquipmentMapper equipmentMapper;

    public CharacterResponse toResponse(DndCharacter character) {
        if (character == null) return null;

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
                .skills(mapSkills(character.getSkills(), character.getAbilityScores(), character.getProficiencyBonus()))
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
        if (character == null) return null;

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

    public List<CharacterSummaryResponse> toSummaryResponseList(List<DndCharacter> characters) {
        return characters.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public DndCharacter toEntity(CharacterCreateRequest request) {
        if (request == null) return null;

        DndCharacter character = DndCharacter.builder()
                .name(request.getName())
                .race(request.getRace())
                .subrace(request.getSubrace())
                .alignment(request.getAlignment())
                .background(request.getBackground())
                .portraitUrl(request.getPortraitUrl())
                .build();

        // Add initial class
        CharacterClass characterClass = CharacterClass.builder()
                .className(request.getClassName())
                .subClass(request.getSubclass())
                .level(1)
                .build();
        character.getClasses().add(characterClass);

        // Set ability scores
        if (request.getAbilityScores() != null) {
            character.setAbilityScores(mapAbilityScoresRequest(request.getAbilityScores()));
        }

        // Set hit points
        if (request.getMaxHitPoints() != null) {
            character.setMaxHitPoints(request.getMaxHitPoints());
            character.setCurrentHitPoints(request.getMaxHitPoints());
        }

        // Initialize skills
        initializeSkills(character);

        return character;
    }

    //FIXME: REFACTOR THIS SH^&%*$
    public void updateEntity(DndCharacter character, CharacterUpdateRequest request) {
        if (request.getName() != null) character.setName(request.getName());
        if (request.getRace() != null) character.setRace(request.getRace());
        if (request.getSubrace() != null) character.setSubrace(request.getSubrace());
        if (request.getAlignment() != null) character.setAlignment(request.getAlignment());
        if (request.getBackground() != null) character.setBackground(request.getBackground());
        if (request.getMaxHitPoints() != null) character.setMaxHitPoints(request.getMaxHitPoints());
        if (request.getCurrentHitPoints() != null) character.setCurrentHitPoints(request.getCurrentHitPoints());
        if (request.getTemporaryHitPoints() != null) character.setTemporaryHitPoints(request.getTemporaryHitPoints());
        if (request.getArmorClass() != null) character.setArmorClass(request.getArmorClass());
        if (request.getSpeed() != null) character.setSpeed(request.getSpeed());
        if (request.getPortraitUrl() != null) character.setPortraitUrl(request.getPortraitUrl());
        if (request.getBackstory() != null) character.setBackstory(request.getBackstory());
        if (request.getPersonalityTraits() != null) character.setPersonalityTraits(request.getPersonalityTraits());
        if (request.getIdeals() != null) character.setIdeals(request.getIdeals());
        if (request.getBonds() != null) character.setBonds(request.getBonds());
        if (request.getFlaws() != null) character.setFlaws(request.getFlaws());
        if (request.getNotes() != null) character.setNotes(request.getNotes());

        if (request.getAbilityScores() != null) {
            character.setAbilityScores(mapAbilityScoresRequest(request.getAbilityScores()));
        }
    }

    // Helper methods
    private List<CharacterResponse.CharacterClassResponse> mapClasses(List<CharacterClass> classes) {
        return classes.stream()
                .map(c -> CharacterResponse.CharacterClassResponse.builder()
                        .id(c.getId())
                        .className(c.getClassName())
                        .subclass(c.getSubClass())
                        .level(c.getLevel())
                        .build())
                .collect(Collectors.toList());
    }

    private AbilityScoresResponse mapAbilityScores(AbilityScores scores) {
        if (scores == null) return null;

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

    private List<SkillResponse> mapSkills(List<Skill> skills, AbilityScores abilityScores, int proficiencyBonus) {
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
                            .proficient(skill.isProficient())
                            .expertise(skill.isExpertise())
                            .totalBonus(totalBonus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private CharacterResponse.DndCurrencyResponse mapCurrency(DndCurrency currency) {
        if (currency == null) return null;

        return CharacterResponse.DndCurrencyResponse.builder()
                .copper(currency.getCopper())
                .silver(currency.getSilver())
                .electrum(currency.getElectrum())
                .gold(currency.getGold())
                .platinum(currency.getPlatinum())
                .build();
    }

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
