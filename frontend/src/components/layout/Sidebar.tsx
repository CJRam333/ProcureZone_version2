import React, { useState, useEffect } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {
  FaHome,
  FaFileAlt,
  FaShoppingCart,
  FaTruck,
  FaClipboardList,
  FaBoxes,
  FaChevronLeft,
  FaChevronRight,
  FaChevronDown,
  FaChevronUp,
  FaBuilding,
  FaChartBar,
  FaChartLine,
  FaDatabase,
  FaIndustry,
  FaMapMarkerAlt,
  FaSitemap,
  FaLayerGroup,
  FaRuler,
  FaUsers,
  FaUserCog,
  FaUserTag,
  FaCubes,
  FaHandshake,
  FaSeedling,
  FaCheckCircle,
  FaExclamationTriangle,
  FaCog,
  FaHistory,
  FaEnvelope,
  FaLeaf,
  FaLock,
} from 'react-icons/fa';

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
  mobileOpen?: boolean;
  onMobileClose?: () => void;
}

interface NavItem {
  path: string;
  label: string;
  icon: React.ReactNode;
  roles?: string[];
  /** Set true to hide from nav without removing the route or page. Re-enable by removing this flag. */
  hidden?: boolean;
  /** Module code for hasModuleAccess() check. If absent, no module gate applied. */
  moduleCode?: string;
  /** Show this item greyed-out as a future/coming-soon feature rather than hiding it. */
  future?: boolean;
}

