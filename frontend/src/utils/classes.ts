import type {CharacterClass, DndClassLevel} from '@/types';

export const MAX_CLASS_LEVEL = 20;

export function getClassByKey(
    classes: CharacterClass[],
    key: string
): CharacterClass | undefined {
  return classes.find(cls => cls.key === key);
}

export function getClassDisplayName(
    classes: CharacterClass[],
    key: string
): string {
  return getClassByKey(classes, key)?.name ?? key;
}

export function formatClassLevel(
    classes: CharacterClass[],
    entry: DndClassLevel
): string {
  return `${getClassDisplayName(classes, entry.classKey)} ${entry.level}`;
}

export function formatClassLevels(
    classes: CharacterClass[],
    entries: DndClassLevel[]
): string {
  if (!entries || entries.length === 0) return '';
  return entries.map(entry => formatClassLevel(classes, entry)).join(' / ');
}

export function totalLevelOf(entries: DndClassLevel[]): number {
  return entries.reduce((sum, entry) => sum + entry.level, 0);
}

export function pendingLevels(
    totalLevelFromXp: number,
    entries: DndClassLevel[]
): number {
  const assigned = totalLevelOf(entries);
  return Math.max(0, totalLevelFromXp - assigned);
}