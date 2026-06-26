// src/components/character/CombatStats.tsx

import React, {type ChangeEvent} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CombatStats.module.css';
import {Card} from '@/components/common/Card';
import type {EquipmentResponse} from '@/types';

type EditableCombatKey = 'armorClass' | 'speed';

export function CombatStats({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  const debouncedUpdate = useDebouncedCallback(
      (key: EditableCombatKey, value: number) => {
        if (currentCharacter) {
          updateCharacter(currentCharacter.id, {[key]: value});
        }
      },
      500
  );

  if (!currentCharacter) {
    return null;
  }

  const {
    armorClass,
    speed,
    equipment,
    proficiencyBonus,
    abilityScores,
    initiative
  } = currentCharacter;

  const currentInitiative = initiative;

  const formatModifier = (value: number): string =>
      value >= 0 ? `+${value}` : `${value}`;

  const getAttackBonus = (weapon: EquipmentResponse): string => {
    try {
      const isFinesse = weapon.properties?.toLowerCase().includes('finesse');
      const strMod = abilityScores.strengthModifier;
      const dexMod = abilityScores.dexterityModifier;

      const relevantModifier = isFinesse ? Math.max(strMod, dexMod) : strMod;
      const bonus = proficiencyBonus + relevantModifier;

      return formatModifier(bonus);
    } catch {
      return 'N/A';
    }
  };

  const activeWeapons = equipment
      .filter((item) => item.type === 'WEAPON' && item.equipped)
      .slice(0, 3);

  const displayWeapons: (EquipmentResponse | null)[] = Array(3)
      .fill(null)
      .map((_, index) => activeWeapons[index] || null);

  const handleChange = (
      e: ChangeEvent<HTMLInputElement>,
      key: EditableCombatKey
  ) => {
    const value = parseInt(e.target.value, 10) || 0;
    debouncedUpdate(key, value);
  };

  return (
      <Card title="Combat Stats" className={className}>
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
            <span className={styles.value} aria-live="polite">
              {formatModifier(currentInitiative)}
            </span>
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

        <div className={styles.attacksContainer}>
          <div className={styles.attacksHeader}>
            <span>Name</span>
            <span>Atk Bonus</span>
            <span>Damage / Type</span>
          </div>

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
