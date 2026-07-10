import styles from './SelectionCard.module.css';

interface SelectionCardProps {
  title: string;
  subtitle?: string;
  badges?: string[];
  description?: string;
  isSelected: boolean;
  onClick: () => void;
}

export function SelectionCard({
                                title,
                                subtitle,
                                badges,
                                description,
                                isSelected,
                                onClick,
                              }: SelectionCardProps) {
  return (
      <button
          type="button"
          className={styles.card}
          data-selected={isSelected}
          onClick={onClick}
      >
        <div className={styles.header}>
          <span className={styles.title}>{title}</span>
          {subtitle && <span className={styles.subtitle}>{subtitle}</span>}
        </div>
        {badges && badges.length > 0 && (
            <div className={styles.badges}>
              {badges.map(b => (
                  <span key={b} className={styles.badge}>{b}</span>
              ))}
            </div>
        )}
        {description && <p className={styles.description}>{description}</p>}
      </button>
  );
}