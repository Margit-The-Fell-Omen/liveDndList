import {useEffect, useState} from 'react';
import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import {Input} from '@/components/common/Input';
import {MAX_LEVEL, MIN_LEVEL, xpForLevel} from '@/utils/experience';
import styles from './SetLevelModal.module.css';

interface SetLevelModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentLevel: number;
  onConfirm: (xp: number) => void | Promise<void>;
  saving?: boolean;
}

export function SetLevelModal({
                                isOpen,
                                onClose,
                                currentLevel,
                                onConfirm,
                                saving = false,
                              }: SetLevelModalProps) {
  const [levelText, setLevelText] = useState(String(currentLevel));
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setLevelText(String(currentLevel));
      setError(null);
    }
  }, [isOpen, currentLevel]);

  const validateAndSubmit = async () => {
    const parsed = parseInt(levelText, 10);
    if (isNaN(parsed) || parsed < MIN_LEVEL || parsed > MAX_LEVEL) {
      setError(`Level must be between ${MIN_LEVEL} and ${MAX_LEVEL}.`);
      return;
    }
    setError(null);
    await onConfirm(xpForLevel(parsed));
  };

  const parsedPreview = (() => {
    const p = parseInt(levelText, 10);
    if (isNaN(p) || p < MIN_LEVEL || p > MAX_LEVEL) return null;
    return xpForLevel(p);
  })();

  const footer = (
      <div className={styles.footerActions}>
        <Button variant="secondary" onClick={onClose}>Cancel</Button>
        <Button onClick={validateAndSubmit} disabled={saving}>
          {saving ? 'Saving...' : 'Set Level'}
        </Button>
      </div>
  );

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title="Set Character Level"
          size="small"
          footer={footer}
      >
        <div className={styles.body}>
          <p className={styles.hint}>
            Enter the level you want to reach. Experience points will be set to the minimum XP for
            that level.
          </p>
          <Input
              label={`Level (${MIN_LEVEL}–${MAX_LEVEL})`}
              type="number"
              value={levelText}
              onChange={e => setLevelText(e.target.value)}
              error={error}
              min={MIN_LEVEL}
              max={MAX_LEVEL}
              fullWidth
          />
          {parsedPreview !== null && (
              <p className={styles.preview}>
                Experience will be set to <strong>{parsedPreview.toLocaleString()}</strong> XP.
              </p>
          )}
        </div>
      </Modal>
  );
}