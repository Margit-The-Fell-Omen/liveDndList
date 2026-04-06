/**
 * Calculate ability modifier from ability score
 */
export function getAbilityModifier(score) {
  return Math.floor((score - 10) / 2);
}

/**
 * Format modifier for display (+2, -1, etc.)
 */
export function formatModifier(modifier) {
  return modifier >= 0 ? `+${modifier}` : `${modifier}`;
}

/**
 * Calculate proficiency bonus from level
 */
export function getProficiencyBonus(level) {
  return Math.ceil(level / 4) + 1;
}

/**
 * Calculate skill modifier
 */
export function getSkillModifier(abilityScore, proficiencyBonus, isProficient, hasExpertise) {
  const abilityMod = getAbilityModifier(abilityScore);
  let bonus = abilityMod;
  
  if (isProficient) {
    bonus += proficiencyBonus;
  }
  if (hasExpertise) {
    bonus += proficiencyBonus; // Expertise doubles proficiency
  }
  
  return bonus;
}

/**
 * Calculate passive perception
 */
export function getPassivePerception(wisdomScore, proficiencyBonus, isProficient) {
  return 10 + getSkillModifier(wisdomScore, proficiencyBonus, isProficient, false);
}

/**
 * Calculate spell save DC
 */
export function getSpellSaveDC(abilityScore, proficiencyBonus) {
  return 8 + proficiencyBonus + getAbilityModifier(abilityScore);
}

/**
 * Calculate spell attack bonus
 */
export function getSpellAttackBonus(abilityScore, proficiencyBonus) {
  return proficiencyBonus + getAbilityModifier(abilityScore);
}

/**
 * Validate ability score (1-30)
 */
export function validateAbilityScore(value) {
  const num = parseInt(value, 10);
  if (isNaN(num)) return { valid: false, message: 'Must be a number' };
  if (num < 1) return { valid: false, message: 'Minimum is 1' };
  if (num > 30) return { valid: false, message: 'Maximum is 30' };
  return { valid: true, value: num };
}

/**
 * Validate level (1-20)
 */
export function validateLevel(value) {
  const num = parseInt(value, 10);
  if (isNaN(num)) return { valid: false, message: 'Must be a number' };
  if (num < 1) return { valid: false, message: 'Minimum level is 1' };
  if (num > 20) return { valid: false, message: 'Maximum level is 20' };
  return { valid: true, value: num };
}

/**
 * Roll a die
 */
export function rollDie(sides) {
  return Math.floor(Math.random() * sides) + 1;
}

/**
 * Roll multiple dice
 */
export function rollDice(count, sides, modifier = 0) {
  const rolls = [];
  for (let i = 0; i < count; i++) {
    rolls.push(rollDie(sides));
  }
  const total = rolls.reduce((sum, roll) => sum + roll, 0) + modifier;
  return { rolls, modifier, total };
}

/**
 * Parse dice notation (e.g., "2d6+3")
 */
export function parseDiceNotation(notation) {
  const match = notation.match(/^(\d+)d(\d+)([+-]\d+)?$/i);
  if (!match) return null;
  
  return {
    count: parseInt(match[1], 10),
    sides: parseInt(match[2], 10),
    modifier: match[3] ? parseInt(match[3], 10) : 0,
  };
}
