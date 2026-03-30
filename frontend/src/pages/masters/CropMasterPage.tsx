import React, { useState } from 'react';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Modal, Table } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaSearch, FaLeaf, FaPlus, FaEdit, FaTrash, FaSave, FaTimes,
  FaCheckCircle, FaTimesCircle
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, ConfirmDialog, Column } from '../../components/common';
import { getErrorMessage } from '../../api';
import apiClient from '../../api/client';
import { toast } from 'react-toastify';

interface Crop {
  id: number;
  cropCode: string;
  cropName: string;
  cropType: string;
  cropGroup: string;
  description?: string;
  season?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

// Predefined crop types and groups
const CROP_TYPES = ['Cereals', 'Pulses', 'Oilseeds', 'Vegetables', 'Fruits', 'Spices', 'Fiber', 'Sugar', 'Others'];
const CROP_GROUPS = ['Kharif', 'Rabi', 'Zaid', 'Perennial', 'Annual'];
const SEASONS = ['Summer', 'Winter', 'Monsoon', 'All Season'];

// API functions for crops
const cropsApi = {
  list: async (params: { search?: string; cropType?: string; isActive?: boolean; page?: number; size?: number }) => {
    const response = await apiClient.get('/crops', { params });
    return response.data;
  },
  create: async (data: Partial<Crop>) => {
    const response = await apiClient.post('/crops', data);
    return response.data;
  },
  update: async (id: number, data: Partial<Crop>) => {
    const response = await apiClient.put(`/crops/${id}`, data);
    return response.data;
  },
  delete: async (id: number) => {
    const response = await apiClient.delete(`/crops/${id}`);
    return response.data;
  },
};

const CropMasterPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedCrop, setSelectedCrop] = useState<Crop | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [cropToDelete, setCropToDelete] = useState<Crop | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    cropCode: '',
    cropName: '',
    cropType: '',
    cropGroup: '',
    description: '',
    season: '',
    isActive: true,
  });

  // Fetch crops
  const { data: cropsData, isLoading, error } = useQuery({
    queryKey: ['crops', searchTerm, typeFilter, currentPage],
    queryFn: () => cropsApi.list({
      search: searchTerm || undefined,
      cropType: typeFilter || undefined,
      page: currentPage,
      size: pageSize,
    }),
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: Partial<Crop>) => cropsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['crops'] });
      toast.success('Crop created successfully');
      closeModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Crop> }) => cropsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['crops'] });
      toast.success('Crop updated successfully');
      closeModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => cropsApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['crops'] });
      toast.success('Crop deleted successfully');
      setShowDeleteConfirm(false);
      setCropToDelete(null);
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const openAddModal = () => {
    setIsEditing(false);
    setSelectedCrop(null);
    setFormData({
      cropCode: '',
      cropName: '',
      cropType: '',
      cropGroup: '',
      description: '',
      season: '',
      isActive: true,
    });
    setShowModal(true);
  };

  const openEditModal = (crop: Crop) => {
    setIsEditing(true);
    setSelectedCrop(crop);
    setFormData({
      cropCode: crop.cropCode,
      cropName: crop.cropName,
      cropType: crop.cropType,
      cropGroup: crop.cropGroup,
      description: crop.description || '',
      season: crop.season || '',
      isActive: crop.isActive,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setIsEditing(false);
    setSelectedCrop(null);
  };

  const handleSubmit = () => {
    if (!formData.cropCode || !formData.cropName || !formData.cropType || !formData.cropGroup) {
      toast.error('Please fill all required fields');
      return;
    }

    if (isEditing && selectedCrop) {
      updateMutation.mutate({ id: selectedCrop.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (crop: Crop) => {
    setCropToDelete(crop);
    setShowDeleteConfirm(true);
  };

  const confirmDelete = () => {
    if (cropToDelete) {
      deleteMutation.mutate(cropToDelete.id);
    }
  };

  const columns = [
    {
      key: 'cropCode',
      label: 'Code',
      render: (row: Crop) => <Badge bg="primary">{row.cropCode}</Badge>,
    },
    {
      key: 'cropName',
      label: 'Crop Name',
      render: (row: Crop) => (
        <div>
          <FaLeaf className="me-1 text-success" />
          <strong>{row.cropName}</strong>
          {row.description && (
            <div className="text-muted small">{row.description}</div>
          )}
        </div>
      ),
    },
    {
      key: 'cropType',
      label: 'Type',
      render: (row: Crop) => <Badge bg="info">{row.cropType}</Badge>,
    },
    {
      key: 'cropGroup',
      label: 'Group',
      render: (row: Crop) => <Badge bg="secondary">{row.cropGroup}</Badge>,
    },
    {
      key: 'season',
      label: 'Season',
      render: (row: Crop) => row.season || '-',
    },
    {
      key: 'isActive',
      label: 'Status',
      render: (row: Crop) => (
        <Badge bg={row.isActive ? 'success' : 'danger'}>
          {row.isActive ? <><FaCheckCircle className="me-1" /> Active</> : <><FaTimesCircle className="me-1" /> Inactive</>}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: Crop) => (
        <div className="d-flex gap-1">
          <Button variant="outline-primary" size="sm" onClick={() => openEditModal(row)} title="Edit">
            <FaEdit />
          </Button>
          <Button variant="outline-danger" size="sm" onClick={() => handleDelete(row)} title="Delete">
            <FaTrash />
          </Button>
        </div>
      ),
    },
  ];

  if (error) {
    return <div className="alert alert-danger">Error loading crops: {getErrorMessage(error)}</div>;
  }

  return (
    <div className="crop-master">
      <PageHeader
        title="Crop Master"
        subtitle="Manage crop types and categories for plant indents"
        breadcrumbs={[
          { label: 'Masters', path: '/masters' },
          { label: 'Crops', path: '' },
        ]}
        actions={
          <Button variant="primary" onClick={openAddModal}>
            <FaPlus className="me-1" /> Add Crop
          </Button>
        }
      />

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={5}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by crop name or code..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
                <option value="">All Types</option>
                {CROP_TYPES.map(type => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </Form.Select>
            </Col>
            <Col md={2}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={() => { setSearchTerm(''); setTypeFilter(''); }}
              >
                Clear
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading crops..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={cropsData?.content || []}
          keyField="id"
          totalItems={cropsData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {/* Add/Edit Modal */}
      <Modal show={showModal} onHide={closeModal}>
        <Modal.Header closeButton className="bg-success text-white">
          <Modal.Title>
            <FaLeaf className="me-2" />
            {isEditing ? 'Edit Crop' : 'Add New Crop'}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Row className="g-3">
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Crop Code <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    value={formData.cropCode}
                    onChange={(e) => setFormData({ ...formData, cropCode: e.target.value.toUpperCase() })}
                    placeholder="e.g., CRP001"
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Crop Name <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    value={formData.cropName}
                    onChange={(e) => setFormData({ ...formData, cropName: e.target.value })}
                    placeholder="e.g., Rice Paddy"
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Crop Type <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    value={formData.cropType}
                    onChange={(e) => setFormData({ ...formData, cropType: e.target.value })}
                  >
                    <option value="">Select Type</option>
                    {CROP_TYPES.map(type => (
                      <option key={type} value={type}>{type}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Crop Group <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    value={formData.cropGroup}
                    onChange={(e) => setFormData({ ...formData, cropGroup: e.target.value })}
                  >
                    <option value="">Select Group</option>
                    {CROP_GROUPS.map(group => (
                      <option key={group} value={group}>{group}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Season</Form.Label>
                  <Form.Select
                    value={formData.season}
                    onChange={(e) => setFormData({ ...formData, season: e.target.value })}
                  >
                    <option value="">Select Season</option>
                    {SEASONS.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Status</Form.Label>
                  <Form.Check
                    type="switch"
                    label={formData.isActive ? 'Active' : 'Inactive'}
                    checked={formData.isActive}
                    onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                  />
                </Form.Group>
              </Col>
              <Col md={12}>
                <Form.Group>
                  <Form.Label>Description</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    placeholder="Optional description..."
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
          <Button
            variant="success"
            onClick={handleSubmit}
            disabled={createMutation.isPending || updateMutation.isPending}
          >
            <FaSave className="me-1" />
            {createMutation.isPending || updateMutation.isPending ? 'Saving...' : 'Save'}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        show={showDeleteConfirm}
        title="Delete Crop"
        message={`Are you sure you want to delete crop "${cropToDelete?.cropName}"?`}
        onConfirm={confirmDelete}
        onCancel={() => { setShowDeleteConfirm(false); setCropToDelete(null); }}
        variant="danger"
        loading={deleteMutation.isPending}
      />
    </div>
  );
};

export default CropMasterPage;
