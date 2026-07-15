import type {Race} from '@/types';

export interface RaceSelection {
  baseRaceKey: string;
  raceKey: string;
}

export const EMPTY_RACE_SELECTION: RaceSelection = {
  baseRaceKey: '',
  raceKey: '',
};

export function getRaceByKey(races: Race[], key: string): Race | undefined {
  return races.find(race => race.key === key);
}

export function getBaseRaces(races: Race[]): Race[] {
  return races.filter(race => !race.subspecies);
}

export function getSubracesForBaseRace(baseRace: Race, races: Race[]): Race[] {
  return (baseRace.subraceOfThis ?? [])
      .map(key => getRaceByKey(races, key))
      .filter((race): race is Race => Boolean(race));
}

export function getParentRace(race: Race, races: Race[]): Race | undefined {
  if (!race.subraceOf) return undefined;
  return getRaceByKey(races, race.subraceOf);
}

export function createRaceSelectionFromRaceKey(races: Race[], raceKey: string): RaceSelection {
  if (!raceKey) return EMPTY_RACE_SELECTION;

  const race = getRaceByKey(races, raceKey);

  if (!race) return EMPTY_RACE_SELECTION;

  if (race.subspecies) {
    return {
      baseRaceKey: race.subraceOf ?? '',
      raceKey: race.key,
    };
  }

  const subraces = getSubracesForBaseRace(race, races);

  return {
    baseRaceKey: race.key,
    raceKey: subraces.length > 0 ? '' : race.key,
  };
}

export function isRaceSelectionComplete(races: Race[], selection: RaceSelection): boolean {
  if (!selection.baseRaceKey) return false;

  const baseRace = getRaceByKey(races, selection.baseRaceKey);

  if (!baseRace) return false;

  const subraces = getSubracesForBaseRace(baseRace, races);

  if (subraces.length === 0) {
    return selection.raceKey === baseRace.key;
  }

  return subraces.some(subrace => subrace.key === selection.raceKey);
}

export function getRaceDisplayName(races: Race[], raceKey: string): string {
  if (!raceKey) return '';

  const race = getRaceByKey(races, raceKey);

  if (!race) return '';

  if (!race.subspecies) return race.name;

  const parentRace = getParentRace(race, races);

  return parentRace ? `${parentRace.name} — ${race.name}` : race.name;
}