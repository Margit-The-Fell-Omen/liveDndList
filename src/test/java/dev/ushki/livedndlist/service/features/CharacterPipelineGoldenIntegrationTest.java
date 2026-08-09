package dev.ushki.livedndlist.service.features;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.ushki.livedndlist.AbstractIntegrationTest;
import dev.ushki.livedndlist.entity.User;
import dev.ushki.livedndlist.entity.dndCharacter.AbilityScores;
import dev.ushki.livedndlist.entity.dndCharacter.DndCharacter;
import dev.ushki.livedndlist.entity.dndCharacter.Equipment;
import dev.ushki.livedndlist.entity.dndCharacter.background.Background;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndCharacterClassLevel;
import dev.ushki.livedndlist.entity.dndCharacter.dndClass.DndClass;
import dev.ushki.livedndlist.entity.dndCharacter.feature.Feature;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureChoice;
import dev.ushki.livedndlist.entity.dndCharacter.feature.FeatureEffect;
import dev.ushki.livedndlist.entity.dndCharacter.race.Race;
import dev.ushki.livedndlist.enums.ArmorCategory;
import dev.ushki.livedndlist.enums.CharacterAlignment;
import dev.ushki.livedndlist.enums.ChoiceOptionsSource;
import dev.ushki.livedndlist.enums.CreatureSize;
import dev.ushki.livedndlist.enums.CreatureType;
import dev.ushki.livedndlist.enums.EquipmentType;
import dev.ushki.livedndlist.enums.FeatureEffectType;
import dev.ushki.livedndlist.enums.FeatureSourceType;
import dev.ushki.livedndlist.repository.BackgroundRepository;
import dev.ushki.livedndlist.repository.CharacterFeatureRepository;
import dev.ushki.livedndlist.repository.CharacterRepository;
import dev.ushki.livedndlist.repository.DndClassRepository;
import dev.ushki.livedndlist.repository.FeatureRepository;
import dev.ushki.livedndlist.repository.RaceRepository;
import dev.ushki.livedndlist.repository.UserRepository;
import dev.ushki.livedndlist.service.features.pipeline.ComputedCharacterState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayName("Character Pipeline — L3 Human Fighter / Acolyte golden fixture")
class CharacterPipelineGoldenIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  UserRepository userRepository;
  @Autowired
  RaceRepository raceRepository;
  @Autowired
  DndClassRepository classRepository;
  @Autowired
  BackgroundRepository backgroundRepository;
  @Autowired
  FeatureRepository featureRepository;
  @Autowired
  CharacterRepository characterRepository;
  @Autowired
  CharacterFeatureRepository characterFeatureRepository;
  @Autowired
  CharacterFeatureMaterializer materializer;
  @Autowired
  CharacterPipelineService pipelineService;
  @Autowired
  CharacterChoiceService choiceService;
  @Autowired
  FeatureCatalogService featureCatalogService;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  TransactionTemplate transactionTemplate;

  @PersistenceContext
  EntityManager em;

  Long characterId;

  @BeforeEach
  void setUp() {
    transactionTemplate.executeWithoutResult(status -> truncateAll());
    featureCatalogService.invalidateCache();

    characterId = transactionTemplate.execute(status -> seedDataAndCreateCharacter());

    featureCatalogService.invalidateCache();
    transactionTemplate.executeWithoutResult(status -> materializer.syncFeatures(characterId));
  }

  private Long seedDataAndCreateCharacter() {
    User user = userRepository.save(User.builder()
        .username("testuser")
        .email("test@example.com")
        .password("hash")
        .enabled(true)
        .build());

    Race human = raceRepository.save(buildHumanRace());
    Background acolyte = backgroundRepository.save(buildAcolyteBackground());
    DndClass fighter = classRepository.save(buildFighterClass());

    seedHumanFeatures(human);
    seedAcolyteFeatures(acolyte);
    seedFighterFeatures(fighter);

    DndCharacter character = DndCharacter.builder()
        .name("Test Fighter")
        .owner(user)
        .race(human)
        .background(acolyte)
        .alignment(CharacterAlignment.LAWFUL_GOOD)
        .experiencePoints(900)
        .maxHitPoints(28)
        .currentHitPoints(28)
        .abilityScores(AbilityScores.builder()
            .strength(15)
            .dexterity(13)
            .constitution(14)
            .intelligence(10)
            .wisdom(12)
            .charisma(8)
            .build())
        .armorClassBonus(0)
        .classes(new HashSet<>())
        .build();

    DndCharacterClassLevel classLevel = DndCharacterClassLevel.builder()
        .dndClass(fighter)
        .level(3)
        .build();
    character.addClass(classLevel);

    character = characterRepository.save(character);
    return characterId = character.getId();
  }

  @Test
  @DisplayName("Pipeline produces correct level, prof bonus, and ability scores")
  void levelAndAbilities() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getTotalLevel()).isEqualTo(3);
    assertThat(state.getProficiencyBonus()).isEqualTo(2);

    assertThat(state.getFinalAbilityScores().get("STR")).isEqualTo(16);
    assertThat(state.getFinalAbilityScores().get("DEX")).isEqualTo(14);
    assertThat(state.getFinalAbilityScores().get("CON")).isEqualTo(15);
    assertThat(state.getFinalAbilityScores().get("INT")).isEqualTo(11);
    assertThat(state.getFinalAbilityScores().get("WIS")).isEqualTo(13);
    assertThat(state.getFinalAbilityScores().get("CHA")).isEqualTo(9);

    assertThat(state.getAbilityModifiers().get("STR")).isEqualTo(3);
    assertThat(state.getAbilityModifiers().get("DEX")).isEqualTo(2);
    assertThat(state.getAbilityModifiers().get("CON")).isEqualTo(2);
    assertThat(state.getAbilityModifiers().get("INT")).isEqualTo(0);
    assertThat(state.getAbilityModifiers().get("WIS")).isEqualTo(1);
    assertThat(state.getAbilityModifiers().get("CHA")).isEqualTo(-1);
  }

  @Test
  @DisplayName("Race grants size, speed, and language proficiencies")
  void raceEffects() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getSize()).isEqualTo(CreatureSize.MEDIUM);
    assertThat(state.getCreatureType()).isEqualTo(CreatureType.HUMANOID);
    assertThat(state.getSpeeds().get("WALK")).isEqualTo(30);
    assertThat(state.getLanguages()).contains("common");
  }

  @Test
  @DisplayName("Background grants fixed skill and language proficiencies")
  void backgroundEffects() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getSkillProficiencies())
        .contains("INSIGHT", "RELIGION");
    assertThat(state.getLanguages())
        .contains("common", "celestial");
  }

  @Test
  @DisplayName("Class starting features grant armor, weapons, saves, and hit die")
  void classStartingProficiencies() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getArmorProficiencies())
        .contains("LIGHT", "MEDIUM", "HEAVY", "SHIELD");
    assertThat(state.getWeaponProficiencies())
        .contains("SIMPLE", "MARTIAL");
    assertThat(state.getSavingThrowProficiencies())
        .contains("STR", "CON");
    assertThat(state.getHitDice())
        .anySatisfy((k, v) -> assertThat(v.getDie()).isEqualTo("d10"));
  }

  @Test
  @DisplayName("Skill and save totals include correct modifiers and proficiency bonus")
  void skillAndSaveTotals() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getSkillTotals().get("INSIGHT")).isEqualTo(3);
    assertThat(state.getSkillTotals().get("RELIGION")).isEqualTo(2);
    assertThat(state.getSkillTotals().get("ATHLETICS")).isEqualTo(3);

    assertThat(state.getSaveTotals().get("STR")).isEqualTo(5);
    assertThat(state.getSaveTotals().get("CON")).isEqualTo(4);
    assertThat(state.getSaveTotals().get("DEX")).isEqualTo(2);
  }

  @Test
  @DisplayName("Fighter L1 grants Second Wind resource with correct max uses")
  void secondWindResource() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getResources())
        .anySatisfy(r -> {
          assertThat(r.getResourceKey()).isEqualTo("second_wind");
          assertThat(r.getMaxUses()).isEqualTo(1);
          assertThat(r.getRefreshOn()).isEqualTo("SHORT");
        });
  }

  @Test
  @DisplayName("Only class features up to current class level are applied")
  void classFeatureLevelFiltering() {
    ComputedCharacterState state = pipelineService.compute(characterId);
    assertThat(state.getDisplayFeatures())
        .extracting(ComputedCharacterState.DisplayFeature::getName)
        .contains("Fighting Style", "Second Wind", "Action Surge");

    assertThat(state.getDisplayFeatures())
        .extracting(ComputedCharacterState.DisplayFeature::getName)
        .doesNotContain("Extra Attack");
  }

  @Test
  @DisplayName("Unanswered choice produces pendingChoice; no derived effect applied yet")
  void unansweredChoicePending() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getPendingChoices())
        .anySatisfy(pc -> {
          assertThat(pc.getChoiceKey()).isEqualTo("fighter_skill_pick");
          assertThat(pc.getChooseCount()).isEqualTo(2);
        });

    assertThat(state.getSkillProficiencies())
        .doesNotContain("ATHLETICS", "PERCEPTION");
  }

  @Test
  @DisplayName("Answered choice removes from pending and applies effect")
  void answeredChoiceApplied() {
    ComputedCharacterState before = pipelineService.compute(characterId);
    var pending = before.getPendingChoices().stream()
        .filter(pc -> pc.getChoiceKey().equals("fighter_skill_pick"))
        .findFirst().orElseThrow();

    ArrayNode selection = objectMapper.createArrayNode();
    selection.add("ATHLETICS");
    selection.add("PERCEPTION");

    choiceService.submitChoice(characterId, pending.getCharacterFeatureId(),
        "fighter_skill_pick", selection);

    ComputedCharacterState after = pipelineService.compute(characterId);

    assertThat(after.getPendingChoices())
        .noneMatch(pc -> pc.getChoiceKey().equals("fighter_skill_pick"));
    assertThat(after.getSkillProficiencies())
        .contains("ATHLETICS", "PERCEPTION");
    assertThat(after.getSkillTotals().get("ATHLETICS")).isEqualTo(5);
    assertThat(after.getSkillTotals().get("PERCEPTION")).isEqualTo(3);
  }

  @Test
  @DisplayName("Equipped light armor and shield yield correct AC")
  @Transactional
  void equipmentArmorClass() {
    DndCharacter character = characterRepository.findById(characterId).orElseThrow();

    Equipment leather = Equipment.builder()
        .character(character)
        .name("Leather Armor")
        .type(EquipmentType.ARMOR)
        .armorCategory(ArmorCategory.LIGHT)
        .armorClass(11)
        .equipped(true)
        .quantity(1)
        .build();

    Equipment shield = Equipment.builder()
        .character(character)
        .name("Shield")
        .type(EquipmentType.ARMOR)
        .armorCategory(ArmorCategory.SHIELD)
        .armorClass(0)
        .equipped(true)
        .quantity(1)
        .build();

    character.getEquipment().add(leather);
    character.getEquipment().add(shield);
    characterRepository.save(character);

    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getArmorClass()).isEqualTo(15);
  }

  @Test
  @DisplayName("Initiative equals Dex modifier without initiative-modifying effects")
  void initiative() {
    ComputedCharacterState state = pipelineService.compute(characterId);
    assertThat(state.getInitiative()).isEqualTo(2);
  }

  @Test
  @DisplayName("Base AC = 10 + Dex without armor or unarmored defense formulas")
  void baseArmorClass() {
    ComputedCharacterState state = pipelineService.compute(characterId);
    assertThat(state.getArmorClass()).isEqualTo(12);
  }

  @Test
  @DisplayName("Features include source labels grouped by origin")
  void featureDisplayGrouping() {
    ComputedCharacterState state = pipelineService.compute(characterId);

    assertThat(state.getDisplayFeatures())
        .anySatisfy(f -> assertThat(f.getSource()).isEqualTo("RACE"))
        .anySatisfy(f -> assertThat(f.getSource()).isEqualTo("BACKGROUND"))
        .anySatisfy(f -> assertThat(f.getSource()).isEqualTo("CLASS"));
  }

  @Test
  @DisplayName("Re-materialization preserves user's answered choices")
  void rematerializationPreservesChoices() {
    ComputedCharacterState before = pipelineService.compute(characterId);
    var pending = before.getPendingChoices().stream()
        .filter(pc -> pc.getChoiceKey().equals("fighter_skill_pick"))
        .findFirst().orElseThrow();

    ArrayNode selection = objectMapper.createArrayNode();
    selection.add("ATHLETICS");
    selection.add("PERCEPTION");
    choiceService.submitChoice(characterId, pending.getCharacterFeatureId(),
        "fighter_skill_pick", selection);

    materializer.syncFeatures(characterId);

    ComputedCharacterState after = pipelineService.compute(characterId);
    assertThat(after.getSkillProficiencies()).contains("ATHLETICS", "PERCEPTION");
    assertThat(after.getPendingChoices())
        .noneMatch(pc -> pc.getChoiceKey().equals("fighter_skill_pick"));
  }

  // ─────────────────────────────────────────────────────────────
  // Fixture builders
  // ─────────────────────────────────────────────────────────────

  private Race buildHumanRace() {
    return Race.builder()
        .key("srd_human")
        .name("Human")
        .description("Adaptable and ambitious.")
        .subspecies(false)
        .build();
  }

  private Background buildAcolyteBackground() {
    return Background.builder()
        .key("srd_acolyte")
        .name("Acolyte")
        .desc("Servant of a temple.")
        .build();
  }

  private DndClass buildFighterClass() {
    return DndClass.builder()
        .key("srd_fighter")
        .name("Fighter")
        .description("Martial expert.")
        .build();
  }

  private void seedHumanFeatures(Race race) {
    Feature ability = Feature.builder()
        .key("race_srd_human_ability")
        .name("Ability Score Increase")
        .description("+1 to all ability scores.")
        .sourceType(FeatureSourceType.RACE)
        .sourceKey(race.getKey())
        .build();
    for (String ab : List.of("STR", "DEX", "CON", "INT", "WIS", "CHA")) {
      ability.addEffect(FeatureEffect.builder()
          .effectType(FeatureEffectType.MODIFY_ABILITY_SCORE)
          .payload(payload("ability", ab, "amount", 1))
          .build());
    }
    featureRepository.save(ability);

    Feature size = Feature.builder()
        .key("race_srd_human_size")
        .name("Size")
        .description("Medium.")
        .sourceType(FeatureSourceType.RACE)
        .sourceKey(race.getKey())
        .build();
    size.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.SET_CREATURE_SIZE)
        .payload(payload("size", "MEDIUM"))
        .build());
    size.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.SET_CREATURE_TYPE)
        .payload(payload("type", "HUMANOID"))
        .build());
    featureRepository.save(size);

    Feature speed = Feature.builder()
        .key("race_srd_human_speed")
        .name("Speed")
        .description("30 ft walking.")
        .sourceType(FeatureSourceType.RACE)
        .sourceKey(race.getKey())
        .build();
    featureRepository.save(speed);

    Feature language = Feature.builder()
        .key("race_srd_human_language")
        .name("Languages")
        .description("You know Common.")
        .sourceType(FeatureSourceType.RACE)
        .sourceKey(race.getKey())
        .build();
    language.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_LANGUAGE)
        .payload(payload("languageKey", "common"))
        .build());
    featureRepository.save(language);
  }

  private void seedAcolyteFeatures(Background bg) {
    Feature proficiencies = Feature.builder()
        .key("bg_srd_acolyte_proficiencies")
        .name("Acolyte Proficiencies")
        .description("Insight and Religion.")
        .sourceType(FeatureSourceType.BACKGROUND)
        .sourceKey(bg.getKey())
        .build();
    proficiencies.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_SKILL_PROFICIENCY)
        .payload(payload("skill", "INSIGHT"))
        .build());
    proficiencies.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_SKILL_PROFICIENCY)
        .payload(payload("skill", "RELIGION"))
        .build());
    proficiencies.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_LANGUAGE)
        .payload(payload("languageKey", "celestial"))
        .build());
    featureRepository.save(proficiencies);
  }

  private void seedFighterFeatures(DndClass fighter) {
    Feature starting = Feature.builder()
        .key("cls_srd_fighter_starting")
        .name("Fighter Class Features")
        .description("Armor, weapons, and saves.")
        .sourceType(FeatureSourceType.CLASS)
        .sourceKey(fighter.getKey())
        .gainedAtLevel(1)
        .build();
    starting.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.SET_HIT_DIE)
        .payload(payload("die", "d10"))
        .build());
    for (String cat : List.of("LIGHT", "MEDIUM", "HEAVY", "SHIELD")) {
      starting.addEffect(FeatureEffect.builder()
          .effectType(FeatureEffectType.GRANT_ARMOR_PROFICIENCY)
          .payload(payload("category", cat))
          .build());
    }
    for (String cat : List.of("SIMPLE", "MARTIAL")) {
      ObjectNode wpn = objectMapper.createObjectNode();
      wpn.put("scope", "CATEGORY");
      wpn.put("category", cat);
      starting.addEffect(FeatureEffect.builder()
          .effectType(FeatureEffectType.GRANT_WEAPON_PROFICIENCY)
          .payload(wpn)
          .build());
    }
    for (String ab : List.of("STR", "CON")) {
      starting.addEffect(FeatureEffect.builder()
          .effectType(FeatureEffectType.GRANT_SAVING_THROW_PROFICIENCY)
          .payload(payload("ability", ab))
          .build());
    }
    starting.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_SKILL_PROFICIENCY)
        .payload(objectMapper.createObjectNode())
        .choiceKey("fighter_skill_pick")
        .build());
    starting.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_SKILL_PROFICIENCY)
        .payload(objectMapper.createObjectNode())
        .choiceKey("fighter_skill_pick")
        .build());
    ObjectNode filter = objectMapper.createObjectNode();
    ArrayNode fromList = filter.putArray("fromList");
    List.of("ACROBATICS", "ANIMAL_HANDLING", "ATHLETICS", "HISTORY",
            "INSIGHT", "INTIMIDATION", "PERCEPTION", "SURVIVAL")
        .forEach(fromList::add);
    starting.addChoice(FeatureChoice.builder()
        .choiceKey("fighter_skill_pick")
        .name("Skill Proficiencies")
        .description("Choose two.")
        .chooseCount(2)
        .optionsSource(ChoiceOptionsSource.SKILL_LIST)
        .optionsFilter(filter)
        .build());
    featureRepository.save(starting);

    Feature secondWind = Feature.builder()
        .key("cls_srd_fighter_second_wind")
        .name("Second Wind")
        .description("Regain HP as bonus action.")
        .sourceType(FeatureSourceType.CLASS)
        .sourceKey(fighter.getKey())
        .gainedAtLevel(1)
        .build();
    ObjectNode swPayload = objectMapper.createObjectNode();
    swPayload.put("resourceKey", "second_wind");
    swPayload.put("displayName", "Second Wind");
    swPayload.put("refresh", "SHORT");
    ObjectNode formula = swPayload.putObject("usesFormula");
    ObjectNode perLevel = formula.putObject("perClassLevel");
    perLevel.put("1", 1);
    perLevel.put("6", 2);
    perLevel.put("14", 3);
    secondWind.addEffect(FeatureEffect.builder()
        .effectType(FeatureEffectType.GRANT_RESOURCE)
        .payload(swPayload)
        .build());
    featureRepository.save(secondWind);

    Feature fightingStyle = Feature.builder()
        .key("cls_srd_fighter_fighting_style")
        .name("Fighting Style")
        .description("Adopt a particular style of fighting.")
        .sourceType(FeatureSourceType.CLASS)
        .sourceKey(fighter.getKey())
        .gainedAtLevel(2)
        .build();
    featureRepository.save(fightingStyle);

    Feature actionSurge = Feature.builder()
        .key("cls_srd_fighter_action_surge")
        .name("Action Surge")
        .description("Take an additional action.")
        .sourceType(FeatureSourceType.CLASS)
        .sourceKey(fighter.getKey())
        .gainedAtLevel(2)
        .build();
    featureRepository.save(actionSurge);

    Feature extraAttack = Feature.builder()
        .key("cls_srd_fighter_extra_attack")
        .name("Extra Attack")
        .description("Attack twice.")
        .sourceType(FeatureSourceType.CLASS)
        .sourceKey(fighter.getKey())
        .gainedAtLevel(5)
        .build();
    featureRepository.save(extraAttack);
  }

  private ObjectNode payload(String key, Object value) {
    ObjectNode node = objectMapper.createObjectNode();
    putAny(node, key, value);
    return node;
  }

  private ObjectNode payload(String k1, Object v1, String k2, Object v2) {
    ObjectNode node = objectMapper.createObjectNode();
    putAny(node, k1, v1);
    putAny(node, k2, v2);
    return node;
  }

  private void putAny(ObjectNode node, String key, Object value) {
    switch (value) {
      case Integer i -> node.put(key, i);
      case Boolean b -> node.put(key, b);
      case String s -> node.put(key, s);
      case null -> node.putNull(key);
      default -> node.put(key, value.toString());
    }
  }

  private void truncateAll() {
    em.createNativeQuery("""
        TRUNCATE TABLE
          character_feature_choices,
          character_features,
          character_custom_features,
          character_feats,
          character_resources,
          character_subclass_choices,
          feature_effects,
          feature_choices,
          features,
          character_saving_throws,
          characters,
          dnd_classes,
          races,
          backgrounds,
          users
        RESTART IDENTITY CASCADE
        """).executeUpdate();
  }

}
