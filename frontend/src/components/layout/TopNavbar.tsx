import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Dropdown, Badge } from 'react-bootstrap';
import { useAuth } from '../../contexts/AuthContext';
import {
  FaBars,
  FaBell,
  FaUser,
  FaSignOutAlt,
  FaCog,
  FaKey,
} from 'react-icons/fa';

interface TopNavbarProps {
  onMenuToggle: () => void;
}

const TopNavbar: React.FC<TopNavbarProps> = ({ onMenuToggle }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="top-navbar d-flex align-items-center justify-content-between">
      <div className="d-flex align-items-center">
        <button
          className="btn btn-link text-dark d-lg-none me-3"
          onClick={onMenuToggle}
        >
          <FaBars size={20} />
        </button>
      </div>

      <div className="d-flex align-items-center gap-3">
        {/* Notifications */}
        <Dropdown align="end">
          <Dropdown.Toggle
            variant="link"
            className="text-dark position-relative p-0"
            id="notifications-dropdown"
          >
            <FaBell size={18} />
            <Badge
              bg="danger"
              pill
              className="position-absolute"
              style={{ top: '-5px', right: '-5px', fontSize: '0.65rem' }}
            >
              3
            </Badge>
          </Dropdown.Toggle>

          <Dropdown.Menu style={{ minWidth: '300px' }}>
            <Dropdown.Header>Notifications</Dropdown.Header>
            <Dropdown.Item>
              <small className="text-muted">New indent pending approval</small>
            </Dropdown.Item>
            <Dropdown.Item>
              <small className="text-muted">PO delivery due tomorrow</small>
            </Dropdown.Item>
            <Dropdown.Item>
              <small className="text-muted">Low stock alert: 5 items</small>
            </Dropdown.Item>
            <Dropdown.Divider />
            <Dropdown.Item className="text-center text-primary">
              View all notifications
            </Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>

        {/* User Menu */}
        <Dropdown align="end">
          <Dropdown.Toggle
            variant="link"
            className="text-dark d-flex align-items-center gap-2 p-0 text-decoration-none"
            id="user-dropdown"
          >
            <div
              className="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center"
              style={{ width: '36px', height: '36px' }}
            >
              <FaUser size={14} />
            </div>
            <div className="text-start d-none d-md-block">
              <div className="fw-medium" style={{ fontSize: '0.875rem' }}>
                {user?.employeeName || user?.username}
              </div>
              <div className="text-muted" style={{ fontSize: '0.75rem' }}>
                {user?.roles?.[0] || 'User'}
              </div>
            </div>
          </Dropdown.Toggle>

          <Dropdown.Menu>
            <Dropdown.Header>
              <div>{user?.employeeName || user?.username}</div>
              <small className="text-muted">{user?.email}</small>
            </Dropdown.Header>
            <Dropdown.Divider />
            <Dropdown.Item onClick={() => navigate('/profile')}>
              <FaUser className="me-2" /> Profile
            </Dropdown.Item>
            <Dropdown.Item onClick={() => navigate('/change-password')}>
              <FaKey className="me-2" /> Change Password
            </Dropdown.Item>
            <Dropdown.Item onClick={() => navigate('/settings')}>
              <FaCog className="me-2" /> Settings
            </Dropdown.Item>
            <Dropdown.Divider />
            <Dropdown.Item onClick={handleLogout} className="text-danger">
              <FaSignOutAlt className="me-2" /> Logout
            </Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
      </div>
    </div>
  );
};

export default TopNavbar;
