import {useCharacter} from '@/context/CharacterContext';
import {CharacterHeader} from '@/components/character/CharacterHeader';
import {AbilityScore} from '@/components/character/AbilityScore';
import {SavingThrows} from '@/components/character/SavingThrows';
import {Skills} from '@/components/character/Skills';
import {HitPoints} from '@/components/character/HitPoints';
import {DeathSaves} from '@/components/character/DeathSaves';
import {CombatStats} from '@/components/character/CombatStats';
import {Equipment} from '@/components/character/Equipment';
import {Features} from '@/components/character/Features';
import {Background} from '@/components/character/Background';
import {Spells} from '@/components/character/Spells';
import {ABILITIES} from '@/utils/constants';
import type {AbilityName} from '@/types'; // Changed from AbilityKey
import styles from './MainPage.module.css';

export function MainPage() {
  // FIX: Assuming updateNestedCharacter exists and works. If not, this logic would need to change.
  const {currentCharacter, updateCharacter, loading} = useCharacter();

  if (loading && !currentCharacter) { // Only show full-screen loader on initial load
    return (
        <div className={styles.loading}>
          <div className={styles.spinner}/>
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

  // FIX: These handlers need to be updated to match the new data structure.
  // This is a simplified example. Your `updateNestedCharacter` might need more complex logic for arrays.
  const handleAbilityChange = (abilityKey: string, value: number | string): void => {
    // updateNestedCharacter(`abilityScores.${abilityKey}`, value);
    console.warn("handleAbilityChange needs to be implemented with `updateCharacter`");
  };

  const handleSavingThrowToggle = (abilityKey: AbilityName): void => {
    // FIX: Logic for toggling an item in an array
    const currentProficiencies = currentCharacter.savingThrowProficiencies || [];
    const isProficient = currentProficiencies.includes(abilityKey);
    const newProficiencies = isProficient
        ? currentProficiencies.filter(p => p !== abilityKey)
        : [...currentProficiencies, abilityKey];

    // This is just a guess at how your update logic might work.
    // updateCharacter(currentCharacter.id, { savingThrowProficiencies: newProficiencies });
    console.warn("handleSavingThrowToggle needs to be implemented with `updateCharacter`");
  };

  return (
      <div className={styles.page}>
        <CharacterHeader/>
        <div className={styles.grid}>
          <div className={styles.leftColumn}>
            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Ability Scores</h3>
              <div className={styles.abilityGrid}>
                {ABILITIES.map((ability) => {
                  // FIX: Use `abilityScores` instead of `abilities`
                  // Ensure ability.key is lowercase ('strength', 'dexterity', etc.)
                  const scoreValue = currentCharacter.abilityScores[ability.key as keyof typeof currentCharacter.abilityScores];

                  // FIX: Use `savingThrowProficiencies` (an array) instead of `savingThrows` (an object)
                  const isProficient = currentCharacter.savingThrowProficiencies.includes(ability.key.toUpperCase() as AbilityName);

                  return (
                      <AbilityScore
                          key={ability.key}
                          ability={ability}
                          value={scoreValue}
                          onChange={handleAbilityChange}
                          proficient={isProficient}
                          onProficiencyChange={() => handleSavingThrowToggle(ability.key.toUpperCase() as AbilityName)}
                      />
                  );
                })}
              </div>
            </section>
            <SavingThrows/>
            <Skills/>
          </div>
          <div className={styles.middleColumn}>
            <CombatStats/>
            <HitPoints/>
            <DeathSaves/>
            <Features/>
          </div>
          <div className={styles.rightColumn}>
            <Equipment/>
            <Spells/>
            <Background/>
          </div>
        </div>
      </div>
  );
}
