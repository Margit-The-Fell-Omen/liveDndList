import { useState } from 'react';
import { useCharacter } from '../../context/CharacterContext';
import { Input, Select, TextArea } from '../common/Input';
import { Button } from '../common/Button';
import { Modal } from '../common/Modal';
import { ABILITIES } from '../../utils/constants';
import { getSpellSaveDC, getSpellAttackBonus } from '../../utils/dndCalculations';
import styles from './Spells.module.css';

export function Spells() {
  const { currentCharacter, updateCharacter, updateNestedCharacter } = useCharacter();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newSpell, setNewSpell] = useState({ name: '', level: 0, description: '' });

  if (!currentCharacter) return null;

  const { spells, abilities, proficiencyBonus } = currentCharacter;

  // Calculate spell save DC and attack bonus based on spellcasting ability
  const spellcastingAbility = spells.spellcastingAbility;
  const abilityScore = spellcastingAbility ? abilities[spellcastingAbility] : 10;
  const spellSaveDC = spellcastingAbility 
    ? getSpellSaveDC(abilityScore, proficiencyBonus) 
    : 0;
  const spellAttackBonus = spellcastingAbility 
    ? getSpellAttackBonus(abilityScore, proficiencyBonus) 
    : 0;

  const handleSlotChange = (level, used) => {
    updateNestedCharacter(`spells.slotsUsed.${level}`, 
      Math.max(0, Math.min(used, spells.slots[level]))
    );
  };

  const handleMaxSlotChange = (level, max) => {
    updateNestedCharacter(`spells.slots.${level}`, Math.max(0, max));
  };

  const addSpell = () => {
    if (!newSpell.name.trim()) return;

    const spellList = newSpell.level === 0 ? 'cantrips' : 'known';
    updateCharacter({
      spells: {
        ...spells,
        [spellList]: [...spells[spellList], {
          id: Date.now(),
          name: newSpell.name,
          level: newSpell.level,
          description: newSpell.description,
        }]
      }
    });

    setNewSpell({ name: '', level: 0, description: '' });
    setIsModalOpen(false);
  };

  const removeSpell = (spellId, isCantrip) => {
    const spellList = isCantrip ? 'cantrips' : 'known';
    updateCharacter({
      spells: {
        ...spells,
        [spellList]: spells[spellList].filter(s => s.id !== spellId)
      }
    });
  };

  return (
    <div className={styles.spells}>
      <div className={styles.header}>
        <h3 className={styles.title}>Spellcasting</h3>
        <Button variant="secondary" size="small" onClick={() => setIsModalOpen(true)}>
          + Add Spell
        </Button>
      </div>

      {/* Spellcasting Stats */}
      <div className={styles.spellcastingStats}>
        <Select
          label="Spellcasting Ability"
          value={spells.spellcastingAbility}
          onChange={(e) => updateNestedCharacter('spells.spellcastingAbility', e.target.value)}
          options={ABILITIES.map(a => ({ value: a.key, label: a.name }))}
          placeholder="Select ability"
        />
        
        <div className={styles.stat}>
          <span className={styles.statLabel}>Spell Save DC</span>
          <span className={styles.statValue}>{spellSaveDC || '-'}</span>
        </div>
        
        <div className={styles.stat}>
          <span className={styles.statLabel}>Spell Attack</span>
          <span className={styles.statValue}>
            {spellAttackBonus ? `+${spellAttackBonus}` : '-'}
          </span>
        </div>
      </div>

      {/* Spell Slots */}
      <div className={styles.spellSlots}>
        <h4 className={styles.sectionTitle}>Spell Slots</h4>
        <div className={styles.slotGrid}>
          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(level => (
            <div key={level} className={styles.slotRow}>
              <span className={styles.slotLevel}>{level}</span>
              <Input
                type="number"
                value={spells.slotsUsed[level]}
                onChange={(e) => handleSlotChange(level, parseInt(e.target.value, 10) || 0)}
                min="0"
                max={spells.slots[level]}
                className={styles.slotInput}
              />
              <span className={styles.slotSeparator}>/</span>
              <Input
                type="number"
                value={spells.slots[level]}
                onChange={(e) => handleMaxSlotChange(level, parseInt(e.target.value, 10) || 0)}
                min="0"
                className={styles.slotInput}
              />
            </div>
          ))}
        </div>
      </div>

      {/* Cantrips */}
      <div className={styles.spellList}>
        <h4 className={styles.sectionTitle}>Cantrips</h4>
        {spells.cantrips.length === 0 ? (
          <p className={styles.emptyMessage}>No cantrips known</p>
        ) : (
          spells.cantrips.map(spell => (
            <div key={spell.id} className={styles.spell}>
              <span className={styles.spellName}>{spell.name}</span>
              <button
                type="button"
                className={styles.removeButton}
                onClick={() => removeSpell(spell.id, true)}
              >
                ✕
              </button>
            </div>
          ))
        )}
      </div>

      {/* Known Spells by Level */}
      <div className={styles.spellList}>
        <h4 className={styles.sectionTitle}>Spells Known</h4>
        {spells.known.length === 0 ? (
          <p className={styles.emptyMessage}>No spells known</p>
        ) : (
          [...spells.known]
            .sort((a, b) => a.level - b.level)
            .map(spell => (
              <div key={spell.id} className={styles.spell}>
                <span className={styles.spellLevel}>Lvl {spell.level}</span>
                <span className={styles.spellName}>{spell.name}</span>
                <button
                  type="button"
                  className={styles.removeButton}
                  onClick={() => removeSpell(spell.id, false)}
                >
                  ✕
                </button>
              </div>
            ))
        )}
      </div>

      {/* Add Spell Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Add Spell"
        footer={
          <>
            <Button variant="ghost" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={addSpell} disabled={!newSpell.name.trim()}>
              Add Spell
            </Button>
          </>
        }
      >
        <div className={styles.form}>
          <Input
            label="Spell Name"
            value={newSpell.name}
            onChange={(e) => setNewSpell({ ...newSpell, name: e.target.value })}
            placeholder="Enter spell name"
            fullWidth
            autoFocus
          />
          <Select
            label="Spell Level"
            value={newSpell.level}
            onChange={(e) => setNewSpell({ ...newSpell, level: parseInt(e.target.value, 10) })}
            options={[
              { value: 0, label: 'Cantrip' },
              { value: 1, label: '1st Level' },
              { value: 2, label: '2nd Level' },
              { value: 3, label: '3rd Level' },
              { value: 4, label: '4th Level' },
              { value: 5, label: '5th Level' },
              { value: 6, label: '6th Level' },
              { value: 7, label: '7th Level' },
              { value: 8, label: '8th Level' },
              { value: 9, label: '9th Level' },
            ]}
            fullWidth
          />
          <TextArea
            label="Description (optional)"
            value={newSpell.description}
            onChange={(e) => setNewSpell({ ...newSpell, description: e.target.value })}
            placeholder="Spell description, components, etc."
            rows={3}
            fullWidth
          />
        </div>
      </Modal>
    </div>
  );
}
