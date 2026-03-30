import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    Card,
    Row,
    Col,
    Button,
    Badge,
    Alert,
    Tab,
    Tabs,
    Table,
} from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
    FaArrowLeft,
    FaEdit,
    FaPrint,
    FaUser,
    FaPhone,
    FaEnvelope,
    FaSitemap,
    FaHistory,
    FaUserTag,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { employeesApi, getErrorMessage } from '../../api';
import apiClient from '../../api/client';
import { useAuth } from '../../contexts/AuthContext';
import type { Employee } from '../../api/employees';

interface Role {
    id: number;
    code: string;
    name: string;
    description?: string;
}

const EmployeeDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { hasAnyRole } = useAuth();

    // Fetch employee details
    const { data: employee, isLoading, error: fetchError } = useQuery({
        queryKey: ['employee', id],
        queryFn: () => employeesApi.getById(Number(id)),
        enabled: !!id,
    });

    // Fetch employee roles
    const { data: employeeRoles } = useQuery<Role[]>({
        queryKey: ['employee-roles', id],
        queryFn: async () => {
            try {
                const response = await apiClient.get(`/employee-roles/employee/${id}`);
                return response.data;
            } catch {
                return [];
            }
        },
        enabled: !!id,
    });

    const canEdit = hasAnyRole(['ADMIN', 'SUPERADMIN']);

    if (isLoading) {
        return <LoadingSpinner fullPage text="Loading employee details..." />;
    }

    if (fetchError || !employee) {
        return (
            <div className="text-center py-5">
                <Alert variant="danger">
                    {fetchError ? getErrorMessage(fetchError) : 'Employee not found'}
                </Alert>
                <Button variant="primary" onClick={() => navigate('/masters/employees')}>
                    Back to Employees
                </Button>
            </div>
        );
    }

    return (
        <div>
            <PageHeader
                title={employee.empName}
                subtitle={`Employee Code: ${employee.empId}`}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Employees', path: '/masters/employees' },
                    { label: employee.empId },
                ]}
                actions={
                    <div className="d-flex flex-wrap gap-2">
                        <Button variant="outline-secondary" onClick={() => navigate('/masters/employees')}>
                            <FaArrowLeft className="me-2" /> Back
                        </Button>
                        <Button variant="outline-secondary" onClick={() => window.print()}>
                            <FaPrint className="me-2" /> Print
                        </Button>
                        {canEdit && (
                            <Button variant="primary" onClick={() => navigate(`/masters/employees/${id}/edit`)}>
                                <FaEdit className="me-2" /> Edit
                            </Button>
                        )}
                    </div>
                }
            />

            {/* Status Banner */}
            <Card className="mb-4">
                <Card.Body className="d-flex flex-wrap justify-content-between align-items-center gap-3">
                    <div className="d-flex flex-wrap align-items-center gap-4">
                        <div>
                            <small className="text-muted d-block">Status</small>
                            <Badge bg={employee.empStatus === 1 ? 'success' : 'secondary'} className="fs-6">
                                {employee.empStatus === 1 ? 'ACTIVE' : 'INACTIVE'}
                            </Badge>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Department</small>
                            <strong>{employee.departmentName || 'N/A'}</strong>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Designation</small>
                            <strong>{employee.empDesignation || 'N/A'}</strong>
                        </div>
                        <div className="vr d-none d-sm-block" />
                        <div>
                            <small className="text-muted d-block">Location</small>
                            <strong>{employee.locationName || employee.plantName || 'N/A'}</strong>
                        </div>
                    </div>
                </Card.Body>
            </Card>

            <Tabs defaultActiveKey="details" className="mb-4">
                {/* Details Tab */}
                <Tab eventKey="details" title={<><FaUser className="me-2" />Details</>}>
                    <Row className="g-4">
                        {/* Personal Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaUser className="me-2" />
                                        Personal Information
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Employee Code</small>
                                                <strong className="text-primary fs-5">{employee.empId}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Full Name</small>
                                                <strong>{employee.empName}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">
                                                    <FaEnvelope className="me-1" /> Email
                                                </small>
                                                <strong>{employee.empEmail || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Date of Joining</small>
                                                <strong>
                                                    {employee.empJoinDate
                                                        ? format(new Date(employee.empJoinDate), 'PPP')
                                                        : 'N/A'}
                                                </strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Cost Center</small>
                                                <strong>{employee.empCostCenter || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>

                        {/* Organization Info */}
                        <Col lg={6}>
                            <Card className="h-100">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaSitemap className="me-2" />
                                        Organization
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Department</small>
                                                <strong>{employee.departmentName || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Designation</small>
                                                <strong>{employee.empDesignation || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Company</small>
                                                <strong>{employee.companyName || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                        <Col sm={6}>
                                            <div className="mb-3">
                                                <small className="text-muted d-block">Location</small>
                                                <strong>{employee.locationName || employee.plantName || 'N/A'}</strong>
                                            </div>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </Tab>

                {/* Roles Tab */}
                <Tab eventKey="roles" title={<><FaUserTag className="me-2" />Roles</>}>
                    <Card>
                        <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">Assigned Roles</h5>
                            {canEdit && (
                                <Button
                                    size="sm"
                                    variant="outline-primary"
                                    onClick={() => navigate('/mappings/employee-roles')}
                                >
                                    Manage Roles
                                </Button>
                            )}
                        </Card.Header>
                        <Card.Body className="p-0">
                            <div className="table-responsive">
                                <Table hover className="mb-0">
                                    <thead className="bg-light">
                                        <tr>
                                            <th>Role Code</th>
                                            <th>Role Name</th>
                                            <th>Description</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {employeeRoles && employeeRoles.length > 0 ? (
                                            employeeRoles.map((role) => (
                                                <tr key={role.id}>
                                                    <td>
                                                        <Badge bg="secondary">{role.code}</Badge>
                                                    </td>
                                                    <td><strong>{role.name}</strong></td>
                                                    <td className="text-muted">{role.description || '-'}</td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan={3} className="text-center text-muted py-4">
                                                    No roles assigned to this employee
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </Table>
                            </div>
                        </Card.Body>
                    </Card>
                </Tab>

                {/* History Tab */}
                <Tab eventKey="history" title={<><FaHistory className="me-2" />History</>}>
                    <Card>
                        <Card.Body>
                            <div className="timeline">
                                <div className="timeline-item">
                                    <div className="timeline-marker bg-success"></div>
                                    <div className="timeline-content">
                                        <strong>Employee Record Created</strong>
                                        <p className="text-muted mb-0">
                                            Added on {employee.createdAt ? format(new Date(employee.createdAt), 'PPpp') : 'N/A'}
                                        </p>
                                    </div>
                                </div>
                                {employee.empJoinDate && (
                                    <div className="timeline-item">
                                        <div className="timeline-marker bg-primary"></div>
                                        <div className="timeline-content">
                                            <strong>Joined Organization</strong>
                                            <p className="text-muted mb-0">
                                                On {format(new Date(employee.empJoinDate), 'PPP')}
                                            </p>
                                        </div>
                                    </div>
                                )}
                                {employee.updatedAt && employee.updatedAt !== employee.createdAt && (
                                    <div className="timeline-item">
                                        <div className="timeline-marker bg-info"></div>
                                        <div className="timeline-content">
                                            <strong>Last Updated</strong>
                                            <p className="text-muted mb-0">
                                                Modified on {format(new Date(employee.updatedAt), 'PPpp')}
                                            </p>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </Card.Body>
                    </Card>
                </Tab>
            </Tabs>

            {/* Timeline Styles */}
            <style>{`
        .timeline { position: relative; padding-left: 30px; }
        .timeline::before { content: ''; position: absolute; left: 8px; top: 0; bottom: 0; width: 2px; background: #dee2e6; }
        .timeline-item { position: relative; padding-bottom: 20px; }
        .timeline-marker { position: absolute; left: -26px; width: 14px; height: 14px; border-radius: 50%; border: 2px solid #fff; }
        .timeline-content { padding-left: 10px; }
      `}</style>
        </div>
    );
};

export default EmployeeDetailPage;
