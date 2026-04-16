import type {ReactNode} from 'react';
import styles from './Card.module.css';

interface CardProps {
  title?: string;
  children: ReactNode;
  className?: string; // Allow passing extra classes for grid placement
}

export function Card({title, children, className = ''}: CardProps) {
  return (
      <section className={`${styles.card} ${className}`}>
        {title && <h3 className={styles.title}>{title}</h3>}
        <div className={styles.content}>
          {children}
        </div>
      </section>
  );
}