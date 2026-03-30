import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Row, Col, Badge, Button, InputGroup } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaPlus, FaEye, FaEdit, FaTrash, FaSearch, FaFilter, FaLeaf, FaIndustry } from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { plantIndentsApi, plantsApi, getErrorMessage } from '../../api';
import type { PlantIndent } from '../../api/plantIndents';

const statusColors: Record<number, string> = {
  0: 'secondary',     // Draft
  1: 'warning',       // Pending DEO/QM Review
  2: 'info',          // Pending Manager Approval
  3: 'success',       // Manager Approved
  4: 'danger',        // Rejected by DEO/QM
  5: 'danger',        // Rejected by Manager
  6: 'primary',       // Processing
  7: 'dark',          // Completed
};

const statusNames: Record<number, string> = {
  0: 'Draft',
  1: 'Pending DEO/QM',
  2: 'Pending Manager',
  3: 'Approved',
  4: 'Rejected (DEO)',
  5: 'Rejected (Mgr)',
  6: 'Processing',
  7: 'Completed',
};

const PlantIndentListPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState('');
  const [plantFilter, setPlantFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;
  const queryClient = useQueryClient();

  // Fetch plants for filter
  const { data: plantsData } = useQuery({
    queryKey: ['plants-list'],
    queryFn: () => plantsApi.getAll(0, 100),
  });

  // Fetch plant indents from the correct API
  const { data: indentsData, isLoading, error } = useQuery({
    queryKey: ['plant-indents', searchTerm, plantFilter, statusFilter, currentPage],
    queryFn: () => {
      if (searchTerm) {
        return plantIndentsApi.search(searchTerm, { page: currentPage, size: pageSize });
      }
      if (plantFilter) {
        return plantIndentsApi.listByPlant(Number(plantFilter), { page: currentPage, size: pageSize });
      }
      if (statusFilter) {
        return plantIndentsApi.getByStatus(Number(statusFilter), { page: currentPage, size: pageSize });
      }
      return plantIndentsApi.list({ page: currentPage, size: pageSize });
    },
  });

  const columns = [
    {
      key: 'indentNumber',
      label: 'Indent No.',
      render: (row: PlantIndent) => (
        <span className="fw-bold text-primary">{row.indentNumber || row.indentCode}</span>
      ),
    },
    {
      key: 'plantName',
      label: 'Plant',
      render: (row: PlantIndent) => (
        <div>
          <FaIndustry className="me-1 text-muted" />
          {row.plantName || '—'}
        </div>
      ),
    },
    {
      key: 'cropTypeName',
      label: 'Crop Type',
      render: (row: PlantIndent) => (
        <div>
          {(row.outputMaterial || row.cropTypeName) && (
            <div className="small">
              <FaLeaf className="me-1 text-success" />
              {row.outputMaterial || row.cropTypeName}
            </div>
          )}
          {row.packProcess && (
            <div className="text-muted small">{row.packProcess}</div>
          )}
          {!row.outputMaterial && !row.cropTypeName && !row.packProcess && '—'}
        </div>
      ),
    },
    {
      key: 'employeeNumber',
      label: 'Employee',
      render: (row: PlantIndent) => row.employeeNumber || '—',
    },
    {
      key: 'batchNumber',
      label: 'Batch No.',
      render: (row: PlantIndent) => row.batchNumber || '—',
    },
    {
      key: 'masterUom',
      label: 'UOM',
      render: (row: PlantIndent) => row.masterUom || '—',
    },
    {
      key: 'detailCount',
      label: 'Items',
      render: (row: PlantIndent) => row.detailCount || 0,
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: PlantIndent) => (
        <Badge bg={statusColors[row.status ?? 0] || 'secondary'}>
          {row.statusDescription || statusNames[row.status ?? 0] || 'Unknown'}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: PlantIndent) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            title="View"
            onClick={() => navigate(`/plant-indent/${row.id}`)}
          >
            <FaEye />
          </Button>
          {(row.status === 0 || row.status === 4 || row.status === 5) && (
            <Button
              variant="outline-warning"
              size="sm"
              title="Edit"
              onClick={() => navigate(`/plant-indent/${row.id}/edit`)}
            >
              <FaEdit />
            </Button>
          )}
          {row.status === 0 && (
            <Button
              variant="outline-danger"
              size="sm"
              title="Delete"
              onClick={() => {
                if (window.confirm(`Delete indent ${row.indentNumber || row.id}?`)) {
                  plantIndentsApi.delete(row.id).then(() => {
                    queryClient.invalidateQueries({ queryKey: ['plant-indents'] });
                  });
                }
              }}
            >
              <FaTrash />
            </Button>
          )}
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading plant indents: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="plant-indent-list">
      <PageHeader
        title="Plant Indents"
        subtitle="Manage plant-specific material requisitions (R&D / Production)"
        actions={
          <Button variant="primary" onClick={() => navigate('/plant-indent/new')}>
            <FaPlus className="me-1" /> New Plant Indent
          </Button>
        }
      />

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={4}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by indent number, crop, batch..."
                  value={searchTerm}
                  onChange={(e) => { setSearchTerm(e.target.value); setCurrentPage(0); }}
                />
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Select
                value={plantFilter}
                onChange={(e) => { setPlantFilter(e.target.value); setCurrentPage(0); }}
              >
                <option value="">All Plants</option>
                {plantsData?.content?.map((plant: { id: number; name: string }) => (
                  <option key={plant.id} value={plant.id}>{plant.name}</option>
                ))}
              </Form.Select>
            </Col>
            <Col md={3}>
              <Form.Select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(0); }}
              >
                <option value="">All Status</option>
                <option value="0">Draft</option>
                <option value="1">Pending DEO/QM</option>
                <option value="2">Pending Manager</option>
                <option value="3">Approved</option>
                <option value="4">Rejected (DEO/QM)</option>
                <option value="5">Rejected (Manager)</option>
                <option value="6">Processing</option>
                <option value="7">Completed</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={() => {
                  setSearchTerm('');
                  setPlantFilter('');
                  setStatusFilter('');
                  setCurrentPage(0);
                }}
              >
                <FaFilter className="me-1" /> Clear
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading plant indents..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={(indentsData?.content || []) as unknown as Record<string, unknown>[]}
          keyField="id"
          totalItems={indentsData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}
    </div>
  );
};

export default PlantIndentListPage;
