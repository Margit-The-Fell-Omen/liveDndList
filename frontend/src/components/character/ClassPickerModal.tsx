import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import {StepClass} from './wizard/StepClass';
import type {CharacterClass} from '@/types';
import styles from './ClassPickerModal.module.css';

interface ClassPickerModalProps {
  isOpen: boolean;
  onClose: () => void;
  classes: CharacterClass[];
  title: string;
  disabledKeys?: string[];
  initialClassKey?: string;
  onConfirm: (classKey: string) => void | Promise<void>;
  saving?: boolean;
}

export function ClassPickerModal({
                                   isOpen,
                                   onClose,
                                   classes,
                                   title,
                                   disabledKeys = [],
                                   initialClassKey = '',
                                   onConfirm,
                                   saving = false,
                                 }: ClassPickerModalProps) {
  const [selectedKey, setSelectedKey] = useState(initialClassKey);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setSelectedKey(initialClassKey);
      setError(null);
    }
  }, [isOpen, initialClassKey]);

  const availableClasses = classes.filter(cls => {
    if (cls.key === initialClassKey) return true;
    return !disabledKeys.includes(cls.key);
  });

  const canConfirm = !!selectedKey && !saving;

  const handleConfirm = async () => {
    try {
      setError(null);
      await onConfirm(selectedKey);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to apply class change.');
    }
  };

  const footer = (
      <div className={styles.footerActions}>
        {error && <span className={styles.errorText}>{error}</span>}
        <Button variant="secondary" onClick={onClose}>Cancel</Button>
        <Button onClick={handleConfirm} disabled={!canConfirm}>
          {saving ? 'Saving...' : 'Confirm'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={title}
          size="large"
          footer={footer}
      >
        {availableClasses.length === 0 ? (
            <p className={styles.emptyMessage}>
              No classes available. You are already using every class.
            </p>
        ) : (
            <StepClass
                key={isOpen ? 'open' : 'closed'}
                classes={availableClasses}
                selectedKey={selectedKey}
                onSelect={setSelectedKey}
            />
        )}
      </Modal>
  );
}
