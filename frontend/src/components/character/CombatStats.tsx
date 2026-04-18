// src/components/character/CombatStats.tsx

import React, {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CombatStats.module.css';
import {Card} from '@/components/common/Card';
import type {EquipmentResponse} from '@/types';

export function CombatStats({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (key: 'armorClass' | 'initiative' | 'speed', value: number) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {[key]: value});
        }
      },
      500
  );

  if (!currentCharacter) {
    return null;
  }

  // Destructure all necessary properties for display and calculations
  const {
    armorClass,
    initiative,
    speed,
    equipment,
    proficiencyBonus,
    abilityScores,
  } = currentCharacter;

  // --- WEAPON LOGIC ---
  const getAttackBonus = (weapon: EquipmentResponse): string => {
    // A simplified calculation for STR/DEX (finesse) weapons.
    // This makes the sheet feel more alive!
    try {
      const isFinesse = weapon.properties?.toLowerCase().includes('finesse');
      const strMod = abilityScores.strengthModifier;
      const dexMod = abilityScores.dexterityModifier;

      // Use the higher of STR/DEX for finesse, otherwise STR
      const relevantModifier = isFinesse ? Math.max(strMod, dexMod) : strMod;
      const bonus = proficiencyBonus + relevantModifier;

      return bonus >= 0 ? `+${bonus}` : `${bonus}`;
    } catch {
      return 'N/A'; // Failsafe
    }
  };

  const activeWeapons = equipment
  .filter((item) => item.type === 'WEAPON' && item.equipped)
  .slice(0, 3); // Get up to 3 equipped weapons

  // Create an array of exactly 3 items for consistent rendering
  const displayWeapons: (EquipmentResponse | null)[] = Array(3)
  .fill(null)
  .map((_, index) => activeWeapons[index] || null);
  // --- END WEAPON LOGIC ---

  const handleChange = (
      e: ChangeEvent<HTMLInputElement>,
      key: 'armorClass' | 'initiative' | 'speed'
  ) => {
    const value = parseInt(e.target.value, 10) || 0;
    debouncedUpdate(key, value);
  };

  return (
      <Card title="Combat Stats" className={className}>
        {/* Top section: AC, Initiative, Speed (Unchanged) */}
        <div className={styles.grid}>
          <div className={styles.stat}>
            <label className={styles.label}>Armor Class</label>
            <Input
                type="number"
                defaultValue={armorClass}
                onChange={(e) => handleChange(e, 'armorClass')}
                min={0}
                className={styles.input}
            />
          </div>
          <div className={styles.stat}>
            <label className={styles.label}>Initiative</label>
            <Input
                type="number"
                defaultValue={initiative}
                onChange={(e) => handleChange(e, 'initiative')}
                className={styles.input}
            />
          </div>
          <div className={styles.stat}>
            <label className={styles.label}>Speed</label>
            <div className={styles.speedInput}>
              <Input
                  type="number"
                  defaultValue={speed}
                  onChange={(e) => handleChange(e, 'speed')}
                  min={0}
                  className={styles.input}
              />
              <span className={styles.unit}>ft</span>
            </div>
          </div>
        </div>

        {/* --- REVISED "Attacks" Section --- */}
        <div className={styles.attacksContainer}>
          {/* Header Row */}
          <div className={styles.attacksHeader}>
            <span>Name</span>
            <span>Atk Bonus</span>
            <span>Damage / Type</span>
          </div>

          {/* Data Rows */}
          {displayWeapons.map((weapon, index) => (
              <div key={index} className={styles.attackRow}>
                <div className={styles.attackCell}>
                  {weapon?.name || '—'}
                </div>
                <div className={`${styles.attackCell} ${styles.centerText}`}>
                  {weapon ? getAttackBonus(weapon) : '—'}
                </div>
                <div className={styles.attackCell}>
                  {weapon ? `${weapon.damage || ''} ${weapon.damageType || ''}`.trim() : '—'}
                </div>
              </div>
          ))}

          {/* Optional: A box to show weapon properties cleanly */}
          <div className={styles.propertiesBox}>
            <p>
              <strong>Equipped Weapon Properties:</strong>{' '}
              {activeWeapons.map(w => w.properties).filter(Boolean).join(', ') || 'None'}
            </p>
          </div>
        </div>
      </Card>
  );
}
