import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    Form,
    Button,
    Card,
    Row,
    Col,
    Alert,
    Spinner,
    InputGroup,
} from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { FaSave, FaArrowLeft, FaBox, FaExchangeAlt } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { inventoryApi, getErrorMessage } from '../../api';

// Validation schema
const stockAdjustmentSchema = z.object({
    adjustmentType: z.enum(['ADD', 'SUBTRACT', 'SET']),
    quantity: z.number().min(0.01, 'Quantity must be greater than 0'),
    reason: z.string().min(10, 'Reason must be at least 10 characters'),
});

type StockAdjustmentFormData = z.infer<typeof stockAdjustmentSchema>;

const StockAdjustmentPage: React.FC = () => {
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();
    const queryClient = useQueryClient();
    const [error, setError] = useState<string | null>(null);

    // Parse the ID - it might be in the format "plantId-materialId" or just "id"
    const parseId = () => {
        if (!id) return { plantId: 0, materialId: 0 };
        if (id.includes('-')) {
            const parts = id.split('-');
            return { plantId: Number(parts[0]), materialId: Number(parts[1]) };
        }
        return { plantId: 1, materialId: Number(id) }; // Default plantId
    };

    const { plantId, materialId } = parseId();

    // Form setup
    const {
        register,
        handleSubmit,
        watch,
        formState: { errors, isSubmitting },
    } = useForm<StockAdjustmentFormData>({
        resolver: zodResolver(stockAdjustmentSchema),
        defaultValues: {
            adjustmentType: 'ADD',
            quantity: 0,
            reason: '',
        },
    });

    const watchAdjustmentType = watch('adjustmentType');
    const watchQuantity = watch('quantity');

    // Fetch inventory item
    const { data: inventoryItem, isLoading } = useQuery({
        queryKey: ['inventory-item', plantId, materialId],
        queryFn: () => inventoryApi.getByPlantAndMaterial(plantId, materialId),
        enabled: plantId > 0 && materialId > 0,
    });

    // Adjustment mutation
    const adjustMutation = useMutation({
        mutationFn: (data: StockAdjustmentFormData) =>
            inventoryApi.adjustStock({
                companyId: 1, // Default company ID
                plantId,
                materialId,
                adjustmentType: data.adjustmentType,
                quantity: data.quantity,
                reason: data.reason,
            }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['inventory'] });
            queryClient.invalidateQueries({ queryKey: ['inventory-item', plantId, materialId] });
            navigate('/inventory');
        },
        onError: (err) => setError(getErrorMessage(err)),
    });

    // Handle form submission
    const onSubmit = async (data: StockAdjustmentFormData) => {
        setError(null);
        await adjustMutation.mutateAsync(data);
    };

    // Calculate new quantity preview
    const calculateNewQuantity = () => {
        if (!inventoryItem) return 0;
        const current = inventoryItem.quantity || 0;
        const qty = watchQuantity || 0;
        switch (watchAdjustmentType) {
            case 'ADD':
                return current + qty;
            case 'SUBTRACT':
                return Math.max(0, current - qty);
            case 'SET':
                return qty;
            default:
                return current;
        }
    };

    if (isLoading) {
        return <LoadingSpinner fullPage text="Loading inventory item..." />;
    }

    if (!inventoryItem && plantId > 0 && materialId > 0) {
        return (
            <div className="text-center py-5">
                <Alert variant="danger">Inventory item not found</Alert>
                <Button variant="primary" onClick={() => navigate('/inventory')}>
                    Back to Inventory
                </Button>
            </div>
        );
    }

    return (
        <div>
            <PageHeader
                title="Stock Adjustment"
                subtitle={inventoryItem ? `Adjusting stock for ${inventoryItem.materialCode}` : 'Adjust inventory stock levels'}
                breadcrumbs={[
                    { label: 'Dashboard', path: '/dashboard' },
                    { label: 'Inventory', path: '/inventory' },
                    { label: 'Stock Adjustment' },
                ]}
                actions={
                    <Button variant="outline-secondary" onClick={() => navigate('/inventory')}>
                        <FaArrowLeft className="me-2" /> Back to Inventory
                    </Button>
                }
            />

            {error && (
                <Alert variant="danger" dismissible onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}

            <Row>
                <Col lg={8}>
                    <Form onSubmit={handleSubmit(onSubmit)}>
                        {/* Material Information */}
                        {inventoryItem && (
                            <Card className="mb-4">
                                <Card.Header>
                                    <h5 className="mb-0">
                                        <FaBox className="me-2" />
                                        Material Information
                                    </h5>
                                </Card.Header>
                                <Card.Body>
                                    <Row className="g-3">
                                        <Col md={6}>
                                            <small className="text-muted d-block">Material Code</small>
                                            <strong className="text-primary">{inventoryItem.materialCode}</strong>
                                        </Col>
                                        <Col md={6}>
                                            <small className="text-muted d-block">Description</small>
                                            <strong>{inventoryItem.materialDescription}</strong>
                                        </Col>
                                        <Col md={4}>
                                            <small className="text-muted d-block">Current Stock</small>
                                            <span className="fs-4 fw-bold text-success">
                                                {inventoryItem.quantity} {inventoryItem.uomCode}
                                            </span>
                                        </Col>
                                        <Col md={4}>
                                            <small className="text-muted d-block">Reserved</small>
                                            <span className="fs-5">{inventoryItem.reservedQuantity || 0}</span>
                                        </Col>
                                        <Col md={4}>
                                            <small className="text-muted d-block">Available</small>
                                            <span className="fs-5 text-primary">{inventoryItem.availableQuantity || inventoryItem.quantity}</span>
                                        </Col>
                                    </Row>
                                </Card.Body>
                            </Card>
                        )}

                        {/* Adjustment Details */}
                        <Card className="mb-4">
                            <Card.Header>
                                <h5 className="mb-0">
                                    <FaExchangeAlt className="me-2" />
                                    Adjustment Details
                                </h5>
                            </Card.Header>
                            <Card.Body>
                                <Row className="g-3">
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label>Adjustment Type <span className="text-danger">*</span></Form.Label>
                                            <Form.Select {...register('adjustmentType')} isInvalid={!!errors.adjustmentType}>
                                                <option value="ADD">Add Stock (+)</option>
                                                <option value="SUBTRACT">Subtract Stock (-)</option>
                                                <option value="SET">Set Stock (=)</option>
                                            </Form.Select>
                                            <Form.Text className="text-muted">
                                                {watchAdjustmentType === 'ADD' && 'This will increase the current stock.'}
                                                {watchAdjustmentType === 'SUBTRACT' && 'This will decrease the current stock.'}
                                                {watchAdjustmentType === 'SET' && 'This will set the stock to the exact quantity.'}
                                            </Form.Text>
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label>Quantity <span className="text-danger">*</span></Form.Label>
                                            <InputGroup>
                                                <Form.Control
                                                    type="number"
                                                    step="0.01"
                                                    min="0.01"
                                                    {...register('quantity', { valueAsNumber: true })}
                                                    isInvalid={!!errors.quantity}
                                                    placeholder="Enter quantity"
                                                />
                                                <InputGroup.Text>{inventoryItem?.uomCode || 'UOM'}</InputGroup.Text>
                                            </InputGroup>
                                            <Form.Control.Feedback type="invalid">
                                                {errors.quantity?.message}
                                            </Form.Control.Feedback>
                                        </Form.Group>
                                    </Col>
                                    <Col md={12}>
                                        <Form.Group>
                                            <Form.Label>Reason for Adjustment <span className="text-danger">*</span></Form.Label>
                                            <Form.Control
                                                as="textarea"
                                                rows={3}
                                                {...register('reason')}
                                                isInvalid={!!errors.reason}
                                                placeholder="Provide a detailed reason for this adjustment (e.g., physical count reconciliation, damage, found in warehouse)..."
                                            />
                                            <Form.Control.Feedback type="invalid">
                                                {errors.reason?.message}
                                            </Form.Control.Feedback>
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
                                    onClick={() => navigate('/inventory')}
                                    disabled={isSubmitting}
                                >
                                    Cancel
                                </Button>
                                <Button
                                    type="submit"
                                    variant="primary"
                                    disabled={isSubmitting || adjustMutation.isPending}
                                >
                                    {adjustMutation.isPending && (
                                        <Spinner as="span" animation="border" size="sm" className="me-2" />
                                    )}
                                    <FaSave className="me-2" /> Apply Adjustment
                                </Button>
                            </Card.Body>
                        </Card>
                    </Form>
                </Col>

                {/* Preview Panel */}
                <Col lg={4}>
                    <Card className="sticky-top" style={{ top: '20px' }}>
                        <Card.Header className="bg-primary text-white">
                            <h5 className="mb-0">Adjustment Preview</h5>
                        </Card.Header>
                        <Card.Body>
                            {inventoryItem && (
                                <div className="text-center">
                                    <div className="mb-4">
                                        <small className="text-muted d-block">Current Stock</small>
                                        <span className="fs-2 fw-bold">{inventoryItem.quantity}</span>
                                        <span className="ms-2 text-muted">{inventoryItem.uomCode}</span>
                                    </div>
                                    <div className="mb-4">
                                        <span className="fs-4 text-muted">
                                            {watchAdjustmentType === 'ADD' && '+'}
                                            {watchAdjustmentType === 'SUBTRACT' && '-'}
                                            {watchAdjustmentType === 'SET' && '='}
                                        </span>
                                        <span className="fs-4 ms-2">{watchQuantity || 0}</span>
                                    </div>
                                    <hr />
                                    <div>
                                        <small className="text-muted d-block">New Stock Level</small>
                                        <span className={`fs-2 fw-bold ${calculateNewQuantity() < (inventoryItem.minStockLevel || 0) ? 'text-danger' : 'text-success'}`}>
                                            {calculateNewQuantity()}
                                        </span>
                                        <span className="ms-2 text-muted">{inventoryItem.uomCode}</span>
                                    </div>
                                    {calculateNewQuantity() < (inventoryItem.minStockLevel || 0) && (
                                        <Alert variant="warning" className="mt-3 mb-0">
                                            <small>Warning: New stock will be below minimum level ({inventoryItem.minStockLevel})</small>
                                        </Alert>
                                    )}
                                    {calculateNewQuantity() > (inventoryItem.maxStockLevel || Infinity) && inventoryItem.maxStockLevel && (
                                        <Alert variant="info" className="mt-3 mb-0">
                                            <small>Note: New stock will exceed maximum level ({inventoryItem.maxStockLevel})</small>
                                        </Alert>
                                    )}
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </div>
    );
};

export default StockAdjustmentPage;
