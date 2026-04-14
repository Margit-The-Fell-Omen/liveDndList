// src/components/character/AbilityScore.tsx

// FIX 1: Import the AbilityInfo type from its single source of truth.
import type {AbilityInfo} from '@/utils/constants';

// FIX 2: Define the component's props. Notice it does NOT include 'key'.
interface AbilityScoreProps {
  ability: AbilityInfo;
  value: number;
  proficient: boolean;
  onChange: (abilityKey: string, value: string | number) => void;
  onProficiencyChange: () => void;
}

export function AbilityScore({
                               ability,
                               value,
                               proficient,
                               onChange,
                               onProficiencyChange,
                             }: AbilityScoreProps) {

  // FIX 3: Use `ability.key` (from the 'ability' prop) instead of a non-existent `key` prop.
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(ability.key, e.target.value);
  };

  return (
      <div className="ability-score-container">
        <label>{ability.name}</label>
        <input
            type="number"
            value={value}
            onChange={handleInputChange}
        />
        {/* You can also display the modifier here */}
        {/* And the proficiency toggle */}
        <button onClick={onProficiencyChange}>
          {proficient ? '✅' : '⬜️'}
        </button>
      </div>
  );
}
