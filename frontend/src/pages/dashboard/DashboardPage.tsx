import React from 'react';
import { Card, Table, Badge } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  FaFileAlt,
  FaClipboardList,
  FaHistory,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { useAuth } from '../../contexts/AuthContext';
import { dashboardApi } from '../../api';
import type { LatestActivityItem } from '../../api/dashboard';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

/**
 * Dashboard — deliberately minimal (Pass 2 rebuild). Exactly three cards:
 *   1. Quick Actions (top)
 *   2. Latest Activity (the meaningful addition — role-scoped feed of indents + issue notes)
 *   3. Welcome-back (bottom)
 * All former stat tiles, charts, low-stock and recent-GRN widgets were removed.
 */
const DashboardPage: React.FC = () => {
  const { user, hasAnyRole } = useAuth();
  const navigate = useNavigate();

  // Creator column only matters to roles that can see other people's documents;
  // a plain USER only ever sees their own, so it would be redundant for them.
  const showCreatorColumn = hasAnyRole([
    'SUPERVISOR', 'DEPTHEAD', 'PLANTMANAGER', 'PROCUREMENT', 'ISSUECONFIRM', 'ADMIN', 'SUPERADMIN',
  ]);

  const { data: activity, isLoading } = useQuery({
    queryKey: ['dashboard', 'latest-activity'],
    queryFn: () => dashboardApi.getLatestActivity(15),
    enabled: !!user,
  });

  const formatDate = (dateStr: string | null | undefined): string => {
    if (!dateStr) return '—';
    try {
      const d = new Date(dateStr);
      if (isNaN(d.getTime())) return '—';
      return format(d, 'dd MMM yyyy, HH:mm');
    } catch {
      return '—';
    }
  };

  const detailPath = (item: LatestActivityItem) =>
    item.type === 'INDENT' ? `/indents/${item.id}` : `/issue-notes/${item.id}`;

  return (
    <div>
      <PageHeader title="Dashboard" />

      {/* 1 — Quick Actions (top) */}
      <Card className="mb-4 border-0 shadow-sm">
        <Card.Header className="bg-transparent border-0">
          <h5 className="mb-0">Quick Actions</h5>
        </Card.Header>
        <Card.Body>
          <div className="d-flex flex-wrap gap-2">
            {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'SUPERVISOR']) && (
              <Link to="/indents/new" className="btn btn-primary">
                <FaFileAlt className="me-2" /> Create Indent
              </Link>
            )}
            {hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'ISSUECONFIRM', 'SUPERVISOR']) && (
              <Link to="/issue-notes/new" className="btn btn-secondary">
                <FaClipboardList className="me-2" /> Create Issue Note
              </Link>
            )}
          </div>
        </Card.Body>
      </Card>

      {/* 2 — Latest Activity */}
      <Card className="mb-4 border-0 shadow-sm">
        <Card.Header className="bg-transparent border-0">
          <h5 className="mb-0 d-flex align-items-center">
            <FaHistory className="me-2 text-primary" /> Latest Activity
          </h5>
          <small className="text-muted">
            Your recent indents and issue notes. Completed items drop off after 3 days.
          </small>
        </Card.Header>
        <Card.Body className="p-0">
          {isLoading ? (
            <LoadingSpinner />
          ) : !activity || activity.length === 0 ? (
            <div className="p-4 text-center text-muted">No recent activity.</div>
          ) : (
            <div className="table-responsive">
              <Table className="mb-0" hover>
                <thead className="table-light">
                  <tr>
                    <th>Type</th>
                    <th>Number</th>
                    <th>Status</th>
                    <th>Last Updated</th>
                    {showCreatorColumn && <th>Created By</th>}
                  </tr>
                </thead>
                <tbody>
                  {activity.map((item) => (
                    <tr
                      key={`${item.type}-${item.id}`}
                      style={{ cursor: 'pointer' }}
                      onClick={() => navigate(detailPath(item))}
                    >
                      <td>
                        <Badge bg={item.type === 'INDENT' ? 'info' : 'secondary'}>
                          {item.type === 'INDENT' ? 'Indent' : 'Issue Note'}
                        </Badge>
                      </td>
                      <td>
                        <span className="fw-medium text-primary">{item.documentNumber}</span>
                      </td>
                      <td>
                        <Badge bg={INDENT_STATUS_COLORS[item.displayStatus || ''] || 'secondary'}>
                          {item.displayStatus || 'Unknown'}
                        </Badge>
                      </td>
                      <td>{formatDate(item.lastModifiedDate)}</td>
                      {showCreatorColumn && <td>{item.creatorName || '—'}</td>}
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          )}
        </Card.Body>
      </Card>

      {/* 3 — Welcome back (bottom) */}
      <Card className="border-0 shadow-sm">
        <Card.Body>
          <h5 className="mb-1">
            Welcome back, {user?.displayName || user?.employeeName || user?.username || 'User'}!
          </h5>
          <div className="text-muted small">Here's an overview of your procurement activities.</div>
        </Card.Body>
      </Card>
    </div>
  );
};

export default DashboardPage;
