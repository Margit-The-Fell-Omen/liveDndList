// src/components/character/Features.tsx
import {useState} from 'react';
import {useCharacter} from '@/context/CharacterContext';
import {Card} from '@/components/common/Card';
import {Button} from '@/components/common/Button';
import {MarkdownContent} from '@/components/common/MarkdownContent';
import type {CharacterFeatureResponse, CustomFeatureResponse, PendingChoiceResponse} from '@/types';
import {FeatureChoiceModal} from './FeatureChoiceModal';
import {CustomFeatureFormModal} from './CustomFeatureFormModal';
import styles from './Features.module.css';

const SOURCE_ORDER: CharacterFeatureResponse['source'][] = [
  'RACE', 'SUBRACE', 'BACKGROUND', 'CLASS', 'SUBCLASS', 'FEAT', 'FIGHTING_STYLE',
];

const SOURCE_COLORS: Record<string, string> = {
  CLASS: 'var(--color-accent-primary)',
  SUBCLASS: 'var(--color-accent-tertiary, #5c8dd4)',
  RACE: '#4caf7d',
  SUBRACE: '#4caf7d',
  BACKGROUND: '#c49a3c',
  FEAT: '#a05cb5',
  FIGHTING_STYLE: '#d4715c',
  CUSTOM: 'var(--color-text-muted)',
};

// ── Extraction Helpers ────────────────────────────────────────

function extractLevel(feature: CharacterFeatureResponse): number {
  const ctx = feature.sourceContext as Record<string, unknown> | null;
  if (typeof ctx?.['classLevel'] === 'number') return ctx['classLevel'];

  if (feature.sourceLabel) {
    const match = feature.sourceLabel.match(/Level\s+(\d+)/i);
    if (match) return parseInt(match[1], 10);
  }
  return 0;
}

function extractSourceKey(feature: CharacterFeatureResponse, keyName: 'classKey' | 'subclassKey'): string {
  const ctx = feature.sourceContext as Record<string, unknown> | null;
  if (typeof ctx?.[keyName] === 'string') return ctx[keyName] as string;

  if (feature.sourceLabel) {
    return feature.sourceLabel.split(' ')[0];
  }
  return '';
}

function buildSourceLabel(
    feature: CharacterFeatureResponse,
    classes: { name: string; key: string }[],
    races: { name: string; key: string; subspecies: boolean; subraceOf: string | null }[],
    backgrounds: { name: string; key: string }[]
): string {
  const level = extractLevel(feature);

  switch (feature.source) {
    case 'CLASS': {
      const classKey = extractSourceKey(feature, 'classKey');
      const className = classes.find(c => c.key === classKey)?.name ?? classKey;
      return level > 0 ? `${className} (Level ${level})` : (className || 'Class');
    }
    case 'SUBCLASS': {
      const subclassKey = extractSourceKey(feature, 'subclassKey');
      const subclassName = classes.find(c => c.key === subclassKey)?.name ?? subclassKey;
      return level > 0 ? `${subclassName} (Level ${level})` : (subclassName || 'Subclass');
    }
    case 'RACE':
    case 'SUBRACE':
      return 'Race';
    case 'BACKGROUND':
      return 'Background';
    case 'FEAT':
      return 'Feat';
    case 'FIGHTING_STYLE':
      return 'Fighting Style';
    default:
      return feature.sourceLabel || feature.source;
  }
}

function featureSortKey(feature: CharacterFeatureResponse): string {
  const sourceIdx = SOURCE_ORDER.indexOf(feature.source);
  const priority = sourceIdx >= 0 ? sourceIdx : 99;
  const level = extractLevel(feature);
  return `${String(priority).padStart(2, '0')}_${String(level).padStart(2, '0')}_${feature.name}`;
}

