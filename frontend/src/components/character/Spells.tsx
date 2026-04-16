// src/components/character/Spells.tsx

import {type ChangeEvent, useMemo} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Select} from '@/components/common/Input';
import {ABILITIES} from '@/utils/constants';
import {getSpellAttackBonus, getSpellSaveDC} from '@/utils/helpers';
import type {AbilityName, SpellResponse} from '@/types';
import styles from './Spells.module.css';
import {Card} from '@/components/common/Card';

// Helper function to group spells by level
const groupSpellsByLevel = (spells: SpellResponse[]) => {
  return spells.reduce((acc, spell) => {
    const level = spell.level;
    if (!acc[level]) {
      acc[level] = [];
    }
    acc[level].push(spell);
    return acc;
  }, {} as Record<number, SpellResponse[]>);
};

export function Spells({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  // Guard clause: If there's no character, don't render anything.
  if (!currentCharacter) {
    return null;
  }

  // Destructure the relevant, correct properties from the character object
  const {
    spells,
    spellcastingAbility,
    abilityScores,
    proficiencyBonus,
  } = currentCharacter;

  // Calculate spell stats using the correct data
  const spellcastingAbilityScore = spellcastingAbility
      ? abilityScores[spellcastingAbility.toLowerCase() as keyof typeof abilityScores]
      : 10;
  const spellSaveDC = getSpellSaveDC(spellcastingAbilityScore, proficiencyBonus);
  const spellAttackBonus = getSpellAttackBonus(spellcastingAbilityScore, proficiencyBonus);

  // Group spells for rendering
  const spellsByLevel = useMemo(() => groupSpellsByLevel(spells || []), [spells]);
  const spellLevels = Object.keys(spellsByLevel).map(Number).sort();

  // --- Event Handlers ---
  const handleSpellcastingAbilityChange = (e: ChangeEvent<HTMLSelectElement>) => {
    // Call the context's update function with the new value
    updateCharacter(currentCharacter.id, {
      spellcastingAbility: e.target.value as AbilityName,
    });
  };

  const handleRemoveSpell = (spellId: number) => {
    // This would eventually call an API function, e.g., `charactersApi.removeSpell(character.id, spellId)`
    // For now, we'll log it as it requires backend changes.
    console.log(`Request to remove spell with ID: ${spellId}`);
    console.warn("Remove spell functionality needs to be implemented via the API.");
  };

  return (
      <Card title="Spellcasting" className={className}>

        {/* Spellcasting Stats */}
        <div className={styles.spellcastingStats}>
          <Select
              label="Spellcasting Ability"
              value={spellcastingAbility || ''}
              onChange={handleSpellcastingAbilityChange}
              options={ABILITIES.map((a) => ({value: a.key.toUpperCase(), label: a.name}))}
              placeholder="None"
          />

          <div className={styles.stat}>
            <span className={styles.statLabel}>Spell Save DC</span>
            <span className={styles.statValue}>{spellSaveDC}</span>
          </div>

          <div className={styles.stat}>
            <span className={styles.statLabel}>Spell Attack</span>
            <span className={styles.statValue}>
            {spellAttackBonus >= 0 ? `+${spellAttackBonus}` : spellAttackBonus}
          </span>
          </div>
        </div>

        {/* Spells List */}
        <div className={styles.spellListContainer}>
          {spells && spells.length > 0 ? (
              spellLevels.map((level) => (
                  <div key={level} className={styles.spellLevelGroup}>
                    <h4 className={styles.sectionTitle}>
                      {level === 0 ? 'Cantrips' : `Level ${level}`}
                    </h4>
                    {spellsByLevel[level].map((spell) => (
                        <div key={spell.id} className={styles.spell}>
                          <span className={styles.spellName}>{spell.name}</span>
                          <button
                              type="button"
                              className={styles.removeButton}
                              onClick={() => handleRemoveSpell(spell.id)}
                          >
                            ✕
                          </button>
                        </div>
                    ))}
                  </div>
              ))
          ) : (
              <p className={styles.emptyMessage}>No spells known.</p>
          )}
        </div>

        {/* Note: The old modal for adding custom spells and managing slots has been removed
          as it doesn't match the new data structure. Functionality for adding/removing
          spells should be handled by a different UI, likely one that searches a master spell list. */}
      </Card>
  );
}
