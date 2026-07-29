import {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input} from '@/components/common/Input';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import styles from './CombatStats.module.css';
import {Card} from '@/components/common/Card';
import type {EquipmentResponse} from '@/types';

type EditableCombatKey = 'speed' | 'armorClassBonus';

export function CombatStats({className}: { className?: string }) {
  const {currentCharacter, updateCharacter} = useCharacter();

  const [speedDraft, setSpeedDraft] = useState<string>('');
  const [acBonusDraft, setAcBonusDraft] = useState<string>('');

  useEffect(() => {
    if (!currentCharacter) return;
    setSpeedDraft(String(currentCharacter.speed));
    setAcBonusDraft(String(currentCharacter.armorClassBonus ?? 0));
  }, [currentCharacter?.id, currentCharacter?.speed, currentCharacter?.armorClassBonus]);

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
    equipment,
    proficiencyBonus,
    abilityScores,
    initiative,
  } = currentCharacter;

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
      key: EditableCombatKey,
      setDraft: (v: string) => void
  ) => {
    const raw = e.target.value;
    setDraft(raw);
    const parsed = parseInt(raw, 10);
    if (!Number.isNaN(parsed)) {
      debouncedUpdate(key, parsed);
    }
  };

  return (
      <Card title="Combat Stats" className={className}>
        <div className={styles.grid}>
          <div className={styles.stat}>
            <label className={styles.label}>Armor Class</label>
            <div className={styles.acContainer}>
    <span className={styles.value} aria-live="polite">
      {currentCharacter.armorClass}
    </span>
              <div className={styles.acBonusWrapper}>
                <span className={styles.acBonusPrefix} aria-hidden="true">Bonus:</span>
                <input
                    type="number"
                    className={styles.acBonusInput}
                    value={acBonusDraft}
                    onChange={(e) => handleChange(e, 'armorClassBonus', setAcBonusDraft)}
                    aria-label="Armor Class bonus"
                    title="Manual AC bonus"
                    placeholder="0"
                />
              </div>
            </div>
          </div>
          <div className={styles.stat}>
            <label className={styles.label}>Initiative</label>
            <span className={styles.value} aria-live="polite">
              {formatModifier(initiative)}
            </span>
          </div>
          <div className={styles.stat}>
            <label className={styles.label}>Speed</label>
            <div className={styles.speedInput}>
              <Input
                  type="number"
                  value={speedDraft}
                  onChange={(e) => handleChange(e, 'speed', setSpeedDraft)}
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
