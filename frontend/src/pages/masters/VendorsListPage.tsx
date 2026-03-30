import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Form, InputGroup, Table, Badge, Modal, Row, Col } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaTruck, FaEye, FaStar } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { vendorsApi, getErrorMessage } from '../../api';

const VendorsListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; id: number | null; name: string }>({ show: false, id: null, name: '' });

  const { data: pagedData, isLoading, error } = useQuery({ 
    queryKey: ['vendors', searchTerm, statusFilter], 
    queryFn: () => vendorsApi.list({ 
      search: searchTerm || undefined, 
      isActive: statusFilter === 'all' ? undefined : statusFilter === 'active' 
    }) 
  });
  const vendors = pagedData?.content || [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => vendorsApi.delete(id),
    onSuccess: () => { toast.success('Vendor deleted successfully'); queryClient.invalidateQueries({ queryKey: ['vendors'] }); setDeleteModal({ show: false, id: null, name: '' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const formatDate = (d: any) => { if (!d) return '-'; try { const date = new Date(d); return isNaN(date.getTime()) ? '-' : date.toLocaleDateString(); } catch { return '-'; } };
  const formatRating = (r: number | null | undefined) => r != null ? <span><FaStar className="text-warning me-1" />{r.toFixed(1)}</span> : '-';

  if (isLoading) return <LoadingSpinner text="Loading vendors..." />;
  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Vendors" subtitle="Manage vendor/supplier records" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Vendors' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/vendors/new')}><FaPlus className="me-2" />Add Vendor</Button>} />
      <Card>
        <Card.Header>
          <Row className="align-items-center">
            <Col><span><FaTruck className="me-2 text-primary" /><strong>Vendors List</strong> ({vendors.length})</span></Col>
            <Col xs="auto"><Form.Select size="sm" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} style={{ width: '130px' }}><option value="all">All Status</option><option value="active">Active</option><option value="inactive">Inactive</option></Form.Select></Col>
            <Col xs="auto"><InputGroup style={{ width: '250px' }}><InputGroup.Text><FaSearch /></InputGroup.Text><Form.Control placeholder="Search vendors..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /></InputGroup></Col>
          </Row>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0 small">
            <thead className="table-light"><tr><th>ID</th><th>Code</th><th>Name</th><th>Contact</th><th>City</th><th>GST No</th><th>Rating</th><th>Status</th><th className="text-center">Actions</th></tr></thead>
            <tbody>
              {vendors.length === 0 ? <tr><td colSpan={9} className="text-center py-4 text-muted">No vendors found</td></tr> :
                vendors.map((v) => (
                  <tr key={v.id}>
                    <td>{v.id}</td><td><code>{v.vendorCode}</code></td><td className="fw-medium">{v.vendorName}</td><td>{v.contactPerson || '-'}<br /><small className="text-muted">{v.phone}</small></td>
                    <td>{v.city || '-'}</td><td>{v.gstNumber || '-'}</td><td>{formatRating(v.rating)}</td>
                    <td><Badge bg={v.isActive ? 'success' : v.isBlacklisted ? 'danger' : 'secondary'}>{v.isBlacklisted ? 'Blacklisted' : v.isActive ? 'Active' : 'Inactive'}</Badge></td>
                    <td className="text-center">
                      <Button variant="outline-info" size="sm" className="me-1" onClick={() => navigate(`/masters/vendors/${v.id}`)} title="View"><FaEye /></Button>
                      <Button variant="outline-primary" size="sm" className="me-1" onClick={() => navigate(`/masters/vendors/${v.id}/edit`)} title="Edit"><FaEdit /></Button>
                      <Button variant="outline-danger" size="sm" onClick={() => setDeleteModal({ show: true, id: v.id!, name: v.vendorName || '' })} title="Delete"><FaTrash /></Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
      <Modal show={deleteModal.show} onHide={() => setDeleteModal({ show: false, id: null, name: '' })} centered>
        <Modal.Header closeButton><Modal.Title>Confirm Delete</Modal.Title></Modal.Header>
        <Modal.Body>Are you sure you want to delete vendor "<strong>{deleteModal.name}</strong>"?</Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setDeleteModal({ show: false, id: null, name: '' })}>Cancel</Button><Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteModal.id && deleteMutation.mutate(deleteModal.id)}>{deleteMutation.isPending ? 'Deleting...' : 'Delete'}</Button></Modal.Footer>
      </Modal>
    </div>
  );
};

export default VendorsListPage;
