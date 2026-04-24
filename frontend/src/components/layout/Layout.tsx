import {useState, type ReactNode} from 'react';
import {Sidebar} from './Sidebar';
import {Header} from './Header';
import styles from './Layout.module.css';
import {useMediaQuery} from '@/hooks/useMediaQuery'; // NEW

interface LayoutProps {
  children: ReactNode;
}

export function Layout({children}: LayoutProps) {
  // This state now controls the sidebar visibility on all screen sizes
  const [isSidebarExpanded, setIsSidebarExpanded] = useState(false);
  // NEW: Use the hook to determine if we are on a desktop-sized screen
  const isDesktop = useMediaQuery('(min-width: 1024px)');

  // NEW: Automatically collapse the sidebar when switching to mobile view
  // This prevents the overlay from getting stuck if the window is resized
  // while the sidebar is open on desktop.
  if (!isDesktop && isSidebarExpanded) {
    const isHoveringSidebar = document.querySelector(`.${styles.sidebar}:hover`);
    if (!isHoveringSidebar) {
      setIsSidebarExpanded(false);
    }
  }


  return (
      <div className={styles.layout}>
        <Sidebar
            isExpanded={isSidebarExpanded}
            setIsExpanded={setIsSidebarExpanded}
            isDesktop={isDesktop} // NEW: Pass isDesktop as a prop
        />

        {/* MODIFIED: The main content margin is now handled more cleanly */}
        <div
            className={`${styles.main} ${isSidebarExpanded && isDesktop ? styles.desktopSidebarExpanded : ''}`}>
          {/* NEW: Pass state and setter to Header for the mobile toggle */}
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
