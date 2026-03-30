import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Form, InputGroup, Table, Badge, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaUserShield, FaCheck, FaTimes } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { rolesApi, getErrorMessage } from '../../api';
import type { Role } from '../../api';

const RolesListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; id: number | null; name: string }>({ show: false, id: null, name: '' });

  const { data: rolesData, isLoading, error } = useQuery({ queryKey: ['roles'], queryFn: async () => rolesApi.getAll(0, 100) });
  const roles: Role[] = (rolesData as any)?.content || [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => rolesApi.delete(id),
    onSuccess: () => { toast.success('Role deleted successfully'); queryClient.invalidateQueries({ queryKey: ['roles'] }); setDeleteModal({ show: false, id: null, name: '' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const filteredRoles = useMemo(() => {
    if (!searchTerm) return roles;
    const term = searchTerm.toLowerCase();
    return roles.filter((r: Role) => r.code?.toLowerCase().includes(term) || r.name?.toLowerCase().includes(term));
  }, [roles, searchTerm]);

  const formatDate = (d: any) => { if (!d) return '-'; try { const date = new Date(d); return isNaN(date.getTime()) ? '-' : date.toLocaleDateString(); } catch { return '-'; } };

  const PermBadge: React.FC<{ allowed: boolean }> = ({ allowed }) => allowed ? <FaCheck className="text-success" /> : <FaTimes className="text-muted" />;

  if (isLoading) return <LoadingSpinner text="Loading roles..." />;
  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Roles" subtitle="Manage user roles and permissions" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Roles' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/roles/new')}><FaPlus className="me-2" />Add Role</Button>} />
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span><FaUserShield className="me-2 text-primary" /><strong>Roles List</strong> ({filteredRoles.length})</span>
          <InputGroup style={{ width: '300px' }}><InputGroup.Text><FaSearch /></InputGroup.Text><Form.Control placeholder="Search roles..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /></InputGroup>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0">
            <thead className="table-light"><tr><th>ID</th><th>Code</th><th>Name</th><th className="text-center">View</th><th className="text-center">Add</th><th className="text-center">Edit</th><th className="text-center">Delete</th><th>Status</th><th>Last Modified</th><th className="text-center">Actions</th></tr></thead>
            <tbody>
              {filteredRoles.length === 0 ? <tr><td colSpan={10} className="text-center py-4 text-muted">No roles found</td></tr> :
                filteredRoles.map((r: Role) => (
                  <tr key={r.id}>
                    <td>{r.id}</td><td><code>{r.code}</code></td><td>{r.name}</td>
                    <td className="text-center"><PermBadge allowed={r.canView || false} /></td>
                    <td className="text-center"><PermBadge allowed={r.canAdd || false} /></td>
                    <td className="text-center"><PermBadge allowed={r.canEdit || false} /></td>
                    <td className="text-center"><PermBadge allowed={r.canDelete || false} /></td>
                    <td><Badge bg={r.status === 1 ? 'success' : 'secondary'}>{r.status === 1 ? 'Active' : 'Inactive'}</Badge></td>
                    <td>{formatDate(r.updatedAt)}</td>
                    <td className="text-center">
                      <Button variant="outline-primary" size="sm" className="me-1" onClick={() => navigate(`/masters/roles/${r.id}/edit`)} title="Edit"><FaEdit /></Button>
                      <Button variant="outline-danger" size="sm" onClick={() => setDeleteModal({ show: true, id: r.id!, name: r.name || '' })} title="Delete"><FaTrash /></Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
      <Modal show={deleteModal.show} onHide={() => setDeleteModal({ show: false, id: null, name: '' })} centered>
        <Modal.Header closeButton><Modal.Title>Confirm Delete</Modal.Title></Modal.Header>
        <Modal.Body>Are you sure you want to delete role "<strong>{deleteModal.name}</strong>"? This will affect all users with this role.</Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setDeleteModal({ show: false, id: null, name: '' })}>Cancel</Button><Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteModal.id && deleteMutation.mutate(deleteModal.id)}>{deleteMutation.isPending ? 'Deleting...' : 'Delete'}</Button></Modal.Footer>
      </Modal>
    </div>
  );
};

export default RolesListPage;
