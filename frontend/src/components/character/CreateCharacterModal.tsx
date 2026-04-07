import { useState, useEffect, type ChangeEvent, type FormEvent } from 'react';
import { useCharacter } from '@/context/CharacterContext';
import { Modal } from '@/components/common/Modal';
import { Input, Select, TextArea } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import type {
  CharacterFormData,
  CharacterFormErrors,
  CharacterCreateRequest,
  CharacterAlignment,
  AbilityName,
} from '@/types';
import styles from './CreateCharacterModal.module.css';

interface CreateCharacterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

const ALIGNMENTS: { value: CharacterAlignment; label: string }[] = [
  { value: 'LAWFUL_GOOD', label: 'Lawful Good' },
  { value: 'NEUTRAL_GOOD', label: 'Neutral Good' },
  { value: 'CHAOTIC_GOOD', label: 'Chaotic Good' },
  { value: 'LAWFUL_NEUTRAL', label: 'Lawful Neutral' },
  { value: 'TRUE_NEUTRAL', label: 'True Neutral' },
  { value: 'CHAOTIC_NEUTRAL', label: 'Chaotic Neutral' },
  { value: 'LAWFUL_EVIL', label: 'Lawful Evil' },
  { value: 'NEUTRAL_EVIL', label: 'Neutral Evil' },
  { value: 'CHAOTIC_EVIL', label: 'Chaotic Evil' },
];

const SPELLCASTING_ABILITIES: { value: AbilityName; label: string }[] = [
  { value: 'INTELLIGENCE', label: 'Intelligence' },
  { value: 'WISDOM', label: 'Wisdom' },
  { value: 'CHARISMA', label: 'Charisma' },
];

const DEFAULT_ABILITY_SCORES = {
  strength: 10,
  dexterity: 10,
  constitution: 10,
  intelligence: 10,
  wisdom: 10,
  charisma: 10,
};

const INITIAL_FORM_DATA: CharacterFormData = {
  name: '',
  raceId: null,
  classId: null,
  archetypeId: null,
  alignment: '',
  background: '',
  abilityScores: { ...DEFAULT_ABILITY_SCORES },
  maxHitPoints: 10,
  portraitUrl: '',
  spellcastingAbility: '',
};

