import {useState} from 'react';
import type {CharacterClass, ClassFeature, FeatureType} from '@/types';
import {MarkdownContent} from '../../common/MarkdownContent.tsx';
import styles from './StepGrid.module.css';

const humanize = (raw: string): string =>
    raw.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());

const FEATURE_TYPE_LABELS: Record<FeatureType, string> = {
  PROFICIENCIES: 'Proficiencies',
  STARTING_EQUIPMENT: 'Starting Equipment',
  CORE_TRAITS_TABLE: 'Core Traits',
  CLASS_TABLE_DATA: 'Class Progression Table',
  CLASS_LEVEL_FEATURE: 'Class Features',
  CLASS_FEATURE_OPTION_LIST: 'Feature Options',
  SPELL_SLOTS: 'Spell Slots',
  PROFICIENCY_BONUS: 'Proficiency Bonus',
};

const PREFERRED_ORDER: FeatureType[] = [
  'CORE_TRAITS_TABLE',
  'PROFICIENCIES',
  'STARTING_EQUIPMENT',
  'CLASS_LEVEL_FEATURE',
  'CLASS_FEATURE_OPTION_LIST',
  'CLASS_TABLE_DATA',
  'PROFICIENCY_BONUS',
  'SPELL_SLOTS',
];

const PROGRESSION_TYPES: string[] = [
  'PROFICIENCY_BONUS',
  'CLASS_TABLE_DATA',
  'SPELL_SLOTS',
];

const labelForFeatureType = (raw: string): string =>
    FEATURE_TYPE_LABELS[raw as FeatureType] ?? humanize(raw);

interface NormalizedFeature {
  key: string;
  name: string;
  desc: string;
  featureType: string;
  gainedAt: Array<{ level: number; detail: string | null }>;
  tableRows: Array<{ level: number; value: string }>;
}

function normalizeFeature(raw: ClassFeature | Record<string, unknown>): NormalizedFeature {
  const r = raw as Record<string, unknown>;
  const featureType = (r.featureType ?? r.feature_type ?? 'OTHER') as string;
  const gainedAtRaw = (r.gainedAt ?? r.gained_at ?? []) as Array<Record<string, unknown>>;
  const gainedAt = gainedAtRaw
      .map(g => ({
        level: Number(g.level ?? 0),
        detail: (g.detail as string | null) ?? null,
      }))
      .sort((a, b) => a.level - b.level);
  const tableRaw = (r.dataForClassTable ?? r.data_for_class_table ?? []) as Array<Record<string, unknown>>;
  const tableRows = tableRaw.map(row => ({
    level: Number(row.level ?? 0),
    value: String(row.columnValue ?? row.column_value ?? ''),
  }));
  return {
    key: (r.key as string) ?? '',
    name: (r.name as string) ?? '',
    desc: (r.desc as string) ?? '',
    featureType,
    gainedAt,
    tableRows,
  };
}

function minGainedLevel(feat: NormalizedFeature): number {
  if (!feat.gainedAt.length) return Number.MAX_SAFE_INTEGER;
  return Math.min(...feat.gainedAt.map(g => g.level));
}

function filterFeatureByLevel(
    feat: NormalizedFeature,
    currentLevel: number | undefined,
): NormalizedFeature | null {
  if (currentLevel === undefined) return feat;

  if (feat.gainedAt.length === 0) {
    return feat;
  }

  const eligibleGainedAt = feat.gainedAt.filter(g => g.level <= currentLevel);
  if (eligibleGainedAt.length === 0) return null;

  return {...feat, gainedAt: eligibleGainedAt};
}

function ProgressionTable({
                            features,
                            valueLabel,
                            currentLevel,
                          }: {
  features: NormalizedFeature[];
  valueLabel?: string;
  currentLevel?: number;
}) {
  const levels = new Set<number>();
  features.forEach(f => f.tableRows.forEach(r => levels.add(r.level)));
  const sortedLevels = Array.from(levels).sort((a, b) => a - b);
  if (sortedLevels.length === 0) return null;

  return (
      <div className={styles.tableWrapper}>
        <table className={styles.classTable}>
          <thead>
          <tr>
            <th>Level</th>
            {features.map(f => (
                <th key={f.key} title={valueLabel}>{f.name}</th>
            ))}
          </tr>
          </thead>
          <tbody>
          {sortedLevels.map(level => {
            const isActive = currentLevel !== undefined && level === currentLevel;
            return (
                <tr
                    key={level}
                    className={isActive ? styles.activeTableRow : undefined}
                    data-active={isActive ? 'true' : 'false'}
                    data-level={level}
                >
                  <td>{level}</td>
                  {features.map(f => {
                    const row = f.tableRows.find(r => r.level === level);
                    return <td key={f.key}>{row?.value ?? '—'}</td>;
                  })}
                </tr>
            );
          })}
          </tbody>
        </table>
      </div>
  );
}

function FeatureItem({feat}: { feat: NormalizedFeature }) {
  const gained = feat.gainedAt
      .map(g => `Lv ${g.level}${g.detail ? ` (${g.detail})` : ''}`)
      .join(', ');
  const hasTable = feat.tableRows.length > 0;
  const hasDesc = feat.desc.trim().length > 0;
  return (
      <div className={styles.traitItem}>
        <div className={styles.traitName}>
          {feat.name || '—'}
          {gained && <span className={styles.featureGained}>{gained}</span>}
        </div>
        {hasDesc && <MarkdownContent text={feat.desc}/>}
        {hasTable && (
            <div className={styles.tableWrapper}>
              <table className={styles.classTable}>
                <thead>
                <tr>
                  <th>Level</th>
                  <th>{feat.name || 'Value'}</th>
                </tr>
                </thead>
                <tbody>
                {feat.tableRows
                    .slice()
                    .sort((a, b) => a.level - b.level)
                    .map(row => (
                        <tr key={row.level}>
                          <td>{row.level}</td>
                          <td>{row.value || '—'}</td>
                        </tr>
                    ))}
                </tbody>
              </table>
            </div>
        )}
        {!hasDesc && !hasTable && (
            <div className={styles.traitDesc}>No additional details.</div>
        )}
      </div>
  );
}

