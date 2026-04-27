import type {ReactNode} from 'react';
import styles from './Card.module.css';

interface CardProps {
  title?: string;
  children: ReactNode;
  className?: string;
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