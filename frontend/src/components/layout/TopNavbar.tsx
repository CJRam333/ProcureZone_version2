import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Dropdown, Badge } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../contexts/AuthContext';
import { notificationsApi, NotificationItem } from '../../api/notifications';
import {
  FaBars,
  FaBell,
  FaUser,
  FaSignOutAlt,
  FaCog,
  FaKey,
  FaCircle,
} from 'react-icons/fa';

interface TopNavbarProps {
  onMenuToggle: () => void;
}

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

const TopNavbar: React.FC<TopNavbarProps> = ({ onMenuToggle }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const [notifOpen, setNotifOpen] = useState(false);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  const { data: unreadCount = 0 } = useQuery<number>({
    queryKey: ['notifications', 'unread-count'],
    queryFn: notificationsApi.getUnreadCount,
    refetchInterval: 30000,
    staleTime: 25000,
  });

  const { data: notifPage } = useQuery({
    queryKey: ['notifications', 'list'],
    queryFn: () => notificationsApi.getList(0, 10),
    enabled: notifOpen,
    staleTime: 15000,
  });

  const markReadMutation = useMutation({
    mutationFn: notificationsApi.markAsRead,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const markAllMutation = useMutation({
    mutationFn: notificationsApi.markAllAsRead,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['notifications'] });
    },
  });

  const handleNotifClick = (n: NotificationItem) => {
    if (!n.read) markReadMutation.mutate(n.id);
    if (n.link) navigate(n.link);
    setNotifOpen(false);
  };

  const notifications: NotificationItem[] = notifPage?.content ?? [];

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
        <Dropdown
          align="end"
          show={notifOpen}
          onToggle={(open) => setNotifOpen(open)}
        >
          <Dropdown.Toggle
            variant="link"
            className="text-dark position-relative p-0"
            id="notifications-dropdown"
          >
            <FaBell size={18} />
            {unreadCount > 0 && (
              <Badge
                bg="danger"
                pill
                className="position-absolute"
                style={{ top: '-6px', right: '-8px', fontSize: '0.65rem', minWidth: '16px', padding: '2px 4px' }}
              >
                {unreadCount > 99 ? '99+' : unreadCount}
              </Badge>
            )}
          </Dropdown.Toggle>

          <Dropdown.Menu style={{ minWidth: '320px', maxHeight: '400px', overflowY: 'auto' }}>
            <div className="d-flex align-items-center justify-content-between px-3 py-2 border-bottom">
              <span className="fw-semibold">Notifications</span>
              {unreadCount > 0 && (
                <button
                  className="btn btn-link btn-sm p-0 text-decoration-none"
                  style={{ fontSize: '0.75rem' }}
                  onClick={() => markAllMutation.mutate()}
                  disabled={markAllMutation.isPending}
                >
                  Mark all read
                </button>
              )}
            </div>

            {notifications.length === 0 ? (
              <div className="px-3 py-4 text-center text-muted">
                <small>No notifications</small>
              </div>
            ) : (
              notifications.map((n) => (
                <Dropdown.Item
                  key={n.id}
                  className={`d-flex gap-2 py-2 px-3 ${!n.read ? 'bg-light' : ''}`}
                  onClick={() => handleNotifClick(n)}
                >
                  <div className="flex-shrink-0 pt-1">
                    <FaCircle
                      size={8}
                      color={n.read ? 'transparent' : '#0d6efd'}
                    />
                  </div>
                  <div className="flex-grow-1 overflow-hidden">
                    <div className="fw-medium text-truncate" style={{ fontSize: '0.85rem' }}>
                      {n.title}
                    </div>
                    {n.message && (
                      <div
                        className="text-muted text-truncate"
                        style={{ fontSize: '0.75rem', maxWidth: '260px' }}
                      >
                        {n.message}
                      </div>
                    )}
                    <div className="text-muted" style={{ fontSize: '0.7rem' }}>
                      {timeAgo(n.createdAt)}
                    </div>
                  </div>
                </Dropdown.Item>
              ))
            )}
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
                {user?.displayName || user?.email}
              </div>
              <div className="text-muted" style={{ fontSize: '0.75rem' }}>
                {user?.roles?.[0] || 'User'}
              </div>
            </div>
          </Dropdown.Toggle>

          <Dropdown.Menu>
            <Dropdown.Header>
              <div>{user?.displayName}</div>
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
