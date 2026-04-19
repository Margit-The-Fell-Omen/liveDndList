import {Modal} from '@/components/common/Modal';
import {Button} from '@/components/common/Button';
import type {SpellResponse} from '@/types';
import styles from './SpellInfoModal.module.css';

interface SpellInfoModalProps {
  isOpen: boolean;
  onClose: () => void;
  spell: SpellResponse | null;
}

export function SpellInfoModal({isOpen, onClose, spell}: SpellInfoModalProps) {
  if (!isOpen || !spell) {
    return null;
  }

  return (
      <Modal
          isOpen={isOpen}
          onClose={onClose}
          title={spell.name}
          size="medium"
          footer={<Button onClick={onClose}>Close</Button>}
      >
        <div className={styles.container}>
          <div className={styles.detailsGrid}>
            <div className={styles.detailItem}>
              <span className={styles.label}>Level</span>
              <span className={styles.value}>{spell.level === 0 ? 'Cantrip' : spell.level}</span>
            </div>
            <div className={styles.detailItem}>
              <span className={styles.label}>School</span>
              <span className={styles.value}>{spell.school}</span>
            </div>
            <div className={styles.detailItem}>
              <span className={styles.label}>Casting Time</span>
              <span className={styles.value}>{spell.castingTime}</span>
            </div>
            <div className={styles.detailItem}>
              <span className={styles.label}>Range</span>
              <span className={styles.value}>{spell.range}</span>
            </div>
            <div className={styles.detailItem}>
              <span className={styles.label}>Duration</span>
              <span
                  className={styles.value}>{spell.duration}{spell.concentration && ' (Concentration)'}</span>
            </div>
            <div className={styles.detailItem}>
              <span className={styles.label}>Ritual</span>
              <span className={styles.value}>{spell.ritual ? 'Yes' : 'No'}</span>
            </div>
          </div>

          <div className={styles.descriptionBlock}>
            <p className={styles.label}>Components</p>
            <p>{spell.components}</p>
          </div>

          <div className={styles.descriptionBlock}>
            <p className={styles.label}>Description</p>
            <p>{spell.description}</p>
          </div>

          {spell.higherLevels && (
              <div className={styles.descriptionBlock}>
                <p className={styles.label}>At Higher Levels</p>
                <p>{spell.higherLevels}</p>
              </div>
          )}
        </div>
      </Modal>
  );
}