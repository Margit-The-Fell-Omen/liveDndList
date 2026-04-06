import { Sidebar } from './Sidebar';
import { Header } from './Header';
import styles from './Layout.module.css';

export function Layout({ children }) {
  return (
    <div className={styles.layout}>
      <Sidebar />
      <div className={styles.main}>
        <Header />
        <main className={styles.content}>
          {children}
        </main>
      </div>
    </div>
  );
}
