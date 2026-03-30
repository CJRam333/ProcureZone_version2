import React, { useState, useEffect } from 'react';
import {
    Card,
    Row,
    Col,
    Form,
    Button,
    Table,
    Badge,
    Alert,
    InputGroup,
    Spinner,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    FaSearch,
    FaSave,
    FaUsers,
    FaUserTag,
    FaCheck,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { getErrorMessage } from '../../api';
import apiClient from '../../api/client';

interface Employee {
    id: number;
    empId: string;
    empName: string;
    empEmail: string;
    departmentName: string;
    empStatus: number;
}

interface Role {
    id: number;
    code: string;
    name: string;
    description: string;
}

interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
}

const EmployeeRoleMappingPage: React.FC = () => {
    const queryClient = useQueryClient();

    const [selectedEmployee, setSelectedEmployee] = useState<number | null>(null);
    const [mappings, setMappings] = useState<Record<number, boolean>>({});
    const [searchTerm, setSearchTerm] = useState('');
    const [employeeSearch, setEmployeeSearch] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Fetch employees
    const { data: employeesData, isLoading: loadingEmployees } = useQuery({
        queryKey: ['employees-all', employeeSearch],
        queryFn: async () => {
            const params: Record<string, unknown> = { page: 0, size: 50 };
            if (employeeSearch) params.search = employeeSearch;
            const response = await apiClient.get<PageResponse<Employee>>('/employees', { params });
            return response.data;
        },
    });

    // Fetch roles
    const { data: rolesData, isLoading: loadingRoles } = useQuery({
        queryKey: ['roles-all'],
        queryFn: async () => {
            const response = await apiClient.get<PageResponse<Role>>('/roles', {
                params: { page: 0, size: 100 }
            });
            return response.data;
        },
    });

    // Fetch current role assignments for selected employee
    const { data: currentRoles, isLoading: loadingCurrentRoles } = useQuery({
        queryKey: ['employee-roles', selectedEmployee],
        queryFn: async () => {
            if (!selectedEmployee) return [];
            const response = await apiClient.get(`/employee-roles/employee/${selectedEmployee}/roles`);
            return response.data?.data || [];
        },
        enabled: !!selectedEmployee,
    });

    // Update mappings state when data loads
    useEffect(() => {
        if (currentRoles && Array.isArray(currentRoles)) {
            const newMappings: Record<number, boolean> = {};
            currentRoles.forEach((r: { roleId?: number; id?: number }) => {
                newMappings[r.roleId || r.id || 0] = true;
            });
            setMappings(newMappings);
        }
    }, [currentRoles]);

    // Save mutation
    const saveMutation = useMutation({
        mutationFn: async () => {
            if (!selectedEmployee) throw new Error('Please select an employee');

            const roleIds = Object.entries(mappings)
                .filter(([_, active]) => active)
                .map(([roleId]) => parseInt(roleId));

            await apiClient.post(`/employee-roles/employee/${selectedEmployee}/batch`, {
                roleIds,
            });
        },
        onSuccess: () => {
            setSuccess('Role assignments saved successfully!');
            queryClient.invalidateQueries({ queryKey: ['employee-roles'] });
            setTimeout(() => setSuccess(null), 3000);
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    const employees: Employee[] = employeesData?.content || [];
    const roles: Role[] = rolesData?.content || [];

    const filteredRoles = roles.filter(
        (role) =>
            role.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            role.code.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleToggle = (roleId: number) => {
        setMappings((prev) => ({
            ...prev,
            [roleId]: !prev[roleId],
        }));
    };

    const selectedCount = Object.values(mappings).filter(Boolean).length;

    const selectedEmployeeData = employees.find(e => e.id === selectedEmployee);

    if (loadingEmployees || loadingRoles) {
        return <LoadingSpinner fullPage text="Loading data..." />;
    }

    return (
        <div>
            <PageHeader
                title="Employee-Role Mapping"
                subtitle="Assign roles to employees"
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Masters', path: '/masters/employees' },
                    { label: 'Employee-Role Mapping' },
                ]}
            />

            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}

            {success && (
                <Alert variant="success" dismissible onClose={() => setSuccess(null)}>
                    {success}
                </Alert>
            )}

            <Row className="g-4">
                {/* Employee Selection */}
                <Col lg={4}>
                    <Card className="h-100">
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaUsers className="me-2" />
                                Select Employee
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            <InputGroup className="mb-3">
                                <InputGroup.Text>
                                    <FaSearch />
                                </InputGroup.Text>
                                <Form.Control
                                    type="text"
                                    placeholder="Search employees..."
                                    value={employeeSearch}
                                    onChange={(e) => setEmployeeSearch(e.target.value)}
                                />
                            </InputGroup>

                            <div style={{ maxHeight: 300, overflowY: 'auto' }}>
                                {employees.map((emp) => (
                                    <div
                                        key={emp.id}
                                        className={`p-2 border-bottom cursor-pointer ${selectedEmployee === emp.id ? 'bg-primary text-white' : ''
                                            }`}
                                        style={{ cursor: 'pointer' }}
                                        onClick={() => {
                                            setSelectedEmployee(emp.id);
                                            setMappings({});
                                        }}
                                    >
                                        <div className="fw-medium">{emp.empName}</div>
                                        <small className={selectedEmployee === emp.id ? 'text-white-50' : 'text-muted'}>
                                            {emp.empId} | {emp.departmentName}
                                        </small>
                                    </div>
                                ))}
                            </div>

                            {selectedEmployee && selectedEmployeeData && (
                                <div className="mt-4 p-3 bg-light rounded">
                                    <h6 className="mb-2">Selected:</h6>
                                    <div className="fw-bold">{selectedEmployeeData.empName}</div>
                                    <small className="text-muted">{selectedEmployeeData.empEmail}</small>
                                    <div className="d-flex justify-content-between align-items-center mt-3">
                                        <span className="text-muted">Assigned Roles:</span>
                                        <Badge bg="primary" className="fs-6">
                                            {selectedCount}
                                        </Badge>
                                    </div>
                                    <Button
                                        variant="success"
                                        className="w-100 mt-3"
                                        onClick={() => saveMutation.mutate()}
                                        disabled={saveMutation.isPending}
                                    >
                                        {saveMutation.isPending ? (
                                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                                        ) : (
                                            <FaSave className="me-2" />
                                        )}
                                        Save Role Assignments
                                    </Button>
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Role Selection */}
                <Col lg={8}>
                    <Card>
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaUserTag className="me-2" />
                                Available Roles
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            {!selectedEmployee ? (
                                <div className="text-center py-5 text-muted">
                                    <FaUsers size={48} className="mb-3" />
                                    <h5>Select an Employee</h5>
                                    <p>Choose an employee from the left panel to manage their role assignments.</p>
                                </div>
                            ) : loadingCurrentRoles ? (
                                <LoadingSpinner text="Loading role assignments..." />
                            ) : (
                                <>
                                    <InputGroup className="mb-3">
                                        <InputGroup.Text>
                                            <FaSearch />
                                        </InputGroup.Text>
                                        <Form.Control
                                            type="text"
                                            placeholder="Search roles..."
                                            value={searchTerm}
                                            onChange={(e) => setSearchTerm(e.target.value)}
                                        />
                                    </InputGroup>

                                    <div className="table-responsive" style={{ maxHeight: 400, overflowY: 'auto' }}>
                                        <Table hover className="mb-0">
                                            <thead className="bg-light sticky-top">
                                                <tr>
                                                    <th style={{ width: 50 }}>Assign</th>
                                                    <th>Code</th>
                                                    <th>Role Name</th>
                                                    <th>Description</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {filteredRoles.map((role) => (
                                                    <tr
                                                        key={role.id}
                                                        onClick={() => handleToggle(role.id)}
                                                        style={{ cursor: 'pointer' }}
                                                        className={mappings[role.id] ? 'table-success' : ''}
                                                    >
                                                        <td className="text-center">
                                                            <Form.Check
                                                                type="checkbox"
                                                                checked={!!mappings[role.id]}
                                                                onChange={() => handleToggle(role.id)}
                                                                onClick={(e) => e.stopPropagation()}
                                                            />
                                                        </td>
                                                        <td>
                                                            <Badge bg="secondary">{role.code}</Badge>
                                                        </td>
                                                        <td>
                                                            {mappings[role.id] && (
                                                                <FaCheck className="text-success me-2" />
                                                            )}
                                                            {role.name}
                                                        </td>
                                                        <td className="text-muted">{role.description || '-'}</td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </Table>
                                    </div>

                                    {filteredRoles.length === 0 && (
                                        <div className="text-center py-4 text-muted">
                                            No roles found matching "{searchTerm}"
                                        </div>
                                    )}
                                </>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default EmployeeRoleMappingPage;
