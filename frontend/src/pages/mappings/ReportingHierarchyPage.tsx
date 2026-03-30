import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Form,
    Button,
    Alert,
    Table,
    Badge,
} from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import {
    FaArrowLeft,
    FaSave,
    FaSearch,
    FaSitemap,
    FaUserTie,
    FaEdit,
    FaTrash,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import apiClient from '../../api/client';
import { getErrorMessage } from '../../api';

interface Employee {
    id: number;
    empId: string;
    empName: string;
    empDesignation?: string;
    departmentName?: string;
    reportingToId?: number;
}

interface ReportingRelation {
    id?: number;
    employeeId: number;
    reportingToId: number;
    effectiveFrom?: string;
    effectiveTo?: string;
    employeeName?: string;
    employeeCode?: string;
    reportingToName?: string;
    reportingToCode?: string;
}

const ReportingHierarchyPage: React.FC = () => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [selectedEmployee, setSelectedEmployee] = useState<number | ''>('');
    const [selectedManager, setSelectedManager] = useState<number | ''>('');
    const [searchTerm, setSearchTerm] = useState('');
    const [editingId, setEditingId] = useState<number | null>(null);

    // Fetch employees
    const { data: employeesData, isLoading: loadingEmployees } = useQuery({
        queryKey: ['employees-all'],
        queryFn: async () => {
            const response = await apiClient.get('/employees?size=500');
            return response.data;
        },
    });

    // Fetch existing hierarchy
    const { data: hierarchyData, isLoading: loadingHierarchy, error } = useQuery({
        queryKey: ['reporting-hierarchy'],
        queryFn: async () => {
            const response = await apiClient.get('/employee-reporting');
            return response.data;
        },
    });

    const employees: Employee[] = employeesData?.content || employeesData || [];
    const relations: ReportingRelation[] = hierarchyData?.content || hierarchyData || [];

    // Get managers (employees who can be reported to)
    const managers = useMemo(() => {
        return employees.filter(e => e.empDesignation?.toLowerCase().includes('manager') ||
            e.empDesignation?.toLowerCase().includes('head') ||
            e.empDesignation?.toLowerCase().includes('director') ||
            e.empDesignation?.toLowerCase().includes('lead') ||
            employees.some(other => other.reportingToId === e.id));
    }, [employees]);

    // Filter relations based on search
    const filteredRelations = useMemo(() => {
        if (!searchTerm) return relations;
        const lower = searchTerm.toLowerCase();
        return relations.filter(r =>
            r.employeeName?.toLowerCase().includes(lower) ||
            r.employeeCode?.toLowerCase().includes(lower) ||
            r.reportingToName?.toLowerCase().includes(lower) ||
            r.reportingToCode?.toLowerCase().includes(lower)
        );
    }, [relations, searchTerm]);

    // Save hierarchy mutation
    const saveMutation = useMutation({
        mutationFn: async (data: { employeeId: number; reportingToId: number }) => {
            if (editingId) {
                const response = await apiClient.put(`/employee-reporting/${editingId}`, data);
                return response.data;
            } else {
                const response = await apiClient.post('/employee-reporting', data);
                return response.data;
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['reporting-hierarchy'] });
            toast.success(editingId ? 'Reporting updated successfully!' : 'Reporting added successfully!');
            resetForm();
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    // Delete relation mutation
    const deleteMutation = useMutation({
        mutationFn: async (id: number) => {
            await apiClient.delete(`/employee-reporting/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['reporting-hierarchy'] });
            toast.success('Reporting relation removed!');
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    const resetForm = () => {
        setSelectedEmployee('');
        setSelectedManager('');
        setEditingId(null);
    };

    const handleSave = () => {
        if (!selectedEmployee || !selectedManager) {
            toast.error('Please select both employee and reporting manager');
            return;
        }

        if (selectedEmployee === selectedManager) {
            toast.error('Employee cannot report to themselves');
            return;
        }

        saveMutation.mutate({
            employeeId: Number(selectedEmployee),
            reportingToId: Number(selectedManager),
        });
    };

    const handleEdit = (relation: ReportingRelation) => {
        setSelectedEmployee(relation.employeeId);
        setSelectedManager(relation.reportingToId);
        setEditingId(relation.id || null);
    };

    const handleDelete = (id: number) => {
        if (window.confirm('Are you sure you want to remove this reporting relation?')) {
            deleteMutation.mutate(id);
        }
    };

    const isLoading = loadingEmployees || loadingHierarchy;

    if (isLoading && !relations.length) {
        return <LoadingSpinner fullPage text="Loading data..." />;
    }

    // Get employee details for display
    const getEmployeeName = (id: number) => {
        const emp = employees.find(e => e.id === id);
        return emp ? `${emp.empName} (${emp.empId})` : 'Unknown';
    };

    return (
        <div>
            <PageHeader
                title="Employee Reporting Hierarchy"
                subtitle="Configure who reports to whom in the organization"
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Mappings', path: '/mappings/company-departments' },
                    { label: 'Reporting Hierarchy' },
                ]}
                actions={
                    <Button variant="outline-secondary" onClick={() => navigate(-1)}>
                        <FaArrowLeft className="me-2" /> Back
                    </Button>
                }
            />

            {error && (
                <Alert variant="danger" className="mb-4">
                    {getErrorMessage(error)}
                </Alert>
            )}

            {/* Add/Edit Form */}
            <Card className="mb-4">
                <Card.Header>
                    <h5 className="mb-0">
                        <FaSitemap className="me-2" />
                        {editingId ? 'Edit Reporting Relation' : 'Add Reporting Relation'}
                    </h5>
                </Card.Header>
                <Card.Body>
                    <Row className="g-3 align-items-end">
                        <Col md={4}>
                            <Form.Group>
                                <Form.Label>
                                    <FaUserTie className="me-2" />
                                    Employee
                                </Form.Label>
                                <Form.Select
                                    value={selectedEmployee}
                                    onChange={(e) => setSelectedEmployee(e.target.value ? Number(e.target.value) : '')}
                                >
                                    <option value="">Select Employee</option>
                                    {employees.map((e) => (
                                        <option key={e.id} value={e.id}>
                                            {e.empName} ({e.empId}) - {e.empDesignation || 'N/A'}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={1} className="text-center">
                            <div className="mt-4">
                                <FaSitemap className="text-muted" size={24} />
                            </div>
                        </Col>
                        <Col md={4}>
                            <Form.Group>
                                <Form.Label>
                                    <FaUserTie className="me-2" />
                                    Reports To
                                </Form.Label>
                                <Form.Select
                                    value={selectedManager}
                                    onChange={(e) => setSelectedManager(e.target.value ? Number(e.target.value) : '')}
                                >
                                    <option value="">Select Manager</option>
                                    {employees
                                        .filter(e => e.id !== selectedEmployee)
                                        .map((e) => (
                                            <option key={e.id} value={e.id}>
                                                {e.empName} ({e.empId}) - {e.empDesignation || 'N/A'}
                                            </option>
                                        ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <div className="d-flex gap-2">
                                <Button
                                    variant="primary"
                                    onClick={handleSave}
                                    disabled={saveMutation.isPending || !selectedEmployee || !selectedManager}
                                    className="flex-grow-1"
                                >
                                    {saveMutation.isPending ? 'Saving...' : <><FaSave className="me-2" /> {editingId ? 'Update' : 'Add'}</>}
                                </Button>
                                {editingId && (
                                    <Button variant="outline-secondary" onClick={resetForm}>
                                        Cancel
                                    </Button>
                                )}
                            </div>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* Current Hierarchy */}
            <Card>
                <Card.Header className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Current Reporting Structure</h5>
                    <div className="d-flex align-items-center gap-2">
                        <Badge bg="primary">{filteredRelations.length} relations</Badge>
                        <div className="position-relative">
                            <FaSearch className="position-absolute top-50 translate-middle-y ms-2 text-muted" />
                            <Form.Control
                                type="text"
                                placeholder="Search..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                style={{ paddingLeft: '2rem', width: '200px' }}
                            />
                        </div>
                    </div>
                </Card.Header>
                <Card.Body className="p-0">
                    <div className="table-responsive">
                        <Table hover className="mb-0">
                            <thead className="bg-light">
                                <tr>
                                    <th>Employee</th>
                                    <th>Designation</th>
                                    <th>Reports To</th>
                                    <th>Manager Designation</th>
                                    <th className="text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredRelations.length > 0 ? (
                                    filteredRelations.map((relation) => {
                                        const emp = employees.find(e => e.id === relation.employeeId);
                                        const mgr = employees.find(e => e.id === relation.reportingToId);
                                        return (
                                            <tr key={relation.id}>
                                                <td>
                                                    <Badge bg="secondary" className="me-2">
                                                        {relation.employeeCode || emp?.empId}
                                                    </Badge>
                                                    <strong>{relation.employeeName || emp?.empName}</strong>
                                                </td>
                                                <td className="text-muted">{emp?.empDesignation || 'N/A'}</td>
                                                <td>
                                                    <Badge bg="info" className="me-2">
                                                        {relation.reportingToCode || mgr?.empId}
                                                    </Badge>
                                                    <strong>{relation.reportingToName || mgr?.empName}</strong>
                                                </td>
                                                <td className="text-muted">{mgr?.empDesignation || 'N/A'}</td>
                                                <td className="text-center">
                                                    <Button
                                                        variant="outline-primary"
                                                        size="sm"
                                                        className="me-2"
                                                        onClick={() => handleEdit(relation)}
                                                    >
                                                        <FaEdit />
                                                    </Button>
                                                    <Button
                                                        variant="outline-danger"
                                                        size="sm"
                                                        onClick={() => relation.id && handleDelete(relation.id)}
                                                        disabled={deleteMutation.isPending}
                                                    >
                                                        <FaTrash />
                                                    </Button>
                                                </td>
                                            </tr>
                                        );
                                    })
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="text-center text-muted py-4">
                                            {searchTerm ? 'No relations match your search' : 'No reporting relations configured yet'}
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </Table>
                    </div>
                </Card.Body>
            </Card>
        </div>
    );
};

export default ReportingHierarchyPage;
