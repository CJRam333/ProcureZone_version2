import React, { useState } from 'react';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaSearch, FaIndustry, FaPlus, FaEdit, FaTrash, FaSave, FaTimes,
  FaBoxes, FaLink
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, ConfirmDialog, Column } from '../../components/common';
import { plantsApi, materialsApi, getErrorMessage } from '../../api';
import apiClient from '../../api/client';
import { toast } from 'react-toastify';

interface PlantMaterialMapping {
  id: number;
  plantId: number;
  plantCode: string;
  plantName: string;
  materialId: number;
  materialCode: string;
  materialDescription: string;
  minQuantity?: number;
  maxQuantity?: number;
  reorderLevel?: number;
  leadTimeDays?: number;
  isActive: boolean;
  createdAt: string;
}

// API functions
const plantMaterialApi = {
  list: async (params: { plantId?: number; materialId?: number; search?: string; page?: number; size?: number }) => {
    const response = await apiClient.get('/plant-materials', { params });
    return response.data;
  },
  create: async (data: Partial<PlantMaterialMapping>) => {
    const response = await apiClient.post('/plant-materials', data);
    return response.data;
  },
  update: async (id: number, data: Partial<PlantMaterialMapping>) => {
    const response = await apiClient.put(`/plant-materials/${id}`, data);
    return response.data;
  },
  delete: async (id: number) => {
    const response = await apiClient.delete(`/plant-materials/${id}`);
    return response.data;
  },
};

interface CompanyPlantMaterialPageProps {
  embedded?: boolean;
}

