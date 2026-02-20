package dev.ushki.live_dnd_list.config;

import dev.ushki.live_dnd_list.entity.User;
import dev.ushki.live_dnd_list.entity.character.*;
import dev.ushki.live_dnd_list.enums.*;
import dev.ushki.live_dnd_list.repository.CharacterRepository;
import dev.ushki.live_dnd_list.repository.EquipmentRepository;
import dev.ushki.live_dnd_list.repository.SpellRepository;
import dev.ushki.live_dnd_list.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final SpellRepository spellRepository;
    private final CharacterRepository characterRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing test data... ");

        createSpells();

        User admin = createUser("admin", "admin@dndsheets.com", "admin123", Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        User user = createUser("player1", "player1@dndsheets.com", "player123", Set.of(Role.ROLE_USER));

        createCharacterForUser(admin, "Gandalf the Grey", CharacterRace.HUMAN, "Wizard", 10);
        createCharacterForUser(user, "Aragorn", CharacterRace.HUMAN, "Fighter", 8);
        createCharacterForUser(user, "Legolas", CharacterRace.ELF, "Fighter", 8);

        log.info("Test data initialized successfully");

        log.info("Test Users:");
        log.info(" Admin: username = admin, password = admin123");
        log.info(" User: username = player1, password = player123");
    }

    private User createUser(String username, String email, String password, Set<Role> roles) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    private void createSpells() {
        List<Spell> spells = List.of(
                Spell.builder()
                        .name("Fire Bolt")
                        .level(0)
                        .school(SpellSchool.EVOCATION)
                        .castingTime("1 action")
                        .range("120 feet")
                        .components("V, S")
                        .duration("Instantaneous")
                        .description("You hurl a mote of fire at a creature or object within range.")
                        .build(),

                Spell.builder()
                        .name("Mage Hand")
                        .level(0)
                        .school(SpellSchool.CONJURATION)
                        .castingTime("1 action")
                        .range("30 feet")
                        .components("V, S")
                        .duration("1 minute")
                        .description("A spectral, floating hand appears at a point you choose within range.")
                        .build(),

                Spell.builder()
                        .name("Prestidigitation")
                        .level(0)
                        .school(SpellSchool.TRANSMUTATION)
                        .castingTime("1 action")
                        .range("10 feet")
                        .components("V, S")
                        .duration("Up to 1 hour")
                        .description("This spell is a minor magical trick that novice spellcasters use for practice.")
                        .build(),

                Spell.builder()
                        .name("Magic Missile")
                        .level(1)
                        .school(SpellSchool.EVOCATION)
                        .castingTime("1 action")
                        .range("120 feet")
                        .components("V, S")
                        .duration("Instantaneous")
                        .description("You create three glowing darts of magical force. Each dart hits a creature.")
                        .higherLevels("The spell creates one more dart for each slot level above 1st.")
                        .build(),

                Spell.builder()
                        .name("Shield")
                        .level(1)
                        .school(SpellSchool.ABJURATION)
                        .castingTime("1 reaction")
                        .range("Self")
                        .components("V, S")
                        .duration("1 round")
                        .description("An invisible barrier of magical force appears and protects you.")
                        .build(),

                Spell.builder()
                        .name("Cure Wounds")
                        .level(1)
                        .school(SpellSchool.EVOCATION)
                        .castingTime("1 action")
                        .range("Touch")
                        .components("V, S")
                        .duration("Instantaneous")
                        .description("A creature you touch regains a number of hit points equal to 1d8 + your " +
                                "spellcasting modifier.")
                        .higherLevels("The healing increases by 1d8 for each slot level above 1st.")
                        .build(),

                Spell.builder()
                        .name("Misty Step")
                        .level(2)
                        .school(SpellSchool.CONJURATION)
                        .castingTime("1 bonus action")
                        .range("Self")
                        .components("V")
                        .duration("Instantaneous")
                        .description("Briefly surrounded by silvery mist, you teleport up to 30 feet.")
                        .build(),

                Spell.builder()
                        .name("Hold Person")
                        .level(2)
                        .school(SpellSchool.ENCHANTMENT)
                        .castingTime("1 action")
                        .range("60 feet")
                        .components("V, S, M (a small piece of iron)")
                        .duration("Concentration, up to 1 minute")
                        .concentration(true)
                        .description("Choose a humanoid that you can see within range. The target must succeed on a " +
                                "Wisdom saving throw or be paralyzed.")
                        .build(),

                Spell.builder()
                        .name("Fireball")
                        .level(3)
                        .school(SpellSchool.EVOCATION)
                        .castingTime("1 action")
                        .range("150 feet")
                        .components("V, S, M (a tiny ball of bat guano and sulfur)")
                        .duration("Instantaneous")
                        .description("A bright streak flashes from your pointing finger to a point you choose within " +
                                "range and then blossoms with a low roar into an explosion of flame.")
                        .higherLevels("The damage increases by 1d6 for each slot level above 3rd.")
                        .build(),

                Spell.builder()
                        .name("Counterspell")
                        .level(3)
                        .school(SpellSchool.ABJURATION)
                        .castingTime("1 reaction")
                        .range("60 feet")
                        .components("S")
                        .duration("Instantaneous")
                        .description("You attempt to interrupt a creature in the process of casting a spell.")
                        .build()
        );

        spellRepository.saveAll(spells);
        log.info("Created {} spells", spells.size());
    }

    private void createCharacterForUser(User owner, String name, CharacterRace race, String className, int level) {
        List<Spell> allSpells = spellRepository.findAll();
        Set<Spell> characterSpells = new HashSet<>();

        if (!allSpells.isEmpty()) {
            characterSpells.add(allSpells.get(0));
            if (allSpells.size() > 1) {
                characterSpells.add(allSpells.get(1));
            }
        }

        CharacterClass characterClass = CharacterClass.builder()
                .className(className)
                .level(level)
                .build();

        AbilityScores abilityScores = AbilityScores.builder()
                .strength(15)
                .dexterity(14)
                .constitution(13)
                .intelligence(12)
                .wisdom(10)
                .charisma(8)
                .build();

        Equipment sword = Equipment.builder()
                .name("Longsword")
                .type(EquipmentType.WEAPON)
                .damage("1d8")
                .damageType("slashing")
                .properties("Versatile (1d10)")
                .weight(3.0)
                .equipped(true)
                .build();

        Equipment armor = Equipment.builder()
                .name("Chain Mail")
                .type(EquipmentType.ARMOR)
                .weight(55.0)
                .equipped(true)
                .description("AC 16")
                .build();

        Equipment potion = Equipment.builder()
                .name("Potion of Healing")
                .type(EquipmentType.CONSUMABLE)
                .quantity(2)
                .description("Heals 2d4+2 hit points")
                .build();

        DndCurrency currency = DndCurrency.builder()
                .gold(80)
                .silver(30)
                .copper(15)
                .build();

        List<Skill> skills = List.of(
                Skill.builder().skillType(SkillType.ATHLETICS).proficiency(true).build(),
                Skill.builder().skillType(SkillType.ACROBATICS).proficiency(true).build(),
                Skill.builder().skillType(SkillType.STEALTH).proficiency(true).build(),
                Skill.builder().skillType(SkillType.SLEIGHT_OF_HAND).proficiency(true).build(),
                Skill.builder().skillType(SkillType.SURVIVAL).proficiency(true).build(),
                Skill.builder().skillType(SkillType.HISTORY).proficiency(true).build(),
                Skill.builder().skillType(SkillType.INSIGHT).proficiency(true).build(),
                Skill.builder().skillType(SkillType.INVESTIGATION).proficiency(true).build(),
                Skill.builder().skillType(SkillType.PERCEPTION).proficiency(true).build(),
                Skill.builder().skillType(SkillType.PERFORMANCE).proficiency(true).build(),
                Skill.builder().skillType(SkillType.PERSUASION).proficiency(true).build(),
                Skill.builder().skillType(SkillType.ARCANA).proficiency(true).build(),
                Skill.builder().skillType(SkillType.ANIMAL_HANDLING).proficiency(true).build(),
                Skill.builder().skillType(SkillType.NATURE).proficiency(false).build(),
                Skill.builder().skillType(SkillType.RELIGION).proficiency(false).build(),
                Skill.builder().skillType(SkillType.MEDICINE).proficiency(false).build(),
                Skill.builder().skillType(SkillType.INTIMIDATION).proficiency(true).build(),
                Skill.builder().skillType(SkillType.DECEPTION).proficiency(false).build()
        );

        int maxHp = 10 + (level - 1) * 6 + (abilityScores.getModifier(AbilityType.CONSTITUTION) * level);

        DndCharacter character = DndCharacter.builder()
                .owner(owner)
                .name(name)
                .race(race)
                .alignment(CharacterAlignment.NEUTRAL_GOOD)
                .background("Adventurer")
                .abilityScores(abilityScores)
                .maxHitPoints(maxHp)
                .currentHitPoints(maxHp)
                .armorClass(16)
                .speed(30)
                .proficiencyBonus(2 + (level - 1) / 4)
                .hitDice(level + "d10") // NOTE: check PNB on this
                .currency(currency)
                .spells(characterSpells)
                .backstory("Essay, that players wrote and forgot about the same day")
                .personalityTraits("Brave and loyal")
                .ideals("Justice and honor")
                .bonds("My companions are my family")
                .flaws("Too trusting sometimes")
                .build();

        character.getClasses().add(characterClass);
        character.getEquipment().addAll(List.of(sword, armor, potion));
        character.getSkills().addAll(skills);
        character.getSavingThrowProficiencies().add(AbilityType.STRENGTH);
        character.getSavingThrowProficiencies().add(AbilityType.CONSTITUTION);

        // Not so good implementation
        if (className.equals("Wizard")) {
            character.setSpellcastingAbility(AbilityType.INTELLIGENCE);
        } else if (className.equals("Ranger")) {
            character.setSpellcastingAbility(AbilityType.WISDOM);
        }

        characterRepository.save(character);
        log.info("Created character: {} ({})", name, className);
    }
}
