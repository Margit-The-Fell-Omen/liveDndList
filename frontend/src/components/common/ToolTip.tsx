// src/components/common/Tooltip.tsx

import {type ReactNode, useEffect, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import styles from './Tooltip.module.css';

export interface TooltipProps {
  children: ReactNode;
  content: ReactNode; // Allow React nodes for more flexible content
  position?: 'top' | 'bottom' | 'left' | 'right';
  delay?: number;
}

interface Coords {
  top: number;
  left: number;
}

export function Tooltip({children, content, position = 'top', delay = 300}: TooltipProps) {
  const [isVisible, setIsVisible] = useState(false);
  const [coords, setCoords] = useState<Coords>({top: 0, left: 0});

  const triggerRef = useRef<HTMLSpanElement | null>(null);
  const timeoutRef = useRef<number | null>(null);

  const showTooltip = () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    timeoutRef.current = window.setTimeout(() => {
      if (triggerRef.current) {
        const rect = triggerRef.current.getBoundingClientRect();
        const scrollTop = window.scrollY;
        const scrollLeft = window.scrollX;

        let top = 0;
        let left = 0;

        switch (position) {
          case 'top':
            top = rect.top + scrollTop - 8;
            left = rect.left + scrollLeft + rect.width / 2;
            break;
          case 'bottom':
            top = rect.bottom + scrollTop + 8;
            left = rect.left + scrollLeft + rect.width / 2;
            break;
          case 'left':
            top = rect.top + scrollTop + rect.height / 2;
            left = rect.left + scrollLeft - 8;
            break;
          case 'right':
            top = rect.top + scrollTop + rect.height / 2;
            left = rect.right + scrollLeft + 8;
            break;
        }

        setCoords({top, left});
        setIsVisible(true);
      }
    }, delay);
  };

  const hideTooltip = () => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }
    setIsVisible(false);
  };

  useEffect(() => {
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, []);

  return (
      <>
      <span
          ref={triggerRef}
          onMouseEnter={showTooltip}
          onMouseLeave={hideTooltip}
          onFocus={showTooltip}
          onBlur={hideTooltip}
          className={styles.trigger}
          tabIndex={0}
      >
        {children}
      </span>

        {isVisible &&
            createPortal(
                <div
                    className={`${styles.tooltip} ${styles[position]}`}
                    style={{top: `${coords.top}px`, left: `${coords.left}px`}}
                    role="tooltip"
                >
                  {content}
                </div>,
                document.body
            )}
      </>
  );
}
