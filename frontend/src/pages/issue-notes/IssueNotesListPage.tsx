import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  Row,
  Col,
  Form,
  Button,
  Badge,
  InputGroup,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  FaPlus,
  FaSearch,
  FaEye,
  FaEdit,
  FaSyncAlt,
  FaFileExport,
} from 'react-icons/fa';
import { PageHeader, DataTable } from '../../components/common';
import { issueNotesApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';
import { INDENT_STATUS_COLORS } from '../../constants/indentStatus';

interface IssueNoteFilters {
  search: string;
  statusKey: string;   // composite key: '', '1', '2', '3_1', '3_11', '3_2'
  dateFrom: string;
  dateTo: string;
}

// Decode composite status key into API params
function decodeStatusFilter(key: string): { approvedStatus?: number; storesByStatus?: number } {
  if (key === '1')    return { approvedStatus: 1 };
  if (key === '2')    return { approvedStatus: 2 };
  if (key === '3_1')  return { approvedStatus: 3, storesByStatus: 1 };
  if (key === '3_11') return { approvedStatus: 3, storesByStatus: 11 };
  if (key === '3_2')  return { approvedStatus: 3, storesByStatus: 2 };
  return {};
}

const IssueNotesListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();

  const pageTitle = hasAnyRole(['ADMIN', 'SUPERADMIN', 'PROCUREMENT', 'ISSUECONFIRM'])
    ? 'All Issue Notes'
    : hasAnyRole(['DEPTHEAD', 'PLANTMANAGER'])
    ? 'Department Issue Notes'
    : hasAnyRole(['SUPERVISOR'])
    ? 'My Team Issue Notes'
    : 'My Issue Notes';

  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [filters, setFilters] = useState<IssueNoteFilters>({
    search: '',
    statusKey: '',
    dateFrom: '',
    dateTo: '',
  });

  const { approvedStatus, storesByStatus } = decodeStatusFilter(filters.statusKey);

  // Fetch issue notes
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['issue-notes', page, pageSize, filters],
    queryFn: () =>
      issueNotesApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        approvedStatus,
        storesByStatus,
      }),
  });

  // Safe date formatter
  const formatDate = (dateStr: string | null | undefined): string => {
    if (!dateStr) return 'N/A';
    try {
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return 'N/A';
      return format(date, 'dd MMM yyyy');
    } catch {
      return 'N/A';
    }
  };

  // "Created By" is only meaningful to roles that see other people's issue notes —
  // a plain USER only ever sees their own, so the column is redundant for them.
  const showCreatorColumn = hasAnyRole(['SUPERVISOR', 'DEPTHEAD', 'PROCUREMENT', 'ISSUECONFIRM', 'ADMIN', 'SUPERADMIN']);

  // Table columns
  const columns = [
    {
      key: 'issueNoteNumber',
      label: 'Issue Note #',
      render: (row: any) => (
        <div>
          <strong className="text-primary">{row.issueNoteNumber}</strong>
          <div className="small text-muted">
            {formatDate(row.issueDate)}
          </div>
        </div>
      ),
    },
    ...(showCreatorColumn ? [{
      key: 'employeeName',
      label: 'Created By',
      render: (row: any) => <span>{row.employeeName || '—'}</span>,
    }] : []),
    {
      key: 'statusDescription',
      label: 'Status',
      render: (row: any) => (
        <Badge bg={INDENT_STATUS_COLORS[row.statusDescription || ''] || 'secondary'}>{row.statusDescription || 'Unknown'}</Badge>
      ),
    },
    {
      key: 'lineItemCount',
      label: 'Items',
      render: (row: any) => (
        <Badge bg="secondary">{row.lineItemCount || 0} items</Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: any) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/issue-notes/${row.id}`);
            }}
            title="View Details"
          >
            <FaEye />
          </Button>
          {/* Edit only for true unsubmitted drafts: Spring status=1 AND no two-column
              workflow progress (legacy rows carry status=1 as an active flag) */}
          {row.status === 1 && row.approvedStatus === 1 && row.storesByStatus === 1
            && hasAnyRole(['ADMIN', 'ISSUECONFIRM']) && (
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                navigate(`/issue-notes/${row.id}/edit`);
              }}
              title="Edit"
            >
              <FaEdit />
            </Button>
          )}
        </div>
      ),
    },
  ];

  // Reset filters
  const resetFilters = () => {
    setFilters({ search: '', statusKey: '', dateFrom: '', dateTo: '' });
    setPage(0);
  };

  // Handle search
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
  };

  // Export mirrors the Indent list pattern (same blob-download mechanism); the file reflects the
  // current filter state and the caller's role-scoped visibility (backend routes through getAll).
  const handleExport = async (fmt: 'xlsx' | 'csv') => {
    try {
      const blob = await issueNotesApi.export({
        format: fmt,
        search: filters.search || undefined,
        approvedStatus,
        storesByStatus,
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fmt === 'csv' ? 'issue_notes_export.csv' : 'issue_notes_export.xlsx';
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      alert('Export failed. You may not have permission.');
    }
  };

  return (
    <div>
      <PageHeader
        title={pageTitle}
        subtitle="Manage material issues and returns"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Issue Notes' },
        ]}
        actions={
          <div className="d-flex gap-2">
            {/* Export buttons match the Indent list exactly: same placement (header toolbar,
                top-right), same variants/icon/size/spacing. Visible to every role that can view
                the list; the backend export is role-scoped so each file contains only what the
                caller can see. */}
            <Button variant="outline-success" size="sm" onClick={() => handleExport('xlsx')}>
              <FaFileExport className="me-1" /> Excel
            </Button>
            <Button variant="outline-secondary" size="sm" onClick={() => handleExport('csv')}>
              <FaFileExport className="me-1" /> CSV
            </Button>
            {hasAnyRole(['USER', 'SUPERVISOR', 'DEPTHEAD', 'ADMIN', 'SUPERADMIN']) && (
              <Button variant="primary" onClick={() => navigate('/issue-notes/new')}>
                <FaPlus className="me-2" /> Create Issue Note
              </Button>
            )}
          </div>
        }
      />

      {/* Search & Filters */}
      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3 align-items-end">
              <Col lg={3} md={6}>
                <InputGroup>
                  <Form.Control
                    placeholder="Search issue note #..."
                    value={filters.search}
                    onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                  />
                  <Button type="submit" variant="primary">
                    <FaSearch />
                  </Button>
                </InputGroup>
              </Col>
              <Col lg={2} md={3}>
                <Form.Select
                  value={filters.statusKey}
                  onChange={(e) => {
                    setFilters({ ...filters, statusKey: e.target.value });
                    setPage(0);
                  }}
                >
                  <option value="">All Statuses</option>
                  <option value="1">Pending RM Approval</option>
                  <option value="2">RM Rejected</option>
                  <option value="3_1">RM Approved</option>
                  <option value="3_11">Goods Issued</option>
                  <option value="3_2">Stores Rejected</option>
                </Form.Select>
              </Col>
              <Col lg="auto">
                <div className="d-flex gap-2">
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt />
                  </Button>
                  {(filters.search || filters.statusKey) && (
                    <Button variant="outline-danger" onClick={resetFilters}>
                      Clear
                    </Button>
                  )}
                </div>
              </Col>
            </Row>

          </Form>
        </Card.Body>
      </Card>

      {/* Data Table */}
      <Card>
        <Card.Body className="p-0">
          {error ? (
            <div className="p-4 text-center text-danger">{getErrorMessage(error)}</div>
          ) : (
            <DataTable
              columns={columns}
              data={data?.content || []}
              keyField="id"
              loading={isLoading}
              totalItems={data?.totalElements || 0}
              currentPage={page}
              pageSize={pageSize}
              onPageChange={setPage}
              onRowClick={(row: any) => navigate(`/issue-notes/${row.id}`)}
              emptyMessage="No issue notes found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default IssueNotesListPage;
