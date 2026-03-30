import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Button, Form, InputGroup, Table, Badge, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaUsers, FaEye } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { employeesApi, getErrorMessage } from '../../api';
import type { Employee } from '../../api/employees';

const EmployeesListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteModal, setDeleteModal] = useState<{ show: boolean; id: number | null; name: string }>({ show: false, id: null, name: '' });

  const { data, isLoading, error } = useQuery({ queryKey: ['employees'], queryFn: () => employeesApi.getAll(0, 100) });
  const employees = data?.content ?? [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => employeesApi.delete(id),
    onSuccess: () => { toast.success('Employee deleted successfully'); queryClient.invalidateQueries({ queryKey: ['employees'] }); setDeleteModal({ show: false, id: null, name: '' }); },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const filteredEmployees = useMemo(() => {
    if (!searchTerm) return employees;
    const term = searchTerm.toLowerCase();
    return employees.filter((e: Employee) => 
      e.empId?.toLowerCase().includes(term) || 
      e.empName?.toLowerCase().includes(term) || 
      e.empEmail?.toLowerCase().includes(term) ||
      e.empDesignation?.toLowerCase().includes(term)
    );
  }, [employees, searchTerm]);

  const formatDate = (d: any) => { if (!d) return '-'; try { const date = new Date(d); return isNaN(date.getTime()) ? '-' : date.toLocaleDateString(); } catch { return '-'; } };

  if (isLoading) return <LoadingSpinner text="Loading employees..." />;
  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader title="Employees" subtitle="Manage employee records" breadcrumbs={[{ label: 'Dashboard', path: '/dashboard' }, { label: 'Masters', path: '/masters' }, { label: 'Employees' }]} actions={<Button variant="primary" onClick={() => navigate('/masters/employees/new')}><FaPlus className="me-2" />Add Employee</Button>} />
      <Card>
        <Card.Header className="d-flex justify-content-between align-items-center">
          <span><FaUsers className="me-2 text-primary" /><strong>Employees List</strong> ({filteredEmployees.length})</span>
          <InputGroup style={{ width: '300px' }}><InputGroup.Text><FaSearch /></InputGroup.Text><Form.Control placeholder="Search employees..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} /></InputGroup>
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0">
            <thead className="table-light"><tr><th>ID</th><th>Emp ID</th><th>Name</th><th>Email</th><th>Designation</th><th>Department</th><th>Status</th><th>Join Date</th><th className="text-center">Actions</th></tr></thead>
            <tbody>
              {filteredEmployees.length === 0 ? <tr><td colSpan={9} className="text-center py-4 text-muted">No employees found</td></tr> :
                filteredEmployees.map((e: Employee) => (
                  <tr key={e.id}>
                    <td>{e.id}</td><td><code>{e.empId}</code></td><td className="fw-medium">{e.empName}</td><td>{e.empEmail || '-'}</td>
                    <td>{e.empDesignation || '-'}</td><td>{e.departmentName || '-'}</td>
                    <td><Badge bg={e.empStatus === 1 ? 'success' : 'secondary'}>{e.empStatus === 1 ? 'Active' : 'Inactive'}</Badge></td>
                    <td>{formatDate(e.empJoinDate)}</td>
                    <td className="text-center">
                      <Button variant="outline-info" size="sm" className="me-1" onClick={() => navigate(`/masters/employees/${e.id}`)} title="View"><FaEye /></Button>
                      <Button variant="outline-primary" size="sm" className="me-1" onClick={() => navigate(`/masters/employees/${e.id}/edit`)} title="Edit"><FaEdit /></Button>
                      <Button variant="outline-danger" size="sm" onClick={() => setDeleteModal({ show: true, id: e.id, name: e.empName || '' })} title="Delete"><FaTrash /></Button>
                    </td>
                  </tr>
                ))}
            </tbody>
          </Table>
        </Card.Body>
      </Card>
      <Modal show={deleteModal.show} onHide={() => setDeleteModal({ show: false, id: null, name: '' })} centered>
        <Modal.Header closeButton><Modal.Title>Confirm Delete</Modal.Title></Modal.Header>
        <Modal.Body>Are you sure you want to delete employee "<strong>{deleteModal.name}</strong>"?</Modal.Body>
        <Modal.Footer><Button variant="secondary" onClick={() => setDeleteModal({ show: false, id: null, name: '' })}>Cancel</Button><Button variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteModal.id && deleteMutation.mutate(deleteModal.id)}>{deleteMutation.isPending ? 'Deleting...' : 'Delete'}</Button></Modal.Footer>
      </Modal>
    </div>
  );
};

export default EmployeesListPage;
