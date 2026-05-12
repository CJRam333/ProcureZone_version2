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
    FaPlus,
    FaTrash,
    FaSearch,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import apiClient from '../../api/client';
import { getErrorMessage } from '../../api';

interface Company {
    id: number;
    code: string;
    name: string;
}

interface Location {
    id: number;
    code: string;
    name: string;
}

interface Material {
    id: number;
    materialCode: string;
    description: string;
}

interface CompanyLocationMaterial {
    id?: number;
    companyId: number;
    locationId: number;
    materialId: number;
    companyName?: string;
    locationName?: string;
    materialCode?: string;
    materialDescription?: string;
}

interface CompanyLocationMaterialPageProps {
    embedded?: boolean;
}

const CompanyLocationMaterialPage: React.FC<CompanyLocationMaterialPageProps> = ({ embedded = false }) => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [selectedCompany, setSelectedCompany] = useState<number | ''>('');
    const [selectedLocation, setSelectedLocation] = useState<number | ''>('');
    const [selectedMaterial, setSelectedMaterial] = useState<number | ''>('');
    const [searchTerm, setSearchTerm] = useState('');

    // Fetch companies
    const { data: companiesData, isLoading: loadingCompanies } = useQuery({
        queryKey: ['companies-all'],
        queryFn: async () => {
            const response = await apiClient.get('/companies');
            return response.data;
        },
    });

    // Fetch locations
    const { data: locationsData, isLoading: loadingLocations } = useQuery({
        queryKey: ['locations-all'],
        queryFn: async () => {
            const response = await apiClient.get('/locations');
            return response.data;
        },
    });

    // Fetch materials
    const { data: materialsData, isLoading: loadingMaterials } = useQuery({
        queryKey: ['materials-all'],
        queryFn: async () => {
            const response = await apiClient.get('/materials');
            return response.data;
        },
    });

    // Fetch existing mappings
    const { data: mappingsData, isLoading: loadingMappings, error } = useQuery({
        queryKey: ['company-location-materials'],
        queryFn: async () => {
            const response = await apiClient.get('/company-location-materials', {
                params: { page: 0, size: 500 }
            });
            return response.data;
        },
    });

    const companies: Company[] = companiesData?.content || companiesData || [];
    const locations: Location[] = locationsData?.content || locationsData || [];
    const materials: Material[] = materialsData?.content || materialsData || [];
    const mappings: CompanyLocationMaterial[] = mappingsData?.content || mappingsData || [];

    // Filter mappings based on search
    const filteredMappings = useMemo(() => {
        if (!searchTerm) return mappings;
        const lower = searchTerm.toLowerCase();
        return mappings.filter(m =>
            m.companyName?.toLowerCase().includes(lower) ||
            m.locationName?.toLowerCase().includes(lower) ||
            m.materialCode?.toLowerCase().includes(lower) ||
            m.materialDescription?.toLowerCase().includes(lower)
        );
    }, [mappings, searchTerm]);

    // Add mapping mutation
    const addMutation = useMutation({
        mutationFn: async (data: { companyId: number; locationId: number; materialId: number }) => {
            const response = await apiClient.post('/company-location-materials', data);
            return response.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['company-location-materials'] });
            toast.success('Material mapping added successfully!');
            setSelectedMaterial('');
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    // Delete mapping mutation
    const deleteMutation = useMutation({
        mutationFn: async (id: number) => {
            await apiClient.delete(`/company-location-materials/${id}`);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['company-location-materials'] });
            toast.success('Material mapping removed!');
        },
        onError: (error) => {
            toast.error(getErrorMessage(error));
        },
    });

    const handleAddMapping = () => {
        if (!selectedCompany || !selectedLocation || !selectedMaterial) {
            toast.error('Please select company, location, and material');
            return;
        }

        // Check if mapping already exists
        const exists = mappings.some(m =>
            m.companyId === selectedCompany &&
            m.locationId === selectedLocation &&
            m.materialId === selectedMaterial
        );

        if (exists) {
            toast.error('This mapping already exists');
            return;
        }

        addMutation.mutate({
            companyId: Number(selectedCompany),
            locationId: Number(selectedLocation),
            materialId: Number(selectedMaterial),
        });
    };

    const handleDeleteMapping = (id: number) => {
        if (window.confirm('Are you sure you want to remove this material mapping?')) {
            deleteMutation.mutate(id);
        }
    };

    const isLoading = loadingCompanies || loadingLocations || loadingMaterials || loadingMappings;

    if (isLoading && !mappings.length) {
        return <LoadingSpinner fullPage text="Loading data..." />;
    }

    return (
        <div>
            {!embedded && (
                <PageHeader
                    title="Company-Location-Material Mapping"
                    subtitle="Define which materials are available at which company locations"
                    breadcrumbs={[
                        { label: 'Dashboard', path: '/dashboard' },
                        { label: 'Masters', path: '/masters' },
                        { label: 'Locations', path: '/masters/locations' },
                        { label: 'Material Mapping' },
                    ]}
                    actions={
                        <Button variant="outline-secondary" onClick={() => navigate(-1)}>
                            <FaArrowLeft className="me-2" /> Back
                        </Button>
                    }
                />
            )}

            {error && (
                <Alert variant="danger" className="mb-4">
                    {getErrorMessage(error)}
                </Alert>
            )}

            {/* Add New Mapping */}
            <Card className="mb-4">
                <Card.Header>
                    <h5 className="mb-0">
                        <FaPlus className="me-2" />
                        Add Material to Location
                    </h5>
                </Card.Header>
                <Card.Body>
                    <Row className="g-3 align-items-end">
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label>Company</Form.Label>
                                <Form.Select
                                    value={selectedCompany}
                                    onChange={(e) => setSelectedCompany(e.target.value ? Number(e.target.value) : '')}
                                >
                                    <option value="">Select Company</option>
                                    {companies.map((c) => (
                                        <option key={c.id} value={c.id}>{c.name}</option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={3}>
                            <Form.Group>
                                <Form.Label>Location</Form.Label>
                                <Form.Select
                                    value={selectedLocation}
                                    onChange={(e) => setSelectedLocation(e.target.value ? Number(e.target.value) : '')}
                                >
                                    <option value="">Select Location</option>
                                    {locations.map((l) => (
                                        <option key={l.id} value={l.id}>{l.name}</option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={4}>
                            <Form.Group>
                                <Form.Label>Material</Form.Label>
                                <Form.Select
                                    value={selectedMaterial}
                                    onChange={(e) => setSelectedMaterial(e.target.value ? Number(e.target.value) : '')}
                                >
                                    <option value="">Select Material</option>
                                    {materials.map((m) => (
                                        <option key={m.id} value={m.id}>
                                            {m.materialCode} - {m.description}
                                        </option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        </Col>
                        <Col md={2}>
                            <Button
                                variant="primary"
                                onClick={handleAddMapping}
                                disabled={addMutation.isPending || !selectedCompany || !selectedLocation || !selectedMaterial}
                                className="w-100"
                            >
                                {addMutation.isPending ? 'Adding...' : <><FaSave className="me-2" /> Add</>}
                            </Button>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            {/* Current Mappings */}
            <Card>
                <Card.Header className="d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Current Mappings</h5>
                    <div className="d-flex align-items-center gap-2">
                        <Badge bg="primary">{filteredMappings.length} mappings</Badge>
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
                                    <th>Company</th>
                                    <th>Location</th>
                                    <th>Material Code</th>
                                    <th>Description</th>
                                    <th className="text-center">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredMappings.length > 0 ? (
                                    filteredMappings.map((mapping) => (
                                        <tr key={mapping.id}>
                                            <td>
                                                <Badge bg="secondary" className="me-2">
                                                    {companies.find(c => c.id === mapping.companyId)?.code || ''}
                                                </Badge>
                                                {mapping.companyName || companies.find(c => c.id === mapping.companyId)?.name}
                                            </td>
                                            <td>{mapping.locationName || locations.find(l => l.id === mapping.locationId)?.name}</td>
                                            <td>
                                                <code className="text-primary">
                                                    {mapping.materialCode || materials.find(m => m.id === mapping.materialId)?.materialCode}
                                                </code>
                                            </td>
                                            <td>{mapping.materialDescription || materials.find(m => m.id === mapping.materialId)?.description}</td>
                                            <td className="text-center">
                                                <Button
                                                    variant="outline-danger"
                                                    size="sm"
                                                    onClick={() => mapping.id && handleDeleteMapping(mapping.id)}
                                                    disabled={deleteMutation.isPending}
                                                >
                                                    <FaTrash />
                                                </Button>
                                            </td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan={5} className="text-center text-muted py-4">
                                            {searchTerm ? 'No mappings match your search' : 'No mappings configured yet'}
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

export default CompanyLocationMaterialPage;