function documentLabel(doc?: CharacterClass['document']): string | undefined {
  if (!doc) return undefined;
  return doc.display_name || doc.name || doc.key;
}

interface ClassDetailPanelProps {
  cls: CharacterClass;
  allClasses: CharacterClass[];
  currentLevel?: number;
}

export function ClassDetailPanel({cls, allClasses, currentLevel}: ClassDetailPanelProps) {

  const [expandedSubclasses, setExpandedSubclasses] = useState<Set<string>>(new Set());

  const hitDie = cls.hitDice ?? cls.hit_dice ?? '—';
  const saves = (cls.savingThrows ?? []).map(humanize).join(', ') || '—';
  const hp1st = cls.hitPointsOn1stLevel;
  const hpHigher = cls.hitPointsOnHigherLevels;

  const rawFeatures = (cls.features ?? []).map(normalizeFeature);
  const features = rawFeatures
      .map(f => filterFeatureByLevel(f, currentLevel))
      .filter((f): f is NormalizedFeature => f !== null);

  const groupedByType = features.reduce<Record<string, NormalizedFeature[]>>((acc, f) => {
    const t = f.featureType || 'OTHER';
    acc[t] = [...(acc[t] ?? []), f];
    return acc;
  }, {});

  const orderedKeys: string[] = [
    ...PREFERRED_ORDER.filter(k => groupedByType[k] && !PROGRESSION_TYPES.includes(k)),
    ...Object.keys(groupedByType).filter(
        k => !(PREFERRED_ORDER as string[]).includes(k) && !PROGRESSION_TYPES.includes(k),
    ),
  ];

  const progressionFeatures: NormalizedFeature[] = PROGRESSION_TYPES.flatMap(
      t => groupedByType[t] ?? [],
  );

  const subclasses = (cls.subclasses ?? [])
      .map(sc => allClasses.find(c => c.key === sc.key) ?? sc);

  const toggleSubclass = (key: string) => {
    setExpandedSubclasses(prev => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  const renderSection = (type: string, items: NormalizedFeature[]) => {
    const sortedItems = items
        .slice()
        .sort((a, b) => minGainedLevel(a) - minGainedLevel(b));

    return (
        <div key={type} className={styles.detailSection}>
          <h4 className={styles.detailSectionTitle}>{labelForFeatureType(type)}</h4>
          <div className={styles.traitList}>
            {sortedItems.map((feat, idx) => (
                <FeatureItem key={`${feat.key || feat.name}-${idx}`} feat={feat}/>
            ))}
          </div>
        </div>
    );
  };

  const docLabel = documentLabel(cls.document);

  return (
      <div className={styles.detailPanel}>
        <div className={styles.detailHeader}>
          <h3 className={styles.detailTitle}>{cls.name}</h3>
          {docLabel && <span className={styles.detailSubtitle}>{docLabel}</span>}
        </div>
        <div className={styles.detailBadges}>
          <span className={styles.detailBadge}>Hit Die: {hitDie}</span>
          <span className={styles.detailBadge}>Saves: {saves}</span>
          {hp1st && <span className={styles.detailBadge}>1st Level HP: {hp1st}</span>}
          {hpHigher && <span className={styles.detailBadge}>Higher Levels: {hpHigher}</span>}
        </div>

        {cls.desc && <MarkdownContent text={cls.desc}/>}

        {progressionFeatures.length > 0 && (
            <div className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>Class Progression Table</h4>
              <ProgressionTable features={progressionFeatures} currentLevel={currentLevel}/>
            </div>
        )}

        {orderedKeys.length === 0 && progressionFeatures.length === 0 && (
            <p className={styles.detailDesc}>No features available for this class.</p>
        )}

        {orderedKeys.map(type => renderSection(type, groupedByType[type]))}

        {subclasses.length > 0 && (
            <div className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>Available Subclasses</h4>
              <p className={styles.subInfoNote}>
                Subclasses unlock at higher levels. You cannot pick one at level 1.
              </p>
              <div className={styles.expandableList}>
                {subclasses.map(sc => {
                  const isExpanded = expandedSubclasses.has(sc.key);
                  const full = 'desc' in sc ? (sc as CharacterClass) : null;
                  return (
                      <div key={sc.key} className={styles.expandableItem}>
                        <button
                            type="button"
                            className={styles.expandableHeader}
                            onClick={() => toggleSubclass(sc.key)}
                            aria-expanded={isExpanded}
                        >
                          <span className={styles.expandableTitle}>{sc.name}</span>
                          <span
                              className={styles.expandableChevron}
                              data-expanded={isExpanded}
                              aria-hidden="true"
                          >
                      ›
                    </span>
                        </button>
                        {isExpanded && (
                            <div className={styles.expandableBody}>
                              {full?.desc ? (
                                  <MarkdownContent text={full.desc}/>
                              ) : (
                                  <p className={styles.traitDesc}>No description available.</p>
                              )}
                            </div>
                        )}
                      </div>
                  );
                })}
              </div>
            </div>
        )}
      </div>
  );
}
