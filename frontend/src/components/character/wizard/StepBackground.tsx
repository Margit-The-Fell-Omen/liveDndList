import type {Background, BackgroundBenefit, DocumentInfo} from '@/types';
import {SelectionCard} from './SelectionCard';
import {MarkdownContent} from '../../common/MarkdownContent.tsx';
import {stripMarkdown} from '@/utils/markdown';
import styles from './StepGrid.module.css';

interface StepBackgroundProps {
  backgrounds: Background[];
  selectedKey: string;
  onSelect: (key: string) => void;
}

const TYPE_LABELS: Record<string, string> = {
  skill_proficiency: 'Skill Proficiencies',
  tool_proficiency: 'Tool Proficiencies',
  language: 'Languages',
  equipment: 'Equipment',
  feature: 'Feature',
  ability_score: 'Ability Score Increase',
  feat: 'Feat',
  connection_and_memento: 'Connection & Memento',
  adventures_and_advancement: 'Adventures & Advancement',
  other: 'Other',
};

const humanizeKey = (raw: string): string =>
    raw.split(/[_\s]+/).map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');

const labelFor = (type: string): string => TYPE_LABELS[type] ?? humanizeKey(type);

function documentLabel(doc?: DocumentInfo): string | undefined {
  if (!doc) return undefined;
  return doc.display_name || doc.name || doc.key;
}

function BackgroundDetailPanel({bg}: { bg: Background }) {
  const benefits = bg.benefits ?? [];
  const grouped = benefits.reduce<Record<string, BackgroundBenefit[]>>((acc, b) => {
    const type = b.type || 'other';
    acc[type] = [...(acc[type] ?? []), b];
    return acc;
  }, {});

  const docLabel = documentLabel(bg.document);

  return (
      <div className={styles.detailPanel}>
        <div className={styles.detailHeader}>
          <h3 className={styles.detailTitle}>{bg.name}</h3>
          {docLabel && (
              <span className={styles.detailSubtitle}>{docLabel}</span>
          )}
        </div>
        {bg.desc && <MarkdownContent text={bg.desc}/>}
        {Object.entries(grouped).map(([type, items]) => (
            <div key={type} className={styles.detailSection}>
              <h4 className={styles.detailSectionTitle}>{labelFor(type)}</h4>
              <div className={styles.traitList}>
                {items.map((benefit, idx) => (
                    <div key={`${benefit.name}-${idx}`} className={styles.traitItem}>
                      <div className={styles.traitName}>{benefit.name}</div>
                      {benefit.desc && <MarkdownContent text={benefit.desc}/>}
                    </div>
                ))}
              </div>
            </div>
        ))}
      </div>
  );
}

export function StepBackground({backgrounds, selectedKey, onSelect}: StepBackgroundProps) {
  const handleClick = (key: string) => {
    onSelect(selectedKey === key ? '' : key);
  };

  return (
      <div className={styles.container}>
        <p className={styles.hint}>
          Choose a background that shaped your character's history and skills. Click again to
          deselect.
        </p>
        <div className={styles.grid}>
          {backgrounds.map(bg => {
            const benefits = bg.benefits ?? [];
            const skills = benefits.find(b => b.type === 'skill_proficiency');
            const equipment = benefits.find(b => b.type === 'equipment');
            const isSelected = selectedKey === bg.key;

            const badges = skills?.desc ? [skills.desc] : [];
            const descriptionSource = equipment?.desc || bg.desc || '';
            const description = descriptionSource ? stripMarkdown(descriptionSource) : undefined;

            return (
                <>
                  <SelectionCard
                      key={bg.key}
                      title={bg.name}
                      topRight={documentLabel(bg.document)}
                      badges={badges}
                      description={description}
                      isSelected={isSelected}
                      onClick={() => handleClick(bg.key)}
                  />
                  {isSelected && <BackgroundDetailPanel key={`${bg.key}-detail`} bg={bg}/>}
                </>
            );
          })}
        </div>
      </div>
  );
}
