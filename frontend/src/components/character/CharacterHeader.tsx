import {type ChangeEvent, useEffect, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Input, Select} from '@/components/common/Input';
import {Button} from '@/components/common/Button';
import {ALIGNMENT_OPTIONS} from '@/utils/constants';
import {useDebouncedCallback} from '@/hooks/useDebounce';
import {RacePickerModal} from './RacePickerModal';
import {ClassManagerModal} from './ClassManagerModal';
import {ClassPickerModal} from './ClassPickerModal';
import {LevelUpModal} from './LevelUpModal';
import {LevelDownModal} from './LevelDownModal';
import {SetLevelModal} from './SetLevelModal';
import {getRaceDisplayName} from '@/utils/races';
import {excessLevels, formatClassLevels, pendingLevels, totalLevelOf} from '@/utils/classes';
import {levelForXp} from '@/utils/experience';
import type {DndClassLevel} from '@/types';
import styles from './CharacterHeader.module.css';

export function CharacterHeader({className}: { className?: string }) {
  const {
    currentCharacter,
    backgrounds,
    races,
    classes,
    updateCharacter,
    saving,
  } = useCharacter();

  const [characterName, setCharacterName] = useState('');
  const [backgroundKey, setBackgroundKey] = useState<string>('');
  const [alignment, setAlignment] = useState<string>('');
  const [experience, setExperience] = useState(0);

  const [isRacePickerOpen, setIsRacePickerOpen] = useState(false);
  const [isClassManagerOpen, setIsClassManagerOpen] = useState(false);
  const [isLevelUpOpen, setIsLevelUpOpen] = useState(false);
  const [isLevelDownOpen, setIsLevelDownOpen] = useState(false);
  const [isAddClassOpen, setIsAddClassOpen] = useState(false);
  const [isSetLevelOpen, setIsSetLevelOpen] = useState(false);

  const debouncedUpdate = useDebouncedCallback(
      (payload: object) => {
        if (currentCharacter) updateCharacter(currentCharacter.id, payload);
      },
      500
  );

  useEffect(() => {
    if (currentCharacter) {
      setCharacterName(currentCharacter.name);
      setBackgroundKey(currentCharacter.backgroundKey);
      setAlignment(currentCharacter.alignment);
      setExperience(currentCharacter.experiencePoints);
    }
  }, [currentCharacter]);

  if (!currentCharacter) return null;

  const backgroundOptions = backgrounds.map(bg => ({value: bg.key, label: bg.name}));
  const raceDisplay =
      getRaceDisplayName(races, currentCharacter.raceKey) || currentCharacter.raceKey || 'N/A';

  const classDisplay = formatClassLevels(classes, currentCharacter.classesInfo);
  const takenClassKeys = currentCharacter.classesInfo.map(entry => entry.classKey);

  const earnedLevel = levelForXp(currentCharacter.experiencePoints);
  const assignedLevel = totalLevelOf(currentCharacter.classesInfo);
  const pending = pendingLevels(earnedLevel, currentCharacter.classesInfo);
  const excess = excessLevels(earnedLevel, currentCharacter.classesInfo);
  const hasPending = pending > 0;
  const hasExcess = excess > 0;

  const handleNameChange = (e: ChangeEvent<HTMLInputElement>) => {
    setCharacterName(e.target.value);
    debouncedUpdate({name: e.target.value});
  };

  const handleBackgroundChange = (e: ChangeEvent<HTMLSelectElement>) => {
    setBackgroundKey(e.target.value);
    debouncedUpdate({backgroundKey: e.target.value});
  };

  const handleAlignmentChange = (e: ChangeEvent<HTMLSelectElement>) => {
    setAlignment(e.target.value);
    debouncedUpdate({alignment: e.target.value});
  };

  const handleExperienceChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value, 10) || 0;
    setExperience(value);
    debouncedUpdate({experiencePoints: value});
  };

  const handleRaceConfirm = async (raceKey: string) => {
    await updateCharacter(currentCharacter.id, {raceKey});
    setIsRacePickerOpen(false);
  };

  const persistClassList = async (nextClasses: DndClassLevel[]) => {
    await updateCharacter(currentCharacter.id, {dndClassLevels: nextClasses});
  };

  const handleSetLevel = async (xp: number) => {
    await updateCharacter(currentCharacter.id, {experiencePoints: xp});
    setExperience(xp);
    setIsSetLevelOpen(false);
  };

  const handleLevelUpExisting = async (classKey: string) => {
    const next = currentCharacter.classesInfo.map(entry =>
        entry.classKey === classKey ? {...entry, level: entry.level + 1} : entry
    );
    await persistClassList(next);
    setIsLevelUpOpen(false);
  };

  const handleLevelDown = async (classKey: string) => {
    const entry = currentCharacter.classesInfo.find(e => e.classKey === classKey);
    if (!entry) return;

    const next = entry.level <= 1
        ? currentCharacter.classesInfo.filter(e => e.classKey !== classKey)
        : currentCharacter.classesInfo.map(e =>
            e.classKey === classKey ? {...e, level: e.level - 1} : e
        );

    await persistClassList(next);
    setIsLevelDownOpen(false);
  };

  const handleAddNewClass = async (classKey: string) => {
    const next: DndClassLevel[] = [
      ...currentCharacter.classesInfo,
      {classKey, level: 1},
    ];
    await persistClassList(next);
    setIsAddClassOpen(false);
  };

  const handlePrimaryLevelButton = () => {
    if (hasPending) setIsLevelUpOpen(true);
    else if (hasExcess) setIsLevelDownOpen(true);
    else setIsSetLevelOpen(true);
  };

  return (
      <>
        <div className={`${styles.header} ${className ?? ''}`}>
          <div className={styles.mainInfo}>
            <Input
                name="name"
                value={characterName}
                onChange={handleNameChange}
                placeholder="Character Name"
                className={styles.nameInput}
            />
          </div>

          <div className={styles.secondaryInfo}>
            <div className={styles.raceField}>
              <label className={styles.raceLabel}>Race</label>
              <button
                  type="button"
                  className={styles.raceButton}
                  onClick={() => setIsRacePickerOpen(true)}
              >
                <span className={styles.raceValue}>{raceDisplay}</span>
                <span className={styles.raceEditIcon} aria-hidden="true">✎</span>
              </button>
            </div>

            <div className={styles.raceField}>
              <label className={styles.raceLabel}>
                Class & Level
                {hasPending && (
                    <span className={styles.pendingBadge}>
                      +{pending} pending
                    </span>
                )}
                {hasExcess && (
                    <span className={styles.excessBadge}>
                      -{excess} excess
                    </span>
                )}
              </label>
              <button
                  type="button"
                  className={styles.raceButton}
                  onClick={() => setIsClassManagerOpen(true)}
              >
                <span className={styles.raceValue}>
                  {classDisplay || 'N/A'}
                  {` — Total ${assignedLevel}`}
                </span>
                <span className={styles.raceEditIcon} aria-hidden="true">✎</span>
              </button>
            </div>

            <Select
                label="Background"
                name="background"
                value={backgroundKey}
                onChange={handleBackgroundChange}
                options={backgroundOptions}
            />
            <Select
                label="Alignment"
                name="alignment"
                value={alignment}
                onChange={handleAlignmentChange}
                options={ALIGNMENT_OPTIONS}
            />
            <Input
                label="Experience"
                name="experiencePoints"
                type="number"
                value={experience}
                onChange={handleExperienceChange}
            />

            <div className={styles.raceField}>
              <label className={styles.raceLabel}>&nbsp;</label>
              <Button
                  variant={hasPending ? 'primary' : hasExcess ? 'danger' : 'secondary'}
                  onClick={handlePrimaryLevelButton}
                  disabled={saving}
              >
                {hasPending ? 'Apply Level' : hasExcess ? 'Remove Level' : 'Set Level'}
              </Button>
            </div>
          </div>
        </div>

        <RacePickerModal
            isOpen={isRacePickerOpen}
            onClose={() => setIsRacePickerOpen(false)}
            races={races}
            initialRaceKey={currentCharacter.raceKey}
            onConfirm={handleRaceConfirm}
            saving={saving}
        />

        <ClassManagerModal
            isOpen={isClassManagerOpen}
            onClose={() => setIsClassManagerOpen(false)}
            classes={classes}
            currentClasses={currentCharacter.classesInfo}
            onSave={persistClassList}
            saving={saving}
        />

        <SetLevelModal
            isOpen={isSetLevelOpen}
            onClose={() => setIsSetLevelOpen(false)}
            currentLevel={earnedLevel}
            onConfirm={handleSetLevel}
            saving={saving}
        />

        <LevelUpModal
            isOpen={isLevelUpOpen}
            onClose={() => setIsLevelUpOpen(false)}
            classes={classes}
            currentClasses={currentCharacter.classesInfo}
            pendingLevels={pending}
            onExistingClassLevelUp={handleLevelUpExisting}
            onAddNewClass={() => {
              setIsLevelUpOpen(false);
              setIsAddClassOpen(true);
            }}
            saving={saving}
        />

        <LevelDownModal
            isOpen={isLevelDownOpen}
            onClose={() => setIsLevelDownOpen(false)}
            classes={classes}
            currentClasses={currentCharacter.classesInfo}
            excessLevels={excess}
            onLevelDown={handleLevelDown}
            saving={saving}
        />

        <ClassPickerModal
            isOpen={isAddClassOpen}
            onClose={() => setIsAddClassOpen(false)}
            classes={classes}
            title="Multiclass: Pick a New Class"
            disabledKeys={takenClassKeys}
            onConfirm={handleAddNewClass}
            saving={saving}
        />
      </>
  );
}
