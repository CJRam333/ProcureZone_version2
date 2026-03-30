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
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaPlus,
  FaSearch,
  FaEye,
  FaEdit,
  FaSyncAlt,
  FaToggleOn,
  FaToggleOff,
  FaFileImport,
  FaFileExport,
} from 'react-icons/fa';
import { PageHeader, DataTable, StatusBadge } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface MaterialFilters {
  search: string;
  status: string;
  categoryId: string;
}

const MaterialsListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [filters, setFilters] = useState<MaterialFilters>({
    search: '',
    status: '',
    categoryId: '',
  });

  // Fetch materials
  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['materials', page, pageSize, filters],
    queryFn: () =>
      materialsApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        isActive: filters.status === 'active' ? true : filters.status === 'inactive' ? false : undefined,
        categoryId: filters.categoryId ? Number(filters.categoryId) : undefined,
      }),
  });

  // Toggle active mutation
  const toggleActiveMutation = useMutation({
    mutationFn: ({ id, isActive }: { id: number; isActive: boolean }) =>
      isActive ? materialsApi.deactivate(id) : materialsApi.activate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['materials'] });
    },
  });

  // Status options
  const statusOptions = [
    { value: '', label: 'All Materials' },
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
  ];

  // Table columns
  const columns = [
    {
      key: 'materialCode',
      label: 'Code',
      render: (row: any) => (
        <code className="fw-bold text-primary">{row.materialCode}</code>
      ),
    },
    {
      key: 'description',
      label: 'Description',
      render: (row: any) => (
        <div>
          <div className="fw-medium">{row.description}</div>
          {row.specification && (
            <small className="text-muted">{row.specification}</small>
          )}
        </div>
      ),
    },
    {
      key: 'category',
      label: 'Category',
      render: (row: any) => (
        <Badge bg="info">{row.categoryName || '-'}</Badge>
      ),
    },
    {
      key: 'uom',
      label: 'UOM',
      render: (row: any) => (
        <Badge bg="secondary">{row.uomCode}</Badge>
      ),
    },
    {
      key: 'hsnCode',
      label: 'HSN Code',
      render: (row: any) => row.hsnCode || '-',
    },
    {
      key: 'reorderLevel',
      label: 'Reorder Level',
      render: (row: any) => row.reorderLevel || '-',
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
            onClick={() => navigate(`/materials/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
          {hasAnyRole(['ADMIN', 'MASTER_DATA_ADMIN']) && (
            <>
              <Button
                variant="outline-secondary"
                size="sm"
                onClick={() => navigate(`/materials/${row.id}/edit`)}
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
    setFilters({ search: '', status: '', categoryId: '' });
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
        title="Materials"
        subtitle="Manage material master data"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Materials' },
        ]}
        actions={
          <div className="d-flex gap-2">
            {hasAnyRole(['ADMIN', 'MASTER_DATA_ADMIN']) && (
              <>
                <Button 
                  variant="outline-secondary" 
                  onClick={() => {
                    // Export materials to CSV
                    materialsApi.list({ page: 0, size: 1000 }).then((data) => {
                      const csv = [
                        ['Code', 'Description', 'HSN Code', 'UOM', 'Status', 'Created At'],
                        ...(data.content || []).map((m: any) => [
                          m.code || '',
                          m.description || '',
                          m.hsnCode || '',
                          m.uomCode || '',
                          m.status === 1 ? 'Active' : 'Inactive',
                          m.createdAt || ''
                        ])
                      ].map(row => row.map(cell => `"${cell}"`).join(',')).join('\n');
                      
                      const blob = new Blob([csv], { type: 'text/csv' });
                      const url = URL.createObjectURL(blob);
                      const a = document.createElement('a');
                      a.href = url;
                      a.download = 'materials_export.csv';
                      a.click();
                      URL.revokeObjectURL(url);
                    });
                  }}
                >
                  <FaFileExport className="me-2" /> Export
                </Button>
                <Button 
                  variant="outline-secondary" 
                  onClick={() => {
                    alert('To import materials in bulk:\n\n1. Prepare a CSV file with columns:\n   - code, description, hsnCode, uomId, status\n\n2. Use the backend API endpoint:\n   POST /api/v1/materials/bulk-import\n\n(Full UI import will be added in next release)');
                  }}
                  title="Bulk import materials from CSV"
                >
                  <FaFileImport className="me-2" /> Import
                </Button>
                <Button variant="primary" onClick={() => navigate('/materials/new')}>
                  <FaPlus className="me-2" /> Add Material
                </Button>
              </>
            )}
          </div>
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
                    placeholder="Search material code, description, HSN..."
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
              <div className="text-muted small text-uppercase">Total Materials</div>
              <div className="h3 mb-0">{data?.totalElements || 0}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-success h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Active Materials</div>
              <div className="h3 mb-0 text-success">{(data as any)?.activeCount || 0}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-warning h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Categories</div>
              <div className="h3 mb-0">{(data as any)?.categoryCount || 0}</div>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-danger h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Low Stock</div>
              <div className="h3 mb-0 text-danger">{(data as any)?.lowStockCount || 0}</div>
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
              emptyMessage="No materials found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default MaterialsListPage;
