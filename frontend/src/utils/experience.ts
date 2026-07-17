const LEVEL_THRESHOLDS: ReadonlyArray<{ level: number; xp: number }> = [
  {level: 1, xp: 0},
  {level: 2, xp: 300},
  {level: 3, xp: 900},
  {level: 4, xp: 2700},
  {level: 5, xp: 6500},
  {level: 6, xp: 14000},
  {level: 7, xp: 23000},
  {level: 8, xp: 34000},
  {level: 9, xp: 48000},
  {level: 10, xp: 64000},
  {level: 11, xp: 85000},
  {level: 12, xp: 100000},
  {level: 13, xp: 120000},
  {level: 14, xp: 140000},
  {level: 15, xp: 165000},
  {level: 16, xp: 195000},
  {level: 17, xp: 225000},
  {level: 18, xp: 265000},
  {level: 19, xp: 305000},
  {level: 20, xp: 355000},
];

export const MIN_LEVEL = 1;
export const MAX_LEVEL = 20;

export function xpForLevel(level: number): number {
  const clamped = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
  return LEVEL_THRESHOLDS.find(entry => entry.level === clamped)?.xp ?? 0;
}

export function levelForXp(xp: number): number {
  const safe = Math.max(0, xp);
  let level = MIN_LEVEL;
  for (const entry of LEVEL_THRESHOLDS) {
    if (safe >= entry.xp) level = entry.level;
    else break;
  }
  return level;
}