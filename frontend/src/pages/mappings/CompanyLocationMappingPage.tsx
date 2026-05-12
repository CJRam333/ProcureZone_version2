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
    FaMapMarkerAlt,
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

interface Location {
    id: number;
    code: string;
    name: string;
    status: number;
}

interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
}

interface CompanyLocationMappingPageProps {
    embedded?: boolean;
}

const CompanyLocationMappingPage: React.FC<CompanyLocationMappingPageProps> = ({ embedded = false }) => {
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

    // Fetch locations
    const { data: locationsData, isLoading: loadingLocations } = useQuery({
        queryKey: ['locations-all'],
        queryFn: async () => {
            const response = await apiClient.get<PageResponse<Location>>('/locations', {
                params: { page: 0, size: 100 }
            });
            return response.data;
        },
    });

    // Fetch current mappings for selected company
    const { data: currentMappings, isLoading: loadingMappings } = useQuery({
        queryKey: ['company-location-mappings', selectedCompany],
        queryFn: async () => {
            if (!selectedCompany) return [];
            const response = await apiClient.get(`/company-locations/company/${selectedCompany}/locations?size=1000`);
            return response.data?.content || [];
        },
        enabled: !!selectedCompany,
    });

    // Update mappings state when data loads
    useEffect(() => {
        if (currentMappings && Array.isArray(currentMappings)) {
            const newMappings: Record<number, boolean> = {};
            currentMappings.forEach((m: { locationId: number; active?: boolean }) => {
                newMappings[m.locationId] = m.active !== false;
            });
            setMappings(newMappings);
        }
    }, [currentMappings]);

    // Save mutation
    const saveMutation = useMutation({
        mutationFn: async () => {
            if (!selectedCompany) throw new Error('Please select a company');

            const locationIds = Object.entries(mappings)
                .filter(([_, active]) => active)
                .map(([locId]) => parseInt(locId));

            await apiClient.post(`/company-locations/company/${selectedCompany}/batch`, {
                locationIds,
            });
        },
        onSuccess: () => {
            setSuccess('Mappings saved successfully!');
            queryClient.invalidateQueries({ queryKey: ['company-location-mappings'] });
            setTimeout(() => setSuccess(null), 3000);
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    const companies: Company[] = companiesData?.content || [];
    const locations: Location[] = locationsData?.content || [];

    const filteredLocations = locations.filter(
        (loc) =>
            loc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            loc.code.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleToggle = (locId: number) => {
        setMappings((prev) => ({
            ...prev,
            [locId]: !prev[locId],
        }));
    };

    const handleSelectAll = () => {
        const newMappings: Record<number, boolean> = {};
        filteredLocations.forEach((loc) => {
            newMappings[loc.id] = true;
        });
        setMappings((prev) => ({ ...prev, ...newMappings }));
    };

    const handleDeselectAll = () => {
        const newMappings: Record<number, boolean> = {};
        filteredLocations.forEach((loc) => {
            newMappings[loc.id] = false;
        });
        setMappings((prev) => ({ ...prev, ...newMappings }));
    };

    const selectedCount = Object.values(mappings).filter(Boolean).length;

    if (loadingCompanies || loadingLocations) {
        return <LoadingSpinner fullPage text="Loading data..." />;
    }

    return (
        <div>
            {!embedded && (
                <PageHeader
                    title="Company-Location Mapping"
                    subtitle="Assign locations to companies"
                    breadcrumbs={[
                        { label: 'Dashboard', path: '/dashboard' },
                        { label: 'Masters', path: '/masters' },
                        { label: 'Companies', path: '/masters/companies' },
                        { label: 'Location Mapping' },
                    ]}
                />
            )}

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
                                        <span className="text-muted">Selected Locations:</span>
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

                {/* Location Selection */}
                <Col lg={8}>
                    <Card>
                        <Card.Header className="d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">
                                <FaMapMarkerAlt className="me-2" />
                                Locations
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
                                    <p>Choose a company from the left panel to manage its location mappings.</p>
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
                                            placeholder="Search locations..."
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
                                                    <th>Location Name</th>
                                                    <th>Status</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {filteredLocations.map((loc) => (
                                                    <tr
                                                        key={loc.id}
                                                        onClick={() => handleToggle(loc.id)}
                                                        style={{ cursor: 'pointer' }}
                                                        className={mappings[loc.id] ? 'table-success' : ''}
                                                    >
                                                        <td className="text-center">
                                                            <Form.Check
                                                                type="checkbox"
                                                                checked={!!mappings[loc.id]}
                                                                onChange={() => handleToggle(loc.id)}
                                                                onClick={(e) => e.stopPropagation()}
                                                            />
                                                        </td>
                                                        <td>
                                                            <code>{loc.code}</code>
                                                        </td>
                                                        <td>
                                                            {mappings[loc.id] && (
                                                                <FaCheck className="text-success me-2" />
                                                            )}
                                                            {loc.name}
                                                        </td>
                                                        <td>
                                                            <Badge bg={loc.status === 1 ? 'success' : 'secondary'}>
                                                                {loc.status === 1 ? 'ACTIVE' : 'INACTIVE'}
                                                            </Badge>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </Table>
                                    </div>

                                    {filteredLocations.length === 0 && (
                                        <div className="text-center py-4 text-muted">
                                            No locations found matching "{searchTerm}"
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

export default CompanyLocationMappingPage;
