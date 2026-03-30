import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
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
  FaTruck,
  FaClipboardCheck,
} from 'react-icons/fa';
import { PageHeader, DataTable, StatusBadge } from '../../components/common';
import { grnApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface GRNFilters {
  search: string;
  status: string;
  dateFrom: string;
  dateTo: string;
}

const GRNListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { hasAnyRole } = useAuth();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<GRNFilters>({
    search: '',
    status: '',
    dateFrom: '',
    dateTo: '',
  });

  const poId = searchParams.get('poId');

  // Fetch GRNs
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['grns', page, pageSize, filters, poId],
    queryFn: () =>
      grnApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        status: filters.status ? Number(filters.status) : undefined,
        poId: poId ? Number(poId) : undefined,
      }),
  });

  // Status options - values must match backend GRNStatus enum numeric values
  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: '0', label: 'Draft' },
    { value: '1', label: 'Pending QC' },
    { value: '2', label: 'QC Approved' },
    { value: '3', label: 'QC Rejected' },
    { value: '4', label: 'Posted/Completed' },
    { value: '5', label: 'Cancelled' },
  ];

  // Status color mapping - supports both numeric and string status values
  const getStatusVariant = (status: string | number): 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' => {
    const statusStr = String(status);
    const variants: Record<string, 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info'> = {
      '0': 'secondary',      // DRAFT
      'DRAFT': 'secondary',
      '1': 'warning',        // PENDING_QC
      'PENDING_QC': 'warning',
      '2': 'info',           // QC_APPROVED
      'QC_APPROVED': 'info',
      '3': 'danger',         // QC_REJECTED
      'QC_REJECTED': 'danger',
      '4': 'success',        // POSTED/COMPLETED
      'COMPLETED': 'success',
      'POSTED': 'success',
      '5': 'danger',         // CANCELLED
      'CANCELLED': 'danger',
    };
    return variants[statusStr] || 'secondary';
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
      key: 'grnNumber',
      label: 'GRN Number',
      render: (row: any) => (
        <div>
          <strong className="text-primary">{row.grnNumber}</strong>
          <div className="small text-muted">
            {formatDate(row.receiptDate)}
          </div>
        </div>
      ),
    },
    {
      key: 'vendorName',
      label: 'Vendor',
      render: (row: any) => (
        <div>
          <div className="fw-medium">{row.vendorName}</div>
        </div>
      ),
    },
    {
      key: 'receivedQuantity',
      label: 'Quantity',
      render: (row: any) => row.receivedQuantity,
    },
    {
      key: 'amount',
      label: 'Value',
      render: (row: any) => `₹${row.amount?.toLocaleString('en-IN') || 0}`,
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: any) => (
        <StatusBadge status={row.status} variant={getStatusVariant(row.status)} />
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
            onClick={() => navigate(`/grn/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
          {row.status === 'DRAFT' && hasAnyRole(['ADMIN', 'GRN_CREATOR']) && (
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={() => navigate(`/grn/${row.id}/edit`)}
              title="Edit"
            >
              <FaEdit />
            </Button>
          )}
          {row.status === 'PENDING_QC' && hasAnyRole(['ADMIN', 'QC_INSPECTOR']) && (
            <Button
              variant="outline-info"
              size="sm"
              onClick={() => navigate(`/grn/${row.id}/qc`)}
              title="QC Inspection"
            >
              <FaClipboardCheck />
            </Button>
          )}
        </div>
      ),
    },
  ];

  // Reset filters
  const resetFilters = () => {
    setFilters({ search: '', status: '', dateFrom: '', dateTo: '' });
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
        title="Goods Receipt Notes"
        subtitle="Manage goods receipt and quality control"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'GRN' },
        ]}
        actions={
          hasAnyRole(['ADMIN', 'GRN_CREATOR']) && (
            <Button variant="primary" onClick={() => navigate('/grn/new')}>
              <FaPlus className="me-2" /> Create GRN
            </Button>
          )
        }
      />

      {/* Search & Filters */}
      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3 align-items-end">
              <Col lg={4} md={6}>
                <InputGroup>
                  <Form.Control
                    placeholder="Search GRN number, vendor..."
                    value={filters.search}
                    onChange={(e) => setFilters({ ...filters, search: e.target.value })}
                  />
                  <Button type="submit" variant="primary">
                    <FaSearch />
                  </Button>
                </InputGroup>
              </Col>
              <Col lg={3} md={4}>
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
                  {(filters.search || filters.status || filters.dateFrom) && (
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
                    <Form.Label className="small">Receipt Date From</Form.Label>
                    <Form.Control
                      type="date"
                      value={filters.dateFrom}
                      onChange={(e) => setFilters({ ...filters, dateFrom: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={3}>
                  <Form.Group>
                    <Form.Label className="small">Receipt Date To</Form.Label>
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
                  <div className="text-muted small text-uppercase">Pending QC</div>
                  <div className="h3 mb-0">{(data as any)?.pendingQcCount || 0}</div>
                </div>
                <FaClipboardCheck size={24} className="text-warning opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-info h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">QC Approved</div>
                  <div className="h3 mb-0">{(data as any)?.qcApprovedCount || 0}</div>
                </div>
                <FaTruck size={24} className="text-info opacity-50" />
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-success h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Completed</div>
                  <div className="h3 mb-0">{(data as any)?.completedCount || 0}</div>
                </div>
                <Badge bg="success" className="fs-6">
                  {(data as any)?.completedCount || 0}
                </Badge>
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-danger h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Rejected</div>
                  <div className="h3 mb-0 text-danger">{(data as any)?.rejectedCount || 0}</div>
                </div>
                <Badge bg="danger" className="fs-6">
                  {(data as any)?.rejectedCount || 0}
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
              emptyMessage="No GRNs found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default GRNListPage;
