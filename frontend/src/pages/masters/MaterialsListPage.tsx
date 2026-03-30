import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Form, InputGroup, Table, Badge, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaBox, FaEye } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';

const MaterialsListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; id: number | null; name: string }>({ show: false, id: null, name: '' });

  const { data: pagedData, isLoading, error } = useQuery({ 
    queryKey: ['materials', searchTerm], 
    queryFn: () => materialsApi.list({ search: searchTerm || undefined }) 
  });
  const materials = pagedData?.content || [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => materialsApi.delete(id),
    onSuccess: () => { toast.success('Material deleted successfully'); queryClient.invalidateQueries({ queryKey: ['materials'] }); setDeleteModal({ show: false, id: null, name: '' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const formatDate = (d: any) => { if (!d) return '-'; try { const date = new Date(d); return isNaN(date.getTime()) ? '-' : date.toLocaleDateString(); } catch { return '-'; } };

  if (isLoading) return <LoadingSpinner text="Loading materials..." />;
  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Materials" subtitle="Manage material catalog" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Materials' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/materials/new')}><FaPlus className="me-2" />Add Material</Button>} />
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span><FaBox className="me-2 text-primary" /><strong>Materials List</strong> ({materials.length})</span>
          <InputGroup style={{ width: '300px' }}><InputGroup.Text><FaSearch /></InputGroup.Text><Form.Control placeholder="Search materials..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /></InputGroup>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0">
            <thead className="table-light"><tr><th>ID</th><th>Code</th><th>Description</th><th>UOM</th><th>Category</th><th>Status</th><th>Last Modified</th><th className="text-center">Actions</th></tr></thead>
            <tbody>
              {materials.length === 0 ? <tr><td colSpan={8} className="text-center py-4 text-muted">No materials found</td></tr> :
                materials.map((m) => (
                  <tr key={m.id}>
                    <td>{m.id}</td><td><code>{m.materialCode}</code></td><td>{m.description}</td>
                    <td>{m.uomName || '-'}</td><td>{m.categoryName || '-'}</td>
                    <td><Badge bg={m.isActive ? 'success' : 'secondary'}>{m.isActive ? 'Active' : 'Inactive'}</Badge></td>
                    <td>{formatDate(m.updatedAt)}</td>
                    <td className="text-center">
                      <Button variant="outline-info" size="sm" className="me-1" onClick={() => navigate(`/masters/materials/${m.id}`)} title="View"><FaEye /></Button>
                      <Button variant="outline-primary" size="sm" className="me-1" onClick={() => navigate(`/masters/materials/${m.id}/edit`)} title="Edit"><FaEdit /></Button>
                      <Button variant="outline-danger" size="sm" onClick={() => setDeleteModal({ show: true, id: m.id!, name: m.description || '' })} title="Delete"><FaTrash /></Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
      <Modal show={deleteModal.show} onHide={() => setDeleteModal({ show: false, id: null, name: '' })} centered>
        <Modal.Header closeButton><Modal.Title>Confirm Delete</Modal.Title></Modal.Header>
        <Modal.Body>Are you sure you want to delete material "<strong>{deleteModal.name}</strong>"?</Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setDeleteModal({ show: false, id: null, name: '' })}>Cancel</Button><Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteModal.id && deleteMutation.mutate(deleteModal.id)}>{deleteMutation.isPending ? 'Deleting...' : 'Delete'}</Button></Modal.Footer>
      </Modal>
    </div>
  );
};

export default MaterialsListPage;
