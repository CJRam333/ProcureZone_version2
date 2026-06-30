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
import {
  FaPlus,
  FaTrash,
  FaSave,
  FaPaperPlane,
  FaArrowLeft,
  FaSearch,
  FaLeaf,
  FaIndustry,
} from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { plantIndentsApi, indentsApi, materialsApi, plantsApi, companiesApi, departmentsApi, getErrorMessage } from '../../api';
import type { Material } from '../../api/materials';
import { useAuth } from '../../contexts/AuthContext';

// Crop types for plant indent
const CROP_TYPES = [
  'Rice', 'Wheat', 'Maize', 'Cotton', 'Sugarcane', 'Pulses',
  'Oilseeds', 'Vegetables', 'Fruits', 'Spices', 'Others'
];

const CROP_GROUPS = [
  'Kharif', 'Rabi', 'Zaid', 'Perennial', 'Annual'
];

// Validation schema
const plantIndentItemSchema = z.object({
  materialId: z.number().min(1, 'Material is required'),
  materialCode: z.string().optional(),
  materialDescription: z.string().optional(),
  uomId: z.number().min(0).optional(),
  uomCode: z.string().optional(),
  requestedQuantity: z.number().min(0.01, 'Quantity must be greater than 0'),
  estimatedRate: z.number().min(0).optional(),
  remarks: z.string().optional(),
});

const plantIndentFormSchema = z.object({
  companyId: z.number().optional(),
  departmentId: z.number().optional(),
  plantId: z.number().min(1, 'Plant is required'),
  cropType: z.string().min(1, 'Crop type is required'),
  cropGroup: z.string().min(1, 'Crop group is required'),
  comments: z.string().optional(),
  deliveryDate: z.string().optional(),
  items: z.array(plantIndentItemSchema).min(1, 'At least one item is required'),
});

type PlantIndentFormData = z.infer<typeof plantIndentFormSchema>;

const PlantIndentFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const isEdit = !!id;

  const [error, setError] = useState<string | null>(null);
  const [materialSearch, setMaterialSearch] = useState('');
  const [showMaterialSearch, setShowMaterialSearch] = useState(false);
  const [selectedItemIndex, setSelectedItemIndex] = useState<number | null>(null);
  const [showPlant, setShowPlant] = useState(true);

  // Form setup
  const {
    register,
    control,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<PlantIndentFormData>({
    resolver: zodResolver(plantIndentFormSchema),
    defaultValues: {
      companyId: 0,
      departmentId: user?.departmentId || 0,
      plantId: user?.plantId || 0,
      cropType: '',
      cropGroup: '',
      comments: '',
      deliveryDate: '',
      items: [{ materialId: 0, materialCode: '', materialDescription: '', uomId: 0, uomCode: '', requestedQuantity: 1, estimatedRate: 0, remarks: '' }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'items',
  });

  const watchItems = watch('items');

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

  // Fetch form meta (employee info, company, department)
  const { data: metaData } = useQuery({
    queryKey: ['indent-form-meta'],
    queryFn: indentsApi.getMeta,
    enabled: !isEdit,
    staleTime: 60000,
  });

  // Auto-fill company and department from meta on create mode
  useEffect(() => {
    if (!isEdit && metaData) {
      if (metaData.defaultCompanyId) setValue('companyId', metaData.defaultCompanyId);
      if (metaData.departmentId) setValue('departmentId', metaData.departmentId);
    }
  }, [isEdit, metaData, setValue]);

  // Auto-select plant if only one available; hide field if none
  useEffect(() => {
    if (isEdit || !plantsData?.content) return;
    const plants = plantsData.content;
    if (plants.length === 0) {
      setShowPlant(false);
    } else if (plants.length === 1) {
      setValue('plantId', plants[0].id);
    }
  }, [isEdit, plantsData, setValue]);

  // Fetch materials for search
  const { data: materialsData } = useQuery({
    queryKey: ['materials', materialSearch],
    queryFn: () => materialsApi.list({ search: materialSearch, size: 20, isActive: true }),
    enabled: true,
  });

  // Fetch existing plant indent for edit mode
  const { data: existingIndent } = useQuery({
    queryKey: ['plant-indent', id],
    queryFn: () => plantIndentsApi.getById(Number(id)),
    enabled: isEdit,
  });

  // Populate form when editing
  useEffect(() => {
    if (isEdit && existingIndent) {
      reset({
        companyId: 0,
        departmentId: 0,
        plantId: existingIndent.plantId || 0,
        cropType: existingIndent.outputMaterial || existingIndent.cropTypeName || '',
        cropGroup: existingIndent.packProcess || '',
        comments: existingIndent.remarks || '',
        deliveryDate: existingIndent.outputDescription || '',
        items: existingIndent.details?.map(d => ({
          materialId: d.materialId,
          materialCode: d.materialCode || '',
          materialDescription: d.materialName || '',
          uomId: d.unitOfMeasureId,
          uomCode: d.uomCode || '',
          requestedQuantity: d.quantity || 1,
          estimatedRate: d.pricing || 0,
          remarks: d.purpose || '',
        })) || [{ materialId: 0, materialCode: '', materialDescription: '', uomId: 0, uomCode: '', requestedQuantity: 1, estimatedRate: 0, remarks: '' }],
      });
    }
  }, [isEdit, existingIndent, reset]);

  // Transform form data to Plant Indent API request format
  const transformFormData = (data: PlantIndentFormData) => {
    const employeeNumber = user?.employeeNumber?.toString() || user?.username || '';

    return {
      employeeNumber,
      plantId: data.plantId,
      remarks: data.comments || '',
      outputMaterial: data.cropType || '',        // Store crop type in outputMaterial
      packProcess: data.cropGroup || '',           // Store crop group in packProcess
      outputDescription: data.deliveryDate || '',  // Store delivery date
      details: data.items.map(item => ({
        materialId: item.materialId,
        unitOfMeasureId: item.uomId,
        quantity: item.requestedQuantity,
        pricing: item.estimatedRate || 0,
        purpose: item.remarks || '',
      })),
    };
  };

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: PlantIndentFormData) => {
      return plantIndentsApi.create(transformFormData(data));
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plant-indents'] });
      navigate('/plant-indent');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Submit mutation (create + submit in one step)
  const submitMutation = useMutation({
    mutationFn: async (data: PlantIndentFormData) => {
      const indent = await plantIndentsApi.create(transformFormData(data));
      return plantIndentsApi.submitForReview(indent.id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plant-indents'] });
      navigate('/plant-indent');
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const handleMaterialSelect = (material: Material, index: number) => {
    setValue(`items.${index}.materialId`, material.id, { shouldDirty: true, shouldValidate: true });
    setValue(`items.${index}.materialCode`, material.code, { shouldDirty: true });
    setValue(`items.${index}.materialDescription`, material.name || material.description, { shouldDirty: true });
    // Auto-set UOM from material
    if (material.uomId) {
      setValue(`items.${index}.uomId`, material.uomId, { shouldDirty: true });
      setValue(`items.${index}.uomCode`, material.uomCode || material.uomName || '', { shouldDirty: true });
    }
    setShowMaterialSearch(false);
    setMaterialSearch('');
    setSelectedItemIndex(null);
  };

  const calculateTotal = () => {
    return watchItems.reduce((sum, item) => {
      return sum + (item.requestedQuantity * (item.estimatedRate || 0));
    }, 0);
  };

  const onSubmit = (data: PlantIndentFormData) => {
    setError(null);
    createMutation.mutate(data);
  };

  const onSubmitAndSend = (data: PlantIndentFormData) => {
    setError(null);
    submitMutation.mutate(data);
  };

  return (
    <div className="plant-indent-form">
      <PageHeader
        title={isEdit ? 'Edit Plant Indent' : 'New Plant Indent'}
        subtitle="Create plant-specific material requisition with crop tracking"
        breadcrumbs={[
          { label: 'Plant Indents', path: '/plant-indent' },
          { label: isEdit ? 'Edit' : 'New', path: '' },
        ]}
      />

      {error && <Alert variant="danger" dismissible onClose={() => setError(null)}>{error}</Alert>}

      {/* Show validation errors */}
      {Object.keys(errors).length > 0 && (
        <Alert variant="warning">
          <strong>Please fix the following:</strong>
          <ul className="mb-0 mt-1">
            {errors.plantId && <li>{errors.plantId.message}</li>}
            {errors.cropType && <li>{errors.cropType.message}</li>}
            {errors.cropGroup && <li>{errors.cropGroup.message}</li>}
            {errors.items && typeof errors.items.message === 'string' && <li>{errors.items.message}</li>}
            {Array.isArray(errors.items) && errors.items.map((itemErr, i) => (
              itemErr && <li key={i}>Item {i + 1}: {itemErr.materialId?.message || itemErr.requestedQuantity?.message || 'Check item details'}</li>
            ))}
          </ul>
        </Alert>
      )}

      <Form onSubmit={handleSubmit(onSubmit)}>
        {/* Company, Department & Plant Info */}
        <Card className="mb-3 shadow-sm">
          <Card.Header className="bg-success text-white">
            <FaIndustry className="me-2" /> Company & Plant Information
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Company <span className="text-danger">*</span></Form.Label>
                  <Form.Select {...register('companyId', { valueAsNumber: true })} isInvalid={!!errors.companyId}>
                    <option value={0}>Select Company</option>
                    {companiesData?.content?.map((company) => (
                      <option key={company.id} value={company.id}>
                        {company.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.companyId?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Department <span className="text-danger">*</span></Form.Label>
                  <Form.Select {...register('departmentId', { valueAsNumber: true })} isInvalid={!!errors.departmentId}>
                    <option value={0}>Select Department</option>
                    {departmentsData?.content?.map((dept) => (
                      <option key={dept.id} value={dept.id}>
                        {dept.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.departmentId?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Plant <span className="text-danger">*</span></Form.Label>
                  <Form.Select {...register('plantId', { valueAsNumber: true })} isInvalid={!!errors.plantId}>
                    <option value={0}>Select Plant</option>
                    {plantsData?.content?.map((plant: { id: number; name: string; code: string }) => (
                      <option key={plant.id} value={plant.id}>
                        {plant.code} - {plant.name}
                      </option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.plantId?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Crop Info */}
        <Card className="mb-3 shadow-sm">
          <Card.Header className="bg-success text-white">
            <FaLeaf className="me-2" /> Crop Information
          </Card.Header>
          <Card.Body>
            <Row className="g-3">
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Crop Type <span className="text-danger">*</span></Form.Label>
                  <Form.Select {...register('cropType')} isInvalid={!!errors.cropType}>
                    <option value="">Select Crop Type</option>
                    {CROP_TYPES.map(type => (
                      <option key={type} value={type}>{type}</option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.cropType?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Crop Group <span className="text-danger">*</span></Form.Label>
                  <Form.Select {...register('cropGroup')} isInvalid={!!errors.cropGroup}>
                    <option value="">Select Crop Group</option>
                    {CROP_GROUPS.map(group => (
                      <option key={group} value={group}>{group}</option>
                    ))}
                  </Form.Select>
                  <Form.Control.Feedback type="invalid">{errors.cropGroup?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group>
                  <Form.Label>Delivery Date</Form.Label>
                  <Form.Control type="date" {...register('deliveryDate')} />
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* Line Items */}
        <Card className="mb-3 shadow-sm">
          <Card.Header className="d-flex justify-content-between align-items-center">
            <span>Material Items</span>
            <Button
              variant="outline-primary"
              size="sm"
              onClick={() => append({ materialId: 0, materialCode: '', materialDescription: '', uomId: 0, uomCode: '', requestedQuantity: 1, estimatedRate: 0, remarks: '' })}
            >
              <FaPlus className="me-1" /> Add Item
            </Button>
          </Card.Header>
          <Card.Body className="p-0">
            <Table responsive hover className="mb-0">
              <thead className="table-light">
                <tr>
                  <th style={{ width: '40px' }}>#</th>
                  <th style={{ width: '300px' }}>Material</th>
                  <th style={{ width: '80px' }}>UOM</th>
                  <th style={{ width: '100px' }}>Qty</th>
                  {/* <th style={{ width: '120px' }}>Est. Rate</th> */}
                  <th style={{ width: '120px' }}>Est. Value</th>
                  <th>Remarks</th>
                  <th style={{ width: '60px' }}></th>
                </tr>
              </thead>
              <tbody>
                {fields.map((field, index) => (
                  <tr key={field.id}>
                    <td className="align-middle">{index + 1}</td>
                    <td>
                      {watchItems[index]?.materialId ? (
                        <div>
                          <strong>{watchItems[index]?.materialCode}</strong>
                          <div className="text-muted small">{watchItems[index]?.materialDescription}</div>
                        </div>
                      ) : (
                        <div className="position-relative">
                          <InputGroup size="sm">
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
                            />
                            <Button variant="outline-secondary" tabIndex={-1}>
                              <FaSearch />
                            </Button>
                          </InputGroup>
                          {showMaterialSearch && selectedItemIndex === index && materialsData?.content && (
                            <div
                              className="position-absolute bg-white border rounded shadow-lg"
                              style={{ zIndex: 9999, width: '100%', maxHeight: '250px', overflowY: 'auto', top: '100%', left: 0 }}
                            >
                              {materialsData.content.map((material: Material) => (
                                <div
                                  key={material.id}
                                  className="p-2 border-bottom"
                                  style={{ cursor: 'pointer', transition: 'background-color 0.15s ease' }}
                                  onMouseDown={(e) => {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    handleMaterialSelect(material, index);
                                  }}
                                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#e9ecef'}
                                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'white'}
                                >
                                  <div className="fw-semibold text-primary">{material.code}</div>
                                  <small className="text-muted d-block">{material.name || material.description}</small>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                      {errors.items?.[index]?.materialId && (
                        <div className="text-danger small">{errors.items[index]?.materialId?.message}</div>
                      )}
                    </td>
                    <td className="align-middle">{watchItems[index]?.uomCode || '-'}</td>
                    <td>
                      <Form.Control
                        type="number"
                        size="sm"
                        step="0.01"
                        {...register(`items.${index}.requestedQuantity`, { valueAsNumber: true })}
                        isInvalid={!!errors.items?.[index]?.requestedQuantity}
                      />
                    </td>
                    {/* estimatedRate input hidden
                    <td>
                      <Form.Control
                        type="number"
                        size="sm"
                        step="0.01"
                        {...register(`items.${index}.estimatedRate`, { valueAsNumber: true })}
                      />
                    </td>
                    */}
                    <td className="align-middle">
                      ₹{((watchItems[index]?.requestedQuantity || 0) * (watchItems[index]?.estimatedRate || 0)).toLocaleString()}
                    </td>
                    <td>
                      <Form.Control
                        type="text"
                        size="sm"
                        {...register(`items.${index}.remarks`)}
                        placeholder="Remarks..."
                      />
                    </td>
                    <td className="align-middle">
                      {fields.length > 1 && (
                        <Button
                          variant="outline-danger"
                          size="sm"
                          onClick={() => remove(index)}
                        >
                          <FaTrash />
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot className="table-light">
                <tr>
                  <td colSpan={5} className="text-end fw-bold">Total Estimated Value:</td>
                  <td colSpan={3} className="fw-bold">₹{calculateTotal().toLocaleString()}</td>
                </tr>
              </tfoot>
            </Table>
          </Card.Body>
        </Card>

        {/* Additional Information */}
        <Card className="mb-3 shadow-sm">
          <Card.Header>Additional Information</Card.Header>
          <Card.Body>
            <Row className="g-3">
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
        <div className="d-flex justify-content-between">
          <Button variant="outline-secondary" onClick={() => navigate('/plant-indent')}>
            <FaArrowLeft className="me-1" /> Cancel
          </Button>
          <div>
            <Button
              type="submit"
              variant="secondary"
              className="me-2"
              disabled={isSubmitting || createMutation.isPending}
            >
              {createMutation.isPending ? (
                <Spinner animation="border" size="sm" className="me-1" />
              ) : (
                <FaSave className="me-1" />
              )}
              Save as Draft
            </Button>
            <Button
              variant="primary"
              onClick={handleSubmit(onSubmitAndSend)}
              disabled={isSubmitting || submitMutation.isPending}
            >
              {submitMutation.isPending ? (
                <Spinner animation="border" size="sm" className="me-1" />
              ) : (
                <FaPaperPlane className="me-1" />
              )}
              Submit for Approval
            </Button>
          </div>
        </div>
      </Form>
    </div>
  );
};

export default PlantIndentFormPage;