function getGroupLabel(
    source: string,
    sampleFeature: CharacterFeatureResponse,
    classes: { name: string; key: string }[],
    races: { name: string; key: string; subspecies: boolean; subraceOf: string | null }[],
    backgrounds: { name: string; key: string }[]
): string {
  switch (source) {
    case 'CLASS': {
      const classKey = extractSourceKey(sampleFeature, 'classKey');
      const name = classes.find(c => c.key === classKey)?.name || classKey;
      return name ? `Class — ${name}` : 'Class';
    }
    case 'SUBCLASS': {
      const subclassKey = extractSourceKey(sampleFeature, 'subclassKey');
      const name = classes.find(c => c.key === subclassKey)?.name || subclassKey;
      return name ? `Subclass — ${name}` : 'Subclass';
    }
    case 'RACE':
      return 'Race';
    case 'SUBRACE':
      return 'Subrace';
    case 'BACKGROUND':
      return 'Background';
    case 'FEAT':
      return 'Feats';
    case 'FIGHTING_STYLE':
      return 'Fighting Styles';
    default:
      return source;
  }
}

// ── Sub-components ────────────────────────────────────────────

function PendingChoiceBanner({
                               choices,
                               onAnswerChoice,
                             }: {
  choices: PendingChoiceResponse[];
  onAnswerChoice: (choice: PendingChoiceResponse) => void;
}) {
  if (choices.length === 0) return null;

  return (
      <div className={styles.pendingBanner}>
        <div className={styles.pendingBannerHeader}>
          <span className={styles.pendingBannerIcon}>⚠</span>
          <span className={styles.pendingBannerTitle}>
          {choices.length} choice{choices.length > 1 ? 's' : ''} required
        </span>
        </div>
        <div className={styles.pendingList}>
          {choices.map(choice => (
              <button
                  key={`${choice.characterFeatureId}-${choice.choiceKey}`}
                  type="button"
                  className={styles.pendingItem}
                  onClick={() => onAnswerChoice(choice)}
              >
                <span className={styles.pendingItemName}>{choice.name}</span>
                <span className={styles.pendingItemAction}>Answer →</span>
              </button>
          ))}
        </div>
      </div>
  );
}

function FeatureCard({
                       feature,
                       sourceLabel,
                       sourceColor,
                     }: {
  feature: CharacterFeatureResponse;
  sourceLabel: string;
  sourceColor: string;
}) {
  const [expanded, setExpanded] = useState(false);

  return (
      <div className={styles.featureCard}>
        <button
            type="button"
            className={styles.featureCardHeader}
            onClick={() => setExpanded(e => !e)}
            aria-expanded={expanded}
        >
          <div className={styles.featureCardLeft}>
          <span
              className={styles.sourceTag}
              style={{backgroundColor: sourceColor}}
          >
            {sourceLabel}
          </span>
            <span className={styles.featureName}>{feature.name}</span>
          </div>
          <span className={styles.chevron} data-expanded={expanded} aria-hidden="true">
          ›
        </span>
        </button>

        {expanded && (
            <div className={styles.featureCardBody}>
              {feature.description && (
                  <MarkdownContent text={feature.description}/>
              )}

              {feature.choices.length > 0 && (
                  <div className={styles.answeredChoices}>
                    {feature.choices.map(answer => (
                        <div key={answer.choiceKey} className={styles.answeredChoice}>
                          <span className={styles.answeredChoiceLabel}>{answer.name}:</span>
                          <span className={styles.answeredChoiceValue}>
                    {Array.isArray(answer.selectedValues)
                        ? (answer.selectedValues as string[]).map(formatChoiceValue).join(', ')
                        : String(answer.selectedValues)}
                  </span>
                        </div>
                    ))}
                  </div>
              )}
            </div>
        )}
      </div>
  );
}

