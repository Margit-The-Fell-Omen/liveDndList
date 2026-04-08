import {useState, type ReactNode} from 'react';
import {Sidebar} from './Sidebar';
import {Header} from './Header';
import styles from './Layout.module.css';

interface LayoutProps {
  children: ReactNode;
}

export function Layout({children}: LayoutProps) {
  // Add state to track sidebar expansion
  const [isSidebarExpanded, setIsSidebarExpanded] = useState(false);

  return (
      <div className={styles.layout}>
        {/* Pass the state and setter to the Sidebar */}
        <Sidebar
            isExpanded={isSidebarExpanded}
            setIsExpanded={setIsSidebarExpanded}
        />

        {/* Conditionally add a class to the main content area */}
        <div className={`${styles.main} ${isSidebarExpanded ? styles['sidebar-expanded'] : ''}`}>
          <Header/>
          <main className={styles.content}>
            {children}
          </main>
        </div>
      </div>
  );
}
