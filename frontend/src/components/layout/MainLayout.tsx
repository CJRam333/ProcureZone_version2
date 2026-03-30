import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopNavbar from './TopNavbar';
import './MainLayout.css';

const MainLayout: React.FC = () => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed);
  };

  const toggleMobileSidebar = () => {
    setMobileSidebarOpen(prev => !prev);
  };

  const closeMobileSidebar = () => {
    setMobileSidebarOpen(false);
  };

  return (
    <div className="app-container">
      {/* Mobile sidebar overlay - closes sidebar when clicked */}
      <div
        className={`sidebar-overlay ${mobileSidebarOpen ? 'show' : ''}`}
        onClick={closeMobileSidebar}
        aria-hidden="true"
      />

      {/* Sidebar - uses CSS classes for responsive behavior */}
      <Sidebar 
        collapsed={sidebarCollapsed} 
        onToggle={toggleSidebar}
        mobileOpen={mobileSidebarOpen}
        onMobileClose={closeMobileSidebar}
      />

      {/* Main content */}
      <div
        className={`main-content ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}
      >
        <TopNavbar onMenuToggle={toggleMobileSidebar} />

        <div className="content-wrapper p-4">
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default MainLayout;
