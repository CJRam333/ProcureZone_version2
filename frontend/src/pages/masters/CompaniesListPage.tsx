import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Badge, InputGroup, Form, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaSyncAlt, FaBuilding } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { PageHeader, DataTable, LoadingSpinner, ConfirmDialog } from '../../components/common';
import { companiesApi, getErrorMessage } from '../../api';
import type { Company } from '../../api';

const CompaniesListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteId, setDeleteId] = useState<number | null>(null);

  // Fetch companies
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['companies', page, pageSize, searchTerm],
    queryFn: () => companiesApi.getAll(page, pageSize, searchTerm || undefined),
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => companiesApi.delete(id),
    onSuccess: () => {
      toast.success('Company deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['companies'] });
      setDeleteId(null);
    },
    onError: (error) => {
      toast.error(getErrorMessage(error));
    },
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    refetch();
  };

  const columns = [
    {
      key: 'code',
      label: 'Code',
      width: '150px',
      render: (row: Company) => (
        <code className="fw-bold text-primary">{row.code}</code>
      ),
    },
    {
      key: 'name',
      label: 'Company Name',
      render: (row: Company) => (
        <div className="d-flex align-items-center">
          <FaBuilding className="text-muted me-2" />
          <span className="fw-medium">{row.name}</span>
        </div>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      width: '120px',
      render: (row: Company) => (
        <Badge bg={row.status === 1 ? 'success' : 'secondary'}>
          {row.status === 1 ? 'Active' : 'Inactive'}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      width: '150px',
      render: (row: Company) => (
        <div className="d-flex gap-2">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/masters/companies/${row.id}/edit`);
            }}
            title="Edit"
          >
            <FaEdit />
          </Button>
          <Button
            variant="outline-danger"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              setDeleteId(row.id);
            }}
            title="Delete"
          >
            <FaTrash />
          </Button>
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading companies: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="Companies"
        subtitle="Manage company master data"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Masters', path: '/masters' },
          { label: 'Companies' },
        ]}
        actions={
          <Button variant="primary" onClick={() => navigate('/masters/companies/new')}>
            <FaPlus className="me-2" />
            Add Company
          </Button>
        }
      />

      <Card>
        <Card.Body>
          {/* Filters */}
          <Row className="mb-3">
            <Col md={6}>
              <Form onSubmit={handleSearch}>
                <InputGroup>
                  <Form.Control
                    type="text"
                    placeholder="Search by code or name..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                  />
                  <Button type="submit" variant="outline-primary">
                    <FaSearch />
                  </Button>
                </InputGroup>
              </Form>
            </Col>
            <Col md={6} className="text-end">
              <Button variant="outline-secondary" onClick={() => refetch()}>
                <FaSyncAlt className="me-2" />
                Refresh
              </Button>
            </Col>
          </Row>

          {/* Data Table */}
          <DataTable
            columns={columns}
            data={data?.content || []}
            keyField="id"
            loading={isLoading}
            emptyMessage="No companies found"
            totalItems={data?.totalElements || 0}
            currentPage={page}
            pageSize={pageSize}
            onPageChange={setPage}
            onRowClick={(row) => navigate(`/masters/companies/${row.id}/edit`)}
          />
        </Card.Body>
      </Card>

      {/* Delete Confirmation */}
      <ConfirmDialog
        show={deleteId !== null}
        title="Delete Company"
        message="Are you sure you want to delete this company? This action cannot be undone."
        confirmLabel="Delete"
        variant="danger"
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        onCancel={() => setDeleteId(null)}
        loading={deleteMutation.isPending}
      />
    </div>
  );
};

export default CompaniesListPage;
