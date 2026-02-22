package dev.ushki.livedndlist.enums;

/**
 * Enumeration of the 18 skills in D&D 5th Edition. Each skill is associated with one of the six
 * ability scores.
 *
 * <p>Skill check bonus = ability modifier + proficiency bonus (if proficient)
 * + expertise bonus (if expertise) + other modifiers
 *
 * <p>Skills are organized by their base ability:
 * <ul>
 *   <li>Strength (1): Athletics</li>
 *   <li>Dexterity (3): Acrobatics, Sleight of Hand, Stealth</li>
 *   <li>Intelligence (5): Arcana, History, Investigation, Nature, Religion</li>
 *   <li>Wisdom (5): Animal Handling, Insight, Medicine, Perception, Survival</li>
 *   <li>Charisma (4): Deception, Intimidation, Performance, Persuasion</li>
 *   <li>Constitution (0): No skills are tied to Constitution</li>
 * </ul>
 */
public enum SkillType {
  // Strength
  ATHLETICS(AbilityType.STRENGTH),
  // Dexterity
  ACROBATICS(AbilityType.DEXTERITY),
  SLEIGHT_OF_HAND(AbilityType.DEXTERITY),
  STEALTH(AbilityType.DEXTERITY),
  // Intelligence
  ARCANA(AbilityType.INTELLIGENCE),
  HISTORY(AbilityType.INTELLIGENCE),
  INVESTIGATION(AbilityType.INTELLIGENCE),
  NATURE(AbilityType.INTELLIGENCE),
  RELIGION(AbilityType.INTELLIGENCE),
  // Wisdom
  ANIMAL_HANDLING(AbilityType.WISDOM),
  INSIGHT(AbilityType.WISDOM),
  MEDICINE(AbilityType.WISDOM),
  PERCEPTION(AbilityType.WISDOM),
  SURVIVAL(AbilityType.WISDOM),
  // Charisma
  DECEPTION(AbilityType.CHARISMA),
  INTIMIDATION(AbilityType.CHARISMA),
  PERFORMANCE(AbilityType.CHARISMA),
  PERSUASION(AbilityType.CHARISMA);

  private final AbilityType baseAbility;

  SkillType(AbilityType baseAbility) {
    this.baseAbility = baseAbility;
  }

  public AbilityType getBaseAbility() {
    return baseAbility;
  }
}
