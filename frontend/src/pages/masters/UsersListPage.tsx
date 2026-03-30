import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Form, InputGroup, Table, Badge, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaUserCog, FaKey } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { usersApi, getErrorMessage } from '../../api';
import type { User } from '../../api/users';

const UsersListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; id: number | null; name: string }>({ show: false, id: null, name: '' });
  const [resetModal, setResetModal] = useState<{ show: boolean; id: number | null; name: string; newPassword: string }>({ show: false, id: null, name: '', newPassword: 'Password123!' });

  const { data, isLoading, error } = useQuery({ queryKey: ['users'], queryFn: () => usersApi.getAll(0, 100) });
  const users = data?.content ?? [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => usersApi.delete(id),
    onSuccess: () => { toast.success('User deleted successfully'); queryClient.invalidateQueries({ queryKey: ['users'] }); setDeleteModal({ show: false, id: null, name: '' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const resetMutation = useMutation({
    mutationFn: ({ id, password }: { id: number; password: string }) => usersApi.resetPassword(id, password),
    onSuccess: () => { toast.success('Password reset successfully'); setResetModal({ show: false, id: null, name: '', newPassword: 'Password123!' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const filteredUsers = useMemo(() => {
    if (!searchTerm) return users;
    const term = searchTerm.toLowerCase();
    return users.filter((u: User) => u.username?.toLowerCase().includes(term) || u.employeeName?.toLowerCase().includes(term));
  }, [users, searchTerm]);

  const formatDate = (d: any) => { if (!d) return '-'; try { const date = new Date(d); return isNaN(date.getTime()) ? '-' : date.toLocaleDateString(); } catch { return '-'; } };

  if (isLoading) return <LoadingSpinner text="Loading users..." />;
  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Users" subtitle="Manage user accounts" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Users' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/users/new')}><FaPlus className="me-2" />Add User</Button>} />
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span><FaUserCog className="me-2 text-primary" /><strong>Users List</strong> ({filteredUsers.length})</span>
          <InputGroup style={{ width: '300px' }}><InputGroup.Text><FaSearch /></InputGroup.Text><Form.Control placeholder="Search users..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /></InputGroup>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0">
            <thead className="table-light"><tr><th>ID</th><th>Username</th><th>Employee</th><th>Last Login IP</th><th>Status</th><th>Last Modified</th><th className="text-center">Actions</th></tr></thead>
            <tbody>
              {filteredUsers.length === 0 ? <tr><td colSpan={7} className="text-center py-4 text-muted">No users found</td></tr> :
                filteredUsers.map((u: User) => (
                  <tr key={u.id}>
                    <td>{u.id}</td><td><code>{u.username}</code></td><td>{u.employeeName || '-'}</td><td>{u.lastLoginIp || '-'}</td>
                    <td><Badge bg={u.status === 1 ? 'success' : 'secondary'}>{u.status === 1 ? 'Active' : 'Inactive'}</Badge></td>
                    <td>{formatDate(u.lastModifiedDate || u.createdAt)}</td>
                    <td className="text-center">
                      <Button variant="outline-warning" size="sm" className="me-1" onClick={() => setResetModal({ show: true, id: u.id, name: u.username || '', newPassword: 'Password123!' })} title="Reset Password"><FaKey /></Button>
                      <Button variant="outline-primary" size="sm" className="me-1" onClick={() => navigate(`/masters/users/${u.id}/edit`)} title="Edit"><FaEdit /></Button>
                      <Button variant="outline-danger" size="sm" onClick={() => setDeleteModal({ show: true, id: u.id, name: u.username || '' })} title="Delete"><FaTrash /></Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
      <Modal show={deleteModal.show} onHide={() => setDeleteModal({ show: false, id: null, name: '' })} centered>
        <Modal.Header closeButton><Modal.Title>Confirm Delete</Modal.Title></Modal.Header>
        <Modal.Body>Are you sure you want to delete user "<strong>{deleteModal.name}</strong>"?</Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setDeleteModal({ show: false, id: null, name: '' })}>Cancel</Button><Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteModal.id && deleteMutation.mutate(deleteModal.id)}>{deleteMutation.isPending ? 'Deleting...' : 'Delete'}</Button></Modal.Footer>
      </Modal>
      <Modal show={resetModal.show} onHide={() => setResetModal({ show: false, id: null, name: '', newPassword: 'Password123!' })} centered>
        <Modal.Header closeButton><Modal.Title>Reset Password</Modal.Title></Modal.Header>
        <Modal.Body>
          <p>Reset password for user "<strong>{resetModal.name}</strong>"?</p>
          <Form.Group><Form.Label>New Password</Form.Label><Form.Control type="text" value={resetModal.newPassword} onChange={(e) => setResetModal(prev => ({ ...prev, newPassword: e.target.value }))} /></Form.Group>
        </Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setResetModal({ show: false, id: null, name: '', newPassword: 'Password123!' })}>Cancel</Button><Button variant="warning" disabled={resetMutation.isPending} onClick={() => resetModal.id && resetMutation.mutate({ id: resetModal.id, password: resetModal.newPassword })}>{resetMutation.isPending ? 'Resetting...' : 'Reset Password'}</Button></Modal.Footer>
      </Modal>
    </div>
  );
};

export default UsersListPage;
