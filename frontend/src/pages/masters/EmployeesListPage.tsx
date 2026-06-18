import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Card, Button, Badge, InputGroup, Form, Row, Col, Tabs, Tab } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { FaPlus, FaEdit, FaTrash, FaSearch, FaSyncAlt, FaUsers, FaEye, FaUserTag, FaSitemap } from 'react-icons/fa';
import { PageHeader, DataTable, ConfirmDialog } from '../../components/common';
import { employeesApi, departmentsApi, getErrorMessage } from '../../api';
import type { Employee } from '../../api/employees';
import EmployeeRoleMappingPage from '../mappings/EmployeeRoleMappingPage';
import ReportingHierarchyPage from '../mappings/ReportingHierarchyPage';

const EmployeesListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const queryClient = useQueryClient();

  // Pagination & search state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [statusFilter, setStatusFilter] = useState<number | undefined>(undefined);
  const [departmentFilter, setDepartmentFilter] = useState<number | undefined>(undefined);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [deleteName, setDeleteName] = useState('');

  // Tab state from URL for deep-linkable tabs
  const activeTab = searchParams.get('tab') || 'list';
  const setActiveTab = (tab: string) => {
    setSearchParams(tab === 'list' ? {} : { tab });
  };

  // Server-side paginated query
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['employees', page, pageSize, searchTerm, statusFilter, departmentFilter],
    queryFn: () => employeesApi.getAll(page, pageSize, searchTerm || undefined, {
      status: statusFilter,
      departmentId: departmentFilter,
    }),
  });

  const { data: departmentsData } = useQuery({
    queryKey: ['departments-active'],
    queryFn: () => departmentsApi.getActive(0, 200),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => employeesApi.delete(id),
    onSuccess: () => {
      toast.success('Employee deleted successfully');
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      setDeleteId(null);
      setDeleteName('');
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    setSearchTerm(searchInput);
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setSearchTerm('');
    setStatusFilter(undefined);
    setDepartmentFilter(undefined);
    setPage(0);
  };

  const formatDate = (d: string | undefined | null) => {
    if (!d) return '-';
    try {
      const date = new Date(d);
      return isNaN(date.getTime()) ? '-' : date.toLocaleDateString();
    } catch {
      return '-';
    }
  };

  const columns = [
    {
      key: 'empId',
      label: 'Emp ID',
      width: '120px',
      render: (row: Employee) => <code className="fw-bold text-primary">{row.empId}</code>,
    },
    {
      key: 'empName',
      label: 'Name',
      render: (row: Employee) => (
        <div className="d-flex align-items-center">
          <FaUsers className="text-muted me-2" />
          <span className="fw-medium">{row.empName}</span>
        </div>
      ),
    },
    {
      key: 'empEmail',
      label: 'Email',
      render: (row: Employee) => <span className="text-muted">{row.empEmail || '-'}</span>,
    },
    {
      key: 'empDesignation',
      label: 'Designation',
      render: (row: Employee) => row.empDesignation || '-',
    },
    {
      key: 'departmentName',
      label: 'Department',
      render: (row: Employee) => row.departmentName || '-',
    },
    {
      key: 'empStatus',
      label: 'Status',
      width: '100px',
      render: (row: Employee) => (
        <Badge bg={row.empStatus === 1 ? 'success' : 'secondary'}>
          {row.empStatus === 1 ? 'Active' : 'Inactive'}
        </Badge>
      ),
    },
    {
      key: 'empJoinDate',
      label: 'Join Date',
      width: '120px',
      render: (row: Employee) => formatDate(row.empJoinDate),
    },
    {
      key: 'actions',
      label: 'Actions',
      width: '150px',
      render: (row: Employee) => (
        <div className="d-flex gap-1">
          <Button variant="outline-info" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/masters/employees/${row.id}`); }} title="View">
            <FaEye />
          </Button>
          <Button variant="outline-primary" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/masters/employees/${row.id}/edit`); }} title="Edit">
            <FaEdit />
          </Button>
          <Button variant="outline-danger" size="sm" onClick={(e) => { e.stopPropagation(); setDeleteId(row.id); setDeleteName(row.empName || ''); }} title="Delete">
            <FaTrash />
          </Button>
        </div>
      ),
    },
  ];

  if (error) return <div className="alert alert-danger">{getErrorMessage(error)}</div>;

  return (
    <div>
      <PageHeader
        title="Employees"
        subtitle="Manage employee records, roles, and reporting hierarchy"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Masters', path: '/masters' },
          { label: 'Employees' },
        ]}
        actions={
          activeTab === 'list' ? (
            <Button variant="primary" onClick={() => navigate('/masters/employees/new')}>
              <FaPlus className="me-2" />Add Employee
            </Button>
          ) : undefined
        }
      />

      <Tabs
        activeKey={activeTab}
        onSelect={(k) => setActiveTab(k || 'list')}
        className="mb-4"
        id="employees-tabs"
      >
        <Tab eventKey="list" title={<><FaUsers className="me-2" />Employees</>}>
          <Card>
            <Card.Body>
              {/* Search & Filters */}
              <Row className="mb-3 g-2">
                <Col md={4}>
                  <Form onSubmit={handleSearch}>
                    <InputGroup>
                      <Form.Control
                        type="text"
                        placeholder="Search by ID, name, email, or designation..."
                        value={searchInput}
                        onChange={(e) => setSearchInput(e.target.value)}
                      />
                      <Button type="submit" variant="outline-primary">
                        <FaSearch />
                      </Button>
                    </InputGroup>
                  </Form>
                </Col>
                <Col md={2}>
                  <Form.Select
                    value={statusFilter?.toString() || ''}
                    onChange={(e) => { setStatusFilter(e.target.value ? Number(e.target.value) : undefined); setPage(0); }}
                  >
                    <option value="">All Statuses</option>
                    <option value="1">Active</option>
                    <option value="0">Inactive</option>
                  </Form.Select>
                </Col>
                <Col md={3}>
                  <Form.Select
                    value={departmentFilter?.toString() || ''}
                    onChange={(e) => { setDepartmentFilter(e.target.value ? Number(e.target.value) : undefined); setPage(0); }}
                  >
                    <option value="">All Departments</option>
                    {departmentsData?.content?.map((d: any) => (
                      <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                  </Form.Select>
                </Col>
                <Col md="auto" className="d-flex gap-2">
                  {(searchTerm || statusFilter != null || departmentFilter != null) && (
                    <Button variant="outline-danger" size="sm" onClick={handleClearSearch}>
                      Clear
                    </Button>
                  )}
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt className="me-2" />Refresh
                  </Button>
                </Col>
              </Row>

              {/* Server-side paginated DataTable */}
              <DataTable
                columns={columns}
                data={data?.content || []}
                keyField="id"
                loading={isLoading}
                emptyMessage={searchTerm ? `No employees found matching "${searchTerm}"` : 'No employees found'}
                totalItems={data?.totalElements || 0}
                currentPage={page}
                pageSize={pageSize}
                onPageChange={setPage}
                onPageSizeChange={(size) => { setPageSize(size); setPage(0); }}
                onRowClick={(row) => navigate(`/masters/employees/${row.id}`)}
              />
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="role-mapping" title={<><FaUserTag className="me-2" />Role Mapping</>} mountOnEnter>
          <EmployeeRoleMappingPage embedded />
        </Tab>

        <Tab eventKey="reporting-hierarchy" title={<><FaSitemap className="me-2" />Reporting Hierarchy</>} mountOnEnter>
          <ReportingHierarchyPage embedded />
        </Tab>
      </Tabs>

      {/* Delete Confirmation */}
      <ConfirmDialog
        show={deleteId !== null}
        title="Delete Employee"
        message={`Are you sure you want to delete employee "${deleteName}"? This action cannot be undone.`}
        confirmLabel="Delete"
        variant="danger"
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        onCancel={() => { setDeleteId(null); setDeleteName(''); }}
        loading={deleteMutation.isPending}
      />
    </div>
  );
};

export default EmployeesListPage;
