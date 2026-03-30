import React, { useState } from 'react';
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
  FaLink,
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
}

interface NavGroup {
  label: string;
  icon: React.ReactNode;
  roles?: string[];
  items: NavItem[];
}

const Sidebar: React.FC<SidebarProps> = ({ collapsed, onToggle, mobileOpen = false, onMobileClose }) => {
  const { hasAnyRole } = useAuth();
  const location = useLocation();
  const [expandedGroups, setExpandedGroups] = useState<string[]>([]);

  const toggleGroup = (groupLabel: string) => {
    setExpandedGroups(prev =>
      prev.includes(groupLabel)
        ? prev.filter(g => g !== groupLabel)
        : [...prev, groupLabel]
    );
  };

  const navItems: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', icon: <FaHome /> },
    {
      path: '/analytics',
      label: 'Analytics',
      icon: <FaChartLine />,
      roles: ['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'AUDITOR'],
    },
    {
      path: '/indents',
      label: 'Indents',
      icon: <FaFileAlt />,
      // Legacy: SuperAdmin, Admin, Manager(role=3), Procurement(6), Supervisor(4), DepartmentHead(5)
      roles: ['SUPERADMIN', 'ADMIN', 'PROCUREMENT', 'DEPTHEAD', 'EMPLOYEE'],
    },
    {
      path: '/plant-indent',
      label: 'Plant Indent',
      icon: <FaSeedling />,
      // Plant-specific indent management with crop type tracking
      roles: ['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'STOREKEEPER'],
    },
    {
      path: '/purchase-orders',
      label: 'Purchase Orders',
      icon: <FaShoppingCart />,
      // Legacy: SuperAdmin, Procurement(6)
      roles: ['SUPERADMIN', 'ADMIN', 'PROCUREMENT'],
    },
    {
      path: '/grn',
      label: 'GRN',
      icon: <FaTruck />,
      // Legacy: SuperAdmin, GRNIncharge(11), QualityManager(14), GoodsIncharge(10)
      roles: ['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'QUALITY'],
    },
    {
      path: '/quality-control',
      label: 'Quality Control',
      icon: <FaCheckCircle />,
      // QC inspection and rejected items management
      roles: ['SUPERADMIN', 'ADMIN', 'QUALITY'],
    },
    {
      path: '/issue-notes',
      label: 'Issue Notes',
      icon: <FaClipboardList />,
      // Legacy: SuperAdmin, Admin, Manager(role=3), Procurement(6), Supervisor(4)
      roles: ['SUPERADMIN', 'ADMIN', 'EMPLOYEE', 'DEPTHEAD', 'STOREKEEPER'],
    },
    {
      path: '/confirmations/issue',
      label: 'Issue Confirmation',
      icon: <FaExclamationTriangle />,
      // Issue confirmation workflow
      roles: ['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'DEPTHEAD'],
    },
    {
      path: '/confirmations/receipt',
      label: 'Receipt Confirmation',
      icon: <FaCheckCircle />,
      // Receipt confirmation by departments
      roles: ['SUPERADMIN', 'ADMIN', 'STOREKEEPER', 'DEPTHEAD', 'EMPLOYEE'],
    },
    {
      path: '/inventory',
      label: 'Inventory',
      icon: <FaBoxes />,
      // Legacy: FloorIncharge(8), GoodsIncharge(10), IssueConfirm(12), ReceiptConfirm(13)
      roles: ['SUPERADMIN', 'ADMIN', 'STOREKEEPER'],
    },
    {
      path: '/reports',
      label: 'Reports',
      icon: <FaChartBar />,
      // Legacy: SuperAdmin, DepartmentHead(5), Admin(2)
      roles: ['SUPERADMIN', 'ADMIN', 'DEPTHEAD', 'AUDITOR'],
    },
  ];

  const masterDataGroup: NavGroup = {
    label: 'Master Data',
    icon: <FaDatabase />,
    roles: ['SUPERADMIN', 'ADMIN'],
    items: [
      { path: '/masters/companies', label: 'Companies', icon: <FaBuilding /> },
      { path: '/masters/plants', label: 'Plants', icon: <FaIndustry /> },
      { path: '/masters/locations', label: 'Locations', icon: <FaMapMarkerAlt /> },
      { path: '/masters/departments', label: 'Departments', icon: <FaSitemap /> },
      { path: '/masters/sections', label: 'Sections', icon: <FaLayerGroup /> },
      { path: '/masters/uom', label: 'Unit of Measure', icon: <FaRuler /> },
      { path: '/masters/crops', label: 'Crop Types', icon: <FaLeaf />, roles: ['SUPERADMIN', 'ADMIN', 'PLANTMANAGER'] },
      { path: '/masters/materials', label: 'Materials', icon: <FaCubes /> },
      { path: '/masters/vendors', label: 'Vendors', icon: <FaHandshake /> },
      { path: '/masters/employees', label: 'Employees', icon: <FaUsers /> },
      { path: '/masters/users', label: 'Users', icon: <FaUserCog /> },
      { path: '/masters/roles', label: 'Roles', icon: <FaUserTag />, roles: ['SUPERADMIN'] },
    ],
  };

  const mappingsGroup: NavGroup = {
    label: 'Mappings',
    icon: <FaLink />,
    roles: ['SUPERADMIN', 'ADMIN'],
    items: [
      { path: '/mappings/company-departments', label: 'Company-Department', icon: <FaSitemap /> },
      { path: '/mappings/company-locations', label: 'Company-Location', icon: <FaMapMarkerAlt /> },
      { path: '/mappings/employee-roles', label: 'Employee-Role', icon: <FaUserTag /> },
      { path: '/mappings/company-location-materials', label: 'Location-Material', icon: <FaCubes /> },
      { path: '/mappings/plant-materials', label: 'Plant-Material', icon: <FaIndustry />, roles: ['SUPERADMIN', 'ADMIN', 'PLANTMANAGER'] },
      { path: '/mappings/reporting-hierarchy', label: 'Reporting Hierarchy', icon: <FaUsers /> },
    ],
  };

  const adminGroup: NavGroup = {
    label: 'Administration',
    icon: <FaCog />,
    roles: ['SUPERADMIN', 'ADMIN', 'AUDITOR'],
    items: [
      { path: '/admin/audit-logs', label: 'Audit Logs', icon: <FaHistory />, roles: ['SUPERADMIN', 'ADMIN', 'AUDITOR'] },
      { path: '/admin/email-templates', label: 'Email Templates', icon: <FaEnvelope />, roles: ['SUPERADMIN', 'ADMIN'] },
      { path: '/materials/import', label: 'Material Import', icon: <FaCubes />, roles: ['SUPERADMIN', 'ADMIN'] },
    ],
  };

  const filteredNavItems = navItems.filter(
    (item) => !item.roles || hasAnyRole(item.roles)
  );

  const isMasterDataVisible = !masterDataGroup.roles || hasAnyRole(masterDataGroup.roles);
  const isMasterDataActive = location.pathname.startsWith('/masters');
  const isMasterDataExpanded = expandedGroups.includes('masters');

  const isMappingsVisible = !mappingsGroup.roles || hasAnyRole(mappingsGroup.roles);
  const isMappingsActive = location.pathname.startsWith('/mappings');
  const isMappingsExpanded = expandedGroups.includes('mappings');

  const isAdminVisible = !adminGroup.roles || hasAnyRole(adminGroup.roles);
  const isAdminActive = location.pathname.startsWith('/admin') || location.pathname === '/materials/import';
  const isAdminExpanded = expandedGroups.includes('admin');

  // Handle nav link click to close mobile sidebar
  const handleNavClick = () => {
    if (mobileOpen && onMobileClose) {
      onMobileClose();
    }
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

        {/* Master Data Group */}
        {isMasterDataVisible && (
          <li className="nav-item">
            <button
              className={`nav-link w-100 text-start d-flex align-items-center justify-content-between ${isMasterDataActive ? 'active' : ''}`}
              onClick={() => !collapsed && toggleGroup('masters')}
              title={collapsed ? 'Master Data' : undefined}
            >
              <span className="d-flex align-items-center">
                <span className="nav-icon">{masterDataGroup.icon}</span>
                {!collapsed && <span className="nav-text">{masterDataGroup.label}</span>}
              </span>
              {!collapsed && (
                <span className="ms-auto">
                  {isMasterDataExpanded ? <FaChevronUp size={12} /> : <FaChevronDown size={12} />}
                </span>
              )}
            </button>
            {!collapsed && isMasterDataExpanded && (
              <ul className="nav flex-column ms-3 submenu">
                {masterDataGroup.items
                  .filter(item => !item.roles || hasAnyRole(item.roles))
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
        )}

        {/* Mappings Group */}
        {isMappingsVisible && (
          <li className="nav-item">
            <button
              className={`nav-link w-100 text-start d-flex align-items-center justify-content-between ${isMappingsActive ? 'active' : ''}`}
              onClick={() => !collapsed && toggleGroup('mappings')}
              title={collapsed ? 'Mappings' : undefined}
            >
              <span className="d-flex align-items-center">
                <span className="nav-icon">{mappingsGroup.icon}</span>
                {!collapsed && <span className="nav-text">{mappingsGroup.label}</span>}
              </span>
              {!collapsed && (
                <span className="ms-auto">
                  {isMappingsExpanded ? <FaChevronUp size={12} /> : <FaChevronDown size={12} />}
                </span>
              )}
            </button>
            {!collapsed && isMappingsExpanded && (
              <ul className="nav flex-column ms-3 submenu">
                {mappingsGroup.items
                  .filter(item => !item.roles || hasAnyRole(item.roles))
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
        )}

        {/* Administration Group */}
        {isAdminVisible && (
          <li className="nav-item">
            <button
              className={`nav-link w-100 text-start d-flex align-items-center justify-content-between ${isAdminActive ? 'active' : ''}`}
              onClick={() => !collapsed && toggleGroup('admin')}
              title={collapsed ? 'Administration' : undefined}
            >
              <span className="d-flex align-items-center">
                <span className="nav-icon">{adminGroup.icon}</span>
                {!collapsed && <span className="nav-text">{adminGroup.label}</span>}
              </span>
              {!collapsed && (
                <span className="ms-auto">
                  {isAdminExpanded ? <FaChevronUp size={12} /> : <FaChevronDown size={12} />}
                </span>
              )}
            </button>
            {!collapsed && isAdminExpanded && (
              <ul className="nav flex-column ms-3 submenu">
                {adminGroup.items
                  .filter(item => !item.roles || hasAnyRole(item.roles))
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
        )}
      </ul>
    </nav>
  );
};

export default Sidebar;
