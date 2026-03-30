import React, { useState } from 'react';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Modal, Table } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaSearch, FaFilter, FaClipboardCheck, FaCheck, FaTimes, FaEye,
  FaBoxOpen, FaExclamationTriangle
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { grnApi, getErrorMessage } from '../../api';
import { toast } from 'react-toastify';

interface ReceiptConfirmItem {
  id: number;
  issueNoteNumber: string;
  indentNumber: string;
  issuedDate: string;
  issuedBy: string;
  receivedBy: string;
  departmentName: string;
  materialCode: string;
  materialDescription: string;
  issuedQty: number;
  receivedQty?: number;
  uomCode: string;
  status: 'PENDING_RECEIPT' | 'RECEIVED' | 'PARTIAL_RECEIPT' | 'DISCREPANCY';
}

const ReceiptConfirmationPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('PENDING_RECEIPT');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Modal state
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState<ReceiptConfirmItem | null>(null);
  const [receivedQty, setReceivedQty] = useState(0);
  const [receiptRemarks, setReceiptRemarks] = useState('');
  const [hasDiscrepancy, setHasDiscrepancy] = useState(false);

  // Fetch pending receipt items
  const { data: receiptData, isLoading, error } = useQuery({
    queryKey: ['receipt-confirmation', searchTerm, statusFilter, currentPage],
    queryFn: () => grnApi.list({
      search: searchTerm,
      status: 2, // Status for QC_APPROVED, pending store confirmation
      page: currentPage,
      size: pageSize,
    }),
  });

  // Confirm receipt mutation - use GRN store API
  const confirmReceiptMutation = useMutation({
    mutationFn: async (params: { id: number; receivedQty: number; remarks: string; hasDiscrepancy: boolean }) => {
      return grnApi.store(params.id, params.remarks || 'Receipt confirmed');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['receipt-confirmation'] });
      toast.success('Receipt confirmed successfully');
      setShowConfirmModal(false);
      resetModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const resetModal = () => {
    setSelectedItem(null);
    setReceivedQty(0);
    setReceiptRemarks('');
    setHasDiscrepancy(false);
  };

  const handleConfirmReceipt = (item: ReceiptConfirmItem) => {
    setSelectedItem(item);
    setReceivedQty(item.issuedQty);
    setShowConfirmModal(true);
  };

  const submitConfirmation = () => {
    if (!selectedItem) return;

    if (receivedQty <= 0) {
      toast.error('Received quantity must be greater than 0');
      return;
    }

    const discrepancy = receivedQty !== selectedItem.issuedQty;
    if (discrepancy && !receiptRemarks.trim()) {
      toast.error('Please provide remarks for quantity discrepancy');
      return;
    }

    confirmReceiptMutation.mutate({
      id: selectedItem.id,
      receivedQty,
      remarks: receiptRemarks,
      hasDiscrepancy: discrepancy,
    });
  };

  const columns = [
    {
      key: 'issueNoteNumber',
      label: 'Issue Note No.',
      render: (row: ReceiptConfirmItem) => (
        <span className="fw-bold text-primary">{row.issueNoteNumber}</span>
      ),
    },
    {
      key: 'indentNumber',
      label: 'Indent No.',
    },
    {
      key: 'issuedBy',
      label: 'Issued By',
    },
    {
      key: 'materialCode',
      label: 'Material',
      render: (row: ReceiptConfirmItem) => (
        <div>
          <strong>{row.materialCode}</strong>
          <div className="text-muted small">{row.materialDescription}</div>
        </div>
      ),
    },
    {
      key: 'issuedQty',
      label: 'Issued Qty',
      render: (row: ReceiptConfirmItem) => `${row.issuedQty} ${row.uomCode}`,
    },
    {
      key: 'issuedDate',
      label: 'Issued Date',
      render: (row: ReceiptConfirmItem) => new Date(row.issuedDate).toLocaleDateString(),
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: ReceiptConfirmItem) => (
        <Badge bg={
          row.status === 'PENDING_RECEIPT' ? 'warning' :
          row.status === 'RECEIVED' ? 'success' :
          row.status === 'DISCREPANCY' ? 'danger' : 'info'
        }>
          {row.status === 'DISCREPANCY' && <FaExclamationTriangle className="me-1" />}
          {row.status?.replace('_', ' ')}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: ReceiptConfirmItem) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            title="View Details"
          >
            <FaEye />
          </Button>
          {row.status === 'PENDING_RECEIPT' && (
            <Button
              variant="outline-success"
              size="sm"
              onClick={() => handleConfirmReceipt(row)}
              title="Confirm Receipt"
            >
              <FaCheck />
            </Button>
          )}
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading receipt items: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="receipt-confirmation">
      <PageHeader
        title="Receipt Confirmation"
        subtitle="Confirm material receipt by department from store"
      />

      {/* Stats */}
      <Row className="mb-3">
        <Col md={3}>
          <Card className="bg-warning bg-opacity-10 border-warning">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-warning">8</h3>
              <div className="text-muted small">Pending Receipt</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-success bg-opacity-10 border-success">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-success">35</h3>
              <div className="text-muted small">Received Today</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-danger bg-opacity-10 border-danger">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-danger">2</h3>
              <div className="text-muted small">With Discrepancy</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={3}>
          <Card className="bg-info bg-opacity-10 border-info">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-info">98.5%</h3>
              <div className="text-muted small">Accuracy Rate (MTD)</div>
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
                  placeholder="Search by issue note, indent, material..."
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
                <option value="PENDING_RECEIPT">Pending Receipt</option>
                <option value="RECEIVED">Received</option>
                <option value="DISCREPANCY">With Discrepancy</option>
                <option value="">All Status</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('PENDING_RECEIPT');
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
        <LoadingSpinner text="Loading receipt items..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={(receiptData?.content || []) as unknown as Record<string, unknown>[]}
          keyField="id"
          totalItems={receiptData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {/* Confirm Receipt Modal */}
      <Modal show={showConfirmModal} onHide={() => setShowConfirmModal(false)}>
        <Modal.Header closeButton className="bg-primary text-white">
          <Modal.Title><FaClipboardCheck className="me-2" /> Confirm Receipt</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedItem && (
            <>
              <Table bordered size="sm" className="mb-3">
                <tbody>
                  <tr>
                    <th style={{ width: '35%' }}>Issue Note No.</th>
                    <td>{selectedItem.issueNoteNumber}</td>
                  </tr>
                  <tr>
                    <th>Material</th>
                    <td>{selectedItem.materialCode} - {selectedItem.materialDescription}</td>
                  </tr>
                  <tr>
                    <th>Issued Quantity</th>
                    <td>{selectedItem.issuedQty} {selectedItem.uomCode}</td>
                  </tr>
                  <tr>
                    <th>Issued By</th>
                    <td>{selectedItem.issuedBy}</td>
                  </tr>
                </tbody>
              </Table>

              <Form.Group className="mb-3">
                <Form.Label>Received Quantity <span className="text-danger">*</span></Form.Label>
                <Form.Control
                  type="number"
                  value={receivedQty}
                  onChange={(e) => {
                    const qty = Number(e.target.value);
                    setReceivedQty(qty);
                    setHasDiscrepancy(qty !== selectedItem.issuedQty);
                  }}
                  min={0}
                />
                {hasDiscrepancy && (
                  <Form.Text className="text-danger">
                    <FaExclamationTriangle className="me-1" />
                    Quantity differs from issued quantity. Please provide remarks.
                  </Form.Text>
                )}
              </Form.Group>

              <Form.Group>
                <Form.Label>
                  Remarks {hasDiscrepancy && <span className="text-danger">*</span>}
                </Form.Label>
                <Form.Control
                  as="textarea"
                  rows={2}
                  value={receiptRemarks}
                  onChange={(e) => setReceiptRemarks(e.target.value)}
                  placeholder={hasDiscrepancy ? "Explain the discrepancy..." : "Any additional remarks..."}
                  required={hasDiscrepancy}
                />
              </Form.Group>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowConfirmModal(false)}>
            Cancel
          </Button>
          <Button
            variant="primary"
            onClick={submitConfirmation}
            disabled={confirmReceiptMutation.isPending}
          >
            {confirmReceiptMutation.isPending ? 'Processing...' : 'Confirm Receipt'}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default ReceiptConfirmationPage;
