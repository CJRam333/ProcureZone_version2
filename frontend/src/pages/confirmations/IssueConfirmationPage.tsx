import React, { useState } from 'react';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Modal, Table } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaSearch, FaFilter, FaBoxOpen, FaCheck, FaTimes, FaEye, FaPrint,
  FaClipboardList, FaWarehouse
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { issueNotesApi, getErrorMessage } from '../../api';
import { toast } from 'react-toastify';

interface IssueConfirmItem {
  id: number;
  issueNoteNumber: string;
  issueDate: string;
  indentNumber: string;
  requestedBy: string;
  departmentName: string;
  materialCode: string;
  materialDescription: string;
  requestedQty: number;
  approvedQty: number;
  issuedQty?: number;
  uomCode: string;
  status: 'PENDING_ISSUE' | 'ISSUED' | 'PARTIAL_ISSUE';
  purpose: string;
}

const IssueConfirmationPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('PENDING_ISSUE');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Modal state
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [selectedItem, setSelectedItem] = useState<IssueConfirmItem | null>(null);
  const [issuedQty, setIssuedQty] = useState(0);
  const [issueRemarks, setIssueRemarks] = useState('');

  // Fetch pending issue items
  const { data: issueData, isLoading, error } = useQuery({
    queryKey: ['issue-confirmation', searchTerm, statusFilter, currentPage],
    queryFn: () => issueNotesApi.list({
      search: searchTerm,
      status: statusFilter === 'PENDING_ISSUE' ? 1 : statusFilter === 'ISSUED' ? 3 : 2,
      page: currentPage,
      size: pageSize,
    }),
  });

  // Confirm issue mutation
  const confirmIssueMutation = useMutation({
    mutationFn: async (params: { id: number; issuedQty: number; remarks: string }) => {
      // Use the issue API method instead of update
      return issueNotesApi.issue(params.id, {
        remarks: params.remarks,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-confirmation'] });
      toast.success('Issue confirmed successfully');
      setShowConfirmModal(false);
      resetModal();
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const resetModal = () => {
    setSelectedItem(null);
    setIssuedQty(0);
    setIssueRemarks('');
  };

  const handleConfirmIssue = (item: IssueConfirmItem) => {
    setSelectedItem(item);
    setIssuedQty(item.approvedQty);
    setShowConfirmModal(true);
  };

  const submitConfirmation = () => {
    if (!selectedItem) return;

    if (issuedQty <= 0) {
      toast.error('Issued quantity must be greater than 0');
      return;
    }

    if (issuedQty > selectedItem.approvedQty) {
      toast.error('Issued quantity cannot exceed approved quantity');
      return;
    }

    confirmIssueMutation.mutate({
      id: selectedItem.id,
      issuedQty,
      remarks: issueRemarks,
    });
  };

  const columns = [
    {
      key: 'issueNoteNumber',
      label: 'Issue Note No.',
      render: (row: IssueConfirmItem) => (
        <span className="fw-bold text-primary">{row.issueNoteNumber}</span>
      ),
    },
    {
      key: 'indentNumber',
      label: 'Indent No.',
    },
    {
      key: 'requestedBy',
      label: 'Requested By',
      render: (row: IssueConfirmItem) => (
        <div>
          <div>{row.requestedBy}</div>
          <div className="text-muted small">{row.departmentName}</div>
        </div>
      ),
    },
    {
      key: 'materialCode',
      label: 'Material',
      render: (row: IssueConfirmItem) => (
        <div>
          <strong>{row.materialCode}</strong>
          <div className="text-muted small">{row.materialDescription}</div>
        </div>
      ),
    },
    {
      key: 'approvedQty',
      label: 'Approved Qty',
      render: (row: IssueConfirmItem) => `${row.approvedQty} ${row.uomCode}`,
    },
    {
      key: 'issueDate',
      label: 'Issue Date',
      render: (row: IssueConfirmItem) => new Date(row.issueDate).toLocaleDateString(),
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: IssueConfirmItem) => {
        const statusNum = typeof row.status === 'number' ? row.status : Number(row.status);
        const statusLabel = statusNum === 1 ? 'PENDING ISSUE' : statusNum === 3 ? 'ISSUED' : statusNum === 2 ? 'PARTIAL ISSUE' : String(row.status);
        const statusColor = statusNum === 1 ? 'warning' : statusNum === 3 ? 'success' : 'info';
        return <Badge bg={statusColor}>{statusLabel}</Badge>;
      },
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: IssueConfirmItem) => {
        const statusNum = typeof row.status === 'number' ? row.status : Number(row.status);
        return (
          <div className="d-flex gap-1">
            <Button
              variant="outline-primary"
              size="sm"
              title="View Details"
            >
              <FaEye />
            </Button>
            {statusNum === 1 && (
              <Button
                variant="outline-success"
                size="sm"
                onClick={() => handleConfirmIssue(row)}
                title="Confirm Issue"
              >
                <FaCheck />
              </Button>
            )}
            {statusNum === 3 && (
              <Button
                variant="outline-secondary"
                size="sm"
                title="Print Gate Pass"
              >
                <FaPrint />
              </Button>
            )}
          </div>
        );
      },
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading issue items: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="issue-confirmation">
      <PageHeader
        title="Issue Confirmation"
        subtitle="Confirm material issue from store to departments"
      />

      {/* Stats */}
      <Row className="mb-3 justify-content-center">
        <Col md={4}>
          <Card className="bg-warning bg-opacity-10 border-warning">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-warning">15</h3>
              <div className="text-muted small">Pending Issue</div>
            </Card.Body>
          </Card>
        </Col>
        <Col md={4}>
          <Card className="bg-success bg-opacity-10 border-success">
            <Card.Body className="text-center py-3">
              <h3 className="mb-1 text-success">42</h3>
              <div className="text-muted small">Issued Today</div>
            </Card.Body>
          </Card>
        </Col>
        {/* SCOPE-REDUCTION: "Value Issued Today" card hidden — not active in current production phase */}
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
                <option value="PENDING_ISSUE">Pending Issue</option>
                <option value="ISSUED">Issued</option>
                <option value="PARTIAL_ISSUE">Partial Issue</option>
                <option value="">All Status</option>
              </Form.Select>
            </Col>
            <Col md={2}>
              <Button
                variant="outline-secondary"
                className="w-100"
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('PENDING_ISSUE');
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
        <LoadingSpinner text="Loading issue items..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={(issueData?.content || []) as unknown as Record<string, unknown>[]}
          keyField="id"
          totalItems={issueData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {/* Confirm Issue Modal */}
      <Modal show={showConfirmModal} onHide={() => setShowConfirmModal(false)}>
        <Modal.Header closeButton className="bg-success text-white">
          <Modal.Title><FaBoxOpen className="me-2" /> Confirm Issue</Modal.Title>
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
                    <th>Approved Quantity</th>
                    <td>{selectedItem.approvedQty} {selectedItem.uomCode}</td>
                  </tr>
                  <tr>
                    <th>Requested By</th>
                    <td>{selectedItem.requestedBy} ({selectedItem.departmentName})</td>
                  </tr>
                </tbody>
              </Table>

              <Form.Group className="mb-3">
                <Form.Label>Issue Quantity <span className="text-danger">*</span></Form.Label>
                <Form.Control
                  type="number"
                  value={issuedQty}
                  onChange={(e) => setIssuedQty(Number(e.target.value))}
                  max={selectedItem.approvedQty}
                  min={0}
                />
                <Form.Text className="text-muted">
                  Max: {selectedItem.approvedQty} {selectedItem.uomCode}
                </Form.Text>
              </Form.Group>

              <Form.Group>
                <Form.Label>Remarks</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={2}
                  value={issueRemarks}
                  onChange={(e) => setIssueRemarks(e.target.value)}
                  placeholder="Any additional remarks..."
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
            variant="success"
            onClick={submitConfirmation}
            disabled={confirmIssueMutation.isPending}
          >
            {confirmIssueMutation.isPending ? 'Processing...' : 'Confirm Issue'}
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default IssueConfirmationPage;
