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
  FaFilter,
  FaUndo,
  FaBoxOpen,
} from 'react-icons/fa';
import { PageHeader, DataTable, StatusBadge } from '../../components/common';
import { issueNotesApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface IssueNoteFilters {
  search: string;
  status: string;
  type: string;
  dateFrom: string;
  dateTo: string;
}

const IssueNotesListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<IssueNoteFilters>({
    search: '',
    status: '',
    type: '',
    dateFrom: '',
    dateTo: '',
  });

  // Fetch issue notes
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['issue-notes', page, pageSize, filters],
    queryFn: () =>
      issueNotesApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        status: filters.status ? (filters.status as any) : undefined,
      }),
  });

  // Status options
  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: 'DRAFT', label: 'Draft' },
    { value: 'PENDING_APPROVAL', label: 'Pending Approval' },
    { value: 'APPROVED', label: 'Approved' },
    { value: 'ISSUED', label: 'Issued' },
    { value: 'PARTIALLY_RETURNED', label: 'Partially Returned' },
    { value: 'FULLY_RETURNED', label: 'Fully Returned' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ];

  // Type options
  const typeOptions = [
    { value: '', label: 'All Types' },
    { value: 'PRODUCTION', label: 'Production' },
    { value: 'MAINTENANCE', label: 'Maintenance' },
    { value: 'SAMPLE', label: 'Sample' },
    { value: 'RETURN', label: 'Return' },
    { value: 'TRANSFER', label: 'Transfer' },
  ];

  // Status color mapping
  const getStatusVariant = (status: string): 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' => {
    const variants: Record<string, 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info'> = {
      DRAFT: 'secondary',
      PENDING_APPROVAL: 'warning',
      APPROVED: 'primary',
      ISSUED: 'info',
      PARTIALLY_RETURNED: 'warning',
      FULLY_RETURNED: 'success',
      CANCELLED: 'danger',
    };
    return variants[status] || 'secondary';
  };

  // Type color mapping
  const getTypeVariant = (type: string): string => {
    const variants: Record<string, string> = {
      PRODUCTION: 'primary',
      MAINTENANCE: 'info',
      SAMPLE: 'warning',
      RETURN: 'success',
      TRANSFER: 'secondary',
    };
    return variants[type] || 'secondary';
  };

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
    {
      key: 'issuedTo',
      label: 'Issued To',
      render: (row: any) => (
        <div>
          <div className="fw-medium">{row.issuedTo || 'N/A'}</div>
        </div>
      ),
    },
    {
      key: 'statusDescription',
      label: 'Status',
      render: (row: any) => (
        <Badge bg={getStatusVariant(row.statusDescription || '')}>{row.statusDescription || 'Unknown'}</Badge>
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
      key: 'totalAmount',
      label: 'Total Amount',
      render: (row: any) => (
        <span className="fw-medium">₹{(row.totalAmount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
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
            onClick={() => navigate(`/issue-notes/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
          {row.status === 'DRAFT' && hasAnyRole(['ADMIN', 'STOREKEEPER']) && (
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={() => navigate(`/issue-notes/${row.id}/edit`)}
              title="Edit"
            >
              <FaEdit />
            </Button>
          )}
          {row.status === 'ISSUED' && row.type !== 'RETURN' && hasAnyRole(['ADMIN', 'STOREKEEPER']) && (
            <Button
              variant="outline-success"
              size="sm"
              onClick={() => navigate(`/issue-notes/${row.id}/return`)}
              title="Process Return"
            >
              <FaUndo />
            </Button>
          )}
        </div>
      ),
    },
  ];

  // Reset filters
  const resetFilters = () => {
    setFilters({ search: '', status: '', type: '', dateFrom: '', dateTo: '' });
    setPage(0);
  };

  // Handle search
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
  };

  return (
    <div>
      <PageHeader
        title="Issue Notes"
        subtitle="Manage material issues and returns"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Issue Notes' },
        ]}
        actions={
          hasAnyRole(['ADMIN', 'STOREKEEPER']) && (
            <Button variant="primary" onClick={() => navigate('/issue-notes/new')}>
              <FaPlus className="me-2" /> Create Issue Note
            </Button>
          )
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
                  value={filters.status}
                  onChange={(e) => {
                    setFilters({ ...filters, status: e.target.value });
                    setPage(0);
                  }}
                >
                  {statusOptions.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </Form.Select>
              </Col>
              <Col lg={2} md={3}>
                <Form.Select
                  value={filters.type}
                  onChange={(e) => {
                    setFilters({ ...filters, type: e.target.value });
                    setPage(0);
                  }}
                >
                  {typeOptions.map((opt) => (
                    <option key={opt.value} value={opt.value}>
                      {opt.label}
                    </option>
                  ))}
                </Form.Select>
              </Col>
              <Col lg="auto">
                <div className="d-flex gap-2">
                  <Button
                    variant="outline-secondary"
                    onClick={() => setShowFilters(!showFilters)}
                  >
                    <FaFilter className="me-2" /> Filters
                  </Button>
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt />
                  </Button>
                  {(filters.search || filters.status || filters.type) && (
                    <Button variant="outline-danger" onClick={resetFilters}>
                      Clear
                    </Button>
                  )}
                </div>
              </Col>
            </Row>

            {/* Extended Filters */}
            {showFilters && (
              <Row className="g-3 mt-3 pt-3 border-top">
                <Col md={3}>
                  <Form.Group>
                    <Form.Label className="small">Issue Date From</Form.Label>
                    <Form.Control
                      type="date"
                      value={filters.dateFrom}
                      onChange={(e) => setFilters({ ...filters, dateFrom: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={3}>
                  <Form.Group>
                    <Form.Label className="small">Issue Date To</Form.Label>
                    <Form.Control
                      type="date"
                      value={filters.dateTo}
                      onChange={(e) => setFilters({ ...filters, dateTo: e.target.value })}
                    />
                  </Form.Group>
                </Col>
              </Row>
            )}
          </Form>
        </Card.Body>
      </Card>

      {/* Stats Cards */}
      <Row className="g-3 mb-4">
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-warning h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Pending Approval</div>
                  <div className="h3 mb-0">{(data as any)?.pendingCount || 0}</div>
                </div>
                <Badge bg="warning" className="fs-6">
                  {(data as any)?.pendingCount || 0}
                </Badge>
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-info h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Issued Today</div>
                  <div className="h3 mb-0">{(data as any)?.issuedTodayCount || 0}</div>
                </div>
                <FaBoxOpen size={24} className="text-info opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-success h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Returned</div>
                  <div className="h3 mb-0">{(data as any)?.returnedCount || 0}</div>
                </div>
                <FaUndo size={24} className="text-success opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-primary h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">This Month</div>
                  <div className="h3 mb-0">{(data as any)?.thisMonthCount || 0}</div>
                </div>
                <Badge bg="primary" className="fs-6">
                  {(data as any)?.thisMonthCount || 0}
                </Badge>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

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
              emptyMessage="No issue notes found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default IssueNotesListPage;
