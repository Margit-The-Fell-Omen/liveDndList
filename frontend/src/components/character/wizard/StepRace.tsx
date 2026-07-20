import {Fragment} from 'react';
import type {Race} from '@/types';
import {SelectionCard} from './SelectionCard';
import {MarkdownContent} from '../../common/MarkdownContent.tsx';
import styles from './StepGrid.module.css';
import {stripMarkdown} from '@utils/markdown.ts';
import {
  EMPTY_RACE_SELECTION,
  getBaseRaces,
  getSubracesForBaseRace,
  type RaceSelection,
} from '@/utils/races';

interface StepRaceProps {
  races: Race[];
  selection: RaceSelection;
  onSelect: (selection: RaceSelection) => void;
}

function buildBadges(race: Race): string[] {
  const traits = race.traits ?? [];
  const speedTrait = traits.find(trait => trait.type === 'SPEED');
  const sizeTrait = traits.find(trait => trait.type === 'SIZE');

  return [
    sizeTrait?.desc,
    speedTrait?.desc ? `Speed: ${speedTrait.desc}` : undefined,
  ].filter(Boolean) as string[];
}

function getDescription(race: Race): string | undefined {
  const traits = race.traits ?? [];
  const source = race.desc || traits.find(trait => !trait.type)?.desc || '';
  return source ? stripMarkdown(source) : undefined;
}

function getNarrativeTraits(race: Race) {
  return (race.traits ?? []).filter(
      trait => trait.type !== 'SPEED' && trait.type !== 'SIZE'
  );
}

function TraitSection({title, race}: { title: string; race: Race }) {
  const traits = getNarrativeTraits(race);

  if (traits.length === 0) return null;

  return (
      <div className={styles.detailSection}>
        <h4 className={styles.detailSectionTitle}>{title}</h4>
        <div className={styles.traitList}>
          {traits.map((trait, index) => (
              <div key={`${trait.name}-${index}`} className={styles.traitItem}>
                <div className={styles.traitName}>{trait.name}</div>
                <MarkdownContent text={trait.desc}/>
              </div>
          ))}
        </div>
      </div>
  );
}

function RaceDetailPanel({
                           baseRace,
                           races,
                           selection,
                           onSelect,
                         }: {
  baseRace: Race;
  races: Race[];
  selection: RaceSelection;
  onSelect: (selection: RaceSelection) => void;
}) {
  const subraces = getSubracesForBaseRace(baseRace, races);
  const selectedSubrace = subraces.find(subrace => subrace.key === selection.raceKey);

  const handleSubraceClick = (subraceKey: string) => {
    onSelect({
      baseRaceKey: baseRace.key,
      raceKey: selection.raceKey === subraceKey ? '' : subraceKey,
    });
  };

  return (
      <div className={styles.detailPanel}>
        <div className={styles.detailHeader}>
          <h3 className={styles.detailTitle}>{baseRace.name}</h3>
          {baseRace.document?.display_name && (
              <span className={styles.detailSubtitle}>{baseRace.document.display_name}</span>
          )}
        </div>

        {baseRace.desc && <MarkdownContent text={baseRace.desc}/>}

        <TraitSection title="Racial Traits" race={baseRace}/>

        {subraces.length > 0 && (
            <div className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>Choose a Subrace</h4>
              <p className={styles.subInfoNote}>
                This race has subraces. You must choose one to continue.
              </p>
              <div className={styles.subraceGrid}>
                {subraces.map(subrace => (
                    <SelectionCard
                        key={subrace.key}
                        title={subrace.name}
                        topRight={subrace.document?.display_name}
                        badges={buildBadges(subrace)}
                        description={getDescription(subrace)}
                        isSelected={selection.raceKey === subrace.key}
                        onClick={() => handleSubraceClick(subrace.key)}
                    />
                ))}
              </div>
            </div>
        )}

        {selectedSubrace && (
            <div className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>Selected Subrace</h4>
              {selectedSubrace.desc && <MarkdownContent text={selectedSubrace.desc}/>}
              <TraitSection title="Subrace Traits" race={selectedSubrace}/>
            </div>
        )}
      </div>
  );
}

export function StepRace({races, selection, onSelect}: StepRaceProps) {
  const baseRaces = getBaseRaces(races);

  const handleBaseRaceClick = (race: Race) => {
    const subraces = getSubracesForBaseRace(race, races);

    if (selection.baseRaceKey === race.key) {
      onSelect(EMPTY_RACE_SELECTION);
      return;
    }

    onSelect({
      baseRaceKey: race.key,
      raceKey: subraces.length > 0 ? '' : race.key,
    });
  };

  return (
      <div className={styles.container}>
        <p className={styles.hint}>
          Select your character&apos;s race. If a race has subraces, you must choose one.
        </p>
        <div className={styles.grid}>
          {baseRaces.map(race => {
            const isSelected = selection.baseRaceKey === race.key;

            return (
                <Fragment key={race.key}>
                  <SelectionCard
                      title={race.name}
                      topRight={race.document?.display_name}
                      badges={buildBadges(race)}
                      description={getDescription(race)}
                      isSelected={isSelected}
                      onClick={() => handleBaseRaceClick(race)}
                  />
                  {isSelected && (
                      <RaceDetailPanel
                          baseRace={race}
                          races={races}
                          selection={selection}
                          onSelect={onSelect}
                      />
                  )}
                </Fragment>
            );
          })}
        </div>
      </div>
  );
}