function CustomFeatureCard({
                             feature,
                             onEdit,
                             onDelete,
                           }: {
  feature: CustomFeatureResponse;
  onEdit: (f: CustomFeatureResponse) => void;
  onDelete: (id: number) => void;
}) {
  const [expanded, setExpanded] = useState(false);

  return (
      <div className={styles.featureCard}>
        <div className={styles.featureCardHeaderComposite}>
          <button
              type="button"
              className={styles.featureCardHeaderButton}
              onClick={() => setExpanded(e => !e)}
              aria-expanded={expanded}
          >
            <div className={styles.featureCardLeft}>
            <span
                className={styles.sourceTag}
                style={{backgroundColor: SOURCE_COLORS.CUSTOM}}
            >
              Custom
            </span>
              <span className={styles.featureName}>{feature.name}</span>
            </div>
            <span className={styles.chevron} data-expanded={expanded} aria-hidden="true">
            ›
          </span>
          </button>
          <div className={styles.customActions}>
            <button
                type="button"
                className={styles.iconButton}
                onClick={() => onEdit(feature)}
                title="Edit"
                aria-label={`Edit ${feature.name}`}
            >
              ✎
            </button>
            <button
                type="button"
                className={`${styles.iconButton} ${styles.deleteIcon}`}
                onClick={() => onDelete(feature.id)}
                title="Delete"
                aria-label={`Delete ${feature.name}`}
            >
              ✕
            </button>
          </div>
        </div>

        {expanded && feature.description && (
            <div className={styles.featureCardBody}>
              <MarkdownContent text={feature.description}/>
            </div>
        )}
      </div>
  );
}

function formatChoiceValue(value: string): string {
  return value
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
}

// ── Main component ────────────────────────────────────────────

