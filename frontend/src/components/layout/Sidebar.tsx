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
}

interface NavGroup {
  label: string;
  icon: React.ReactNode;
  roles?: string[];
  items: NavItem[];
  groupKey: string;
}

const Sidebar: React.FC<SidebarProps> = ({ collapsed, onToggle, mobileOpen = false, onMobileClose }) => {
  const { hasAnyRole } = useAuth();
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
      hidden: true, // SCOPE-REDUCTION: not active in current production phase
    },
    {
      path: '/indents',
      label: 'Indents',
      icon: <FaFileAlt />,
      // Legacy: SuperAdmin, Admin, Manager(role=3), Procurement(6), Supervisor(4), DepartmentHead(5)
      roles: ['SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'DEPTHEAD', 'USER'],
    },
    {
      path: '/plant-indent',
      label: 'Plant Indent',
      icon: <FaSeedling />,
      // Plant-specific indent management with crop type tracking
      roles: ['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'FLOORINCHARGE'],
    },
    {
      path: '/purchase-orders',
      label: 'Purchase Orders',
      icon: <FaShoppingCart />,
      // Legacy: SuperAdmin, Procurement(6)
      roles: ['SUPERADMIN', 'ADMIN', 'PROCUREMENT'],
      hidden: true, // SCOPE-REDUCTION: not active in current production phase
    },
    {
      path: '/grn',
      label: 'GRN',
      icon: <FaTruck />,
      // Legacy: SuperAdmin, GRNIncharge(11), QualityManager(14), GoodsIncharge(10)
      roles: ['SUPERADMIN', 'ADMIN', 'GOODSINCHARGE', 'GRNINCHARGE', 'QUALITYMANAGER'],
      hidden: true, // SCOPE-REDUCTION: not active in current production phase
    },
    {
      path: '/quality-control',
      label: 'Quality Control',
      icon: <FaCheckCircle />,
      // QC inspection and rejected items management
      roles: ['SUPERADMIN', 'ADMIN', 'QUALITYMANAGER'],
      hidden: true, // SCOPE-REDUCTION: not active in current production phase
    },
    {
      path: '/issue-notes',
      label: 'Issue Notes',
      icon: <FaClipboardList />,
      // Legacy: SuperAdmin, Admin, Manager(role=3), Procurement(6), Supervisor(4)
      roles: ['SUPERADMIN', 'ADMIN', 'USER', 'DEPTHEAD', 'ISSUECONFIRM'],
    },
    {
      path: '/confirmations/issue',
      label: 'Confirmations',
      icon: <FaExclamationTriangle />,
      // Issue and receipt confirmation workflows
      roles: ['SUPERADMIN', 'ADMIN', 'ISSUECONFIRM', 'RECEIPTCONFIRM', 'DEPTHEAD', 'USER'],
    },
    {
      path: '/inventory',
      label: 'Inventory',
      icon: <FaBoxes />,
      // Legacy: FloorIncharge(8), GoodsIncharge(10), IssueConfirm(12), ReceiptConfirm(13)
      roles: ['SUPERADMIN', 'ADMIN', 'FLOORINCHARGE', 'GOODSINCHARGE'],
      hidden: true, // SCOPE-REDUCTION: not active in current production phase
    },
    {
      path: '/reports',
      label: 'Reports',
      icon: <FaChartBar />,
      // Legacy: SuperAdmin, DepartmentHead(5), Admin(2)
      roles: ['SUPERADMIN', 'ADMIN', 'DEPTHEAD'],
    },
  ];

  const masterDataGroup: NavGroup = {
    label: 'Master Data',
    icon: <FaDatabase />,
    roles: ['SUPERADMIN', 'ADMIN'],
    groupKey: 'masters',
    items: [
      { path: '/masters/companies', label: 'Companies', icon: <FaBuilding /> },
      { path: '/masters/plants', label: 'Plants', icon: <FaIndustry /> },
      { path: '/masters/locations', label: 'Locations', icon: <FaMapMarkerAlt /> },
      { path: '/masters/departments', label: 'Departments', icon: <FaSitemap /> },
      { path: '/masters/sections', label: 'Sections', icon: <FaLayerGroup /> },
      { path: '/masters/uom', label: 'Unit of Measure', icon: <FaRuler /> },
      { path: '/masters/crops', label: 'Crop Types', icon: <FaLeaf />, roles: ['SUPERADMIN', 'ADMIN', 'PLANTMANAGER'] },
      { path: '/masters/materials', label: 'Materials', icon: <FaCubes /> },
      { path: '/masters/vendors', label: 'Vendors', icon: <FaHandshake />, hidden: true }, // SCOPE-REDUCTION: not active in current production phase
      { path: '/masters/employees', label: 'Employees', icon: <FaUsers /> },
      { path: '/masters/users', label: 'Users', icon: <FaUserCog /> },
      { path: '/masters/roles', label: 'Roles', icon: <FaUserTag />, roles: ['SUPERADMIN'] },
    ],
  };

  const adminGroup: NavGroup = {
    label: 'Administration',
    icon: <FaCog />,
    roles: ['SUPERADMIN', 'ADMIN'],
    groupKey: 'admin',
    items: [
      { path: '/admin/audit-logs', label: 'Audit Logs', icon: <FaHistory />, roles: ['SUPERADMIN', 'ADMIN'] },
      { path: '/admin/email-templates', label: 'Email Templates', icon: <FaEnvelope />, roles: ['SUPERADMIN', 'ADMIN'] },
      { path: '/materials/import', label: 'Material Import', icon: <FaCubes />, roles: ['SUPERADMIN', 'ADMIN'] },
    ],
  };

  const navGroups: NavGroup[] = [masterDataGroup, adminGroup];

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

  const filteredNavItems = navItems.filter(
    (item) => !item.hidden && (!item.roles || hasAnyRole(item.roles))
  );

  // Handle nav link click to close mobile sidebar
  const handleNavClick = () => {
    if (mobileOpen && onMobileClose) {
      onMobileClose();
    }
  };

  const renderNavGroup = (group: NavGroup) => {
    const isVisible = !group.roles || hasAnyRole(group.roles);
    const isActive = group.items.some(item => location.pathname.startsWith(item.path));
    const isExpanded = expandedGroups.includes(group.groupKey);

    if (!isVisible) return null;

    return (
      <li key={group.groupKey} className="nav-item">
        <button
          className={`nav-link w-100 text-start d-flex align-items-center justify-content-between ${isActive ? 'active' : ''}`}
          onClick={() => !collapsed && toggleGroup(group.groupKey)}
          title={collapsed ? group.label : undefined}
        >
          <span className="d-flex align-items-center">
            <span className="nav-icon">{group.icon}</span>
            {!collapsed && <span className="nav-text">{group.label}</span>}
          </span>
          {!collapsed && (
            <span className="ms-auto">
              {isExpanded ? <FaChevronUp size={12} /> : <FaChevronDown size={12} />}
            </span>
          )}
        </button>
        {!collapsed && isExpanded && (
          <ul className="nav flex-column ms-3 submenu">
            {group.items
              .filter(item => !item.hidden && (!item.roles || hasAnyRole(item.roles)))
              .map((subItem) => (
                <li key={subItem.path} className="nav-item">
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
                </li>
              ))}
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
        {filteredNavItems.map((item) => (
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

        {/* Render nav groups (Master Data, Administration) */}
        {navGroups.map(group => renderNavGroup(group))}
      </ul>
    </nav>
  );
};

export default Sidebar;
