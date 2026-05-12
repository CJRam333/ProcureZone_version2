import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopNavbar from './TopNavbar';
import { useSettings } from '../../contexts/SettingsContext';
import './MainLayout.css';

const MainLayout: React.FC = () => {
  const { settings } = useSettings();
  // Initialise from saved settings so sidebar preference is honoured on load
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => settings.sidebarCollapsed);
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarCollapsed(prev => !prev);
  };

  const toggleMobileSidebar = () => {
    setMobileSidebarOpen(prev => !prev);
  };

  const closeMobileSidebar = () => {
    setMobileSidebarOpen(false);
  };

  return (
    <div className="app-container">
      {/* Mobile sidebar overlay */}
      <div
        className={`sidebar-overlay ${mobileSidebarOpen ? 'show' : ''}`}
        onClick={closeMobileSidebar}
        aria-hidden="true"
      />

      <Sidebar
        collapsed={sidebarCollapsed}
        onToggle={toggleSidebar}
        mobileOpen={mobileSidebarOpen}
        onMobileClose={closeMobileSidebar}
      />

      <div className={`main-content ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
        <TopNavbar onMenuToggle={toggleMobileSidebar} />
        <div className="content-wrapper p-4">
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default MainLayout;
