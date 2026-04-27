// src/pages/MainPage.tsx
import {useCharacter} from '@/context/CharacterContext';
import {Card} from '@/components/common/Card';
import {CharacterHeader} from '@/components/character/CharacterHeader';
import {AbilityScore} from '@/components/character/AbilityScore';
import {SavingThrows} from '@/components/character/SavingThrows';
import {Skills} from '@/components/character/Skills';
import {HitPoints} from '@/components/character/HitPoints';
import {CombatStats} from '@/components/character/CombatStats';
import {Equipment} from '@/components/character/Equipment';
import {Features} from '@/components/character/Features';
import {Background} from '@/components/character/Background';
import {Spells} from '@/components/character/Spells';
import {ABILITIES} from '@/utils/constants';
import styles from './MainPage.module.css';

export function MainPage() {
  const {currentCharacter, loading} = useCharacter();

  if (loading && !currentCharacter) return <div className={styles.loading}>Loading
    character...</div>;
  if (!currentCharacter) return <div className={styles.empty}>Select a character to begin.</div>;

  return (
      <div className={styles.page}>
        <div className={styles.sheetGrid}>
          <CharacterHeader className={styles.header}/>

          <Card title="Ability Scores" className={styles.abilities}>
            {ABILITIES.map((abilityInfo) => (
                <AbilityScore
                    key={abilityInfo.key}
                    ability={abilityInfo}
                    score={currentCharacter.abilityScores[abilityInfo.key as keyof typeof currentCharacter.abilityScores]}
                    modifier={currentCharacter.abilityScores[`${abilityInfo.key}Modifier` as keyof typeof currentCharacter.abilityScores]}
                />
            ))}
          </Card>

          <SavingThrows className={styles.saves}/>

          <Background className={styles.features}/>

          <CombatStats className={styles.combat}/>
          <HitPoints className={styles.hp}/>
          <Spells className={styles.spells}/>

          <Features className={styles.notes}/>

          <Skills className={styles.skills}/>
          <Equipment className={styles.equipment}/>
        </div>
      </div>
  );
}
