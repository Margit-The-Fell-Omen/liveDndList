import {useState, type ReactNode} from 'react';
import {Sidebar} from './Sidebar';
import {Header} from './Header';
import styles from './Layout.module.css';
import {useMediaQuery} from '@/hooks/useMediaQuery';

interface LayoutProps {
  children: ReactNode;
}

export function Layout({children}: LayoutProps) {
  const [isSidebarExpanded, setIsSidebarExpanded] = useState(false);
  const isDesktop = useMediaQuery('(min-width: 1024px)');

  return (
      <div className={styles.layout}>
        <Sidebar
            isExpanded={isSidebarExpanded}
            setIsExpanded={setIsSidebarExpanded}
            isDesktop={isDesktop}
        />
        <div
            className={`${styles.main} ${isSidebarExpanded && isDesktop ? styles.desktopSidebarExpanded : ''}`}>
          <Header
              isSidebarExpanded={isSidebarExpanded}
              onToggleSidebar={() => setIsSidebarExpanded(!isSidebarExpanded)}
          />
          <main className={styles.content}>
            {children}
          </main>
        </div>
      </div>
  );
}