export function Features({className}: { className?: string }) {
  const {
    currentCharacter,
    saving,
    classes,
    races,
    backgrounds,
    submitChoice,
    clearChoice,
    createCustomFeature,
    updateCustomFeature,
    deleteCustomFeature,
  } = useCharacter();

  const [activeChoice, setActiveChoice] = useState<PendingChoiceResponse | null>(null);
  const [customFormOpen, setCustomFormOpen] = useState(false);
  const [editingCustom, setEditingCustom] = useState<CustomFeatureResponse | null>(null);

  if (!currentCharacter) return null;

  const {features = [], customFeatures = [], pendingChoices = []} = currentCharacter;

  const sortedFeatures = [...features].sort((a, b) => {
    const keyA = featureSortKey(a);
    const keyB = featureSortKey(b);
    return keyA.localeCompare(keyB);
  });

  const grouped: { source: string; label: string; features: CharacterFeatureResponse[] }[] = [];
  let currentGroup: typeof grouped[number] | null = null;

  for (const f of sortedFeatures) {
    const groupKey = f.source;
    if (!currentGroup || currentGroup.source !== groupKey) {
      const groupLabel = getGroupLabel(f.source, f, classes, races, backgrounds);
      currentGroup = {source: groupKey, label: groupLabel, features: []};
      grouped.push(currentGroup);
    }
    currentGroup.features.push(f);
  }

  const handleAnswerChoice = (choice: PendingChoiceResponse) => {
    setActiveChoice(choice);
  };

  const handleSubmitChoice = async (selectedValues: unknown[]) => {
    if (!activeChoice) return;
    await submitChoice(activeChoice.characterFeatureId, activeChoice.choiceKey, selectedValues);
    setActiveChoice(null);
  };

  const handleEditCustom = (f: CustomFeatureResponse) => {
    setEditingCustom(f);
    setCustomFormOpen(true);
  };

  const handleDeleteCustom = async (id: number) => {
    if (confirm('Delete this custom feature?')) {
      await deleteCustomFeature(id);
    }
  };

  const handleSaveCustom = async (name: string, description?: string) => {
    if (editingCustom) {
      await updateCustomFeature(editingCustom.id, name, description);
    } else {
      await createCustomFeature(name, description);
    }
    setCustomFormOpen(false);
    setEditingCustom(null);
  };

  const totalFeatureCount = features.length + customFeatures.length;

  return (
      <>
        <Card
            title={`Features & Traits ${totalFeatureCount > 0 ? `(${totalFeatureCount})` : ''}`}
            className={className}
        >
          <PendingChoiceBanner
              choices={pendingChoices}
              onAnswerChoice={handleAnswerChoice}
          />

          {grouped.map((group, idx) => (
              <div key={`${group.source}-${idx}`} className={styles.group}>
                <h4 className={styles.groupTitle}>{group.label}</h4>
                <div className={styles.groupList}>
                  {group.features.map(f => (
                      <FeatureCard
                          key={f.id}
                          feature={f}
                          sourceLabel={buildSourceLabel(f, classes, races, backgrounds)}
                          sourceColor={SOURCE_COLORS[f.source] ?? 'var(--color-text-muted)'}
                      />
                  ))}
                </div>
              </div>
          ))}

          {customFeatures.length > 0 && (
              <div className={styles.group}>
                <h4 className={styles.groupTitle}>Custom</h4>
                <div className={styles.groupList}>
                  {customFeatures.map(cf => (
                      <CustomFeatureCard
                          key={cf.id}
                          feature={cf}
                          onEdit={handleEditCustom}
                          onDelete={handleDeleteCustom}
                      />
                  ))}
                </div>
              </div>
          )}

          {totalFeatureCount === 0 && pendingChoices.length === 0 && (
              <p className={styles.emptyMessage}>
                No features yet. Features appear here as you add race, class, and background.
              </p>
          )}

          {currentCharacter.proficiencies && (
              <div className={styles.proficienciesWrapper}>
                <h4 className={styles.groupTitle}>Proficiencies & Languages</h4>
                <div className={styles.proficienciesGrid}>
                  {currentCharacter.proficiencies.armor.length > 0 && (
                      <div className={styles.profCard}>
                        <span className={styles.profLabel}>Armor:</span>
                        <span
                            className={styles.profValue}>{currentCharacter.proficiencies.armor.map(formatChoiceValue).join(', ')}</span>
                      </div>
                  )}
                  {currentCharacter.proficiencies.weapons.length > 0 && (
                      <div className={styles.profCard}>
                        <span className={styles.profLabel}>Weapons:</span>
                        <span
                            className={styles.profValue}>{currentCharacter.proficiencies.weapons.map(formatChoiceValue).join(', ')}</span>
                      </div>
                  )}
                  {currentCharacter.proficiencies.tools.length > 0 && (
                      <div className={styles.profCard}>
                        <span className={styles.profLabel}>Tools:</span>
                        <span
                            className={styles.profValue}>{currentCharacter.proficiencies.tools.map(formatChoiceValue).join(', ')}</span>
                      </div>
                  )}
                  {currentCharacter.proficiencies.languages.length > 0 && (
                      <div className={styles.profCard}>
                        <span className={styles.profLabel}>Languages:</span>
                        <span
                            className={styles.profValue}>{currentCharacter.proficiencies.languages.map(formatChoiceValue).join(', ')}</span>
                      </div>
                  )}
                </div>
              </div>
          )}

          <div className={styles.addCustomRow}>
            <Button
                variant="ghost"
                size="small"
                onClick={() => {
                  setEditingCustom(null);
                  setCustomFormOpen(true);
                }}
                disabled={saving}
            >
              + Add Custom Feature
            </Button>
          </div>
        </Card>

        {activeChoice && (
            <FeatureChoiceModal
                isOpen
                choice={activeChoice}
                character={currentCharacter}
                onSubmit={handleSubmitChoice}
                onClear={() => {
                  clearChoice(activeChoice.characterFeatureId, activeChoice.choiceKey);
                  setActiveChoice(null);
                }}
                onClose={() => setActiveChoice(null)}
                saving={saving}
            />
        )}

        <CustomFeatureFormModal
            isOpen={customFormOpen}
            initial={editingCustom}
            onSave={handleSaveCustom}
            onClose={() => {
              setCustomFormOpen(false);
              setEditingCustom(null);
            }}
            saving={saving}
        />
      </>
  );
}
