import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Form, InputGroup, Table, Badge, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaRuler } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { uomApi, getErrorMessage } from '../../api';
import type { UOM } from '../../api/uom';

const UOMListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; id: number | null; name: string }>({ show: false, id: null, name: '' });

  const { data, isLoading, error } = useQuery({ queryKey: ['uom'], queryFn: () => uomApi.getAll(0, 100) });
  const units = data?.content ?? [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => uomApi.delete(id),
    onSuccess: () => { toast.success('UOM deleted successfully'); queryClient.invalidateQueries({ queryKey: ['uom'] }); setDeleteModal({ show: false, id: null, name: '' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const filteredUnits = useMemo(() => {
    if (!searchTerm) return units;
    const term = searchTerm.toLowerCase();
    return units.filter((u: UOM) => u.code?.toLowerCase().includes(term) || u.name?.toLowerCase().includes(term));
  }, [units, searchTerm]);

  const formatDate = (d: any) => { if (!d) return '-'; try { const date = new Date(d); return isNaN(date.getTime()) ? '-' : date.toLocaleDateString(); } catch { return '-'; } };

  if (isLoading) return <LoadingSpinner text="Loading units of measure..." />;
  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Unit of Measure" subtitle="Manage units of measure" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'UOM' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/uom/new')}><FaPlus className="me-2" />Add UOM</Button>} />
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span><FaRuler className="me-2 text-primary" /><strong>UOM List</strong> ({filteredUnits.length})</span>
          <InputGroup style={{ width: '300px' }}><InputGroup.Text><FaSearch /></InputGroup.Text><Form.Control placeholder="Search UOM..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /></InputGroup>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0">
            <thead className="table-light"><tr><th>ID</th><th>Code</th><th>Name</th><th>Status</th><th>Last Modified</th><th className="text-center">Actions</th></tr></thead>
            <tbody>
              {filteredUnits.length === 0 ? <tr><td colSpan={6} className="text-center py-4 text-muted">No units of measure found</td></tr> :
                filteredUnits.map((u: UOM) => (
                  <tr key={u.id}>
                    <td>{u.id}</td><td><code>{u.code}</code></td><td>{u.name}</td>
                    <td><Badge bg={u.status === 1 ? 'success' : 'secondary'}>{u.status === 1 ? 'Active' : 'Inactive'}</Badge></td>
                    <td>{formatDate(u.updatedAt)}</td>
                    <td className="text-center">
                      <Button variant="outline-primary" size="sm" className="me-1" onClick={() => navigate(`/masters/uom/${u.id}/edit`)} title="Edit"><FaEdit /></Button>
                      <Button variant="outline-danger" size="sm" onClick={() => setDeleteModal({ show: true, id: u.id, name: u.name || '' })} title="Delete"><FaTrash /></Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
      <Modal show={deleteModal.show} onHide={() => setDeleteModal({ show: false, id: null, name: '' })} centered>
        <Modal.Header closeButton><Modal.Title>Confirm Delete</Modal.Title></Modal.Header>
        <Modal.Body>Are you sure you want to delete UOM "<strong>{deleteModal.name}</strong>"?</Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setDeleteModal({ show: false, id: null, name: '' })}>Cancel</Button><Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteModal.id && deleteMutation.mutate(deleteModal.id)}>{deleteMutation.isPending ? 'Deleting...' : 'Delete'}</Button></Modal.Footer>
      </Modal>
    </div>
  );
};

export default UOMListPage;