interface NavGroup {
  label: string;
  icon: React.ReactNode;
  roles?: string[];
  items: NavItem[];
  groupKey: string;
  moduleCode?: string;
  future?: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ collapsed, onToggle, mobileOpen = false, onMobileClose }) => {
  const { hasAnyRole, hasModuleAccess } = useAuth();
  const location = useLocation();
  const [expandedGroups, setExpandedGroups] = useState<string[]>([]);

  const toggleGroup = (groupKey: string) => {
    setExpandedGroups(prev =>
      prev.includes(groupKey)
        ? prev.filter(g => g !== groupKey)
        : [...prev, groupKey]
    );
  };

  const navItems: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', icon: <FaHome /> },
    {
      path: '/analytics',
      label: 'Analytics',
      icon: <FaChartLine />,
      roles: ['SUPERADMIN', 'ADMIN', 'DEPTHEAD'],
      moduleCode: 'ANALYTICS',
      future: true,
    },
    {
      path: '/indents',
      label: 'Indents',
      icon: <FaFileAlt />,
      moduleCode: 'INDENTS',
    },
    {
      path: '/plant-indent',
      label: 'Plant Indent',
      icon: <FaSeedling />,
      moduleCode: 'PLANT_INDENTS',
    },
    {
      path: '/purchase-orders',
      label: 'Purchase Orders',
      icon: <FaShoppingCart />,
      roles: ['SUPERADMIN', 'ADMIN', 'PROCUREMENT'],
      moduleCode: 'PURCHASE_ORDERS',
      future: true,
    },
    {
      path: '/grn',
      label: 'GRN',
      icon: <FaTruck />,
      roles: ['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE', 'QUALITYMANAGER'],
      moduleCode: 'GRN',
      future: true,
    },
    {
      path: '/quality-control',
      label: 'Quality Control',
      icon: <FaCheckCircle />,
      roles: ['SUPERADMIN', 'ADMIN', 'QUALITYMANAGER'],
      moduleCode: 'QUALITY_CONTROL',
      future: true,
    },
    {
      path: '/issue-notes',
      label: 'Issue Notes',
      icon: <FaClipboardList />,
      roles: ['SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'ISSUECONFIRM', 'SUPERVISOR', 'PROCUREMENT'],
      moduleCode: 'ISSUE_NOTES',
    },
    {
      path: '/confirmations/issue',
      label: 'Confirmations',
      icon: <FaExclamationTriangle />,
      roles: ['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'DEPTHEAD', 'USER', 'SUPERVISOR'],
      moduleCode: 'CONFIRMATIONS',
    },
    {
      path: '/inventory',
      label: 'Inventory',
      icon: <FaBoxes />,
      roles: ['SUPERADMIN', 'ADMIN', 'FLOORINCHARGE', 'GOODSINCHARGE'],
      moduleCode: 'INVENTORY',
      future: true,
    },
    {
      path: '/audit-logs',
      label: 'Audit Logs',
      icon: <FaHistory />,
      roles: ['SUPERADMIN', 'ADMIN', 'ROLE_VIEWER'],
      moduleCode: 'AUDIT_LOGS',
    },
    {
      path: '/email-templates',
      label: 'Email Templates',
      icon: <FaEnvelope />,
      roles: ['SUPERADMIN', 'ADMIN'],
      moduleCode: 'EMAIL_TEMPLATES',
    },
    {
      path: '/material-import',
      label: 'Material Import',
      icon: <FaCubes />,
      roles: ['SUPERADMIN', 'ADMIN'],
      moduleCode: 'MATERIAL_IMPORT',
    },
  ];

  const reportsGroup: NavGroup = {
    label: 'Reports',
    icon: <FaChartBar />,
    roles: ['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'SUPERVISOR'],
    groupKey: 'reports',
    moduleCode: 'REPORTS',
    items: [
      { path: '/reports', label: 'Summary', icon: <FaChartLine /> },
      { path: '/reports/indents', label: 'Indents', icon: <FaFileAlt /> },
      { path: '/reports/issue-notes', label: 'Issue Notes', icon: <FaClipboardList /> },
    ],
  };

  const masterDataGroup: NavGroup = {
    label: 'Master Data',
    icon: <FaDatabase />,
    roles: ['SUPERADMIN', 'ADMIN', 'MASTER_DATA_ADMIN'],
    groupKey: 'masters',
    moduleCode: 'MASTERS',
    items: [
      { path: '/masters/companies', label: 'Companies', icon: <FaBuilding /> },
      { path: '/masters/plants', label: 'Plants', icon: <FaIndustry /> },
      { path: '/masters/locations', label: 'Locations', icon: <FaMapMarkerAlt /> },
      { path: '/masters/departments', label: 'Departments', icon: <FaSitemap /> },
      { path: '/masters/sections', label: 'Sections', icon: <FaLayerGroup /> },
      { path: '/masters/uom', label: 'Unit of Measure', icon: <FaRuler /> },
      { path: '/masters/crops', label: 'Crop Types', icon: <FaLeaf />, roles: ['SUPERADMIN', 'ADMIN', 'PLANTMANAGER'] },
      { path: '/masters/materials', label: 'Materials', icon: <FaCubes /> },
      { path: '/masters/vendors', label: 'Vendors', icon: <FaHandshake />, moduleCode: 'VENDOR_MASTER', future: true },
      { path: '/masters/employees', label: 'Employees', icon: <FaUsers /> },
      { path: '/masters/users', label: 'Users', icon: <FaUserCog />, hidden: true },
      { path: '/masters/roles', label: 'Roles', icon: <FaUserTag />, roles: ['SUPERADMIN'] },
    ],
  };

  const navGroups: NavGroup[] = [reportsGroup, masterDataGroup];

  // Auto-expand sidebar group when URL matches a child route
  useEffect(() => {
    navGroups.forEach(group => {
      const matchesChild = group.items.some(item => location.pathname.startsWith(item.path));
      if (matchesChild && !expandedGroups.includes(group.groupKey)) {
        setExpandedGroups(prev => [...prev, group.groupKey]);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);

  // Active nav items: not hidden, role allowed, module accessible (or no module gate)
  const activeNavItems = navItems.filter(
    (item) =>
      !item.hidden &&
      !item.future &&
      (!item.roles || hasAnyRole(item.roles)) &&
      (!item.moduleCode || hasModuleAccess(item.moduleCode))
  );

  // Future nav items: role allowed, marked as future, shown greyed-out
  const futureNavItems = navItems.filter(
    (item) =>
      item.future &&
      (!item.roles || hasAnyRole(item.roles))
  );

  // Handle nav link click to close mobile sidebar
  const handleNavClick = () => {
    if (mobileOpen && onMobileClose) {
      onMobileClose();
    }
  };

  const renderNavGroup = (group: NavGroup) => {
    const hasRoleAccess = !group.roles || hasAnyRole(group.roles);
    const hasModule = !group.moduleCode || hasModuleAccess(group.moduleCode);
    const isFutureGroup = group.future === true;

    if (!hasRoleAccess) return null;
    // Future groups are shown greyed out even if module not yet accessible
    if (!hasModule && !isFutureGroup) return null;

    const isActive = !isFutureGroup && group.items.some(item => location.pathname.startsWith(item.path));
    const isExpanded = expandedGroups.includes(group.groupKey);
    const isDisabled = isFutureGroup || !hasModule;

    return (
      <li key={group.groupKey} className="nav-item">
        <button
          className={`nav-link w-100 text-start d-flex align-items-center justify-content-between ${isActive ? 'active' : ''} ${isDisabled ? 'opacity-50' : ''}`}
          onClick={() => !collapsed && !isDisabled && toggleGroup(group.groupKey)}
          title={collapsed ? group.label : (isDisabled ? `${group.label} — coming soon` : undefined)}
          disabled={isDisabled}
          style={isDisabled ? { cursor: 'default', filter: 'grayscale(0.4)' } : undefined}
        >
          <span className="d-flex align-items-center">
            <span className="nav-icon">{group.icon}</span>
            {!collapsed && (
              <span className="nav-text" style={isDisabled ? { fontStyle: 'italic', color: 'inherit' } : undefined}>
                {group.label}
                {isDisabled && <FaLock size={10} className="ms-1 opacity-75" />}
              </span>
            )}
          </span>
          {!collapsed && !isDisabled && (
            <span className="ms-auto">
              {isExpanded ? <FaChevronUp size={12} /> : <FaChevronDown size={12} />}
            </span>
          )}
        </button>
        {!collapsed && isExpanded && !isDisabled && (
          <ul className="nav flex-column ms-3 submenu">
            {group.items
              .filter(item => !item.hidden && (!item.roles || hasAnyRole(item.roles)))
              .map((subItem) => {
                const subHasModule = !subItem.moduleCode || hasModuleAccess(subItem.moduleCode);
                const subIsFuture = subItem.future === true;
                const subDisabled = subIsFuture || !subHasModule;
                return (
                  <li key={subItem.path} className="nav-item">
                    {subDisabled ? (
                      <span
                        className="nav-link py-1 opacity-50 d-flex align-items-center"
                        title={`${subItem.label} — coming soon`}
                        style={{ cursor: 'default', fontStyle: 'italic', filter: 'grayscale(0.4)' }}
                      >
                        <span className="nav-icon small">{subItem.icon}</span>
                        <span className="nav-text small">{subItem.label}</span>
                        <FaLock size={9} className="ms-1 opacity-75" />
                      </span>
                    ) : (
                      <NavLink
                        to={subItem.path}
                        className={({ isActive }) =>
                          `nav-link py-1 ${isActive ? 'active' : ''}`
                        }
                        onClick={handleNavClick}
                      >
                        <span className="nav-icon small">{subItem.icon}</span>
                        <span className="nav-text small">{subItem.label}</span>
                      </NavLink>
                    )}
                  </li>
                );
              })}
          </ul>
        )}
      </li>
    );
  };

  return (
    <nav className={`sidebar ${collapsed ? 'collapsed' : ''} ${mobileOpen ? 'show' : ''}`}>
      <div className="sidebar-header d-flex align-items-center justify-content-between">
        <div className="sidebar-brand d-flex align-items-center">
          {!collapsed ? (
            <img src="/logo-full.png" alt="ProcureZone" style={{ maxHeight: '35px', maxWidth: '160px', objectFit: 'contain' }} />
          ) : (
            <img src="/logo-icon.png" alt="PZ" style={{ maxHeight: '30px' }} />
          )}
        </div>
        <button
          className="btn btn-link text-white p-0"
          onClick={onToggle}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? <FaChevronRight /> : <FaChevronLeft />}
        </button>
      </div>

      <ul className="nav flex-column mt-3">
        {/* Active nav items */}
        {activeNavItems.map((item) => (
          <li key={item.path} className="nav-item">
            <NavLink
              to={item.path}
              className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }
              title={collapsed ? item.label : undefined}
              onClick={handleNavClick}
            >
              <span className="nav-icon">{item.icon}</span>
              {!collapsed && <span className="nav-text">{item.label}</span>}
            </NavLink>
          </li>
        ))}

        {/* Nav groups (Master Data, Administration) */}
        {navGroups.map(group => renderNavGroup(group))}

        {/* Future/coming-soon items — greyed out with lock icon */}
        {futureNavItems.map((item) => (
          <li key={item.path} className="nav-item">
            <span
              className="nav-link opacity-50 d-flex align-items-center"
              title={collapsed ? `${item.label} — coming soon` : undefined}
              style={{ cursor: 'default', fontStyle: 'italic', filter: 'grayscale(0.4)' }}
            >
              <span className="nav-icon">{item.icon}</span>
              {!collapsed && (
                <>
                  <span className="nav-text">{item.label}</span>
                  <FaLock size={10} className="ms-1 opacity-75" />
                </>
              )}
            </span>
          </li>
        ))}
      </ul>
    </nav>
  );
};

export default Sidebar;
