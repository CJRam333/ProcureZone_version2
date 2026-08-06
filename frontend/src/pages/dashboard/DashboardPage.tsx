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
 *   1. Welcome-back
 *   2. Quick Actions
 *   3. Latest Activity (the meaningful addition — role-scoped feed of indents + issue notes)
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

  // Quick Actions visibility, derived per-role. DEPTHEAD was previously missing from both (symptom
  // 5). The Quick Actions card renders only if at least one action is available — a role with no
  // actions (e.g. PROCUREMENT, which creates neither indents nor issue notes) sees no card at all.
  const canCreateIndent = hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'SUPERVISOR', 'DEPTHEAD']);
  const canCreateIssueNote = hasAnyRole(['SUPERADMIN', 'ADMIN', 'PLANTMANAGER', 'USER', 'ISSUECONFIRM', 'SUPERVISOR', 'DEPTHEAD']);
  const hasAnyQuickAction = canCreateIndent || canCreateIssueNote;

  // Identity-aware key (+ staleTime:0 + refetchOnMount:'always') so one viewer's role-scoped activity
  // is never served to another identity under the same key, and every visit revalidates instead of
  // trusting the global 5-min stale cache (root cause of the "Latest Activity not updating" latency).
  // refetchInterval keeps an already-open dashboard current without navigating away — safe here because
  // this is a read-only display with no editable form bound to the data (Rule 11). Matches the
  // Indent/Issue-Note list-page contamination fix.
  const { data: activity, isLoading } = useQuery({
    queryKey: ['dashboard', 'latest-activity', user?.employeeNumber, user?.roles],
    queryFn: () => dashboardApi.getLatestActivity(15),
    enabled: !!user,
    staleTime: 0,
    refetchOnMount: 'always',
    refetchInterval: 30000,
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

      {/* 1 — Welcome back */}
      <Card className="mb-4 border-0 shadow-sm">
        <Card.Body>
          <h5 className="mb-1">
            Welcome back, {user?.displayName || user?.employeeName || user?.username || 'User'}!
          </h5>
        </Card.Body>
      </Card>

      {/* 2 — Quick Actions (only rendered when the role has at least one action) */}
      {hasAnyQuickAction && (
        <Card className="mb-4 border-0 shadow-sm">
          <Card.Header className="bg-transparent border-0">
            <h5 className="mb-0">Quick Actions</h5>
          </Card.Header>
          <Card.Body>
            <div className="d-flex flex-wrap gap-2">
              {canCreateIndent && (
                <Link to="/indents/new" className="btn btn-primary">
                  <FaFileAlt className="me-2" /> Create Indent
                </Link>
              )}
              {canCreateIssueNote && (
                <Link to="/issue-notes/new" className="btn btn-secondary">
                  <FaClipboardList className="me-2" /> Create Issue Note
                </Link>
              )}
            </div>
          </Card.Body>
        </Card>
      )}

      {/* 3 — Latest Activity */}
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
    </div>
  );
};

export default DashboardPage;
