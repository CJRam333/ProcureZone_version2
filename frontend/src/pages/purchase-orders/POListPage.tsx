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
  Dropdown,
  DropdownButton,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  FaPlus,
  FaSearch,
  FaFilter,
  FaEye,
  FaEdit,
  FaFileDownload,
  FaSyncAlt,
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, StatusBadge } from '../../components/common';
import { purchaseOrdersApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface POFilters {
  search: string;
  status: string;
  vendorId: string;
  dateFrom: string;
  dateTo: string;
}

const POListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [filters, setFilters] = useState<POFilters>({
    search: '',
    status: '',
    vendorId: '',
    dateFrom: '',
    dateTo: '',
  });
  const [showFilters, setShowFilters] = useState(false);

  // Fetch POs
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['purchase-orders', page, pageSize, filters],
    queryFn: () =>
      purchaseOrdersApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        status: filters.status ? Number(filters.status) : undefined,
        vendorId: filters.vendorId ? Number(filters.vendorId) : undefined,
      }),
  });

  // Status options — values match backend POStatus integer codes (1-indexed)
  const statusOptions = [
    { value: '', label: 'All Statuses' },
    { value: '1', label: 'Draft' },
    { value: '2', label: 'Submitted' },
    { value: '3', label: 'Approved' },
    { value: '4', label: 'Sent to Vendor' },
    { value: '5', label: 'Partially Received' },
    { value: '6', label: 'Fully Received' },
    { value: '7', label: 'Cancelled' },
    { value: '8', label: 'Closed' },
  ];

  // Status color mapping
  const getStatusVariant = (status: string): 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' => {
    const variants: Record<string, 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info'> = {
      DRAFT: 'secondary',
      PENDING_APPROVAL: 'warning',
      APPROVED: 'primary',
      SENT_TO_VENDOR: 'info',
      ACKNOWLEDGED: 'info',
      PARTIALLY_RECEIVED: 'warning',
      FULLY_RECEIVED: 'success',
      CLOSED: 'success',
      CANCELLED: 'danger',
    };
    return variants[status] || 'secondary';
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
      key: 'poNumber',
      label: 'PO Number',
      render: (row: any) => (
        <div>
          <strong className="text-primary">{row.poNumber}</strong>
          <div className="small text-muted">
            {formatDate(row.poDate)}
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
          <small className="text-muted">{row.vendorCode}</small>
        </div>
      ),
    },
    {
      key: 'indentNumber',
      label: 'Indent Ref',
      render: (row: any) => row.indentNumber || '-',
    },
    {
      key: 'netAmount',
      label: 'Amount',
      render: (row: any) => (
        <strong>
          {new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(
            row.netAmount || 0
          )}
        </strong>
      ),
    },
    {
      key: 'expectedDeliveryDate',
      label: 'Expected Delivery',
      render: (row: any) => formatDate(row.expectedDeliveryDate),
    },
    {
      key: 'poStatus',
      label: 'Status',
      render: (row: any) => (
        <StatusBadge
          status={row.poStatusName ?? String(row.poStatus)}
          variant={getStatusVariant(row.poStatusName ?? '')}
        />
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
            onClick={() => navigate(`/purchase-orders/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
          {row.poStatus === 1 && hasAnyRole(['ADMIN', 'PO_CREATOR']) && (
            <Button
              variant="outline-secondary"
              size="sm"
              onClick={() => navigate(`/purchase-orders/${row.id}/edit`)}
              title="Edit"
            >
              <FaEdit />
            </Button>
          )}
          <DropdownButton
            as="span"
            variant="outline-secondary"
            size="sm"
            title=""
            id={`po-actions-${row.id}`}
          >
            <Dropdown.Item onClick={() => navigate(`/purchase-orders/${row.id}`)}>
              View Details
            </Dropdown.Item>
            {row.poStatus === 3 && (
              <Dropdown.Item onClick={() => alert('Download PDF')}>
                <FaFileDownload className="me-2" /> Download PDF
              </Dropdown.Item>
            )}
          </DropdownButton>
        </div>
      ),
    },
  ];

  // Reset filters
  const resetFilters = () => {
    setFilters({
      search: '',
      status: '',
      vendorId: '',
      dateFrom: '',
      dateTo: '',
    });
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
        title="Purchase Orders"
        subtitle="Manage purchase orders for approved indents"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Purchase Orders' },
        ]}
        actions={
          hasAnyRole(['ADMIN', 'PO_CREATOR']) && (
            <Button variant="primary" onClick={() => navigate('/purchase-orders/new')}>
              <FaPlus className="me-2" /> Create PO
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
                    placeholder="Search PO number, vendor..."
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
                  {(filters.search || filters.status || filters.vendorId) && (
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
                    <Form.Label className="small">Date From</Form.Label>
                    <Form.Control
                      type="date"
                      value={filters.dateFrom}
                      onChange={(e) => setFilters({ ...filters, dateFrom: e.target.value })}
                    />
                  </Form.Group>
                </Col>
                <Col md={3}>
                  <Form.Group>
                    <Form.Label className="small">Date To</Form.Label>
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
          <Card className="border-start border-4 border-primary h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Total POs</div>
                  <div className="h3 mb-0">{data?.totalItems || 0}</div>
                </div>
                <Badge bg="primary" className="fs-6">
                  {data?.totalItems || 0}
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
                  <div className="text-muted small text-uppercase">Current Page</div>
                  <div className="h3 mb-0">{data?.content?.length || 0}</div>
                </div>
                <Badge bg="info" className="fs-6">
                  {page + 1}
                </Badge>
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-warning h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Page Size</div>
                  <div className="h3 mb-0">{pageSize}</div>
                </div>
                <Badge bg="warning" className="fs-6">
                  {pageSize}
                </Badge>
              </div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-success h-100">
            <Card.Body>
              <div className="d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small text-uppercase">Total Pages</div>
                  <div className="h3 mb-0">{data?.totalPages || 0}</div>
                </div>
                <Badge bg="success" className="fs-6">
                  {data?.totalPages || 0}
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
              totalItems={data?.totalItems || 0}
              currentPage={page}
              pageSize={pageSize}
              onPageChange={setPage}
              emptyMessage="No purchase orders found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default POListPage;
