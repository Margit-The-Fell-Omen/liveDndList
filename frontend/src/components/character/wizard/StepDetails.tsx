import type {AbilityScores, CharacterAlignment} from '@/types';
import {Input} from '@/components/common/Input';
import styles from './StepDetails.module.css';

const ALIGNMENTS: { value: CharacterAlignment; label: string }[] = [
  {value: 'LAWFUL_GOOD', label: 'Lawful Good'},
  {value: 'NEUTRAL_GOOD', label: 'Neutral Good'},
  {value: 'CHAOTIC_GOOD', label: 'Chaotic Good'},
  {value: 'LAWFUL_NEUTRAL', label: 'Lawful Neutral'},
  {value: 'TRUE_NEUTRAL', label: 'True Neutral'},
  {value: 'CHAOTIC_NEUTRAL', label: 'Chaotic Neutral'},
  {value: 'LAWFUL_EVIL', label: 'Lawful Evil'},
  {value: 'NEUTRAL_EVIL', label: 'Neutral Evil'},
  {value: 'CHAOTIC_EVIL', label: 'Chaotic Evil'},
];

const ABILITY_KEYS: (keyof AbilityScores)[] = [
  'strength', 'dexterity', 'constitution',
  'intelligence', 'wisdom', 'charisma',
];

interface StepDetailsProps {
  name: string;
  onNameChange: (v: string) => void;
  alignment: CharacterAlignment | '';
  onAlignmentChange: (v: CharacterAlignment | '') => void;
  abilityScores: AbilityScores;
  onScoreChange: (key: keyof AbilityScores, value: string) => void;
  hitPoints: number;
}

export function StepDetails({
                              name,
                              onNameChange,
                              alignment,
                              onAlignmentChange,
                              abilityScores,
                              onScoreChange,
                              hitPoints,
                            }: StepDetailsProps) {
  return (
      <div className={styles.container}>
        <div className={styles.section}>
          <h3 className={styles.sectionTitle}>Identity</h3>
          <div className={styles.identityGrid}>
            <Input
                label="Character Name"
                value={name}
                onChange={e => onNameChange(e.target.value)}
                required
                fullWidth
            />
            <div className={styles.selectWrapper}>
              <label className={styles.selectLabel}>Alignment</label>
              <select
                  className={styles.select}
                  value={alignment}
                  onChange={e => onAlignmentChange(e.target.value as CharacterAlignment | '')}
              >
                <option value="">— None —</option>
                {ALIGNMENTS.map(a => (
                    <option key={a.value} value={a.value}>{a.label}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className={styles.section}>
          <h3 className={styles.sectionTitle}>
            Ability Scores
            <span className={styles.hpPreview}>→ Starting HP: {hitPoints}</span>
          </h3>
          <div className={styles.scoresGrid}>
            {ABILITY_KEYS.map(key => (
                <div key={key} className={styles.scoreItem}>
                  <Input
                      label={key.charAt(0).toUpperCase() + key.slice(1)}
                      type="number"
                      value={abilityScores[key]}
                      onChange={e => onScoreChange(key, e.target.value)}
                      required
                  />
                  <span className={styles.modifier}>
                {(() => {
                  const mod = Math.floor((abilityScores[key] - 10) / 2);
                  return mod >= 0 ? `+${mod}` : `${mod}`;
                })()}
              </span>
                </div>
            ))}
          </div>
        </div>
      </div>
  );
}