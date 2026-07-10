import type {Race} from '@/types';
import {SelectionCard} from './SelectionCard';
import {MarkdownContent} from './MarkdownContent';
import styles from './StepGrid.module.css';

interface StepRaceProps {
  races: Race[];
  selectedKey: string;
  onSelect: (key: string) => void;
}

function RaceDetailPanel({race, allRaces}: { race: Race; allRaces: Race[] }) {
  const traits = race.traits ?? [];
  const speedTrait = traits.find(t => t.type === 'SPEED');
  const sizeTrait = traits.find(t => t.type === 'SIZE');
  const narrativeTraits = traits.filter(t => t.type !== 'SPEED' && t.type !== 'SIZE');

  const subspecies = allRaces.filter(r => r.is_subspecies && r.subspecies_of === race.key);

  return (
      <div className={styles.detailPanel}>
        <div className={styles.detailHeader}>
          <h3 className={styles.detailTitle}>{race.name}</h3>
        </div>
        <div className={styles.detailBadges}>
          {sizeTrait?.desc && <span className={styles.detailBadge}>{sizeTrait.desc}</span>}
          {speedTrait?.desc && <span className={styles.detailBadge}>Speed: {speedTrait.desc}</span>}
        </div>
        {race.desc && <MarkdownContent text={race.desc}/>}

        {narrativeTraits.length > 0 && (
            <div className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>Racial Traits</h4>
              <div className={styles.traitList}>
                {narrativeTraits.map((trait, idx) => (
                    <div key={`${trait.name}-${idx}`} className={styles.traitItem}>
                      <div className={styles.traitName}>{trait.name}</div>
                      <MarkdownContent text={trait.desc}/>
                    </div>
                ))}
              </div>
            </div>
        )}

        {subspecies.length > 0 && (
            <div className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>Known Subspecies</h4>
              <p className={styles.subInfoNote}>
                Subspecies are informational only. Only base races can be selected.
              </p>
              <div className={styles.subInfoGrid}>
                {subspecies.map(sub => (
                    <div key={sub.key} className={styles.subInfoCard}>
                      <div className={styles.subInfoTitle}>{sub.name}</div>
                      {sub.desc && (
                          <div className={styles.subInfoDesc}>
                            <MarkdownContent text={sub.desc}/>
                          </div>
                      )}
                    </div>
                ))}
              </div>
            </div>
        )}
      </div>
  );
}

export function StepRace({races, selectedKey, onSelect}: StepRaceProps) {
  const baseRaces = races.filter(r => !r.is_subspecies);

  const buildBadges = (race: Race): string[] => {
    const traits = race.traits ?? [];
    const speed = traits.find(t => t.type === 'SPEED');
    const size = traits.find(t => t.type === 'SIZE');
    return [
      size?.desc,
      speed ? `Speed: ${speed.desc}` : undefined,
    ].filter(Boolean) as string[];
  };

  const getDescription = (race: Race): string | undefined => {
    const traits = race.traits ?? [];
    return race.desc || traits.find(t => !t.type)?.desc;
  };

  return (
      <div className={styles.container}>
        <p className={styles.hint}>
          Select your character's race. Only base races can be chosen — subspecies are shown as
          informational reference within each race.
        </p>
        <div className={styles.grid}>
          {baseRaces.map(race => {
            const isSelected = selectedKey === race.key;
            return (
                <>
                  <SelectionCard
                      key={race.key}
                      title={race.name}
                      badges={buildBadges(race)}
                      description={getDescription(race)}
                      isSelected={isSelected}
                      onClick={() => onSelect(race.key)}
                  />
                  {isSelected && (
                      <RaceDetailPanel key={`${race.key}-detail`} race={race} allRaces={races}/>
                  )}
                </>
            );
          })}
        </div>
      </div>
  );
}
