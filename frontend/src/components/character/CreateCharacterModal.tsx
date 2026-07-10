import {type FormEvent, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Modal} from '@/components/common/Modal';
import {Input, Select} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import type {AbilityScores, CharacterCreateRequest} from '@/types';
import styles from './CreateCharacterModal.module.css';

interface CreateCharacterModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const initialScores: AbilityScores = {
  strength: 10, dexterity: 10, constitution: 10,
  intelligence: 10, wisdom: 10, charisma: 10
};

export function CreateCharacterModal({isOpen, onClose}: CreateCharacterModalProps) {
  const {createCharacter, races, classes, backgrounds, saving} = useCharacter();
  const [name, setName] = useState('');
  const [raceKey, setRaceKey] = useState<string | ''>('');
  const [backgroundKey, setBackgroundKey] = useState<string | ''>('');
  const [classKey, setClassKey] = useState<string | ''>('');
  const [abilityScores, setAbilityScores] = useState<AbilityScores>(initialScores);
  const [error, setError] = useState<string | null>(null);

  const handleScoreChange = (scoreName: keyof AbilityScores, value: string) => {
    setAbilityScores(prev => ({...prev, [scoreName]: parseInt(value, 10) || 0}));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!name || !raceKey || !classKey) {
      setError("Name, Race, and Class are required.");
      return;
    }

    const selectedClass = classes.find(c => c.key === String(classKey));
    const constitutionModifier = Math.floor((abilityScores.constitution - 10) / 2);

    const hitDieValue = selectedClass ? parseInt(selectedClass.hitDice.split('D')[1], 10) : 10;

    const payload: CharacterCreateRequest = {
      name,
      raceKey: String(raceKey),
      classSlug: String(classKey),
      backgroundKey: String(backgroundKey),
      abilityScores,
      maxHitPoints: hitDieValue + constitutionModifier,
    };

    try {
      await createCharacter(payload);
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create character.");
    }
  };

  const raceOptions = races.map(r => ({value: r.key, label: r.name}));
  const classOptions = classes.map(c => ({value: c.key, label: c.name}));
  const backgroundOptions = backgrounds.map(b => ({value: b.key, label: b.name}));

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title="Create New Character"
          size="large"
          footer={
            <>
              <Button variant="secondary" onClick={onClose}>Cancel</Button>
              <Button type="submit" form="create-character-form" disabled={saving}>
                {saving ? 'Creating...' : 'Create Character'}
              </Button>
            </>
          }
      >
        <form id="create-character-form" onSubmit={handleSubmit} className={styles.form}>
          {error && <div className={styles.errorBanner}>{error}</div>}

          <Input label="Character Name" value={name} onChange={(e) => setName(e.target.value)}
                 required fullWidth/>

          <div className={styles.grid}>
            <Select label="Race" value={raceKey}
                    onChange={(e) => setRaceKey(String(e.target.value))}
                    options={raceOptions} placeholder="-- Select a Race --" required/>
            <Select label="Class" value={classKey}
                    onChange={(e) => setClassKey(String(e.target.value))} options={classOptions}
                    placeholder="-- Select a Class --" required/>
            <Select label="Background" value={backgroundKey}
                    onChange={(e) => setBackgroundKey(String(e.target.value))}
                    options={backgroundOptions}
                    placeholder="-- Select a Background --" required/>
          </div>

          <h3 className={styles.sectionTitle}>Ability Scores</h3>
          <div className={styles.scoresGrid}>
            {Object.keys(initialScores).map(score => (
                <Input
                    key={score}
                    label={score.charAt(0).toUpperCase() + score.slice(1)}
                    type="number"
                    value={abilityScores[score as keyof AbilityScores]}
                    onChange={(e) => handleScoreChange(score as keyof AbilityScores, e.target.value)}
                    required
                />
            ))}
          </div>
        </form>
      </Modal>
  );
}
