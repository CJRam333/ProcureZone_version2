import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Badge, InputGroup, Form, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaSyncAlt, FaMapMarkerAlt } from 'react-icons/fa';
import { toast } from 'react-toastify';
import { PageHeader, DataTable, ConfirmDialog } from '../../components/common';
import { locationsApi, getErrorMessage } from '../../api';
import type { Location } from '../../api';

const LocationsListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['locations', page, pageSize, searchTerm],
    queryFn: () => locationsApi.getAll(page, pageSize, searchTerm || undefined),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => locationsApi.delete(id),
    onSuccess: () => { toast.success('Location deleted successfully'); queryClient.invalidateQueries({ queryKey: ['locations'] }); setDeleteId(null); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const handleSearch = (e: React.FormEvent) => { e.preventDefault(); setPage(0); refetch(); };

  const columns = [
    { key: 'code', label: 'Code', width: '150px', render: (row: Location) => <code className="fw-bold text-primary">{row.code}</code> },
    { key: 'name', label: 'Location Name', render: (row: Location) => (<div className="d-flex align-items-center"><FaMapMarkerAlt className="text-muted me-2" /><span className="fw-medium">{row.name}</span></div>) },
    { key: 'status', label: 'Status', width: '120px', render: (row: Location) => <Badge bg={row.status === 1 ? 'success' : 'secondary'}>{row.status === 1 ? 'Active' : 'Inactive'}</Badge> },
    { key: 'actions', label: 'Actions', width: '150px', render: (row: Location) => (
      <div className="d-flex gap-2">
        <Button variant="outline-primary" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/masters/locations/${row.id}/edit`); }}><FaEdit /></Button>
        <Button variant="outline-danger" size="sm" onClick={(e) => { e.stopPropagation(); setDeleteId(row.id); }}><FaTrash /></Button>
      </div>
    )},
  ];

  if (error) return <div className="alert alert-danger">Error loading locations: {getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Locations" subtitle="Manage location master data" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Locations' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/locations/new')}><FaPlus className="me-2" />Add Location</Button>} />
      <Card><Card.Body>
        <Row className="mb-3">
          <Col md={6}><Form onSubmit={handleSearch}><InputGroup><Form.Control type="text" placeholder="Search..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /><Button type="submit" variant="outline-primary"><FaSearch /></Button></InputGroup></Form></Col>
          <Col md={6} className="text-end"><Button variant="outline-secondary" onClick={() => refetch()}><FaSyncAlt className="me-2" />Refresh</Button></Col>
        </Row>
        <DataTable columns={columns} data={data?.content || []} keyField="id" loading={isLoading} emptyMessage="No locations found" totalItems={data?.totalElements || 0} currentPage={page} pageSize={pageSize} onPageChange={setPage} onRowClick={(row) => navigate(`/masters/locations/${row.id}/edit`)} />
      </Card.Body></Card>
      <ConfirmDialog show={deleteId !== null} title="Delete Location" message="Are you sure you want to delete this location?" confirmLabel="Delete" variant="danger" onConfirm={() => deleteId && deleteMutation.mutate(deleteId)} onCancel={() => setDeleteId(null)} loading={deleteMutation.isPending} />
    </div>
  );
};

export default LocationsListPage;
