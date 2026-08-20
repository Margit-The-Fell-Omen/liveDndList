// src/components/character/BackgroundPickerModal.tsx
import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import {StepBackground} from './wizard/StepBackground';
import type {Background} from '@/types';
import styles from './BackgroundPickerModal.module.css';

interface BackgroundPickerModalProps {
  isOpen: boolean;
  onClose: () => void;
  backgrounds: Background[];
  initialBackgroundKey: string;
  onConfirm: (backgroundKey: string) => void | Promise<void>;
  saving?: boolean;
}

export function BackgroundPickerModal({
                                        isOpen,
                                        onClose,
                                        backgrounds,
                                        initialBackgroundKey,
                                        onConfirm,
                                        saving = false,
                                      }: BackgroundPickerModalProps) {
  const [selectedKey, setSelectedKey] = useState<string>(initialBackgroundKey);

  useEffect(() => {
    if (isOpen) {
      setSelectedKey(initialBackgroundKey);
    }
  }, [isOpen, initialBackgroundKey]);

  const canConfirm = selectedKey.trim().length > 0;

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button
            onClick={() => onConfirm(selectedKey)}
            disabled={!canConfirm || saving}
            loading={saving}
        >
          {saving ? 'Saving...' : 'Confirm'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title="Change Background"
          size="large"
          footer={footer}
      >
        <StepBackground
            backgrounds={backgrounds}
            selectedKey={selectedKey}
            onSelect={setSelectedKey}
        />
      </Modal>
  );
}