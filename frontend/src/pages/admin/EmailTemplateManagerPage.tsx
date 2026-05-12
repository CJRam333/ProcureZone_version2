import React, { useState } from 'react';
import { Card, Form, Row, Col, Badge, Button, InputGroup, Modal, Table } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  FaSearch, FaEnvelope, FaEdit, FaEye, FaSave, FaTimes,
  FaCode, FaCheckCircle, FaTimesCircle
} from 'react-icons/fa';
import { PageHeader, DataTable, LoadingSpinner, Column } from '../../components/common';
import { getErrorMessage } from '../../api';
import apiClient from '../../api/client';
import { toast } from 'react-toastify';

interface EmailTemplate {
  id: number;
  code: string;          // backend field: template_code
  name: string;          // backend field: template_name
  subject: string;
  body: string;
  description?: string;
  type?: string;
  category?: string;
  status: number;        // 1=Active, 0=Inactive (backend integer)
  lastModifiedDate?: string;
  lastModifiedBy?: string;
}

// API functions for email templates
const emailTemplatesApi = {
  list: async (params: { search?: string; page?: number; size?: number }) => {
    const response = await apiClient.get('/email-templates', { params });
    return response.data;
  },
  getById: async (id: number) => {
    const response = await apiClient.get(`/email-templates/${id}`);
    return response.data;
  },
  update: async (id: number, data: Partial<EmailTemplate>) => {
    const response = await apiClient.put(`/email-templates/${id}`, data);
    return response.data;
  },
};

const EmailTemplateManagerPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  // Modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState<EmailTemplate | null>(null);
  const [editSubject, setEditSubject] = useState('');
  const [editBody, setEditBody] = useState('');

  // Fetch templates
  const { data: templatesData, isLoading, error } = useQuery({
    queryKey: ['email-templates', searchTerm, currentPage],
    queryFn: () => emailTemplatesApi.list({
      search: searchTerm || undefined,
      page: currentPage,
      size: pageSize,
    }),
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<EmailTemplate> }) =>
      emailTemplatesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['email-templates'] });
      toast.success('Template updated successfully');
      setShowEditModal(false);
    },
    onError: (err) => toast.error(getErrorMessage(err)),
  });

  const handleEdit = (template: EmailTemplate) => {
    setSelectedTemplate(template);
    setEditSubject(template.subject);
    setEditBody(template.body);
    setShowEditModal(true);
  };

  const handlePreview = (template: EmailTemplate) => {
    setSelectedTemplate(template);
    setShowPreviewModal(true);
  };

  const saveTemplate = () => {
    if (!selectedTemplate) return;

    updateMutation.mutate({
      id: selectedTemplate.id,
      data: {
        subject: editSubject,
        body: editBody,
      },
    });
  };

  // Available placeholders
  const placeholders = [
    '{{employeeName}}', '{{indentNumber}}', '{{poNumber}}', '{{grnNumber}}',
    '{{status}}', '{{date}}', '{{amount}}', '{{vendorName}}', '{{departmentName}}',
    '{{approverName}}', '{{remarks}}', '{{materialName}}', '{{quantity}}'
  ];

  const insertPlaceholder = (placeholder: string) => {
    setEditBody(editBody + ' ' + placeholder);
  };

  const columns = [
    {
      key: 'code',
      label: 'Code',
      render: (row: EmailTemplate) => (
        <Badge bg="secondary">{row.code}</Badge>
      ),
    },
    {
      key: 'name',
      label: 'Template Name',
      render: (row: EmailTemplate) => (
        <div>
          <strong>{row.name}</strong>
          {row.description && (
            <div className="text-muted small">{row.description}</div>
          )}
        </div>
      ),
    },
    {
      key: 'subject',
      label: 'Subject',
      render: (row: EmailTemplate) => (
        <span className="text-truncate d-inline-block" style={{ maxWidth: '200px' }}>
          {row.subject}
        </span>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      render: (row: EmailTemplate) => (
        <Badge bg={row.status === 1 ? 'success' : 'danger'}>
          {row.status === 1 ? (
            <><FaCheckCircle className="me-1" /> Active</>
          ) : (
            <><FaTimesCircle className="me-1" /> Inactive</>
          )}
        </Badge>
      ),
    },
    {
      key: 'lastModifiedDate',
      label: 'Last Updated',
      render: (row: EmailTemplate) => row.lastModifiedDate
        ? new Date(row.lastModifiedDate).toLocaleDateString()
        : '—',
    },
    {
      key: 'actions',
      label: 'Actions',
      render: (row: EmailTemplate) => (
        <div className="d-flex gap-1">
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => handlePreview(row)}
            title="Preview"
          >
            <FaEye />
          </Button>
          <Button
            variant="outline-info"
            size="sm"
            onClick={() => handleEdit(row)}
            title="Edit"
          >
            <FaEdit />
          </Button>
        </div>
      ),
    },
  ];

  if (error) {
    return (
      <div className="alert alert-danger">
        Error loading email templates: {getErrorMessage(error)}
      </div>
    );
  }

  return (
    <div className="email-template-manager">
      <PageHeader
        title="Email Templates"
        subtitle="Manage email notification templates"
      />

      {/* Info Card */}
      <Card className="mb-3 shadow-sm bg-info bg-opacity-10 border-info">
        <Card.Body>
          <Row className="align-items-center">
            <Col>
              <h6 className="mb-1"><FaCode className="me-2" /> Available Placeholders</h6>
              <div className="d-flex flex-wrap gap-1">
                {placeholders.map(p => (
                  <Badge key={p} bg="light" text="dark" className="fw-normal">
                    {p}
                  </Badge>
                ))}
              </div>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Filters */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="g-3 align-items-end">
            <Col md={6}>
              <InputGroup>
                <InputGroup.Text><FaSearch /></InputGroup.Text>
                <Form.Control
                  placeholder="Search by template name, code, or subject..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </InputGroup>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Data Table */}
      {isLoading ? (
        <LoadingSpinner text="Loading templates..." />
      ) : (
        <DataTable
          columns={columns as unknown as Column<Record<string, unknown>>[]}
          data={templatesData?.content || []}
          keyField="id"
          totalItems={templatesData?.totalElements || 0}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {/* Edit Modal */}
      <Modal show={showEditModal} onHide={() => setShowEditModal(false)} size="lg">
        <Modal.Header closeButton className="bg-info text-white">
          <Modal.Title><FaEdit className="me-2" /> Edit Template</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedTemplate && (
            <>
              <Row className="mb-3">
                <Col md={4}>
                  <strong>Code:</strong>
                  <div><Badge bg="secondary">{selectedTemplate.code}</Badge></div>
                </Col>
                <Col md={8}>
                  <strong>Name:</strong>
                  <div>{selectedTemplate.name}</div>
                </Col>
              </Row>

              <Form.Group className="mb-3">
                <Form.Label>Subject</Form.Label>
                <Form.Control
                  type="text"
                  value={editSubject}
                  onChange={(e) => setEditSubject(e.target.value)}
                />
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Body</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={10}
                  value={editBody}
                  onChange={(e) => setEditBody(e.target.value)}
                  style={{ fontFamily: 'monospace' }}
                />
              </Form.Group>

              <div className="mb-3">
                <Form.Label>Insert Placeholder:</Form.Label>
                <div className="d-flex flex-wrap gap-1">
                  {placeholders.map(p => (
                    <Badge
                      key={p}
                      bg="light"
                      text="primary"
                      className="cursor-pointer"
                      style={{ cursor: 'pointer' }}
                      onClick={() => insertPlaceholder(p)}
                    >
                      + {p}
                    </Badge>
                  ))}
                </div>
              </div>
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowEditModal(false)}>
            <FaTimes className="me-1" /> Cancel
          </Button>
          <Button
            variant="primary"
            onClick={saveTemplate}
            disabled={updateMutation.isPending}
          >
            <FaSave className="me-1" />
            {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Preview Modal */}
      <Modal show={showPreviewModal} onHide={() => setShowPreviewModal(false)} size="lg">
        <Modal.Header closeButton className="bg-secondary text-white">
          <Modal.Title><FaEye className="me-2" /> Template Preview</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {selectedTemplate && (
            <div className="email-preview border rounded p-3" style={{ backgroundColor: '#f9f9f9' }}>
              <div className="mb-3 pb-3 border-bottom">
                <strong>Subject:</strong>
                <div className="fs-5">{selectedTemplate.subject}</div>
              </div>
              <div>
                <strong>Body:</strong>
                <div
                  className="mt-2 p-3 bg-white border rounded"
                  style={{ whiteSpace: 'pre-wrap', fontFamily: 'sans-serif' }}
                  dangerouslySetInnerHTML={{ __html: selectedTemplate.body }}
                />
              </div>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowPreviewModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};

export default EmailTemplateManagerPage;
