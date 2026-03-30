import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Row, Col, Badge, Table, Button, Spinner } from 'react-bootstrap';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { FaArrowLeft, FaEdit, FaTrash, FaIndustry, FaLeaf } from 'react-icons/fa';
import { PageHeader } from '../../components/common';
import { plantIndentsApi, getErrorMessage } from '../../api';
import type { PlantIndent } from '../../api/plantIndents';

const statusColors: Record<number, string> = {
  0: 'secondary', 1: 'warning', 2: 'info', 3: 'success',
  4: 'danger', 5: 'danger', 6: 'primary', 7: 'dark',
};
const statusNames: Record<number, string> = {
  0: 'Draft', 1: 'Pending DEO/QM', 2: 'Pending Manager', 3: 'Approved',
  4: 'Rejected (DEO)', 5: 'Rejected (Mgr)', 6: 'Processing', 7: 'Completed',
};

const PlantIndentDetailPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();

  const { data: indent, isLoading, error } = useQuery({
    queryKey: ['plant-indent', id],
    queryFn: () => plantIndentsApi.getById(Number(id)),
    enabled: !!id,
  });

  const handleDelete = async () => {
    if (!indent) return;
    if (window.confirm(`Are you sure you want to delete indent ${indent.indentNumber || indent.id}?`)) {
      try {
        await plantIndentsApi.delete(indent.id);
        queryClient.invalidateQueries({ queryKey: ['plant-indents'] });
        navigate('/plant-indent');
      } catch (err) {
        alert('Failed to delete: ' + getErrorMessage(err));
      }
    }
  };

  if (isLoading) {
    return (
      <div className="d-flex justify-content-center py-5">
        <Spinner animation="border" variant="primary" />
      </div>
    );
  }

  if (error || !indent) {
    return (
      <div className="alert alert-danger">
        {error ? getErrorMessage(error) : 'Plant indent not found'}
      </div>
    );
  }

  const InfoItem = ({ label, value }: { label: string; value?: string | number | null }) => (
    <Col md={4} sm={6} className="mb-3">
      <div className="text-muted small">{label}</div>
      <div className="fw-semibold">{value || '—'}</div>
    </Col>
  );

  return (
    <div className="plant-indent-detail">
      <PageHeader
        title={`Plant Indent: ${indent.indentNumber || indent.indentCode || ''}`}
        subtitle="View plant-specific material requisition details"
        breadcrumbs={[
          { label: 'Plant Indents', path: '/plant-indent' },
          { label: indent.indentNumber || 'View', path: '' },
        ]}
      />

      {/* Header with status */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="align-items-center">
            <Col md={8}>
              <h5 className="mb-1">{indent.indentNumber || indent.indentCode}</h5>
              <div className="text-muted">ID: {indent.id}</div>
            </Col>
            <Col md={4} className="text-end">
              <Badge
                bg={statusColors[indent.status ?? 0] || 'secondary'}
                className="fs-6 px-3 py-2"
              >
                {indent.statusDescription || statusNames[indent.status ?? 0] || 'Unknown'}
              </Badge>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Plant & Employee Info */}
      <Card className="mb-3 shadow-sm">
        <Card.Header className="bg-success text-white">
          <FaIndustry className="me-2" /> Plant & Employee Information
        </Card.Header>
        <Card.Body>
          <Row>
            <InfoItem label="Plant" value={indent.plantName ? `${indent.plantName} (${indent.plantId})` : indent.plantId?.toString()} />
            <InfoItem label="Employee" value={indent.employeeNumber} />
            <InfoItem label="Indent Number" value={indent.indentNumber} />
          </Row>
        </Card.Body>
      </Card>

      {/* Crop & Production Info */}
      <Card className="mb-3 shadow-sm">
        <Card.Header className="bg-success text-white">
          <FaLeaf className="me-2" /> Crop & Production Details
        </Card.Header>
        <Card.Body>
          <Row>
            <InfoItem label="Crop Type" value={indent.outputMaterial || indent.cropTypeName} />
            <InfoItem label="Crop Group / Season" value={indent.packProcess} />
            <InfoItem label="Crop Name" value={indent.cropName} />
            <InfoItem label="Batch Number" value={indent.batchNumber} />
            <InfoItem label="Line Code" value={indent.lineCode} />
            <InfoItem label="Line Description" value={indent.lineDescription} />
            <InfoItem label="Output Material" value={indent.outputMaterial} />
            <InfoItem label="Output Description" value={indent.outputDescription} />
            <InfoItem label="Expected Quantity" value={indent.expectedQuantity} />
            <InfoItem label="UOM" value={indent.masterUom} />
          </Row>
        </Card.Body>
      </Card>

      {/* Remarks */}
      {indent.remarks && (
        <Card className="mb-3 shadow-sm">
          <Card.Header>Remarks / Comments</Card.Header>
          <Card.Body>
            <p className="mb-0">{indent.remarks}</p>
          </Card.Body>
        </Card>
      )}

      {/* Line Items */}
      <Card className="mb-3 shadow-sm">
        <Card.Header>
          Material Items ({indent.detailCount || indent.details?.length || 0})
        </Card.Header>
        <Card.Body className="p-0">
          <Table responsive hover className="mb-0">
            <thead className="table-light">
              <tr>
                <th>#</th>
                <th>Material</th>
                <th>Description</th>
                <th>UOM</th>
                <th>Quantity</th>
                <th>Stock Avail.</th>
                <th>Rate</th>
                <th>Purpose</th>
              </tr>
            </thead>
            <tbody>
              {indent.details && indent.details.length > 0 ? (
                indent.details.map((detail, index) => (
                  <tr key={detail.id || index}>
                    <td>{index + 1}</td>
                    <td className="fw-semibold">
                      {detail.materialCode || detail.indentMaterial || '—'}
                    </td>
                    <td>{detail.materialName || detail.indentMaterialDescription || '—'}</td>
                    <td>{detail.uomCode || detail.uomName || '—'}</td>
                    <td>{detail.quantity ?? '—'}</td>
                    <td>{detail.stockAvailable ?? '—'}</td>
                    <td>{detail.pricing ? `₹${detail.pricing}` : '—'}</td>
                    <td>{detail.purpose || '—'}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={8} className="text-center text-muted py-4">
                    No line items found
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        </Card.Body>
      </Card>

      {/* QC Parameters (if any item has them) */}
      {indent.details?.some(d => d.stl || d.odv || d.got || d.elisa || d.moisture) && (
        <Card className="mb-3 shadow-sm">
          <Card.Header>QC Parameters</Card.Header>
          <Card.Body className="p-0">
            <Table responsive hover size="sm" className="mb-0">
              <thead className="table-light">
                <tr>
                  <th>Material</th>
                  <th>STL</th>
                  <th>ODV</th>
                  <th>GOT</th>
                  <th>ELISA</th>
                  <th>Moisture</th>
                  <th>Pure Seed</th>
                  <th>Germ Normal</th>
                </tr>
              </thead>
              <tbody>
                {indent.details?.map((d, i) => (
                  <tr key={i}>
                    <td>{d.materialCode || d.indentMaterial || '—'}</td>
                    <td>{d.stl || '—'}</td>
                    <td>{d.odv || '—'}</td>
                    <td>{d.got || '—'}</td>
                    <td>{d.elisa || '—'}</td>
                    <td>{d.moisture || '—'}</td>
                    <td>{d.pureSeed || '—'}</td>
                    <td>{d.germNormal || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}

      {/* Actions */}
      <div className="d-flex justify-content-between mb-4">
        <Button variant="outline-secondary" onClick={() => navigate('/plant-indent')}>
          <FaArrowLeft className="me-1" /> Back to List
        </Button>
        <div className="d-flex gap-2">
          {(indent.status === 0 || indent.status === 4 || indent.status === 5) && (
            <Button variant="primary" onClick={() => navigate(`/plant-indent/${indent.id}/edit`)}>
              <FaEdit className="me-1" /> Edit
            </Button>
          )}
          {indent.status === 0 && (
            <Button variant="danger" onClick={handleDelete}>
              <FaTrash className="me-1" /> Delete
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default PlantIndentDetailPage;
