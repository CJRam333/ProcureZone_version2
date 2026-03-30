import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Badge, InputGroup, Form, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaSyncAlt, FaIndustry } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { PageHeader, DataTable, ConfirmDialog } from '../../components/common';
import { plantsApi, getErrorMessage } from '../../api';
import type { Plant } from '../../api';

const PlantsListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['plants', page, pageSize, searchTerm],
    queryFn: () => plantsApi.getAll(page, pageSize, searchTerm || undefined),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => plantsApi.delete(id),
    onSuccess: () => {
      toast.success('Plant deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['plants'] });
      setDeleteId(null);
    },
    onError: (error) => toast.error(getErrorMessage(error)),
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
      render: (row: Plant) => <code className="fw-bold text-primary">{row.code}</code>,
    },
    {
      key: 'name',
      label: 'Plant Name',
      render: (row: Plant) => (
        <div className="d-flex align-items-center">
          <FaIndustry className="text-muted me-2" />
          <span className="fw-medium">{row.name}</span>
        </div>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      width: '120px',
      render: (row: Plant) => (
        <Badge bg={row.status === 1 ? 'success' : 'secondary'}>
          {row.status === 1 ? 'Active' : 'Inactive'}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      width: '150px',
      render: (row: Plant) => (
        <div className="d-flex gap-2">
          <Button variant="outline-primary" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/masters/plants/${row.id}/edit`); }} title="Edit">
            <FaEdit />
          </Button>
          <Button variant="outline-danger" size="sm" onClick={(e) => { e.stopPropagation(); setDeleteId(row.id); }} title="Delete">
            <FaTrash />
          </Button>
        </div>
      ),
    },
  ];

  if (error) return <div className="alert alert-danger">Error loading plants: {getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader
        title="Plants"
        subtitle="Manage plant/facility master data"
        breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Plants' }]}
        actions={<Button variant="primary" onClick={() => navigate('/masters/plants/new')}><FaPlus className="me-2" />Add Plant</Button>}
      />

      <Card>
        <Card.Body>
          <Row className="mb-3">
            <Col md={6}>
              <Form onSubmit={handleSearch}>
                <InputGroup>
                  <Form.Control type="text" placeholder="Search by code or name..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
                  <Button type="submit" variant="outline-primary"><FaSearch /></Button>
                </InputGroup>
              </Form>
            </Col>
            <Col md={6} className="text-end">
              <Button variant="outline-secondary" onClick={() => refetch()}><FaSyncAlt className="me-2" />Refresh</Button>
            </Col>
          </Row>

          <DataTable columns={columns} data={data?.content || []} keyField="id" loading={isLoading} emptyMessage="No plants found" totalItems={data?.totalElements || 0} currentPage={page} pageSize={pageSize} onPageChange={setPage} onRowClick={(row) => navigate(`/masters/plants/${row.id}/edit`)} />
        </Card.Body>
      </Card>

      <ConfirmDialog show={deleteId !== null} title="Delete Plant" message="Are you sure you want to delete this plant?" confirmLabel="Delete" variant="danger" onConfirm={() => deleteId && deleteMutation.mutate(deleteId)} onCancel={() => setDeleteId(null)} loading={deleteMutation.isPending} />
    </div>
  );
};

export default PlantsListPage;
