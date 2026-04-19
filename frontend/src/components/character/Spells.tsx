// src/components/character/Spells.tsx

import {type ChangeEvent, useMemo, useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Button} from '@/components/common/Button';
import {ConfirmModal} from '@/components/common/Modal';
import {Select} from '@/components/common/Input';
import {ManageSpellsModal} from './ManageSpellsModal';
import {ABILITIES} from '@/utils/constants';
import {getSpellAttackBonus, getSpellSaveDC} from '@/utils/helpers';
import type {AbilityName, SpellResponse} from '@/types';
import styles from './Spells.module.css';
import {Card} from '@/components/common/Card';
import {SpellInfoModal} from "@components/character/SpellInfoModal.tsx";

// THE FIX: Provide the implementation for this helper function
const groupSpellsByLevel = (spells: SpellResponse[]) => {
  return spells.reduce((acc, spell) => {
    const level = spell.level;
    if (!acc[level]) {
      acc[level] = [];
    }
    acc[level].push(spell);
    return acc;
  }, {} as Record<number, SpellResponse[]>);
};

export function Spells({className}: { className?: string }) {
  const {currentCharacter, updateCharacter, removeSpellFromCharacter} = useCharacter();

  const [isManageModalOpen, setIsManageModalOpen] = useState(false);
  const [spellToView, setSpellToView] = useState<SpellResponse | null>(null);
  const [spellToRemove, setSpellToRemove] = useState<SpellResponse | null>(null);

  if (!currentCharacter) return null;

  const {spells, spellcastingAbility, abilityScores, proficiencyBonus} = currentCharacter;

  const spellcastingAbilityScore = spellcastingAbility ? abilityScores[spellcastingAbility.toLowerCase() as keyof typeof abilityScores] : 10;
  const spellSaveDC = getSpellSaveDC(spellcastingAbilityScore, proficiencyBonus);
  const spellAttackBonus = getSpellAttackBonus(spellcastingAbilityScore, proficiencyBonus);

  const spellsByLevel = useMemo(() => groupSpellsByLevel(spells || []), [spells]);
  const spellLevels = Object.keys(spellsByLevel).map(Number).sort();

  const handleSpellcastingAbilityChange = (e: ChangeEvent<HTMLSelectElement>) => {
    updateCharacter(currentCharacter.id, {spellcastingAbility: e.target.value as AbilityName});
  };

  const handleOpenDeleteConfirm = (e: React.MouseEvent, spell: SpellResponse) => {
    e.stopPropagation();
    setSpellToRemove(spell);
  };

  const handleConfirmDelete = async () => {
    if (spellToRemove) {
      await removeSpellFromCharacter(spellToRemove.id);
    }
    setSpellToRemove(null);
  };

  return (
      <>
        <Card title="Spellcasting" className={className}>
          {/* THE FIX: Removed the outer header div and duplicate h3 */}
          <div className={styles.spellcastingStats}>
            <Select
                label="Spellcasting Ability"
                value={spellcastingAbility || ''}
                onChange={handleSpellcastingAbilityChange}
                options={ABILITIES.map((a) => ({value: a.key.toUpperCase(), label: a.name}))}
                placeholder="None"
            />

            <div className={styles.stat}>
              <span className={styles.statLabel}>Spell Save DC</span>
              <span className={styles.statValue}>{spellSaveDC}</span>
            </div>

            <div className={styles.stat}>
              <span className={styles.statLabel}>Spell Attack</span>
              <span className={styles.statValue}>
              {spellAttackBonus >= 0 ? `+${spellAttackBonus}` : spellAttackBonus}
            </span>
            </div>
          </div>

          {/* This header is now for the list itself */}
          <div className={styles.listHeader}>
            <h4>Known Spells</h4>
            <Button size="small" onClick={() => setIsManageModalOpen(true)}>
              Add / Manage Spells
            </Button>
          </div>

          <div className={styles.spellListContainer}>
            {(spells || []).length > 0 ? (
                spellLevels.map((level) => (
                    <div key={level} className={styles.spellLevelGroup}>
                      <h4 className={styles.sectionTitle}>{level === 0 ? 'Cantrips' : `Level ${level}`}</h4>
                      {spellsByLevel[level].map((spell) => (
                          // THIS IS NOW A CLICKABLE DIV
                          <div
                              key={spell.id}
                              className={styles.spell}
                              onClick={() => setSpellToView(spell)} // Set the spell to view on click
                              role="button"
                              tabIndex={0}
                              onKeyDown={(e) => {
                                if (e.key === 'Enter' || e.key === ' ') setSpellToView(spell);
                              }}
                          >
                            <span className={styles.spellName}>{spell.name}</span>
                            <button
                                type="button"
                                className={styles.removeButton}
                                onClick={(e) => handleOpenDeleteConfirm(e, spell)}
                            >
                              &times;
                            </button>
                          </div>
                      ))}
                    </div>
                ))
            ) : (
                <p className={styles.emptyMessage}>No spells known.</p>
            )}
          </div>
        </Card>

        <ManageSpellsModal isOpen={isManageModalOpen} onClose={() => setIsManageModalOpen(false)}/>

        {/* NEW: Spell Info Modal */}
        <SpellInfoModal
            isOpen={spellToView !== null}
            onClose={() => setSpellToView(null)}
            spell={spellToView}
        />

        {/* Confirmation Modal (logic slightly changed) */}
        <ConfirmModal
            isOpen={spellToRemove !== null}
            onClose={() => setSpellToRemove(null)}
            onConfirm={handleConfirmDelete}
            title="Forget Spell"
            message={`Are you sure you want to forget the spell "${spellToRemove?.name}"?`}
            variant="danger"
            confirmText="Forget"
        />

      </>
  );
}
