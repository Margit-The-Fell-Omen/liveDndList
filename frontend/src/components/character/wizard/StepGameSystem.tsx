import {SelectionCard} from './SelectionCard';
import styles from './StepGrid.module.css';

const SYSTEMS = [
  {
    key: '2014',
    name: '2014 Rules',
    description: 'Standard 5e rules (2014), including Level Up: Advanced 5e and other legacy expansions.'
  },
  {
    key: '2024',
    name: '2024 Rules',
    description: 'The updated 2024 core rules revision.'
  }
];

interface StepGameSystemProps {
  selectedKey: string;
  onSelect: (key: string) => void;
}

export function StepGameSystem({selectedKey, onSelect}: StepGameSystemProps) {
  const handleClick = (key: string) => {
    onSelect(selectedKey === key ? '' : key);
  };

  return (
      <div className={styles.container}>
        <p className={styles.hint}>
          Choose the rule system for your character. This determines which races, classes, and
          backgrounds will be available.
        </p>
        <div className={styles.grid}>
          {SYSTEMS.map(sys => (
              <SelectionCard
                  key={sys.key}
                  title={sys.name}
                  description={sys.description}
                  isSelected={selectedKey === sys.key}
                  onClick={() => handleClick(sys.key)}
              />
          ))}
        </div>
      </div>
  );
}
