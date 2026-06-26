import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  Form,
  Button,
  Card,
  Row,
  Col,
  Table,
  InputGroup,
  Spinner,
  Alert,
} from 'react-bootstrap';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaPlus, FaTrash, FaSave, FaPaperPlane, FaArrowLeft, FaSearch } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { issueNotesApi, materialsApi, companiesApi, departmentsApi, plantsApi, sectionsApi, uomApi, getErrorMessage } from '../../api';
import { inventoryApi } from '../../api/inventory';
import type { MaterialDropdownItem } from '../../api/materials';
import type { IssueNoteFormMeta } from '../../api/issueNotes';
import { useAuth } from '../../contexts/AuthContext';

// Validation schema - updated to match backend DTO
const issueNoteLineItemSchema = z.object({
  materialId: z.number().min(1, 'Material is required'),
  materialCode: z.string().optional(),
  materialDescription: z.string().optional(),
  uomCode: z.string().optional(),
  unitOfMeasureId: z.number().min(1, 'UOM is required'),
  quantity: z.number().min(0.01, 'Quantity must be greater than 0'),
  purpose: z.string().optional(),
});

const issueNoteFormSchema = z.object({
  companyId: z.number().min(1, 'Company is required'),
  departmentId: z.number().min(1, 'Department is required'),
  sectionId: z.number().optional(),
  plantId: z.number().min(1, 'Plant is required'),
  issuedTo: z.string().min(1, 'Issued To is required'),
  purpose: z.string().optional(),
  comments: z.string().optional(),
  lineItems: z.array(issueNoteLineItemSchema).min(1, 'At least one item is required'),
});

type IssueNoteFormData = z.infer<typeof issueNoteFormSchema>;

const IssueNoteFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const isEdit = !!id;

  const [error, setError] = useState<string | null>(null);
  const [materialSearch, setMaterialSearch] = useState('');
  const [showMaterialSearch, setShowMaterialSearch] = useState(false);
  const [selectedItemIndex, setSelectedItemIndex] = useState<number | null>(null);
  const [stockByIndex, setStockByIndex] = useState<Record<number, number | null>>({});
  type ItemCompanyInfo = { companyId: number; companyName: string; plantId: number; plantName: string; stock: number | null };
  const [itemCompanyMap, setItemCompanyMap] = useState<Record<number, ItemCompanyInfo>>({});
  const [formMeta, setFormMeta] = useState<IssueNoteFormMeta | null>(null);

  // Form setup - updated for new schema
  const {
    register,
    control,
    handleSubmit,
    watch,
    setValue,
    getValues,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<IssueNoteFormData>({
    resolver: zodResolver(issueNoteFormSchema),
    defaultValues: {
      companyId: 0,
      departmentId: 0,
      sectionId: undefined,
      plantId: 0,
      issuedTo: '',
      purpose: '',
      comments: '',
      lineItems: [{ materialId: 0, materialCode: '', materialDescription: '', uomCode: '', unitOfMeasureId: 0, quantity: 1, purpose: '' }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'lineItems',
  });

  const watchLineItems = watch('lineItems');
  const watchCompanyId = watch('companyId');
  const watchDepartmentId = watch('departmentId');
  const watchPlantId = watch('plantId');

  // Fetch master data for dropdowns
  const { data: companiesData } = useQuery({
    queryKey: ['companies'],
    queryFn: () => companiesApi.getAll(0, 100),
  });

  const { data: departmentsData } = useQuery({
    queryKey: ['departments', watchCompanyId],
    queryFn: () => departmentsApi.getAll(0, 100),
    enabled: watchCompanyId > 0,
  });

  const { data: plantsData } = useQuery({
    queryKey: ['plants', watchCompanyId],
    queryFn: () => plantsApi.getAll(0, 100),
    enabled: watchCompanyId > 0,
  });

  const { data: sectionsData } = useQuery({
    queryKey: ['sections', watchDepartmentId],
    queryFn: () => sectionsApi.getAll(0, 100),
    enabled: watchDepartmentId > 0,
  });

  const { data: uomData } = useQuery({
    queryKey: ['uom'],
    queryFn: () => uomApi.getAll(0, 100),
  });

  // Fetch existing issue note for edit mode
  const { data: existingIssueNote, isLoading: loadingIssueNote } = useQuery({
    queryKey: ['issue-note', id],
    queryFn: () => issueNotesApi.getById(Number(id)),
    enabled: isEdit,
  });

  // Fetch materials for dropdown — fires only when user types ≥2 chars
  const { data: dropdownMaterials } = useQuery<MaterialDropdownItem[]>({
    queryKey: ['materials-dropdown', materialSearch],
    queryFn: () => materialsApi.dropdown(materialSearch),
    enabled: materialSearch.length >= 2,
    staleTime: 30000,
  });

  // Fetch form meta (employee info, financial year, next issue note number)
  const { data: metaData } = useQuery({
    queryKey: ['issue-note-form-meta'],
    queryFn: issueNotesApi.getMeta,
    enabled: !isEdit,
    staleTime: 60000,
  });

  // Load existing data in edit mode
  useEffect(() => {
    if (existingIssueNote) {
      reset({
        companyId: existingIssueNote.companyId || 0,
        departmentId: existingIssueNote.departmentId,
        sectionId: existingIssueNote.sectionId,
        plantId: existingIssueNote.plantId,
        issuedTo: existingIssueNote.issuedTo || '',
        purpose: existingIssueNote.purpose || '',
        comments: existingIssueNote.comments || '',
        lineItems: existingIssueNote.details?.map((item) => ({
          materialId: item.materialId,
          materialCode: '', // Will need to fetch material details separately
          materialDescription: '',
          uomCode: '',
          unitOfMeasureId: item.unitOfMeasureId,
          quantity: Number(item.quantity) || 1,
          purpose: item.purpose || '',
        })) || [{ materialId: 0, materialCode: '', materialDescription: '', uomCode: '', unitOfMeasureId: 0, quantity: 1, purpose: '' }],
      });
    }
  }, [existingIssueNote, reset]);

  // Auto-fill form fields from meta on create mode
  useEffect(() => {
    if (!isEdit && metaData) {
      setFormMeta(metaData);
      if (metaData.departmentId) setValue('departmentId', metaData.departmentId);
      if (metaData.defaultCompanyId) setValue('companyId', metaData.defaultCompanyId);
    }
  }, [isEdit, metaData, setValue]);

  // Re-fetch stock whenever the plant selection changes
  useEffect(() => {
    if (!watchPlantId || watchPlantId <= 0) return;
    const currentItems = getValues('lineItems');
    currentItems.forEach((item, index) => {
      if (item.materialId > 0) {
        inventoryApi
          .getStockByMaterialAndPlant(item.materialId, watchPlantId)
          .then((r) => setStockByIndex((prev) => ({ ...prev, [index]: r.availableStock })))
          .catch(() => setStockByIndex((prev) => ({ ...prev, [index]: null })));
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [watchPlantId]);

  // Create/Update mutations
  const createMutation = useMutation({
    mutationFn: issueNotesApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      navigate('/issue-notes');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<IssueNoteFormData> }) =>
      issueNotesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      queryClient.invalidateQueries({ queryKey: ['issue-note', id] });
      navigate('/issue-notes');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const submitMutation = useMutation({
    mutationFn: issueNotesApi.submit,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['issue-notes'] });
      navigate('/issue-notes');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Handle form submission
  const onSubmit = async (data: IssueNoteFormData) => {
    setError(null);
    const formData = {
      companyId: data.companyId,
      departmentId: data.departmentId,
      sectionId: data.sectionId || undefined,
      plantId: data.plantId,
      issuedTo: data.issuedTo,
      purpose: data.purpose,
      comments: data.comments,
      lineItems: data.lineItems.map((item) => ({
        materialId: item.materialId,
        unitOfMeasureId: item.unitOfMeasureId,
        quantity: item.quantity,
        purpose: item.purpose,
      })),
    };

    if (isEdit) {
      await updateMutation.mutateAsync({ id: Number(id), data: formData });
    } else {
      await createMutation.mutateAsync(formData);
    }
  };

  // Handle save and submit
  const handleSaveAndSubmit = async (data: IssueNoteFormData) => {
    setError(null);
    try {
      let issueNoteId = Number(id);

      const formData = {
        companyId: data.companyId,
        departmentId: data.departmentId,
        sectionId: data.sectionId || undefined,
        plantId: data.plantId,
        issuedTo: data.issuedTo,
        purpose: data.purpose,
        comments: data.comments,
        lineItems: data.lineItems.map((item) => ({
          materialId: item.materialId,
          unitOfMeasureId: item.unitOfMeasureId,
          quantity: item.quantity,
          purpose: item.purpose,
        })),
      };

      if (isEdit) {
        await updateMutation.mutateAsync({ id: issueNoteId, data: formData });
      } else {
        const created = await createMutation.mutateAsync(formData);
        issueNoteId = created.id;
      }

      await submitMutation.mutateAsync(issueNoteId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  // Handle material selection from dropdown — MaterialDropdownItem includes company, plant, stock info
  const selectMaterial = (item: MaterialDropdownItem, index: number) => {
    setValue(`lineItems.${index}.materialId`, item.materialId, { shouldDirty: true, shouldValidate: true });
    setValue(`lineItems.${index}.materialCode`, item.materialCode, { shouldDirty: true });
    setValue(`lineItems.${index}.materialDescription`, item.materialName || '', { shouldDirty: true });
    setShowMaterialSearch(false);
    setMaterialSearch('');
    setSelectedItemIndex(null);
    // Store company / stock info for this item
    setItemCompanyMap((prev) => ({
      ...prev,
      [index]: {
        companyId: item.companyId,
        companyName: item.companyName,
        plantId: item.plantId,
        plantName: item.plantName,
        stock: item.stockQuantity !== null ? Number(item.stockQuantity) : null,
      },
    }));
    setStockByIndex((prev) => ({ ...prev, [index]: item.stockQuantity !== null ? Number(item.stockQuantity) : null }));
  };

  if (isEdit && loadingIssueNote) {
    return <LoadingSpinner fullPage text="Loading issue note..." />;
  }

  return (
    <div>
      <PageHeader
        title={isEdit ? 'Edit Issue Note' : 'Create New Issue Note'}
        subtitle={isEdit ? `Editing ${existingIssueNote?.issueNoteNumber}` : 'Request materials for issue from stores'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Issue Notes', path: '/issue-notes' },
          { label: isEdit ? 'Edit' : 'New' },
        ]}
        actions={
          <Button variant="outline-secondary" onClick={() => navigate('/issue-notes')}>
            <FaArrowLeft className="me-2" /> Back to List
          </Button>
        }
      />

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      <Form onSubmit={handleSubmit(onSubmit)}>
        {/* Basic Information */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Issue Note Information</h5>
          </Card.Header>
          <Card.Body>
            {/* Read-only reference info — only shown when creating a new issue note */}
            {!isEdit && formMeta && (
              <Row className="g-3 mb-3 pb-3" style={{ borderBottom: '1px solid #dee2e6' }}>
                <Col md={4}>
                  <small className="text-muted d-block fw-semibold">Employee</small>
                  <span className="fw-bold">{formMeta.empName}</span>
                  <small className="text-muted ms-2">({formMeta.empId || formMeta.empNumber})</small>
                </Col>
                <Col md={2}>
                  <small className="text-muted d-block fw-semibold">Financial Year</small>
                  <span className="fw-bold text-primary">{formMeta.financialYear}</span>
                </Col>
                <Col md={2}>
                  <small className="text-muted d-block fw-semibold">Date</small>
                  <span className="fw-bold">{formMeta.date}</span>
                </Col>
                <Col md={4}>
                  <small className="text-muted d-block fw-semibold">Issue Note No. (Preview)</small>
                  <span className="fw-bold text-success">{formMeta.nextIssueNoteNumber}</span>
                  <small className="text-muted ms-1">(auto-assigned on save)</small>
                </Col>
              </Row>
            )}
            <Row className="g-3">
              {/* Company */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Company <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    {...register('companyId', { valueAsNumber: true })}
                    isInvalid={!!errors.companyId}
                  >
                    <option value={0}>Select Company</option>
                    {companiesData?.content?.map((company) => (
                      <option key={company.id} value={company.id}>
                        {company.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {errors.companyId?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>

              {/* Plant */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Plant <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    {...register('plantId', { valueAsNumber: true })}
                    isInvalid={!!errors.plantId}
                    disabled={!watchCompanyId}
                  >
                    <option value={0}>Select Plant</option>
                    {plantsData?.content?.map((plant) => (
                      <option key={plant.id} value={plant.id}>
                        {plant.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {errors.plantId?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>

              {/* Department */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Department <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    {...register('departmentId', { valueAsNumber: true })}
                    isInvalid={!!errors.departmentId}
                    disabled={!watchCompanyId}
                  >
                    <option value={0}>Select Department</option>
                    {departmentsData?.content?.map((dept) => (
                      <option key={dept.id} value={dept.id}>
                        {dept.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {errors.departmentId?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>

              {/* Section (Optional) */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Section</Form.Label>
                  <Form.Select
                    {...register('sectionId', { valueAsNumber: true })}
                    disabled={!watchDepartmentId}
                  >
                    <option value={0}>Select Section (Optional)</option>
                    {sectionsData?.content?.map((section) => (
                      <option key={section.id} value={section.id}>
                        {section.name}
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              {/* Issued To */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Issued To <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    {...register('issuedTo')}
                    isInvalid={!!errors.issuedTo}
                    placeholder="Person/Department receiving materials..."
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.issuedTo?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>

              {/* Purpose */}
              <Col md={6}>
                <Form.Group>
                  <Form.Label>Purpose</Form.Label>
                  <Form.Control
                    type="text"
                    {...register('purpose')}
                    placeholder="Purpose of issue..."
                  />
                </Form.Group>
              </Col>

              {/* Comments */}
              <Col md={12}>
                <Form.Group>
                  <Form.Label>Comments</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    {...register('comments')}
                    placeholder="Any additional comments..."
                  />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Line Items */}
        <Card className="mb-4">
          <Card.Header className="d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Materials to Issue</h5>
            <Button
              variant="primary"
              size="sm"
              onClick={() =>
                append({ materialId: 0, materialCode: '', materialDescription: '', uomCode: '', unitOfMeasureId: 0, quantity: 1, purpose: '' })
              }
            >
              <FaPlus className="me-1" /> Add Item
            </Button>
          </Card.Header>
          <Card.Body className="p-0">
            <div style={{ overflowX: 'auto', overflowY: 'visible' }}>
              <Table className="mb-0">
                <thead className="bg-light">
                  <tr>
                    <th style={{ width: '50px' }}>#</th>
                    <th style={{ minWidth: '300px' }}>Material</th>
                    <th style={{ width: '120px' }}>UOM</th>
                    <th style={{ width: '120px' }}>Quantity</th>
                    <th style={{ width: '150px' }}>Purpose</th>
                    <th style={{ width: '60px' }}></th>
                  </tr>
                </thead>
                <tbody>
                  {fields.map((field, index) => (
                    <tr key={field.id}>
                      <td className="align-middle text-center">{index + 1}</td>
                      <td>
                        <div className="position-relative">
                          {watchLineItems[index]?.materialCode ? (
                            <div className="d-flex align-items-center gap-2">
                              <div className="flex-grow-1">
                                <div className="fw-medium">{watchLineItems[index].materialCode}</div>
                                <small className="text-muted">
                                  {watchLineItems[index].materialDescription}
                                </small>
                                {stockByIndex[index] !== undefined && (
                                  <small
                                    className={`d-block fw-medium mt-1 ${
                                      stockByIndex[index] !== null && (stockByIndex[index] ?? 0) > 0
                                        ? 'text-success'
                                        : 'text-danger'
                                    }`}
                                  >
                                    Avail:{' '}
                                    {stockByIndex[index] !== null ? stockByIndex[index] : 'N/A'}
                                  </small>
                                )}
                              </div>
                              <Button
                                variant="link"
                                size="sm"
                                className="p-0"
                                onClick={() => {
                                  setValue(`lineItems.${index}.materialId`, 0);
                                  setValue(`lineItems.${index}.materialCode`, '');
                                  setValue(`lineItems.${index}.materialDescription`, '');
                                  setValue(`lineItems.${index}.uomCode`, '');
                                  setValue(`lineItems.${index}.unitOfMeasureId`, 0);
                                }}
                              >
                                Change
                              </Button>
                            </div>
                          ) : (
                            <InputGroup>
                              <Form.Control
                                placeholder="Search material..."
                                value={selectedItemIndex === index ? materialSearch : ''}
                                onChange={(e) => {
                                  setMaterialSearch(e.target.value);
                                  setSelectedItemIndex(index);
                                  setShowMaterialSearch(true);
                                }}
                                onFocus={() => {
                                  setSelectedItemIndex(index);
                                  setShowMaterialSearch(true);
                                }}
                                onBlur={() => {
                                  setTimeout(() => {
                                    setShowMaterialSearch(false);
                                    setSelectedItemIndex(null);
                                  }, 200);
                                }}
                                isInvalid={!!errors.lineItems?.[index]?.materialId}
                              />
                              <Button variant="outline-secondary" tabIndex={-1}>
                                <FaSearch />
                              </Button>
                            </InputGroup>
                          )}

                          {/* Material Search Dropdown */}
                          {showMaterialSearch && selectedItemIndex === index && (
                            <div
                              className="position-absolute bg-white border rounded shadow-lg"
                              style={{ zIndex: 9999, maxHeight: '250px', overflowY: 'auto', top: '100%', left: 0, minWidth: '360px' }}
                            >
                              {dropdownMaterials && dropdownMaterials.length > 0 ? (
                                dropdownMaterials.map((item) => (
                                  <div
                                    key={`${item.materialId}-${item.companyId}`}
                                    className="p-2 border-bottom"
                                    style={{ cursor: 'pointer', transition: 'background-color 0.15s ease' }}
                                    onMouseDown={(e) => {
                                      e.preventDefault();
                                      e.stopPropagation();
                                      selectMaterial(item, index);
                                    }}
                                    onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#e9ecef'; }}
                                    onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'white'; }}
                                  >
                                    <div className="fw-semibold text-primary">[{item.materialCode}] {item.materialName}</div>
                                    <small className="text-muted d-block">
                                      Company: <strong>{item.companyName}</strong> | Plant: {item.plantName} | Stock: <span className={(item.stockQuantity ?? 0) > 0 ? 'text-success fw-medium' : 'text-danger fw-medium'}>{item.stockQuantity ?? 0}</span>
                                    </small>
                                  </div>
                                ))
                              ) : materialSearch.length >= 2 ? (
                                <div className="p-3 text-muted text-center small">No materials found for &ldquo;{materialSearch}&rdquo;</div>
                              ) : (
                                <div className="p-3 text-muted text-center small">Type at least 2 characters to search</div>
                              )}
                            </div>
                          )}
                        </div>
                        {errors.lineItems?.[index]?.materialId && (
                          <div className="text-danger small mt-1">
                            {errors.lineItems[index].materialId?.message}
                          </div>
                        )}
                      </td>
                      <td>
                        <Form.Select
                          size="sm"
                          {...register(`lineItems.${index}.unitOfMeasureId`, { valueAsNumber: true })}
                          isInvalid={!!errors.lineItems?.[index]?.unitOfMeasureId}
                          onChange={(e) => {
                            const uomId = Number(e.target.value);
                            setValue(`lineItems.${index}.unitOfMeasureId`, uomId, { shouldDirty: true });
                            const selectedUom = uomData?.content?.find((u) => u.id === uomId);
                            setValue(`lineItems.${index}.uomCode`, selectedUom?.code || '', { shouldDirty: true });
                          }}
                        >
                          <option value={0}>Select</option>
                          {uomData?.content?.map((uom) => (
                            <option key={uom.id} value={uom.id}>{uom.code}</option>
                          ))}
                        </Form.Select>
                        {errors.lineItems?.[index]?.unitOfMeasureId && (
                          <div className="text-danger small mt-1">
                            UOM is required
                          </div>
                        )}
                      </td>
                      <td>
                        <Form.Control
                          type="number"
                          step="0.01"
                          min="0.01"
                          {...register(`lineItems.${index}.quantity`, { valueAsNumber: true })}
                          isInvalid={!!errors.lineItems?.[index]?.quantity}
                        />
                        {errors.lineItems?.[index]?.quantity && (
                          <div className="text-danger small mt-1">
                            {errors.lineItems[index].quantity?.message}
                          </div>
                        )}
                      </td>
                      <td>
                        <Form.Control
                          type="text"
                          {...register(`lineItems.${index}.purpose`)}
                          placeholder="Purpose"
                        />
                      </td>
                      <td className="align-middle text-center">
                        {fields.length > 1 && (
                          <Button
                            variant="outline-danger"
                            size="sm"
                            onClick={() => remove(index)}
                            title="Remove item"
                          >
                            <FaTrash />
                          </Button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-light">
                  <tr>
                    <td colSpan={3} className="text-end fw-bold">
                      Total Items:
                    </td>
                    <td className="fw-bold">
                      {fields.length}
                    </td>
                    <td colSpan={2}></td>
                  </tr>
                </tfoot>
              </Table>
            </div>
            {errors.lineItems?.message && (
              <div className="text-danger p-3">{errors.lineItems.message}</div>
            )}
          </Card.Body>
        </Card>

        {/* Actions */}
        <Card>
          <Card.Body className="d-flex flex-wrap gap-2 justify-content-between">
            <Button
              variant="outline-secondary"
              onClick={() => navigate('/issue-notes')}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <div className="d-flex gap-2">
              <Button
                type="submit"
                variant="secondary"
                disabled={isSubmitting || createMutation.isPending || updateMutation.isPending}
              >
                {(isSubmitting || createMutation.isPending || updateMutation.isPending) && (
                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                )}
                <FaSave className="me-2" /> Save as Draft
              </Button>
              <Button
                type="button"
                variant="primary"
                onClick={handleSubmit(handleSaveAndSubmit)}
                disabled={isSubmitting || createMutation.isPending || updateMutation.isPending || submitMutation.isPending}
              >
                {submitMutation.isPending && (
                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                )}
                <FaPaperPlane className="me-2" /> Save & Submit for Approval
              </Button>
            </div>
          </Card.Body>
        </Card>
      </Form>

      {/* Click outside to close material search */}
      {showMaterialSearch && (
        <div
          className="position-fixed top-0 start-0 w-100 h-100"
          style={{ zIndex: 999 }}
          onClick={() => {
            setShowMaterialSearch(false);
            setMaterialSearch('');
            setSelectedItemIndex(null);
          }}
        />
      )}
    </div>
  );
};

export default IssueNoteFormPage;
