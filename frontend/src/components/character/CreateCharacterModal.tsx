import {useMemo, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {
  AbilityScores,
  CharacterAlignment,
  CharacterCreateRequest,
  Open5eReference
} from '@/types';
import {WizardProgress} from './wizard/WizardProgress';
import {StepGameSystem} from './wizard/StepGameSystem';
import {StepRace} from './wizard/StepRace';
import {StepClass} from './wizard/StepClass';
import {StepBackground} from './wizard/StepBackground';
import {StepDetails} from './wizard/StepDetails';
import styles from './CreateCharacterModal.module.css';
import {EMPTY_RACE_SELECTION, isRaceSelectionComplete, type RaceSelection} from '@/utils/races';

interface CreateCharacterModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const STEPS = ['System', 'Race', 'Class', 'Background', 'Details'];

const INITIAL_SCORES: AbilityScores = {
  strength: 10,
  dexterity: 10,
  constitution: 10,
  intelligence: 10,
  wisdom: 10,
  charisma: 10,
};

function computeHitPoints(
    classes: ReturnType<typeof useCharacter>['classes'],
    classKey: string,
    constitution: number
): number {
  const cls = classes.find(c => c.key === classKey);
  const conMod = Math.floor((constitution - 10) / 2);
  const dieFaces = parseInt((cls?.hit_dice ?? cls?.hitDice ?? 'D8').replace('D', ''), 10);
  return (isNaN(dieFaces) ? 8 : dieFaces) + conMod;
}

// --- Helper to separate 2024 content from legacy/A5E content ---
function is2024Content(item: {
  key: string;
  document?: { gamesystem?: Open5eReference }
}): boolean {
  const sysKey = item.document?.gamesystem?.key?.toLowerCase() || '';
  const itemKey = item.key.toLowerCase();
  return sysKey.includes('2024') || itemKey.includes('2024');
}

export function CreateCharacterModal({isOpen, onClose}: CreateCharacterModalProps) {
  const {createCharacter, races, classes, backgrounds, saving} = useCharacter();

  const [step, setStep] = useState(0);
  const [gameSystemKey, setGameSystemKey] = useState('');
  const [raceSelection, setRaceSelection] = useState<RaceSelection>(EMPTY_RACE_SELECTION);
  const [classKey, setClassKey] = useState('');
  const [backgroundKey, setBackgroundKey] = useState('');
  const [name, setName] = useState('');
  const [alignment, setAlignment] = useState<CharacterAlignment | ''>('');
  const [abilityScores, setAbilityScores] = useState<AbilityScores>(INITIAL_SCORES);
  const [error, setError] = useState<string | null>(null);

  // --- Filter Logic ---

  const filteredRaces = useMemo(() => {
    if (!gameSystemKey) return [];
    return races.filter(r => gameSystemKey === '2024' ? is2024Content(r) : !is2024Content(r));
  }, [races, gameSystemKey]);

  const filteredClasses = useMemo(() => {
    if (!gameSystemKey) return [];
    return classes.filter(c => gameSystemKey === '2024' ? is2024Content(c) : !is2024Content(c));
  }, [classes, gameSystemKey]);

  const filteredBackgrounds = useMemo(() => {
    if (!gameSystemKey) return [];
    return backgrounds.filter(b => gameSystemKey === '2024' ? is2024Content(b) : !is2024Content(b));
  }, [backgrounds, gameSystemKey]);

  // --- Event Handlers ---

  const handleGameSystemSelect = (key: string) => {
    if (key !== gameSystemKey) {
      setGameSystemKey(key);
      // Reset downstream choices if the system changes
      setRaceSelection(EMPTY_RACE_SELECTION);
      setClassKey('');
      setBackgroundKey('');
    } else {
      setGameSystemKey('');
    }
  };

  const handleScoreChange = (key: keyof AbilityScores, value: string) => {
    setAbilityScores(prev => ({...prev, [key]: parseInt(value, 10) || 0}));
  };

  const handleClose = () => {
    setStep(0);
    setGameSystemKey('');
    setRaceSelection(EMPTY_RACE_SELECTION);
    setClassKey('');
    setBackgroundKey('');
    setName('');
    setAlignment('');
    setAbilityScores(INITIAL_SCORES);
    setError(null);
    onClose();
  };

  const canAdvance = (): boolean => {
    if (step === 0) return !!gameSystemKey;
    if (step === 1) return isRaceSelectionComplete(filteredRaces, raceSelection);
    if (step === 2) return !!classKey;
    if (step === 3) return !!backgroundKey;
    if (step === 4) {
      const hasUnassignedScores = Object.values(abilityScores).some(v => v === 0);
      return !!name.trim() && !hasUnassignedScores;
    }
    return false;
  };

  const handleNext = () => {
    if (canAdvance()) setStep(current => current + 1);
  };

  const handleBack = () => setStep(current => current - 1);

  const handleSubmit = async () => {
    setError(null);

    if (!name.trim() || !raceSelection.raceKey || !classKey) {
      setError('Name, Race, and Class are required.');
      return;
    }

    const payload: CharacterCreateRequest = {
      name: name.trim(),
      raceKey: raceSelection.raceKey,
      classKey,
      backgroundKey,
      ...(alignment && {alignment}),
      abilityScores,
      maxHitPoints: computeHitPoints(filteredClasses, classKey, abilityScores.constitution),
    };

    try {
      await createCharacter(payload);
      handleClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create character.');
    }
  };

  const hp = computeHitPoints(filteredClasses, classKey, abilityScores.constitution);

  const footer = (
      <div className={styles.footer}>
        {error && <span className={styles.footerError}>{error}</span>}
        <div className={styles.footerActions}>
          <Button variant="secondary" onClick={step === 0 ? handleClose : handleBack}>
            {step === 0 ? 'Cancel' : 'Back'}
          </Button>
          {step < STEPS.length - 1 ? (
              <Button onClick={handleNext} disabled={!canAdvance()}>
                Next
              </Button>
          ) : (
              <Button onClick={handleSubmit} disabled={saving || !canAdvance()}>
                {saving ? 'Creating...' : 'Create Character'}
              </Button>
          )}
        </div>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={handleClose}
          title="Create New Character"
          size="large"
          footer={footer}
      >
        <WizardProgress steps={STEPS} currentStep={step}/>

        <div className={styles.stepBody}>
          {step === 0 && (
              <StepGameSystem
                  selectedKey={gameSystemKey}
                  onSelect={handleGameSystemSelect}
              />
          )}
          {step === 1 && (
              <StepRace
                  races={filteredRaces}
                  selection={raceSelection}
                  onSelect={setRaceSelection}
              />
          )}
          {step === 2 && (
              <StepClass classes={filteredClasses} selectedKey={classKey} onSelect={setClassKey}/>
          )}
          {step === 3 && (
              <StepBackground
                  backgrounds={filteredBackgrounds}
                  selectedKey={backgroundKey}
                  onSelect={setBackgroundKey}
              />
          )}
          {step === 4 && (
              <StepDetails
                  name={name}
                  onNameChange={setName}
                  alignment={alignment}
                  onAlignmentChange={setAlignment}
                  abilityScores={abilityScores}
                  onScoreChange={handleScoreChange}
                  hitPoints={hp}
              />
          )}
        </div>
      </Modal>
  );
}
