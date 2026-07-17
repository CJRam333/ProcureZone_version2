import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';

/**
 * Resets the window scroll position to the top on every route change, so a new page never
 * inherits the previous page's scroll offset. Mounted once inside MainLayout (which stays
 * mounted across route changes and lives within the router context). The app scrolls on the
 * window (no inner overflow container), so window.scrollTo is sufficient.
 */
export function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);
  return null;
}

export default ScrollToTop;