export function CreateCharacterModal({ isOpen, onClose, onSuccess }: CreateCharacterModalProps) {
  const { createCharacter, races, classes, archetypes, saving } = useCharacter();
  
  const [formData, setFormData] = useState<CharacterFormData>(INITIAL_FORM_DATA);
  const [errors, setErrors] = useState<CharacterFormErrors>({});
  const [step, setStep] = useState<number>(1);

  // Filter archetypes by selected class
  const filteredArchetypes = formData.classId
    ? archetypes.filter((a) => a.classId === formData.classId)
    : [];

  // Reset form when modal opens
  useEffect(() => {
    if (isOpen) {
      setFormData(INITIAL_FORM_DATA);
      setErrors({});
      setStep(1);
    }
  }, [isOpen]);

  const handleInputChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear error for this field
    if (errors[name as keyof CharacterFormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSelectChange = (name: string, value: string | number | null) => {
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Reset archetype if class changes
    if (name === 'classId') {
      setFormData((prev) => ({ ...prev, archetypeId: null }));
    }

    // Clear error
    if (errors[name as keyof CharacterFormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleAbilityChange = (ability: string, value: number) => {
    setFormData((prev) => ({
      ...prev,
      abilityScores: {
        ...prev.abilityScores,
        [ability]: Math.max(1, Math.min(30, value)),
      },
    }));
  };

  const validateStep1 = (): boolean => {
    const newErrors: CharacterFormErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Character name is required';
    } else if (formData.name.length < 2) {
      newErrors.name = 'Name must be at least 2 characters';
    } else if (formData.name.length > 100) {
      newErrors.name = 'Name must be less than 100 characters';
    }

    if (!formData.raceId) {
      newErrors.raceId = 'Please select a race';
    }

    if (!formData.classId) {
      newErrors.classId = 'Please select a class';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validateStep2 = (): boolean => {
    const newErrors: CharacterFormErrors = {};

    const totalAbilityPoints = Object.values(formData.abilityScores).reduce((sum, val) => sum + val, 0);
    if (totalAbilityPoints < 60) {
      newErrors.abilityScores = 'Ability scores seem too low';
    }

    if (formData.maxHitPoints < 1) {
      newErrors.maxHitPoints = 'Hit points must be at least 1';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNextStep = () => {
    if (step === 1 && validateStep1()) {
      setStep(2);
    }
  };

  const handlePrevStep = () => {
    if (step > 1) {
      setStep(step - 1);
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (step === 1) {
      handleNextStep();
      return;
    }

    if (!validateStep2()) {
      return;
    }

    const createRequest: CharacterCreateRequest = {
      name: formData.name.trim(),
      raceId: formData.raceId!,
      classId: formData.classId!,
      abilityScores: formData.abilityScores,
      maxHitPoints: formData.maxHitPoints,
      alignment: formData.alignment as CharacterAlignment || undefined,
      background: formData.background || undefined,
      archetypeId: formData.archetypeId || undefined,
      portraitUrl: formData.portraitUrl || undefined,
      spellcastingAbility: formData.spellcastingAbility as AbilityName || undefined,
    };

    try {
      await createCharacter(createRequest);
      onClose();
      onSuccess?.();
    } catch (error) {
      setErrors((prev) => ({
        ...prev,
        general: error instanceof Error ? error.message : 'Failed to create character',
      }));
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Create Character - Step ${step} of 2`}
      size="large"
      closeOnOverlay={false}
    >
      <form onSubmit={handleSubmit} className={styles.form}>
        {errors.general && (
          <div className={styles.errorBanner}>{errors.general}</div>
        )}

        {step === 1 && (
          <div className={styles.step}>
            <h3 className={styles.stepTitle}>Basic Information</h3>

            <Input
              label="Character Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              error={errors.name}
              placeholder="Enter character name"
              fullWidth
              required
              autoFocus
            />

            <Select
              label="Race"
              name="raceId"
              value={formData.raceId || ''}
              onChange={(e) => handleSelectChange('raceId', e.target.value ? Number(e.target.value) : null)}
              error={errors.raceId}
              options={races.map((race) => ({ value: race.id, label: race.name }))}
              placeholder="Select a race"
              fullWidth
              required
            />

            <Select
              label="Class"
              name="classId"
              value={formData.classId || ''}
              onChange={(e) => handleSelectChange('classId', e.target.value ? Number(e.target.value) : null)}
              error={errors.classId}
              options={classes.map((cls) => ({ value: cls.id, label: cls.name }))}
              placeholder="Select a class"
              fullWidth
              required
            />

            {filteredArchetypes.length > 0 && (
              <Select
                label="Archetype (Optional)"
                name="archetypeId"
                value={formData.archetypeId || ''}
                onChange={(e) => handleSelectChange('archetypeId', e.target.value ? Number(e.target.value) : null)}
                options={filteredArchetypes.map((arch) => ({ value: arch.id, label: arch.name }))}
                placeholder="Select an archetype"
                fullWidth
              />
            )}

            <Select
              label="Alignment (Optional)"
              name="alignment"
              value={formData.alignment}
              onChange={handleInputChange}
              options={ALIGNMENTS}
              placeholder="Select alignment"
              fullWidth
            />

            <Input
              label="Background (Optional)"
              name="background"
              value={formData.background}
              onChange={handleInputChange}
              placeholder="e.g., Sage, Soldier, Noble"
              fullWidth
            />
          </div>
        )}

        {step === 2 && (
          <div className={styles.step}>
            <h3 className={styles.stepTitle}>Ability Scores & Stats</h3>

            {errors.abilityScores && (
              <div className={styles.fieldError}>{errors.abilityScores}</div>
            )}

            <div className={styles.abilityGrid}>
              {Object.entries(formData.abilityScores).map(([ability, value]) => (
                <div key={ability} className={styles.abilityInput}>
                  <label className={styles.abilityLabel}>
                    {ability.charAt(0).toUpperCase() + ability.slice(1)}
                  </label>
                  <Input
                    type="number"
                    value={value}
                    onChange={(e) => handleAbilityChange(ability, parseInt(e.target.value, 10) || 10)}
                    min={1}
                    max={30}
                  />
                </div>
              ))}
            </div>

            <Input
              label="Maximum Hit Points"
              name="maxHitPoints"
              type="number"
              value={formData.maxHitPoints}
              onChange={(e) => setFormData((prev) => ({ 
                ...prev, 
                maxHitPoints: parseInt(e.target.value, 10) || 1 
              }))}
              error={errors.maxHitPoints}
              min={1}
              fullWidth
              required
            />

            <Select
              label="Spellcasting Ability (Optional)"
              name="spellcastingAbility"
              value={formData.spellcastingAbility}
              onChange={handleInputChange}
              options={SPELLCASTING_ABILITIES}
              placeholder="Select if spellcaster"
              fullWidth
            />

            <Input
              label="Portrait URL (Optional)"
              name="portraitUrl"
              value={formData.portraitUrl}
              onChange={handleInputChange}
              placeholder="https://example.com/portrait.jpg"
              fullWidth
            />
          </div>
        )}

        <div className={styles.footer}>
          {step > 1 && (
            <Button type="button" variant="ghost" onClick={handlePrevStep}>
              Back
            </Button>
          )}
          
          <div className={styles.footerRight}>
            <Button type="button" variant="ghost" onClick={onClose}>
              Cancel
            </Button>
            
            {step < 2 ? (
              <Button type="submit" variant="primary">
                Next
              </Button>
            ) : (
              <Button type="submit" variant="primary" loading={saving}>
                Create Character
              </Button>
            )}
          </div>
        </div>
      </form>
    </Modal>
  );
}
