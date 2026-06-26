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
import { FaPlus, FaTrash, FaSave, FaPaperPlane, FaArrowLeft, FaSearch, FaEdit, FaTimes } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { indentsApi, materialsApi, uomApi, companiesApi, departmentsApi, plantsApi, sectionsApi, getErrorMessage } from '../../api';
import type { IndentFormMeta } from '../../api/indents';
import { inventoryApi } from '../../api/inventory';
import type { MaterialDropdownItem } from '../../api/materials';
import { useAuth } from '../../contexts/AuthContext';

// Validation schema
const indentItemSchema = z.object({
  materialId: z.number().min(1, 'Material is required'),
  materialCode: z.string().optional(),
  materialDescription: z.string().optional(),
  uomId: z.number().min(1, 'UOM is required'),
  uomCode: z.string().optional(),
  requestedQuantity: z.number().min(0.01, 'Quantity must be greater than 0'),
  estimatedRate: z.number().min(0).optional(),
  remarks: z.string().optional(),
  vendor: z.string().optional(),
});

const indentFormSchema = z.object({
  companyId: z.number().min(1, 'Company is required'),
  departmentId: z.number().min(1, 'Department is required'),
  sectionId: z.number().min(0),
  plantId: z.number().min(1, 'Plant is required'),
  comments: z.string().optional(),
  deliveryDate: z.string().optional(),
  items: z.array(indentItemSchema).min(1, 'At least one item is required'),
});

type IndentFormData = z.infer<typeof indentFormSchema>;

const IndentFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const isEdit = !!id;

  const [error, setError] = useState<string | null>(null);
  const [materialSearch, setMaterialSearch] = useState('');
  const [showMaterialSearch, setShowMaterialSearch] = useState(false);
  const [selectedItemIndex, setSelectedItemIndex] = useState<number | null>(null);
  const [dropdownAnchor, setDropdownAnchor] = useState<{ top: number; left: number } | null>(null);
  // index → material company/stock info from the dropdown selection
  type ItemCompanyInfo = { companyId: number; companyName: string; plantId: number; plantName: string; stock: number | null };
  const [itemCompanyMap, setItemCompanyMap] = useState<Record<number, ItemCompanyInfo>>({});
  // legacy stock state kept for plant-change re-fetch compatibility
  const [stockByIndex, setStockByIndex] = useState<Record<number, number | null>>({});
  const [formMeta, setFormMeta] = useState<IndentFormMeta | null>(null);

  // Form setup
  const {
    register,
    control,
    handleSubmit,
    watch,
    setValue,
    getValues,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<IndentFormData>({
    resolver: zodResolver(indentFormSchema),
    defaultValues: {
      companyId: 0,
      departmentId: user?.departmentId || 0,
      sectionId: 0,
      plantId: user?.plantId || 0,
      comments: '',
      deliveryDate: '',
      items: [{ materialId: 0, materialCode: '', materialDescription: '', uomId: 0, uomCode: '', requestedQuantity: 1, estimatedRate: 0, remarks: '', vendor: '' }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const watchItems = watch('items');
  const watchPlantId = watch('plantId');

  // Fetch existing indent for edit mode
  const { data: existingIndent, isLoading: loadingIndent } = useQuery({
    queryKey: ['indent', id],
    queryFn: () => indentsApi.getById(Number(id)),
    enabled: isEdit,
  });

  // Fetch materials for dropdown — fires only when user types ≥2 chars
  const { data: dropdownMaterials } = useQuery<MaterialDropdownItem[]>({
    queryKey: ['materials-dropdown', materialSearch],
    queryFn: () => materialsApi.dropdown(materialSearch),
    enabled: materialSearch.length >= 2,
    staleTime: 30000,
  });

  // Fetch UOMs for dropdown
  const { data: uomData } = useQuery({
    queryKey: ['uoms'],
    queryFn: () => uomApi.getAll(0, 100),
  });

  // Fetch companies
  const { data: companiesData } = useQuery({
    queryKey: ['companies-active'],
    queryFn: () => companiesApi.getActive(0, 100),
  });

  // Fetch departments
  const { data: departmentsData } = useQuery({
    queryKey: ['departments-active'],
    queryFn: () => departmentsApi.getActive(0, 100),
  });

  // Fetch plants
  const { data: plantsData } = useQuery({
    queryKey: ['plants-active'],
    queryFn: () => plantsApi.getActive(0, 100),
  });

  // Fetch sections
  const { data: sectionsData } = useQuery({
    queryKey: ['sections-active'],
    queryFn: () => sectionsApi.getActive(0, 100),
  });

  // Fetch form meta (employee info, financial year, next indent number)
  const { data: metaData } = useQuery({
    queryKey: ['indent-form-meta'],
    queryFn: indentsApi.getMeta,
    enabled: !isEdit,
    staleTime: 60000,
  });

  // Auto-fill form fields from meta on create mode
  useEffect(() => {
    if (!isEdit && metaData) {
      setFormMeta(metaData);
      if (metaData.departmentId) setValue('departmentId', metaData.departmentId);
      if (metaData.defaultCompanyId) setValue('companyId', metaData.defaultCompanyId);
    }
  }, [isEdit, metaData, setValue]);

  // Auto-fill section when sections load
  useEffect(() => {
    if (!isEdit && sectionsData?.content?.length) {
      setValue('sectionId', sectionsData.content[0].id);
    }
  }, [isEdit, sectionsData, setValue]);

  // Load existing data in edit mode
  useEffect(() => {
    if (existingIndent) {
      // Backend response uses 'details' (canonical field from IndentResponse)
      const detailItems = existingIndent.details ?? [];
      reset({
        companyId: (existingIndent as any).companyId || companiesData?.content?.[0]?.id || 0,
        departmentId: (existingIndent as any).departmentId || 0,
        plantId: (existingIndent as any).plantId || 0,
        sectionId: (existingIndent as any).sectionId || 0,
        comments: (existingIndent as any).comments || (existingIndent as any).remarks || (existingIndent as any).purpose || '',
        deliveryDate: (existingIndent as any).deliveryDate || (existingIndent as any).requiredDate || '',
        items: detailItems.map((item: any) => ({
          materialId: item.materialId,
          materialCode: item.materialCode,
          materialDescription: item.materialName || item.materialDescription || '',
          uomId: item.unitOfMeasureId || item.unitOfMeasureId || 0,
          uomCode: item.unitOfMeasureCode || item.uomCode || '',
          requestedQuantity: Number(item.quantity) || 1,
          estimatedRate: Number(item.pricing) || 0,
          remarks: item.purpose || item.remarks || '',
          vendor: item.vendor || '',
        })),
      });
    }
  }, [existingIndent, reset, companiesData]);

  // Re-fetch stock whenever the plant selection changes
  useEffect(() => {
    if (!watchPlantId || watchPlantId <= 0) return;
    const currentItems = getValues('items');
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

  // Helper function to transform form data to API format
  const transformFormData = (data: IndentFormData) => {
    // Get employeeId from user context - it might be stored as string
    const employeeId = user?.employeeNumber || (typeof user?.employeeId === 'string' ? parseInt(user.employeeId, 10) : user?.employeeId) || 0;
    
    return {
      companyId: data.companyId,
      departmentId: data.departmentId,
      sectionId: data.sectionId,
      plantId: data.plantId,
      employeeId: employeeId,
      comments: data.comments || '',
      deliveryDate: data.deliveryDate || null,
      details: data.items.map((item) => ({
        materialId: item.materialId,
        unitOfMeasureId: item.uomId,
        quantity: item.requestedQuantity,
        pricing: item.estimatedRate || 0,
        purpose: item.remarks || '',
        vendor: item.vendor || '',
      })),
    };
  };

  // Create/Update mutations
  const createMutation = useMutation({
    mutationFn: indentsApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indents'] });
      navigate('/indents');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const updateMutation = useMutation({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    mutationFn: ({ id, data }: { id: number; data: any }) =>
      indentsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indents'] });
      queryClient.invalidateQueries({ queryKey: ['indent', id] });
      navigate('/indents');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const submitMutation = useMutation({
    mutationFn: indentsApi.submit,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['indents'] });
      navigate('/indents');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Handle form submission
  const onSubmit = async (data: IndentFormData) => {
    setError(null);
    const formData = transformFormData(data);

    if (isEdit) {
      await updateMutation.mutateAsync({ id: Number(id), data: formData });
    } else {
      await createMutation.mutateAsync(formData);
    }
  };

  // Handle save and submit
  const handleSaveAndSubmit = async (data: IndentFormData) => {
    setError(null);
    try {
      let indentId = Number(id);
      
      const formData = transformFormData(data);

      if (isEdit) {
        await updateMutation.mutateAsync({ id: indentId, data: formData });
      } else {
        const created = await createMutation.mutateAsync(formData);
        indentId = created.id;
      }

      await submitMutation.mutateAsync(indentId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  // Handle material selection from the dropdown
  const selectMaterial = (item: MaterialDropdownItem, index: number) => {
    setValue(`items.${index}.materialId`, item.materialId, { shouldDirty: true, shouldValidate: true });
    setValue(`items.${index}.materialCode`, item.materialCode, { shouldDirty: true });
    setValue(`items.${index}.materialDescription`, item.materialName || '', { shouldDirty: true });
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

  // Calculate totals
  const calculateTotal = () => {
    return watchItems.reduce((sum, item) => {
      return sum + (item.requestedQuantity || 0) * (item.estimatedRate || 0);
    }, 0);
  };

  if (isEdit && loadingIndent) {
    return <LoadingSpinner fullPage text="Loading indent..." />;
  }

  return (
    <div>
      <PageHeader
        title={isEdit ? 'Edit Indent' : 'Create New Indent'}
        subtitle={isEdit ? `Editing ${existingIndent?.indentNumber}` : 'Submit a new purchase indent request'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Indents', path: '/indents' },
          { label: isEdit ? 'Edit' : 'New' },
        ]}
        actions={
          <Button variant="outline-secondary" onClick={() => navigate('/indents')}>
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
        <Card className="mb-4 shadow-sm">
          <Card.Header className="bg-light">
            <h5 className="mb-0">
              <FaEdit className="me-2 text-primary" />
              Basic Information
            </h5>
          </Card.Header>
          <Card.Body>
            {/* Read-only reference info — only shown when creating a new indent */}
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
                  <small className="text-muted d-block fw-semibold">Indent No. (Preview)</small>
                  <span className="fw-bold text-success">{formMeta.nextIndentNumber}</span>
                  <small className="text-muted ms-1">(auto-assigned on save)</small>
                </Col>
              </Row>
            )}
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Company <span className="text-danger">*</span></Form.Label>
                  <Form.Select
                    {...register('companyId', { valueAsNumber: true })}
                    isInvalid={!!errors.companyId}
                  >
                    <option value={0}>Select Company...</option>
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
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Department <span className="text-danger">*</span></Form.Label>
                  <Form.Select 
                    {...register('departmentId', { valueAsNumber: true })} 
                    isInvalid={!!errors.departmentId}
                  >
                    <option value={0}>Select Department...</option>
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
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Plant <span className="text-danger">*</span></Form.Label>
                  <Form.Select 
                    {...register('plantId', { valueAsNumber: true })} 
                    isInvalid={!!errors.plantId}
                  >
                    <option value={0}>Select Plant...</option>
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
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Section <span className="text-danger">*</span></Form.Label>
                  <Form.Select 
                    {...register('sectionId', { valueAsNumber: true })} 
                    isInvalid={!!errors.sectionId}
                  >
                    <option value={0}>Select Section...</option>
                    {sectionsData?.content?.map((section) => (
                      <option key={section.id} value={section.id}>
                        {section.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">
                    {errors.sectionId?.message}
                  </Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Delivery Date</Form.Label>
                  <Form.Control
                    type="date"
                    {...register('deliveryDate')}
                    min={new Date().toISOString().split('T')[0]}
                  />
                </Form.Group>
              </Col>
              <Col md={4}>
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

        {/* Items */}
        <Card className="mb-4 shadow-sm">
          <Card.Header className="d-flex justify-content-between align-items-center bg-light">
            <h5 className="mb-0">
              <FaPlus className="me-2 text-primary" />
              Line Items
            </h5>
            <Button
              variant="success"
              size="sm"
              onClick={() =>
                append({ materialId: 0, materialCode: '', materialDescription: '', uomId: 0, uomCode: '', requestedQuantity: 1, estimatedRate: 0, remarks: '', vendor: '' })
              }
            >
              <FaPlus className="me-1" /> Add Item
            </Button>
          </Card.Header>
          <Card.Body className="p-0">
            <div style={{ overflowX: 'auto', overflowY: 'visible' }}>
              <Table className="mb-0 align-middle" hover>
                <thead className="table-primary">
                  <tr>
                    <th style={{ width: '50px' }} className="text-center">#</th>
                    <th style={{ minWidth: '280px' }}>Material</th>
                    <th style={{ width: '80px' }} className="text-center">UOM</th>
                    <th style={{ width: '100px' }} className="text-center">Qty</th>
                    <th style={{ width: '150px' }}>Purpose</th>
                    <th style={{ width: '150px' }}>Vendor</th>
                    <th style={{ width: '50px' }}></th>
                  </tr>
                </thead>
                <tbody>
                  {fields.map((field, index) => (
                    <tr key={field.id}>
                      <td className="text-center fw-medium">{index + 1}</td>
                      <td style={{ minWidth: '280px' }}>
                        <div className="position-relative">
                          {watchItems[index]?.materialCode ? (
                            <div className="d-flex align-items-center gap-2 p-2 bg-light rounded border">
                              <div className="flex-grow-1">
                                <div className="fw-semibold text-primary">
                                  {watchItems[index].materialCode}
                                </div>
                                <small className="text-muted d-block" style={{ lineHeight: 1.3 }}>
                                  {watchItems[index].materialDescription}
                                </small>
                                {itemCompanyMap[index] && (
                                  <small className="text-muted d-block" style={{ lineHeight: 1.3 }}>
                                    {itemCompanyMap[index].companyName} · {itemCompanyMap[index].plantName}
                                  </small>
                                )}
                                {stockByIndex[index] !== undefined && (
                                  <small
                                    className={`d-block fw-medium mt-1 ${
                                      stockByIndex[index] !== null && (stockByIndex[index] ?? 0) > 0
                                        ? 'text-success'
                                        : 'text-danger'
                                    }`}
                                  >
                                    Stock:{' '}
                                    {stockByIndex[index] !== null ? stockByIndex[index] : 'N/A'}
                                  </small>
                                )}
                              </div>
                              <Button
                                variant="outline-danger"
                                size="sm"
                                className="px-2 py-1"
                                onClick={() => {
                                  setValue(`items.${index}.materialId`, 0);
                                  setValue(`items.${index}.materialCode`, '');
                                  setValue(`items.${index}.materialDescription`, '');
                                  setValue(`items.${index}.uomId`, 0);
                                  setValue(`items.${index}.uomCode`, '');
                                }}
                                title="Change material"
                              >
                                <FaTimes />
                              </Button>
                            </div>
                          ) : (
                            <InputGroup>
                              <Form.Control
                                placeholder="Search material..."
                                value={selectedItemIndex === index ? materialSearch : ''}
                                onChange={(e) => {
                                  const rect = e.currentTarget.getBoundingClientRect();
                                  setDropdownAnchor({ top: rect.bottom, left: rect.left });
                                  setMaterialSearch(e.target.value);
                                  setSelectedItemIndex(index);
                                  setShowMaterialSearch(true);
                                }}
                                onFocus={(e) => {
                                  const rect = e.currentTarget.getBoundingClientRect();
                                  setDropdownAnchor({ top: rect.bottom, left: rect.left });
                                  setSelectedItemIndex(index);
                                  setShowMaterialSearch(true);
                                }}
                                onBlur={() => {
                                  // Delay to allow click on dropdown items
                                  setTimeout(() => {
                                    setShowMaterialSearch(false);
                                    setSelectedItemIndex(null);
                                  }, 200);
                                }}
                                isInvalid={!!errors.items?.[index]?.materialId}
                              />
                              <Button variant="outline-secondary" tabIndex={-1}>
                                <FaSearch />
                              </Button>
                            </InputGroup>
                          )}
                          
                          {/* Material search dropdown — fixed positioning escapes overflow containers */}
                          {showMaterialSearch && selectedItemIndex === index && dropdownAnchor && (
                            <div
                              className="bg-white border rounded shadow-lg"
                              style={{ position: 'fixed', top: dropdownAnchor.top, left: dropdownAnchor.left, minWidth: '400px', maxHeight: '250px', overflowY: 'auto', zIndex: 9999 }}
                            >
                              {dropdownMaterials && dropdownMaterials.length > 0 ? (
                                dropdownMaterials.map((item, idx) => {
                                  const desc = item.materialDescription ? ` (${item.materialDescription})` : '';
                                  const stock = item.stockQuantity !== null ? item.stockQuantity : 0;
                                  return (
                                    <div
                                      key={`${item.materialId}-${item.companyId}-${idx}`}
                                      className="p-2 border-bottom"
                                      style={{ cursor: 'pointer' }}
                                      onMouseDown={(e) => {
                                        e.preventDefault();
                                        e.stopPropagation();
                                        selectMaterial(item, index);
                                      }}
                                      onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#e9ecef'; }}
                                      onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'white'; }}
                                    >
                                      <div className="fw-semibold text-primary">[{item.materialCode}] {item.materialName}{desc}</div>
                                      <small className="text-muted d-block">
                                        Company: <strong>{item.companyName}</strong> | Plant: {item.plantName} | Stock: <span className={Number(stock) > 0 ? 'text-success fw-medium' : 'text-danger fw-medium'}>{stock}</span>
                                      </small>
                                    </div>
                                  );
                                })
                              ) : materialSearch.length >= 2 ? (
                                <div className="p-3 text-muted text-center small">No materials found for &ldquo;{materialSearch}&rdquo;</div>
                              ) : (
                                <div className="p-3 text-muted text-center small">Type at least 2 characters to search</div>
                              )}
                            </div>
                          )}
                        </div>
                        {errors.items?.[index]?.materialId && (
                          <div className="text-danger small mt-1">
                            {errors.items[index].materialId?.message}
                          </div>
                        )}
                      </td>
                      <td>
                        <Form.Select
                          size="sm"
                          {...register(`items.${index}.uomId`, { valueAsNumber: true })}
                          isInvalid={!!errors.items?.[index]?.uomId}
                          onChange={(e) => {
                            const uomId = Number(e.target.value);
                            setValue(`items.${index}.uomId`, uomId, { shouldDirty: true });
                            const selectedUom = uomData?.content?.find((u) => u.id === uomId);
                            setValue(`items.${index}.uomCode`, selectedUom?.code || '', { shouldDirty: true });
                          }}
                        >
                          <option value="0">Select UOM</option>
                          {uomData?.content?.map((uom) => (
                            <option key={uom.id} value={uom.id}>
                              {uom.code}
                            </option>
                          ))}
                        </Form.Select>
                        {errors.items?.[index]?.uomId && (
                          <div className="text-danger small mt-1">UOM required</div>
                        )}
                      </td>
                      <td>
                        <Form.Control
                          type="number"
                          step="0.01"
                          min="0.01"
                          size="sm"
                          className="text-center"
                          {...register(`items.${index}.requestedQuantity`, { valueAsNumber: true })}
                          isInvalid={!!errors.items?.[index]?.requestedQuantity}
                        />
                      </td>
                      <td>
                        <Form.Control
                          type="text"
                          size="sm"
                          {...register(`items.${index}.remarks`)}
                          placeholder="Purpose..."
                        />
                      </td>
                      <td>
                        <Form.Control
                          type="text"
                          size="sm"
                          {...register(`items.${index}.vendor`)}
                          placeholder="Vendor..."
                        />
                      </td>
                      <td className="text-center">
                        {fields.length > 1 && (
                          <Button
                            variant="outline-danger"
                            size="sm"
                            className="px-2"
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
              </Table>
            </div>
            {errors.items?.message && (
              <div className="text-danger p-3 bg-danger bg-opacity-10">{errors.items.message}</div>
            )}
          </Card.Body>
        </Card>

        {/* Actions */}
        <Card>
          <Card.Body className="d-flex flex-wrap gap-2 justify-content-between">
            <Button
              variant="outline-secondary"
              onClick={() => navigate('/indents')}
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

    </div>
  );
};

export default IndentFormPage;
