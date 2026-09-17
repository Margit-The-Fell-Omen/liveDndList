package dev.ushki.livedndlist.service.features;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.ushki.livedndlist.entity.dndCharacter.feature.CharacterFeature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import dev.ushki.livedndlist.exceptions.BadRequestException;
import dev.ushki.livedndlist.repository.DndFeatRepository;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChoiceValidatorTest {

  private final ObjectMapper mapper = new ObjectMapper();
  private ChoiceValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ChoiceValidator(mock(DndFeatRepository.class));
  }

  private FeatureChoice skillChoice(String choiceKey, ObjectNode filter) {
    return FeatureChoice.builder()
        .choiceKey(choiceKey)
        .chooseCount(1)
        .optionsSource(ChoiceOptionsSource.SKILL_LIST)
        .optionsFilter(filter)
        .build();
  }

  @Test
  @DisplayName("Proficiency choice rejects already-proficient skills")
  void proficiencyExcludesAlreadyProficient() {
    FeatureChoice choice = skillChoice("prof", mapper.createObjectNode());
    CharacterFeature cf = CharacterFeature.builder().build();
    ComputedCharacterState state = ComputedCharacterState.builder().build();
    state.setSkillProficiencies(Set.of("INSIGHT"));
    state.setSkillExpertise(Set.of());

    ArrayNode alreadyProficient = mapper.createArrayNode().add("INSIGHT");

    assertThatThrownBy(() -> validator.validate(choice, alreadyProficient, cf, state))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid selections");
  }

  @Test
  @DisplayName("Expertise choice rejects non-proficient and already-expertise skills")
  void expertiseOnlyAllowsProficientWithoutExpertise() {
    ObjectNode filter = mapper.createObjectNode();
    filter.put("onlyProficient", true);
    FeatureChoice choice = skillChoice("exp", filter);
    CharacterFeature cf = CharacterFeature.builder().build();
    ComputedCharacterState state = ComputedCharacterState.builder().build();
    state.setSkillProficiencies(Set.of("INSIGHT", "STEALTH"));
    state.setSkillExpertise(Set.of("STEALTH"));

    ArrayNode notProficient = mapper.createArrayNode().add("ATHLETICS");
    ArrayNode alreadyExpertise = mapper.createArrayNode().add("STEALTH");
    ArrayNode valid = mapper.createArrayNode().add("INSIGHT");

    assertThatThrownBy(() -> validator.validate(choice, notProficient, cf, state))
        .isInstanceOf(BadRequestException.class);
    assertThatThrownBy(() -> validator.validate(choice, alreadyExpertise, cf, state))
        .isInstanceOf(BadRequestException.class);
    assertThatNoException().isThrownBy(() -> validator.validate(choice, valid, cf, state));
  }

  @Test
  @DisplayName("Skill choice rejects a skill outside its fromList constraint")
  void skillChoiceRejectsSkillOutsideFromList() {
    ObjectNode filter = mapper.createObjectNode();
    filter.putArray("fromList").add("ACROBATICS").add("STEALTH");
    FeatureChoice choice = skillChoice("pick", filter);
    CharacterFeature cf = CharacterFeature.builder().build();
    ComputedCharacterState state = ComputedCharacterState.builder().build();
    state.setSkillProficiencies(Set.of());
    state.setSkillExpertise(Set.of());

    ArrayNode valid = mapper.createArrayNode().add("ACROBATICS");
    ArrayNode invalid = mapper.createArrayNode().add("ATHLETICS");

    assertThatNoException().isThrownBy(() -> validator.validate(choice, valid, cf, state));
    assertThatThrownBy(() -> validator.validate(choice, invalid, cf, state))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid selections");
  }
}