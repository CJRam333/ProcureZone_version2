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
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaPlus,
  FaSearch,
  FaEye,
  FaEdit,
  FaSyncAlt,
  FaToggleOn,
  FaToggleOff,
  FaBuilding,
  FaEnvelope,
  FaPhone,
} from 'react-icons/fa';
import { PageHeader, DataTable, StatusBadge } from '../../components/common';
import { vendorsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface VendorFilters {
  search: string;
  status: string;
  category: string;
}

const VendorsListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [filters, setFilters] = useState<VendorFilters>({
    search: '',
    status: '',
    category: '',
  });

  // Fetch vendors
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['vendors', page, pageSize, filters],
    queryFn: () =>
      vendorsApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        isActive: filters.status === 'active' ? true : filters.status === 'inactive' ? false : undefined,
      }),
  });

  // Toggle active mutation
  const toggleActiveMutation = useMutation({
    mutationFn: ({ id, isActive }: { id: number; isActive: boolean }) =>
      isActive ? vendorsApi.deactivate(id) : vendorsApi.activate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vendors'] });
    },
  });

  // Status options
  const statusOptions = [
    { value: '', label: 'All Vendors' },
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
  ];

  // Table columns
  const columns = [
    {
      key: 'vendorCode',
      label: 'Vendor Code',
      render: (row: any) => (
        <div>
          <code className="fw-bold">{row.vendorCode}</code>
        </div>
      ),
    },
    {
      key: 'vendorName',
      label: 'Vendor Name',
      render: (row: any) => (
        <div>
          <div className="fw-medium d-flex align-items-center gap-2">
            <FaBuilding className="text-muted" />
            {row.vendorName}
          </div>
          {row.gstin && (
            <small className="text-muted">GSTIN: {row.gstin}</small>
          )}
        </div>
      ),
    },
    {
      key: 'contact',
      label: 'Contact',
      render: (row: any) => (
        <div>
          {row.email && (
            <div className="small">
              <FaEnvelope className="me-2 text-muted" />
              {row.email}
            </div>
          )}
          {row.phone && (
            <div className="small">
              <FaPhone className="me-2 text-muted" />
              {row.phone}
            </div>
          )}
        </div>
      ),
    },
    {
      key: 'city',
      label: 'Location',
      render: (row: any) => (
        <div>
          <div>{row.city || '-'}</div>
          <small className="text-muted">{row.state}</small>
        </div>
      ),
    },
    {
      key: 'category',
      label: 'Category',
      render: (row: any) => (
        <Badge bg="info">{row.category || 'General'}</Badge>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: any) => (
        <StatusBadge
          status={row.isActive ? 'Active' : 'Inactive'}
          variant={row.isActive ? 'success' : 'secondary'}
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
            onClick={() => navigate(`/vendors/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
          {hasAnyRole(['ADMIN', 'VENDOR_MANAGER']) && (
            <>
              <Button
                variant="outline-secondary"
                size="sm"
                onClick={() => navigate(`/vendors/${row.id}/edit`)}
                title="Edit"
              >
                <FaEdit />
              </Button>
              <Button
                variant={row.isActive ? 'outline-warning' : 'outline-success'}
                size="sm"
                onClick={() => toggleActiveMutation.mutate({ id: row.id, isActive: row.isActive })}
                title={row.isActive ? 'Deactivate' : 'Activate'}
                disabled={toggleActiveMutation.isPending}
              >
                {row.isActive ? <FaToggleOff /> : <FaToggleOn />}
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  // Reset filters
  const resetFilters = () => {
    setFilters({ search: '', status: '', category: '' });
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
        title="Vendors"
        subtitle="Manage supplier and vendor information"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Vendors' },
        ]}
        actions={
          hasAnyRole(['ADMIN', 'VENDOR_MANAGER']) && (
            <Button variant="primary" onClick={() => navigate('/vendors/new')}>
              <FaPlus className="me-2" /> Add Vendor
            </Button>
          )
        }
      />

      {/* Search & Filters */}
      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3 align-items-end">
              <Col lg={5} md={6}>
                <InputGroup>
                  <Form.Control
                    placeholder="Search vendor name, code, GSTIN..."
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
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt />
                  </Button>
                  {(filters.search || filters.status) && (
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

      {/* Stats Cards */}
      <Row className="g-3 mb-4">
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-primary h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Total Vendors</div>
              <div className="h3 mb-0">{data?.totalElements || 0}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-success h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Active Vendors</div>
              <div className="h3 mb-0 text-success">{(data as any)?.activeCount || 0}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-warning h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">With GST</div>
              <div className="h3 mb-0">{(data as any)?.withGstCount || 0}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-info h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">New This Month</div>
              <div className="h3 mb-0 text-info">{(data as any)?.newThisMonth || 0}</div>
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
              emptyMessage="No vendors found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default VendorsListPage;