const CompanyPlantMaterialPage: React.FC<CompanyPlantMaterialPageProps> = ({ embedded = false }) => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [plantFilter, setPlantFilter] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedMapping, setSelectedMapping] = useState<PlantMaterialMapping | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [mappingToDelete, setMappingToDelete] = useState<PlantMaterialMapping | null>(null);

  // Material search
  const [materialSearch, setMaterialSearch] = useState('');
  const [showMaterialDropdown, setShowMaterialDropdown] = useState(false);

  // Form state
  const [formData, setFormData] = useState({
    plantId: 0,
    materialId: 0,
    materialCode: '',
    materialDescription: '',
    minQuantity: 0,
    maxQuantity: 0,
    reorderLevel: 0,
    leadTimeDays: 7,
    isActive: true,
  });

  // Fetch plants
  const { data: plantsData } = useQuery({
    queryKey: ['plants-active'],
    queryFn: () => plantsApi.getActive(0, 100),
  });

  // Fetch materials for search — load on focus/open OR when search text changes
  const { data: materialsData } = useQuery({
    queryKey: ['materials-search', materialSearch],
    queryFn: () => materialsApi.list({ search: materialSearch || undefined, size: 50, isActive: true }),
    enabled: showMaterialDropdown,
  });

  // Fetch mappings
  const { data: mappingsData, isLoading, error } = useQuery({
    queryKey: ['plant-materials', searchTerm, plantFilter, currentPage],
    queryFn: () => plantMaterialApi.list({
      search: searchTerm || undefined,
      plantId: plantFilter ? Number(plantFilter) : undefined,
      page: currentPage,
      size: pageSize,
    }),
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: (data: Partial<PlantMaterialMapping>) => plantMaterialApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plant-materials'] });
      toast.success('Mapping created successfully');
      closeModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<PlantMaterialMapping> }) => plantMaterialApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plant-materials'] });
      toast.success('Mapping updated successfully');
      closeModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => plantMaterialApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plant-materials'] });
      toast.success('Mapping deleted successfully');
      setShowDeleteConfirm(false);
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const openAddModal = () => {
    setIsEditing(false);
    setSelectedMapping(null);
    setFormData({
      plantId: 0,
      materialId: 0,
      materialCode: '',
      materialDescription: '',
      minQuantity: 0,
      maxQuantity: 0,
      reorderLevel: 0,
      leadTimeDays: 7,
      isActive: true,
    });
    setMaterialSearch('');
    setShowModal(true);
  };

  const openEditModal = (mapping: PlantMaterialMapping) => {
    setIsEditing(true);
    setSelectedMapping(mapping);
    setFormData({
      plantId: mapping.plantId,
      materialId: mapping.materialId,
      materialCode: mapping.materialCode,
      materialDescription: mapping.materialDescription,
      minQuantity: mapping.minQuantity || 0,
      maxQuantity: mapping.maxQuantity || 0,
      reorderLevel: mapping.reorderLevel || 0,
      leadTimeDays: mapping.leadTimeDays || 7,
      isActive: mapping.isActive,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setIsEditing(false);
    setSelectedMapping(null);
    setShowMaterialDropdown(false);
  };

  const handleMaterialSelect = (material: { id: number; code: string; name: string }) => {
    setFormData({
      ...formData,
      materialId: material.id,
      materialCode: material.code,
      materialDescription: material.name,
    });
    setMaterialSearch('');
    setShowMaterialDropdown(false);
  };

  const handleSubmit = () => {
    if (!formData.plantId || !formData.materialId) {
      toast.error('Please select both plant and material');
      return;
    }

    const selectedPlant = plantsData?.content?.find(
      (p: { id: number; companyId?: number }) => p.id === formData.plantId
    );

    const payload = {
      companyId: selectedPlant?.companyId || 1, // companyId required by backend
      plantId: formData.plantId,
      materialId: formData.materialId,
      quantity: formData.maxQuantity ? String(formData.maxQuantity) : undefined,
      reorderLevel: formData.reorderLevel ? String(formData.reorderLevel) : undefined,
      maxLevel: formData.maxQuantity ? String(formData.maxQuantity) : undefined,
      status: formData.isActive ? 1 : 0,
    };

    if (isEditing && selectedMapping) {
      updateMutation.mutate({ id: selectedMapping.id, data: payload });
    } else {
      createMutation.mutate(payload);
    }
  };

  const columns = [
    {
      key: 'plantName',
      label: 'Plant',
      render: (row: PlantMaterialMapping) => (
        <div>
          <FaIndustry className="me-1 text-primary" />
          <strong>{row.plantCode}</strong>
          <div className="text-muted small">{row.plantName}</div>
        </div>
      ),
    },
    {
      key: 'materialCode',
      label: 'Material',
      render: (row: PlantMaterialMapping) => (
        <div>
          <FaBoxes className="me-1 text-info" />
          <strong>{row.materialCode}</strong>
          <div className="text-muted small">{row.materialDescription}</div>
        </div>
      ),
    },
    {
      key: 'minQuantity',
      label: 'Min Qty',
    },
    {
      key: 'maxQuantity',
      label: 'Max Qty',
    },
    {
      key: 'reorderLevel',
      label: 'Reorder Level',
    },
    {
      key: 'leadTimeDays',
      label: 'Lead Time',
      render: (row: PlantMaterialMapping) => `${row.leadTimeDays || 0} days`,
    },
    {
      key: 'isActive',
      label: 'Status',
      render: (row: PlantMaterialMapping) => (
        <Badge bg={row.isActive ? 'success' : 'danger'}>
          {row.isActive ? 'Active' : 'Inactive'}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: PlantMaterialMapping) => (
        <div className="d-flex gap-1">
          <Button variant="outline-primary" size="sm" onClick={() => openEditModal(row)}>
            <FaEdit />
          </Button>
          <Button variant="outline-danger" size="sm" onClick={() => { setMappingToDelete(row); setShowDeleteConfirm(true); }}>
            <FaTrash />
          </Button>
        </div>
      ),
    },
  ];

  if (error) {
    return <div className="alert alert-danger">Error loading mappings: {getErrorMessage(error)}</div>;
  }

  return (
    <div className="company-plant-material">
      {!embedded && (
        <PageHeader
          title="Plant-Material Mapping"
          subtitle="Configure material availability and stock levels for each plant"
          breadcrumbs={[
            { label: 'Dashboard', path: '/dashboard' },
            { label: 'Masters', path: '/masters' },
            { label: 'Plants', path: '/masters/plants' },
            { label: 'Material Mapping' },
          ]}
          actions={
            <Button variant="primary" onClick={openAddModal}>
              <FaPlus className="me-1" /> Add Mapping
            </Button>
          }
        />
      )}
      {embedded && (
        <div className="d-flex justify-content-end mb-3">
          <Button variant="primary" onClick={openAddModal}>
            <FaPlus className="me-1" /> Add Mapping
          </Button>
        </div>
      )}

      {/* Summary Cards */}
      <Row className="mb-3">
        <Col md={4}>
          <Card className="bg-primary bg-opacity-10 border-primary">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-primary">{mappingsData?.totalElements || 0}</h4>
              <div className="text-muted small">Total Mappings</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-success bg-opacity-10 border-success">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-success">{plantsData?.content?.length || 0}</h4>
              <div className="text-muted small">Plants</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-info bg-opacity-10 border-info">
            <Card.Body className="text-center py-3">
              <h4 className="mb-1 text-info">{mappingsData?.content?.length || 0}</h4>
              <div className="text-muted small">Materials Mapped</div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={4}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by material code or name..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Select value={plantFilter} onChange={(e) => setPlantFilter(e.target.value)}>
                <option value="">All Plants</option>
                {plantsData?.content?.map((plant: { id: number; code: string; name: string }) => (
                  <option key={plant.id} value={plant.id}>{plant.code} - {plant.name}</option>
                ))}
              </Form.Select>
            </Col>
            <Col md={2}>
              <Button variant="outline-secondary" className="w-100" onClick={() => { setSearchTerm(''); setPlantFilter(''); }}>
                Clear
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading mappings..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={mappingsData?.content || []}
          keyField="id"
          totalItems={mappingsData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {/* Add/Edit Modal */}
      <Modal show={showModal} onHide={closeModal} size="lg">
        <Modal.Header closeButton className="bg-primary text-white">
          <Modal.Title>
            <FaLink className="me-2" />
            {isEditing ? 'Edit Mapping' : 'Add New Mapping'}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Plant <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    value={formData.plantId}
                    onChange={(e) => setFormData({ ...formData, plantId: Number(e.target.value) })}
                    disabled={isEditing}
                  >
                    <option value="">Select Plant</option>
                    {plantsData?.content?.map((plant: { id: number; code: string; name: string }) => (
                      <option key={plant.id} value={plant.id}>{plant.code} - {plant.name}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Material <span className="text-danger">*</span></Form.Label>
                  {formData.materialId ? (
                    <div className="d-flex align-items-center border rounded p-2">
                      <div className="flex-grow-1">
                        <strong>{formData.materialCode}</strong>
                        <div className="text-muted small">{formData.materialDescription}</div>
                      </div>
                      {!isEditing && (
                        <Button
                          variant="outline-secondary"
                          size="sm"
                          onClick={() => setFormData({ ...formData, materialId: 0, materialCode: '', materialDescription: '' })}
                        >
                          <FaTimes />
                        </Button>
                      )}
                    </div>
                  ) : (
                    <div className="position-relative">
                      <Form.Control
                        type="text"
                        placeholder="Search material..."
                        value={materialSearch}
                        onChange={(e) => {
                          setMaterialSearch(e.target.value);
                          setShowMaterialDropdown(true);
                        }}
                        onFocus={() => setShowMaterialDropdown(true)}
                        onBlur={() => {
                          setTimeout(() => setShowMaterialDropdown(false), 200);
                        }}
                        disabled={isEditing}
                      />
                      {showMaterialDropdown && materialsData?.content && (
                        <div className="position-absolute bg-white border rounded shadow-lg mt-1 w-100" style={{ zIndex: 9999, maxHeight: '250px', overflowY: 'auto' }}>
                          {materialsData.content.map((m) => (
                            <div
                              key={m.id}
                              className="p-2 border-bottom"
                              style={{ cursor: 'pointer', transition: 'background-color 0.15s ease' }}
                              onMouseDown={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                handleMaterialSelect(m);
                              }}
                              onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#e9ecef'}
                              onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'white'}
                            >
                              <strong className="text-primary">{m.code}</strong>
                              <div className="text-muted small">{m.name}</div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  )}
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Min Quantity</Form.Label>
                  <Form.Control
                    type="number"
                    value={formData.minQuantity}
                    onChange={(e) => setFormData({ ...formData, minQuantity: Number(e.target.value) })}
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Max Quantity</Form.Label>
                  <Form.Control
                    type="number"
                    value={formData.maxQuantity}
                    onChange={(e) => setFormData({ ...formData, maxQuantity: Number(e.target.value) })}
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Reorder Level</Form.Label>
                  <Form.Control
                    type="number"
                    value={formData.reorderLevel}
                    onChange={(e) => setFormData({ ...formData, reorderLevel: Number(e.target.value) })}
                  />
                </Form.Group>
              </Col>
              <Col md={3}>
                <Form.Group>
                  <Form.Label>Lead Time (days)</Form.Label>
                  <Form.Control
                    type="number"
                    value={formData.leadTimeDays}
                    onChange={(e) => setFormData({ ...formData, leadTimeDays: Number(e.target.value) })}
                  />
                </Form.Group>
              </Col>
              <Col md={12}>
                <Form.Group>
                  <Form.Check
                    type="switch"
                    label={formData.isActive ? 'Active' : 'Inactive'}
                    checked={formData.isActive}
                    onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                  />
                </Form.Group>
              </Col>
            </Row>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={closeModal}>
            <FaTimes className="me-1" /> Cancel
          </Button>
          <Button variant="primary" onClick={handleSubmit} disabled={createMutation.isPending || updateMutation.isPending}>
            <FaSave className="me-1" />
            {createMutation.isPending || updateMutation.isPending ? 'Saving...' : 'Save'}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        show={showDeleteConfirm}
        title="Delete Mapping"
        message={`Are you sure you want to delete this plant-material mapping?`}
        onConfirm={() => mappingToDelete && deleteMutation.mutate(mappingToDelete.id)}
        onCancel={() => { setShowDeleteConfirm(false); setMappingToDelete(null); }}
        variant="danger"
        loading={deleteMutation.isPending}
      />
    </div>
  );
};

export default CompanyPlantMaterialPage;
