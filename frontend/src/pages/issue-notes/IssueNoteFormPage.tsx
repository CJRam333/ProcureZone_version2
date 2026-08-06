import React, { useState, useEffect, useRef } from 'react';
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
import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import { FaPlus, FaTrash, FaSave, FaPaperPlane, FaArrowLeft, FaSearch } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { issueNotesApi, materialsApi, uomApi, getErrorMessage } from '../../api';
import type { MaterialDropdownItem } from '../../api/materials';
import { useAuth } from '../../contexts/AuthContext';

// Validation schema - updated to match backend DTO
const issueNoteLineItemSchema = z.object({
  materialId: z.number().min(1, 'Material is required'),
  materialCode: z.string().optional(),
  materialDescription: z.string().optional(),
  uomCode: z.string().optional(),
  unitOfMeasureId: z.number().min(1, 'UOM is required'),
  quantity: z.number().min(0.01, 'Quantity must be greater than 0').max(99999, 'Quantity cannot exceed 99999'),
  purpose: z.string().optional(),
});

const issueNoteFormSchema = z.object({
  // company / department / plant / section are captured server-side from the employee record —
  // no longer collected on the form, so they are optional here.
  companyId: z.number().optional(),
  departmentId: z.number().optional(),
  sectionId: z.number().optional(),
  plantId: z.number().optional(),
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
  // Debounced copy of materialSearch that actually drives the query. Kept separate so typing
  // is responsive while requests are throttled, and so clearing can reset instantly.
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [showMaterialSearch, setShowMaterialSearch] = useState(false);
  const [selectedItemIndex, setSelectedItemIndex] = useState<number | null>(null);
  const [dropdownAnchor, setDropdownAnchor] = useState<{ top: number; left: number; width: number } | null>(null);
  // Element the open dropdown is anchored to — used to re-track its position on scroll/resize
  // so the fixed-position dropdown stays glued to the input instead of floating away.
  const dropdownInputRef = useRef<HTMLElement | null>(null);
  const [stockByIndex, setStockByIndex] = useState<Record<number, number | null>>({});
  type ItemCompanyInfo = { companyId: number; companyName: string; plantId: number; plantName: string; stock: number | null };
  const [itemCompanyMap, setItemCompanyMap] = useState<Record<number, ItemCompanyInfo>>({});

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
      purpose: '',
      comments: '',
      lineItems: [{ materialId: 0, materialCode: '', materialDescription: '', uomCode: '', unitOfMeasureId: 0, quantity: 1, purpose: '' }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'lineItems',
  });

  // Keep the fixed-position material dropdown attached to its input while the page (or any
  // nested scroll container) scrolls or the window resizes. Capture-phase listener catches
  // scrolls on ancestor containers, not just window.
  useEffect(() => {
    if (!showMaterialSearch) return;
    const reposition = () => {
      const el = dropdownInputRef.current;
      if (!el) return;
      const rect = el.getBoundingClientRect();
      setDropdownAnchor({ top: rect.bottom, left: rect.left, width: rect.width });
    };
    window.addEventListener('scroll', reposition, true);
    window.addEventListener('resize', reposition);
    return () => {
      window.removeEventListener('scroll', reposition, true);
      window.removeEventListener('resize', reposition);
    };
  }, [showMaterialSearch]);

  // Debounce the material search (~300ms). Clearing the box resets the debounced term
  // immediately (no lingering stale results). Because the timer is cleared and rescheduled on
  // every change, the search reliably fires again after clearing and retyping — no blur/refocus.
  useEffect(() => {
    if (materialSearch === '') {
      setDebouncedSearch('');
      return;
    }
    const timer = setTimeout(() => setDebouncedSearch(materialSearch), 300);
    return () => clearTimeout(timer);
  }, [materialSearch]);

  const watchLineItems = watch('lineItems');

  // company/department/plant/section are captured server-side from the employee record now —
  // no dropdowns on this form, so the related master-data queries were removed. Only UOM
  // (used by line items) remains.
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

  // Fetch materials for dropdown — fires whenever the search popup is open
  const { data: dropdownMaterials, isFetching: isSearching } = useQuery<MaterialDropdownItem[]>({
    queryKey: ['materials-dropdown', debouncedSearch],
    queryFn: () => materialsApi.dropdown(debouncedSearch),
    enabled: showMaterialSearch,
    staleTime: 30000,
    // Results are keyed by the debounced term, so a late response for an older term lands in its
    // own cache entry and can never overwrite the current term's results (out-of-order guard).
    // keepPreviousData shows the prior list until the new one arrives, preventing flicker.
    placeholderData: keepPreviousData,
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
      // Repopulate the per-line company map from the captured companyId so editing a draft doesn't
      // drop the selected company (the update path rebuilds details from the submitted payload).
      const companyMap: Record<number, ItemCompanyInfo> = {};
      (existingIssueNote.details ?? []).forEach((item, index) => {
        if (item.companyId) {
          companyMap[index] = {
            companyId: item.companyId,
            companyName: item.companies || '',
            plantId: 0,
            plantName: '',
            stock: item.storesBalance != null ? Number(item.storesBalance) : null,
          };
        }
      });
      setItemCompanyMap(companyMap);
    }
  }, [existingIssueNote, reset]);

  // Optional fallback only: seed company/department from meta if present, so the backend has a
  // hint if the employee record can't resolve them. The fields aren't shown on the form.
  useEffect(() => {
    if (!isEdit && metaData) {
      if (metaData.departmentId) setValue('departmentId', metaData.departmentId);
      if (metaData.defaultCompanyId) setValue('companyId', metaData.defaultCompanyId);
    }
  }, [isEdit, metaData, setValue]);

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
      // Captured server-side from the employee record; sent only as an optional fallback (never 0).
      companyId: data.companyId || undefined,
      departmentId: data.departmentId || undefined,
      sectionId: data.sectionId || undefined,
      plantId: data.plantId || undefined,
      purpose: data.purpose,
      comments: data.comments,
      lineItems: data.lineItems.map((item, index) => ({
        materialId: item.materialId,
        // the specific company selected with this material in the dropdown (captured in itemCompanyMap)
        companyId: itemCompanyMap[index]?.companyId ?? undefined,
        unitOfMeasureId: item.unitOfMeasureId,
        quantity: item.quantity,
        quantityStores: stockByIndex[index] ?? undefined,
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
        // Captured server-side from the employee record; sent only as an optional fallback (never 0).
        companyId: data.companyId || undefined,
        departmentId: data.departmentId || undefined,
        sectionId: data.sectionId || undefined,
        plantId: data.plantId || undefined,
        purpose: data.purpose,
        comments: data.comments,
        lineItems: data.lineItems.map((item, index) => ({
          materialId: item.materialId,
          // the specific company selected with this material in the dropdown (captured in itemCompanyMap)
          companyId: itemCompanyMap[index]?.companyId ?? undefined,
          unitOfMeasureId: item.unitOfMeasureId,
          quantity: item.quantity,
          purpose: item.purpose,
          quantityStores: stockByIndex[index] ?? undefined,
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
        {/* Minimal reference line — company/department/plant/section are captured server-side
            from the employee record, so no dropdowns here. Just show FY, date and the next number. */}
        {!isEdit && metaData && (
          <div className="text-muted small mb-3">
            <strong>Issue Note No:</strong> {metaData.nextIssueNoteNumber}
            {' · '}<strong>Date:</strong> {metaData.date}
            {' · '}<strong>FY:</strong> {metaData.financialYear}
          </div>
        )}

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
                                  dropdownInputRef.current = e.currentTarget;
                                  const rect = e.currentTarget.getBoundingClientRect();
                                  setDropdownAnchor({ top: rect.bottom, left: rect.left, width: rect.width });
                                  setMaterialSearch(e.target.value);
                                  setSelectedItemIndex(index);
                                  setShowMaterialSearch(true);
                                }}
                                onFocus={(e) => {
                                  dropdownInputRef.current = e.currentTarget;
                                  const rect = e.currentTarget.getBoundingClientRect();
                                  setDropdownAnchor({ top: rect.bottom, left: rect.left, width: rect.width });
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

                          {/* Material Search Dropdown — fixed positioning escapes overflow containers */}
                          {showMaterialSearch && selectedItemIndex === index && dropdownAnchor && (
                            <div
                              className="bg-white border rounded shadow-lg"
                              style={{ position: 'fixed', top: dropdownAnchor.top, left: dropdownAnchor.left, width: dropdownAnchor.width, minWidth: '360px', maxHeight: '250px', overflowY: 'auto', zIndex: 9999 }}
                            >
                              {isSearching && (
                                <div className="p-2 text-muted text-center small border-bottom">
                                  <Spinner as="span" animation="border" size="sm" className="me-2" />
                                  Searching...
                                </div>
                              )}
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
                                    <div className="fw-semibold text-primary">{item.materialName}</div>
                                    {item.materialDescription && (
                                      <small className="text-muted d-block" style={{ lineHeight: 1.3 }}>
                                        {item.materialDescription}
                                      </small>
                                    )}
                                    <small className="text-muted d-block">
                                      Company: <strong>{item.companyName}</strong> | Plant: {item.plantName} | Stock: <span className={(item.stockQuantity ?? 0) > 0 ? 'text-success fw-medium' : 'text-danger fw-medium'}>{item.stockQuantity ?? 0}</span>
                                    </small>
                                  </div>
                                ))
                              ) : (
                                !isSearching && (
                                  <div className="p-3 text-muted text-center small">
                                    {debouncedSearch ? `No materials found for "${debouncedSearch}"` : 'No materials found'}
                                  </div>
                                )
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
                          min={0.01}
                          max={99999}
                          {...register(`lineItems.${index}.quantity`, { valueAsNumber: true })}
                          isInvalid={!!errors.lineItems?.[index]?.quantity}
                        />
                        {errors.lineItems?.[index]?.quantity && (
                          <div className="text-danger small mt-1">
                            {errors.lineItems[index].quantity?.message}
                          </div>
                        )}
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

        {/* Additional Information */}
        <Card className="mb-4">
          <Card.Header>
            <h5 className="mb-0">Additional Information</h5>
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
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
              <Col md={12}>
                <Form.Group>
                  <Form.Label>Comments</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    {...register('comments')}
                    placeholder="Any additional comments..."
                  />
                </Form.Group>
              </Col>
            </Row>
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
