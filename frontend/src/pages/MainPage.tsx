import { useCharacter } from '@/context/CharacterContext';
import { CharacterHeader } from '@/components/character/CharacterHeader';
import { AbilityScore } from '@/components/character/AbilityScore';
import { SavingThrows } from '@/components/character/SavingThrows';
import { Skills } from '@/components/character/Skills';
import { HitPoints } from '@/components/character/HitPoints';
import { DeathSaves } from '@/components/character/DeathSaves';
import { CombatStats } from '@/components/character/CombatStats';
import { Equipment } from '@/components/character/Equipment';
import { Features } from '@/components/character/Features';
import { Background } from '@/components/character/Background';
import { Spells } from '@/components/character/Spells';
import { ABILITIES } from '@/utils/constants';
import type { AbilityKey } from '@/types';
import styles from './MainPage.module.css';

export function MainPage() {
  const { currentCharacter, updateNestedCharacter, loading } = useCharacter();

  if (loading) {
    return (
      <div className={styles.loading}>
        <div className={styles.spinner} />
        <p>Loading character...</p>
      </div>
    );
  }

  if (!currentCharacter) {
    return (
      <div className={styles.empty}>
        <div className={styles.emptyIcon}>🎲</div>
        <h2>No Character Selected</h2>
        <p>Create a new character or select one from the sidebar to get started.</p>
      </div>
    );
  }

  const handleAbilityChange = (abilityKey: string, value: number | string): void => {
    updateNestedCharacter(`abilities.${abilityKey}`, value);
  };

  const handleSavingThrowToggle = (abilityKey: string): void => {
    const current = currentCharacter.savingThrows[abilityKey as AbilityKey];
    updateNestedCharacter(`savingThrows.${abilityKey}`, !current);
  };

  return (
    <div className={styles.page}>
      {/* Character Header */}
      <CharacterHeader />

      {/* Main Grid Layout */}
      <div className={styles.grid}>
        {/* Left Column */}
        <div className={styles.leftColumn}>
          {/* Ability Scores */}
          <section className={styles.section}>
            <h3 className={styles.sectionTitle}>Ability Scores</h3>
            <div className={styles.abilityGrid}>
              {ABILITIES.map((ability) => (
                <AbilityScore
                  key={ability.key}
                  ability={ability}
                  value={currentCharacter.abilities[ability.key]}
                  onChange={handleAbilityChange}
                  proficient={currentCharacter.savingThrows[ability.key]}
                  onProficiencyChange={handleSavingThrowToggle}
                />
              ))}
            </div>
          </section>

          {/* Saving Throws */}
          <SavingThrows />

          {/* Skills */}
          <Skills />
        </div>

        {/* Middle Column */}
        <div className={styles.middleColumn}>
          {/* Combat Stats */}
          <CombatStats />

          {/* Hit Points */}
          <HitPoints />

          {/* Death Saves */}
          <DeathSaves />

          {/* Features & Traits */}
          <Features />
        </div>

        {/* Right Column */}
        <div className={styles.rightColumn}>
          {/* Equipment */}
          <Equipment />

          {/* Spells */}
          <Spells />

          {/* Background & Personality */}
          <Background />
        </div>
      </div>
    </div>
  );
}
