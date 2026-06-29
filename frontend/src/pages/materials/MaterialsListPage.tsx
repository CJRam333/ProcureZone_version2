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
  FaFileExport,
} from 'react-icons/fa';
import { PageHeader, DataTable, StatusBadge } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';
import { useAuth } from '../../contexts/AuthContext';

interface MaterialFilters {
  search: string;
  status: string;
}

const MaterialsListPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuth();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [filters, setFilters] = useState<MaterialFilters>({ search: '', status: '' });

  const { data, isLoading, refetch, error } = useQuery({
    queryKey: ['materials', page, pageSize, filters],
    queryFn: () =>
      materialsApi.list({
        page,
        size: pageSize,
        search: filters.search || undefined,
        isActive: filters.status === 'active' ? true : filters.status === 'inactive' ? false : undefined,
      }),
  });

  const toggleActiveMutation = useMutation({
    mutationFn: ({ id, isActive }: { id: number; isActive: boolean }) =>
      isActive ? materialsApi.deactivate(id) : materialsApi.activate(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['materials'] }),
  });

  const columns = [
    {
      key: 'materialCode',
      label: 'Code',
      render: (row: any) => (
        <code className="fw-bold text-primary">{row.materialCode}</code>
      ),
    },
    {
      key: 'materialName',
      label: 'Description',
      render: (row: any) => (
        <div className="fw-medium">{row.materialName || row.description || '-'}</div>
      ),
    },
    {
      key: 'stockQuantity',
      label: 'Avail. Stock',
      render: (row: any) => {
        const stock = row.stockQuantity ?? 0;
        return (
          <span className={`fw-medium ${stock > 0 ? 'text-success' : 'text-muted'}`}>
            {stock}
          </span>
        );
      },
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
          {hasAnyRole(['ADMIN', 'SUPERADMIN']) && (
            <>
              <Button
                variant="outline-secondary"
                size="sm"
                onClick={() => navigate(`/masters/materials/${row.id}/edit`)}
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

  const resetFilters = () => {
    setFilters({ search: '', status: '' });
    setPage(0);
  };

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
            {hasAnyRole(['SUPERADMIN', 'ADMIN']) && (
              <>
                <Button
                  variant="outline-secondary"
                  onClick={() => {
                    materialsApi.list({ page: 0, size: 1000 }).then((d) => {
                      const csv = [
                        ['Code', 'Name', 'Description', 'Stock', 'Status'],
                        ...(d.content || []).map((m: any) => [
                          m.materialCode || '',
                          m.materialName || '',
                          m.description || '',
                          m.stockQuantity ?? 0,
                          m.isActive ? 'Active' : 'Inactive',
                        ]),
                      ]
                        .map((r) => r.map((c) => `"${c}"`).join(','))
                        .join('\n');
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
                <Button variant="primary" onClick={() => navigate('/masters/materials/new')}>
                  <FaPlus className="me-2" /> Add Material
                </Button>
              </>
            )}
          </div>
        }
      />

      {/* Filters */}
      <Card className="mb-4">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3 align-items-end">
              <Col lg={5} md={6}>
                <InputGroup>
                  <Form.Control
                    placeholder="Search material code or name..."
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
                  onChange={(e) => { setFilters({ ...filters, status: e.target.value }); setPage(0); }}
                >
                  <option value="">All Materials</option>
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                </Form.Select>
              </Col>
              <Col lg="auto">
                <div className="d-flex gap-2">
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt />
                  </Button>
                  {(filters.search || filters.status) && (
                    <Button variant="outline-danger" onClick={resetFilters}>Clear</Button>
                  )}
                </div>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      {/* Stats */}
      <Row className="g-3 mb-4">
        <Col sm={6} lg={3}>
          <Card className="border-start border-4 border-primary h-100">
            <Card.Body>
              <div className="text-muted small text-uppercase">Total Materials</div>
              <div className="h3 mb-0">{data?.totalElements || 0}</div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Table */}
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
