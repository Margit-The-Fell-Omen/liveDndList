import {useState, useEffect} from 'react';

/**
 * A custom hook to determine if the screen matches a given media query.
 * Replaces unreliable `window.innerWidth` checks in components.
 * @param query The media query string (e.g., '(min-width: 1024px)')
 */
export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(window.matchMedia(query).matches);

  useEffect(() => {
    const mediaQueryList = window.matchMedia(query);
    const listener = (event: MediaQueryListEvent) => setMatches(event.matches);

    // Add listener
    mediaQueryList.addEventListener('change', listener);

    // Initial check in case the state has changed since initialization
    setMatches(mediaQueryList.matches);

    // Cleanup listener on component unmount
    return () => {
      mediaQueryList.removeEventListener('change', listener);
    };
  }, [query]);

  return matches;
}