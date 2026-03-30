import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Table, Modal } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaSearch, FaFilter, FaClipboardCheck, FaCheck, FaTimes, FaEye,
  FaExclamationTriangle, FaCheckCircle, FaTimesCircle
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { grnApi, getErrorMessage } from '../../api';
import { toast } from 'react-toastify';

// QC Check types
interface QCItem {
  id: number;
  grnNumber: string;
  grnDate: string;
  poNumber: string;
  vendorName: string;
  materialCode: string;
  materialDescription: string;
  receivedQty: number;
  uomCode: string;
  qcStatus: 'PENDING' | 'PASSED' | 'FAILED' | 'PARTIAL';
  inspectionDate?: string;
  inspectedBy?: string;
  acceptedQty?: number;
  rejectedQty?: number;
  rejectionReason?: string;
}

const statusColors = {
  PENDING: 'warning',
  PASSED: 'success',
  FAILED: 'danger',
  PARTIAL: 'info',
};

const QualityControlListPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('PENDING');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Modal state for QC action
  const [showQCModal, setShowQCModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState<QCItem | null>(null);
  const [qcAction, setQCAction] = useState<'accept' | 'reject' | 'partial'>('accept');
  const [acceptedQty, setAcceptedQty] = useState(0);
  const [rejectedQty, setRejectedQty] = useState(0);
  const [rejectionReason, setRejectionReason] = useState('');

  // Fetch QC pending items (filter GRN by QC status)
  const { data: qcData, isLoading, error } = useQuery({
    queryKey: ['qc-items', searchTerm, statusFilter, currentPage],
    queryFn: () => grnApi.list({
      search: searchTerm,
      status: statusFilter === 'PENDING' ? 1 : statusFilter === 'PASSED' ? 2 : 3,
      page: currentPage,
      size: pageSize,
    }),
  });

  // QC Action mutation
  const qcMutation = useMutation({
    mutationFn: async (params: { id: number; action: string; remarks: string }) => {
      // Call appropriate GRN API based on action
      if (params.action === 'accept') {
        return grnApi.approve(params.id, params.remarks);
      } else {
        return grnApi.reject(params.id, params.remarks);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['qc-items'] });
      toast.success('QC action completed successfully');
      setShowQCModal(false);
      resetModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const resetModal = () => {
    setSelectedItem(null);
    setQCAction('accept');
    setAcceptedQty(0);
    setRejectedQty(0);
    setRejectionReason('');
  };

  const handleQCAction = (item: QCItem, action: 'accept' | 'reject' | 'partial') => {
    setSelectedItem(item);
    setQCAction(action);
    setAcceptedQty(item.receivedQty);
    setRejectedQty(0);
    setShowQCModal(true);
  };

  const submitQCAction = () => {
    if (!selectedItem) return;

    if (qcAction === 'reject' && !rejectionReason.trim()) {
      toast.error('Please provide rejection reason');
      return;
    }

    qcMutation.mutate({
      id: selectedItem.id,
      action: qcAction,
      remarks: qcAction === 'reject' 
        ? `Rejected: ${rejectionReason}` 
        : `Accepted qty: ${acceptedQty}`,
    });
  };

  const columns = [
    {
      key: 'grnNumber',
      label: 'GRN No.',
      render: (row: QCItem) => (
        <span className="fw-bold text-primary">{row.grnNumber}</span>
      ),
    },
    {
      key: 'poNumber',
      label: 'PO No.',
    },
    {
      key: 'vendorName',
      label: 'Vendor',
    },
    {
      key: 'materialCode',
      label: 'Material',
      render: (row: QCItem) => (
        <div>
          <strong>{row.materialCode}</strong>
          <div className="text-muted small">{row.materialDescription}</div>
        </div>
      ),
    },
    {
      key: 'receivedQty',
      label: 'Received Qty',
      render: (row: QCItem) => `${row.receivedQty} ${row.uomCode}`,
    },
    {
      key: 'grnDate',
      label: 'Received Date',
      render: (row: QCItem) => new Date(row.grnDate).toLocaleDateString(),
    },
    {
      key: 'qcStatus',
      label: 'QC Status',
      render: (row: QCItem) => (
        <Badge bg={statusColors[row.qcStatus]}>
          {row.qcStatus === 'PENDING' && <FaExclamationTriangle className="me-1" />}
          {row.qcStatus === 'PASSED' && <FaCheckCircle className="me-1" />}
          {row.qcStatus === 'FAILED' && <FaTimesCircle className="me-1" />}
          {row.qcStatus}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: QCItem) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => navigate(`/grn/${row.id}`)}
            title="View Details"
          >
            <FaEye />
          </Button>
          {row.qcStatus === 'PENDING' && (
            <>
              <Button
                variant="outline-success"
                size="sm"
                onClick={() => handleQCAction(row, 'accept')}
                title="Accept"
              >
                <FaCheck />
              </Button>
              <Button
                variant="outline-danger"
                size="sm"
                onClick={() => handleQCAction(row, 'reject')}
                title="Reject"
              >
                <FaTimes />
              </Button>
            </>
          )}
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading QC items: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="quality-control-list">
      <PageHeader
        title="Quality Control"
        subtitle="Inspect and verify received materials quality"
      />

      {/* Stats Cards */}
      <Row className="mb-3">
        <Col md={3}>
          <Card className="bg-warning bg-opacity-10 border-warning">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-warning">12</h3>
              <div className="text-muted small">Pending Inspection</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-success bg-opacity-10 border-success">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-success">45</h3>
              <div className="text-muted small">Passed Today</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-danger bg-opacity-10 border-danger">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-danger">3</h3>
              <div className="text-muted small">Rejected Today</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-info bg-opacity-10 border-info">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-info">98.5%</h3>
              <div className="text-muted small">Pass Rate (MTD)</div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={5}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by GRN, PO, material, vendor..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
            <Col md={3}>
              <Form.Select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="PENDING">Pending Inspection</option>
                <option value="PASSED">QC Passed</option>
                <option value="FAILED">QC Failed</option>
                <option value="">All Status</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('PENDING');
                }}
              >
                <FaFilter className="me-1" /> Clear
              </Button>
            </Col>
            <Col md={2}>
              <Button
                variant="outline-danger"
                className="w-100"
                onClick={() => navigate('/quality-control/rejected')}
              >
                <FaTimes className="me-1" /> Rejected List
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading QC items..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={(qcData?.content || []) as unknown as Record<string, unknown>[]}
          keyField="id"
          totalItems={qcData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {/* QC Action Modal */}
      <Modal show={showQCModal} onHide={() => setShowQCModal(false)} size="lg">
        <Modal.Header closeButton className={
          qcAction === 'accept' ? 'bg-success text-white' :
          qcAction === 'reject' ? 'bg-danger text-white' : 'bg-info text-white'
        }>
          <Modal.Title>
            {qcAction === 'accept' ? <><FaCheck className="me-2" /> Accept QC</> :
             qcAction === 'reject' ? <><FaTimes className="me-2" /> Reject QC</> :
             <><FaClipboardCheck className="me-2" /> Partial Accept</>}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedItem && (
            <>
              <Table bordered size="sm" className="mb-3">
                <tbody>
                  <tr>
                    <th style={{ width: '30%' }}>GRN Number</th>
                    <td>{selectedItem.grnNumber}</td>
                  </tr>
                  <tr>
                    <th>Material</th>
                    <td>{selectedItem.materialCode} - {selectedItem.materialDescription}</td>
                  </tr>
                  <tr>
                    <th>Received Quantity</th>
                    <td>{selectedItem.receivedQty} {selectedItem.uomCode}</td>
                  </tr>
                  <tr>
                    <th>Vendor</th>
                    <td>{selectedItem.vendorName}</td>
                  </tr>
                </tbody>
              </Table>

              {qcAction !== 'accept' && (
                <Row className="g-3">
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Accepted Quantity</Form.Label>
                      <Form.Control
                        type="number"
                        value={acceptedQty}
                        onChange={(e) => {
                          const val = Number(e.target.value);
                          setAcceptedQty(val);
                          setRejectedQty(selectedItem.receivedQty - val);
                        }}
                        max={selectedItem.receivedQty}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group>
                      <Form.Label>Rejected Quantity</Form.Label>
                      <Form.Control
                        type="number"
                        value={rejectedQty}
                        onChange={(e) => {
                          const val = Number(e.target.value);
                          setRejectedQty(val);
                          setAcceptedQty(selectedItem.receivedQty - val);
                        }}
                        max={selectedItem.receivedQty}
                      />
                    </Form.Group>
                  </Col>
                </Row>
              )}

              {(qcAction === 'reject' || rejectedQty > 0) && (
                <Form.Group className="mt-3">
                  <Form.Label>Rejection Reason <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    value={rejectionReason}
                    onChange={(e) => setRejectionReason(e.target.value)}
                    placeholder="Enter reason for rejection (quality issues, specification mismatch, damage, etc.)"
                  />
                </Form.Group>
              )}
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowQCModal(false)}>
            Cancel
          </Button>
          <Button
            variant={qcAction === 'accept' ? 'success' : qcAction === 'reject' ? 'danger' : 'info'}
            onClick={submitQCAction}
            disabled={qcMutation.isPending}
          >
            {qcMutation.isPending ? 'Processing...' : 'Confirm'}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default QualityControlListPage;
