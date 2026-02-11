package dev.ushki.live_dnd_list.enums;

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
