import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import {StepRace} from './wizard/StepRace';
import type {Race} from '@/types';
import {
  createRaceSelectionFromRaceKey,
  isRaceSelectionComplete,
  type RaceSelection,
} from '@/utils/races';
import styles from './RacePickerModal.module.css';

interface RacePickerModalProps {
  isOpen: boolean;
  onClose: () => void;
  races: Race[];
  initialRaceKey: string;
  onConfirm: (raceKey: string) => void | Promise<void>;
  saving?: boolean;
}

export function RacePickerModal({
                                  isOpen,
                                  onClose,
                                  races,
                                  initialRaceKey,
                                  onConfirm,
                                  saving = false,
                                }: RacePickerModalProps) {
  const [selection, setSelection] = useState<RaceSelection>(
      createRaceSelectionFromRaceKey(races, initialRaceKey)
  );

  useEffect(() => {
    if (isOpen) {
      setSelection(createRaceSelectionFromRaceKey(races, initialRaceKey));
    }
  }, [isOpen, races, initialRaceKey]);

  const canConfirm = isRaceSelectionComplete(races, selection);

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button
            onClick={() => onConfirm(selection.raceKey)}
            disabled={!canConfirm || saving}
        >
          {saving ? 'Saving...' : 'Confirm'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title="Change Race"
          size="large"
          footer={footer}
      >
        <StepRace races={races} selection={selection} onSelect={setSelection}/>
      </Modal>
  );
}
