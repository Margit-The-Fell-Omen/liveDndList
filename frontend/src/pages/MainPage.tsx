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
import type {AbilityName, AbilityScoresResponse} from '@/types';
import styles from './MainPage.module.css';

export function MainPage() {
  // Get the necessary data and functions from the context
  const {currentCharacter, updateCharacter, loading} = useCharacter();

  // --- Render Loading and Empty States ---
  // Show a full-page loader only while the app is initially fetching data
  if (loading && !currentCharacter) {
    return (
        <div className={styles.loading}>
          <div className={styles.spinner}/>
          <p>Summoning character sheet...</p>
        </div>
    );
  }

  // Show a welcome/placeholder message if no character is selected
  if (!currentCharacter) {
    return (
        <div className={styles.empty}>
          <div className={styles.emptyIcon}>🎲</div>
          <h2>No Character Selected</h2>
          <p>Create a new character or select one from the sidebar to get started.</p>
        </div>
    );
  }


  // --- Event Handlers ---

  /**
   * Handles changes to an ability score.
   * This function now correctly calls `updateCharacter` from the context.
   */
  const handleAbilityChange = (abilityKey: keyof AbilityScoresResponse, value: number) => {
    // We create a new abilityScores object with the updated value
    const newAbilityScores = {
      ...currentCharacter.abilityScores,
      [abilityKey]: value,
    };
    // We call the updateCharacter function from our context
    updateCharacter(currentCharacter.id, {abilityScores: newAbilityScores});
  };

  /**
   * Toggles proficiency for a saving throw.
   * This now uses the `savingThrowProficiencies` array correctly.
   */
  const handleSavingThrowToggle = (abilityName: AbilityName) => {
    const currentProficiencies = currentCharacter.savingThrowProficiencies || [];
    const isProficient = currentProficiencies.includes(abilityName);

    // Create the new array of proficiencies
    const newProficiencies = isProficient
        ? currentProficiencies.filter(p => p !== abilityName) // Remove if it exists
        : [...currentProficiencies, abilityName]; // Add if it doesn't

    // updateCharacter(currentCharacter.id, { savingThrowProficiencies: newProficiencies });
    // Note: The backend needs to support updating this field. For now, we'll log it.
    console.log(`Toggling saving throw proficiency for ${abilityName}. New list:`, newProficiencies);
    console.warn("Saving throw updates need to be implemented in the backend and CharacterUpdateRequest DTO.");
  };


  // --- Render Component ---

  return (
      <div className={styles.page}>
        {/* CharacterHeader likely uses `currentCharacter` from the context directly */}
        <CharacterHeader/>

        <div className={styles.grid}>
          {/* Left Column */}
          <div className={styles.leftColumn}>
            <section className={styles.section}>
              <h3 className={styles.sectionTitle}>Ability Scores</h3>
              <div className={styles.abilityGrid}>
                {ABILITIES.map((abilityInfo) => {
                  // Type-safe access to the ability score value (e.g., 'strength')
                  const scoreKey = abilityInfo.key as keyof AbilityScoresResponse;
                  const scoreValue = currentCharacter.abilityScores[scoreKey];

                  // The ability name for proficiency check is in uppercase (e.g., 'STRENGTH')
                  const abilityName = abilityInfo.key.toUpperCase() as AbilityName;
                  const isProficient = currentCharacter.savingThrowProficiencies.includes(abilityName);

                  return (
                      <AbilityScore
                          ability={abilityInfo}
                          value={scoreValue}
                          onChange={(key, value) => handleAbilityChange(key as keyof AbilityScoresResponse, Number(value))}
                          proficient={isProficient}
                          onProficiencyChange={() => handleSavingThrowToggle(abilityName)}
                      />
                  );
                })}
              </div>
            </section>

            {/* These components likely also use the `useCharacter` hook internally */}
            <SavingThrows/>
            <Skills/>
          </div>

          {/* Middle Column */}
          <div className={styles.middleColumn}>
            <CombatStats/>
            <HitPoints/>
            <DeathSaves/>
            <Features/>
          </div>

          {/* Right Column */}
          <div className={styles.rightColumn}>
            <Equipment/>
            <Spells/>
            <Background/>
          </div>
        </div>
      </div>
  );
}
