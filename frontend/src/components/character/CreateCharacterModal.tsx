import {useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {AbilityScores, CharacterAlignment, CharacterCreateRequest} from '@/types';
import {WizardProgress} from './wizard/WizardProgress';
import {StepRace} from './wizard/StepRace';
import {StepClass} from './wizard/StepClass';
import {StepBackground} from './wizard/StepBackground';
import {StepDetails} from './wizard/StepDetails';
import styles from './CreateCharacterModal.module.css';

interface CreateCharacterModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const STEPS = ['Race', 'Class', 'Background', 'Details'];

const INITIAL_SCORES: AbilityScores = {
  strength: 10,
  dexterity: 10,
  constitution: 10,
  intelligence: 10,
  wisdom: 10,
  charisma: 10,
};

function computeHitPoints(classes: ReturnType<typeof useCharacter>['classes'], classKey: string, constitution: number): number {
  const cls = classes.find(c => c.key === classKey);
  const conMod = Math.floor((constitution - 10) / 2);
  const dieFaces = parseInt((cls?.hit_dice ?? cls?.hitDice ?? 'D8').replace('D', ''), 10);
  return (isNaN(dieFaces) ? 8 : dieFaces) + conMod;
}

export function CreateCharacterModal({isOpen, onClose}: CreateCharacterModalProps) {
  const {createCharacter, races, classes, backgrounds, saving} = useCharacter();

  const [step, setStep] = useState(0);
  const [raceKey, setRaceKey] = useState('');
  const [classKey, setClassKey] = useState('');
  const [backgroundKey, setBackgroundKey] = useState('');
  const [name, setName] = useState('');
  const [alignment, setAlignment] = useState<CharacterAlignment | ''>('');
  const [abilityScores, setAbilityScores] = useState<AbilityScores>(INITIAL_SCORES);
  const [error, setError] = useState<string | null>(null);

  const handleScoreChange = (key: keyof AbilityScores, value: string) => {
    setAbilityScores(prev => ({...prev, [key]: parseInt(value, 10) || 0}));
  };

  const handleClose = () => {
    setStep(0);
    setRaceKey('');
    setClassKey('');
    setBackgroundKey('');
    setName('');
    setAlignment('');
    setAbilityScores(INITIAL_SCORES);
    setError(null);
    onClose();
  };

  const canAdvance = (): boolean => {
    if (step === 0) return !!raceKey;
    if (step === 1) return !!classKey;
    if (step === 2) return !!backgroundKey;
    if (step === 3) return !!name.trim();
    return false;
  };

  const handleNext = () => {
    if (canAdvance()) setStep(s => s + 1);
  };

  const handleBack = () => setStep(s => s - 1);

  const handleSubmit = async () => {
    setError(null);
    if (!name.trim() || !raceKey || !classKey) {
      setError('Name, Race, and Class are required.');
      return;
    }

    const payload: CharacterCreateRequest = {
      name: name.trim(),
      raceKey,
      classKey,
      backgroundKey,
      ...(alignment && {alignment}),
      abilityScores,
      maxHitPoints: computeHitPoints(classes, classKey, abilityScores.constitution),
    };

    try {
      await createCharacter(payload);
      handleClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create character.');
    }
  };

  const hp = computeHitPoints(classes, classKey, abilityScores.constitution);

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
              <StepRace races={races} selectedKey={raceKey} onSelect={setRaceKey}/>
          )}
          {step === 1 && (
              <StepClass classes={classes} selectedKey={classKey} onSelect={setClassKey}/>
          )}
          {step === 2 && (
              <StepBackground backgrounds={backgrounds} selectedKey={backgroundKey}
                              onSelect={setBackgroundKey}/>
          )}
          {step === 3 && (
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
