import styles from './WizardProgress.module.css';

interface WizardProgressProps {
  steps: string[];
  currentStep: number;
}

export function WizardProgress({steps, currentStep}: WizardProgressProps) {
  return (
      <div className={styles.wrapper}>
        {steps.map((label, index) => {
          const state =
              index < currentStep ? 'done' : index === currentStep ? 'active' : 'upcoming';
          return (
              <div key={label} className={styles.item} data-state={state}>
                <div className={styles.circle}>
                  {state === 'done' ? '✓' : index + 1}
                </div>
                <span className={styles.label}>{label}</span>
                {index < steps.length - 1 && <div className={styles.connector}/>}
              </div>
          );
        })}
      </div>
  );
}