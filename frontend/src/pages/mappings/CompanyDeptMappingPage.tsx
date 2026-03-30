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
    FaBuilding,
    FaSitemap,
    FaCheck,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { getErrorMessage } from '../../api';
import apiClient from '../../api/client';

interface Company {
    id: number;
    code: string;
    name: string;
    status: number;
}

interface Department {
    id: number;
    code: string;
    name: string;
    status: number;
}

interface CompanyDeptMapping {
    companyId: number;
    departmentId: number;
    active: boolean;
}

interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
}

const CompanyDeptMappingPage: React.FC = () => {
    const queryClient = useQueryClient();

    const [selectedCompany, setSelectedCompany] = useState<number | null>(null);
    const [mappings, setMappings] = useState<Record<number, boolean>>({});
    const [searchTerm, setSearchTerm] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    // Fetch companies
    const { data: companiesData, isLoading: loadingCompanies } = useQuery({
        queryKey: ['companies-all'],
        queryFn: async () => {
            const response = await apiClient.get<PageResponse<Company>>('/companies', {
                params: { page: 0, size: 100 }
            });
            return response.data;
        },
    });

    // Fetch departments
    const { data: departmentsData, isLoading: loadingDepartments } = useQuery({
        queryKey: ['departments-all'],
        queryFn: async () => {
            const response = await apiClient.get<PageResponse<Department>>('/departments', {
                params: { page: 0, size: 100 }
            });
            return response.data;
        },
    });

    // Fetch current mappings for selected company
    const { data: currentMappings, isLoading: loadingMappings } = useQuery({
        queryKey: ['company-dept-mappings', selectedCompany],
        queryFn: async () => {
            if (!selectedCompany) return [];
            const response = await apiClient.get(`/company-departments/company/${selectedCompany}`);
            return response.data;
        },
        enabled: !!selectedCompany,
    });

    // Update mappings state when data loads
    useEffect(() => {
        if (currentMappings && Array.isArray(currentMappings)) {
            const newMappings: Record<number, boolean> = {};
            currentMappings.forEach((m: CompanyDeptMapping) => {
                newMappings[m.departmentId] = m.active;
            });
            setMappings(newMappings);
        }
    }, [currentMappings]);

    // Save mutation
    const saveMutation = useMutation({
        mutationFn: async () => {
            if (!selectedCompany) throw new Error('Please select a company');

            const mappingsList = Object.entries(mappings)
                .filter(([_, active]) => active)
                .map(([deptId]) => parseInt(deptId));

            await apiClient.post(`/company-departments/company/${selectedCompany}/batch`, {
                departmentIds: mappingsList,
            });
        },
        onSuccess: () => {
            setSuccess('Mappings saved successfully!');
            queryClient.invalidateQueries({ queryKey: ['company-dept-mappings'] });
            setTimeout(() => setSuccess(null), 3000);
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    const companies: Company[] = companiesData?.content || [];
    const departments: Department[] = departmentsData?.content || [];

    const filteredDepartments = departments.filter(
        (dept) =>
            dept.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            dept.code.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleToggle = (deptId: number) => {
        setMappings((prev) => ({
            ...prev,
            [deptId]: !prev[deptId],
        }));
    };

    const handleSelectAll = () => {
        const newMappings: Record<number, boolean> = {};
        filteredDepartments.forEach((dept) => {
            newMappings[dept.id] = true;
        });
        setMappings((prev) => ({ ...prev, ...newMappings }));
    };

    const handleDeselectAll = () => {
        const newMappings: Record<number, boolean> = {};
        filteredDepartments.forEach((dept) => {
            newMappings[dept.id] = false;
        });
        setMappings((prev) => ({ ...prev, ...newMappings }));
    };

    const selectedCount = Object.values(mappings).filter(Boolean).length;

    if (loadingCompanies || loadingDepartments) {
        return <LoadingSpinner fullPage text="Loading data..." />;
    }

    return (
        <div>
            <PageHeader
                title="Company-Department Mapping"
                subtitle="Assign departments to companies"
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Masters', path: '/masters/companies' },
                    { label: 'Company-Dept Mapping' },
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
                {/* Company Selection */}
                <Col lg={4}>
                    <Card className="h-100">
                        <Card.Header>
                            <h5 className="mb-0">
                                <FaBuilding className="me-2" />
                                Select Company
                            </h5>
                        </Card.Header>
                        <Card.Body>
                            <Form.Select
                                size="lg"
                                value={selectedCompany || ''}
                                onChange={(e) => {
                                    setSelectedCompany(e.target.value ? parseInt(e.target.value) : null);
                                    setMappings({});
                                }}
                            >
                                <option value="">-- Select a Company --</option>
                                {companies.map((company) => (
                                    <option key={company.id} value={company.id}>
                                        {company.code} - {company.name}
                                    </option>
                                ))}
                            </Form.Select>

                            {selectedCompany && (
                                <div className="mt-4">
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        <span className="text-muted">Selected Departments:</span>
                                        <Badge bg="primary" className="fs-6">
                                            {selectedCount}
                                        </Badge>
                                    </div>
                                    <Button
                                        variant="success"
                                        className="w-100"
                                        onClick={() => saveMutation.mutate()}
                                        disabled={saveMutation.isPending}
                                    >
                                        {saveMutation.isPending ? (
                                            <Spinner as="span" animation="border" size="sm" className="me-2" />
                                        ) : (
                                            <FaSave className="me-2" />
                                        )}
                                        Save Mappings
                                    </Button>
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                {/* Department Selection */}
                <Col lg={8}>
                    <Card>
                        <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">
                                <FaSitemap className="me-2" />
                                Departments
                            </h5>
                            {selectedCompany && (
                                <div className="d-flex gap-2">
                                    <Button size="sm" variant="outline-success" onClick={handleSelectAll}>
                                        Select All
                                    </Button>
                                    <Button size="sm" variant="outline-secondary" onClick={handleDeselectAll}>
                                        Deselect All
                                    </Button>
                                </div>
                            )}
                        </Card.Header>
                        <Card.Body>
                            {!selectedCompany ? (
                                <div className="text-center py-5 text-muted">
                                    <FaBuilding size={48} className="mb-3" />
                                    <h5>Select a Company</h5>
                                    <p>Choose a company from the left panel to manage its department mappings.</p>
                                </div>
                            ) : loadingMappings ? (
                                <LoadingSpinner text="Loading mappings..." />
                            ) : (
                                <>
                                    <InputGroup className="mb-3">
                                        <InputGroup.Text>
                                            <FaSearch />
                                        </InputGroup.Text>
                                        <Form.Control
                                            type="text"
                                            placeholder="Search departments..."
                                            value={searchTerm}
                                            onChange={(e) => setSearchTerm(e.target.value)}
                                        />
                                    </InputGroup>

                                    <div className="table-responsive" style={{ maxHeight: 400, overflowY: 'auto' }}>
                                        <Table hover className="mb-0">
                                            <thead className="bg-light sticky-top">
                                                <tr>
                                                    <th style={{ width: 50 }}>Select</th>
                                                    <th>Code</th>
                                                    <th>Department Name</th>
                                                    <th>Status</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {filteredDepartments.map((dept) => (
                                                    <tr
                                                        key={dept.id}
                                                        onClick={() => handleToggle(dept.id)}
                                                        style={{ cursor: 'pointer' }}
                                                        className={mappings[dept.id] ? 'table-success' : ''}
                                                    >
                                                        <td className="text-center">
                                                            <Form.Check
                                                                type="checkbox"
                                                                checked={!!mappings[dept.id]}
                                                                onChange={() => handleToggle(dept.id)}
                                                                onClick={(e) => e.stopPropagation()}
                                                            />
                                                        </td>
                                                        <td>
                                                            <code>{dept.code}</code>
                                                        </td>
                                                        <td>
                                                            {mappings[dept.id] && (
                                                                <FaCheck className="text-success me-2" />
                                                            )}
                                                            {dept.name}
                                                        </td>
                                                        <td>
                                                            <Badge bg={dept.status === 1 ? 'success' : 'secondary'}>
                                                                {dept.status === 1 ? 'ACTIVE' : 'INACTIVE'}
                                                            </Badge>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </Table>
                                    </div>

                                    {filteredDepartments.length === 0 && (
                                        <div className="text-center py-4 text-muted">
                                            No departments found matching "{searchTerm}"
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

export default CompanyDeptMappingPage;
