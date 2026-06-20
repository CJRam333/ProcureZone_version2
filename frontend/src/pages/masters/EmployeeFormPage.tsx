import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Button, Row, Col, Badge, InputGroup, Spinner } from 'react-bootstrap';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { FaSave, FaTimes, FaUser, FaKey, FaUserShield, FaSitemap, FaEye, FaEyeSlash, FaLock, FaPuzzlePiece } from 'react-icons/fa';
import { PageHeader, LoadingSpinner } from '../../components/common';
import { employeesApi, departmentsApi, locationsApi, companiesApi, plantsApi, getErrorMessage } from '../../api';
import type { Plant } from '../../api/plants';
import { rolesApi } from '../../api';
import apiClient from '../../api/client';
import type { EmployeeCreateRequest, EmployeeUpdateRequest, Department, Location, Company } from '../../api';
import type { Role } from '../../api/roles';
import { useAuth } from '../../contexts/AuthContext';
import { moduleAccessApi } from '../../api/moduleAccess';
import type { EmpModuleEntry, ModuleDefinition } from '../../api/moduleAccess';

// ─────────────────────────────────────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────────────────────────────────────

type AuthType = 'ldap' | 'non-ldap';

interface FormData {
  empId: string;
  empName: string;
  empEmail: string;       // holds email (LDAP) or username (Non-LDAP)
  empDesignation: string;
  empCostCenter: string;
  departmentId: number | null;
  locationId: number | null;
  companyId: number | null;
  plantName: string;
  empStatus: number;
  empJoinDate: string;
  password: string;       // Non-LDAP create only
}

// ─────────────────────────────────────────────────────────────────────────────
// Component
// ─────────────────────────────────────────────────────────────────────────────

const EmployeeFormPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const queryClient = useQueryClient();
  const { hasAnyRole } = useAuth();
  const isEditMode = !!id;

  // ── Local state ──────────────────────────────────────────────────────────
  const [authType, setAuthType] = useState<AuthType>('ldap');
  const [primaryRoleId, setPrimaryRoleId] = useState<number | null>(null);
  const [moduleAccess, setModuleAccess] = useState<Record<string, boolean>>({});
  const [reportingManagerId, setReportingManagerId] = useState<number | null>(null);
  const [isPlantEmployee, setIsPlantEmployee] = useState(false);
  const [isSavingModules, setIsSavingModules] = useState(false);

  const canManageModules = hasAnyRole(['ADMIN', 'SUPERADMIN']);

  // Password reset state (edit mode only, ADMIN/SUPERADMIN)
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isResettingPassword, setIsResettingPassword] = useState(false);
  const [passwordResetError, setPasswordResetError] = useState<string | null>(null);

  const canEditReportingManager = hasAnyRole(['SUPERADMIN', 'ADMIN']);

  // ── Form ─────────────────────────────────────────────────────────────────
  const { register, handleSubmit, reset, watch, setValue, formState: { errors, isSubmitting } } = useForm<FormData>({
    defaultValues: {
      empId: '', empName: '', empEmail: '', empDesignation: '', empCostCenter: '',
      departmentId: null, locationId: null, companyId: null,
      plantName: '', empStatus: 1, empJoinDate: '', password: '',
    },
  });

  // ── Data queries ─────────────────────────────────────────────────────────
  const { data: employee, isLoading: loadingEmployee } = useQuery({
    queryKey: ['employee', id],
    queryFn: () => employeesApi.getById(Number(id)),
    enabled: isEditMode,
  });

  const { data: departmentsData } = useQuery({ queryKey: ['departments-active'], queryFn: () => departmentsApi.getActive(0, 100) });
  const { data: locationsData }   = useQuery({ queryKey: ['locations-active'],   queryFn: () => locationsApi.getActive(0, 100) });
  const { data: companiesData }   = useQuery({ queryKey: ['companies-active'],   queryFn: () => companiesApi.getActive(0, 100) });

  const { data: allRoles = [] } = useQuery<Role[]>({
    queryKey: ['roles-active'],
    queryFn: () => rolesApi.getActive(),
  });

  const { data: activeEmployees } = useQuery({
    queryKey: ['employees-active'],
    queryFn: () => employeesApi.getActive(0, 500),
  });

  const { data: plantsData } = useQuery({
    queryKey: ['plants-active'],
    queryFn: () => plantsApi.getActive(0, 100),
  });

  // Current roles for edit mode — loaded from /employees/{id}/roles
  const { data: currentRoles } = useQuery<{ roleId: number }[]>({
    queryKey: ['employee-roles-detail', id],
    queryFn: async () => {
      const resp = await apiClient.get<{ roleId: number }[]>(`/employees/${id}/roles`);
      return resp.data;
    },
    enabled: isEditMode,
  });

  // All module definitions (for admin module access card)
  const { data: allModules = [] } = useQuery<ModuleDefinition[]>({
    queryKey: ['all-modules'],
    queryFn: () => moduleAccessApi.getAllModules(),
    enabled: isEditMode && canManageModules,
  });

  // Current module access for the employee being edited
  const { data: empModules } = useQuery<EmpModuleEntry[]>({
    queryKey: ['employee-modules', id],
    queryFn: () => moduleAccessApi.getEmployeeModules(Number(id)),
    enabled: isEditMode && canManageModules,
  });

  // Current supervisor for edit mode
  const { data: supervisorData } = useQuery<{ supervisorEmployeeNumber: number } | null>({
    queryKey: ['employee-supervisor', id],
    queryFn: async () => {
      try {
        const resp = await apiClient.get(`/employee-reporting/employee/${id}/supervisor`);
        return resp.data?.data ?? null;
      } catch {
        return null; // no supervisor is valid
      }
    },
    enabled: isEditMode,
  });

  const departments = departmentsData?.content ?? [];
  const locations   = locationsData?.content ?? [];
  const companies   = companiesData?.content ?? [];
  const employees   = (activeEmployees?.content ?? []).filter(e => e.id !== Number(id));
  const plants      = plantsData?.content ?? [];

  // ── Populate form on edit ────────────────────────────────────────────────
  useEffect(() => {
    if (!employee) return;
    const detected: AuthType = (employee.empEmail ?? '').includes('@') ? 'ldap' : 'non-ldap';
    setAuthType(detected);
    setIsPlantEmployee(!!(employee.plantName));
    reset({
      empId:        employee.empId        || '',
      empName:      employee.empName      || '',
      empEmail:     employee.empEmail     || '',
      empDesignation: employee.empDesignation || '',
      empCostCenter:  employee.empCostCenter  || '',
      departmentId: employee.departmentId || null,
      locationId:   employee.locationId   || null,
      companyId:    employee.companyId    || null,
      plantName:    employee.plantName    || '',
      empStatus:    employee.empStatus    ?? 1,
      empJoinDate:  employee.empJoinDate  ? new Date(employee.empJoinDate).toISOString().split('T')[0] : '',
      password:     '',
    });
  }, [employee, reset]);

  // Pre-select primary role on edit (use first role as primary)
  useEffect(() => {
    if (currentRoles && currentRoles.length > 0) {
      setPrimaryRoleId(currentRoles[0].roleId);
    }
  }, [currentRoles]);

  // Pre-populate module access toggles on edit
  useEffect(() => {
    if (empModules && empModules.length > 0) {
      const map: Record<string, boolean> = {};
      empModules.forEach(m => { map[m.moduleCode] = m.enabled; });
      setModuleAccess(map);
    }
  }, [empModules]);

  // Pre-select supervisor on edit
  useEffect(() => {
    if (supervisorData) {
      setReportingManagerId(supervisorData.supervisorEmployeeNumber ?? null);
    }
  }, [supervisorData]);

  // ── Module access save ───────────────────────────────────────────────────
  const handleSaveModules = async () => {
    if (!id) return;
    setIsSavingModules(true);
    try {
      const updates = Object.entries(moduleAccess).map(([code, enabled]) => ({ code, enabled }));
      await moduleAccessApi.updateEmployeeModules(Number(id), updates);
      queryClient.invalidateQueries({ queryKey: ['employee-modules', id] });
      toast.success('Module access saved.');
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsSavingModules(false);
    }
  };

  // ── Mutations ────────────────────────────────────────────────────────────
  const createMutation = useMutation({
    mutationFn: (data: EmployeeCreateRequest) => employeesApi.create(data),
    onSuccess: () => {
      toast.success('Employee created successfully');
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      navigate('/masters/employees');
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: (data: EmployeeUpdateRequest) => employeesApi.update(Number(id), data),
    onSuccess: () => {
      toast.success('Employee updated successfully');
      queryClient.invalidateQueries({ queryKey: ['employees'] });
      queryClient.invalidateQueries({ queryKey: ['employee', id] });
      navigate('/masters/employees');
    },
    onError: (error) => toast.error(getErrorMessage(error)),
  });

  // ── Password reset ───────────────────────────────────────────────────────
  const handlePasswordReset = async () => {
    setPasswordResetError(null);
    if (newPassword.length < 8) {
      setPasswordResetError('Password must be at least 8 characters.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordResetError('Passwords do not match.');
      return;
    }
    setIsResettingPassword(true);
    try {
      await apiClient.patch(`/employees/${id}/password`, { newPassword });
      toast.success('Password reset successfully.');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setIsResettingPassword(false);
    }
  };

  // ── Submit ───────────────────────────────────────────────────────────────
  const onSubmit = (data: FormData) => {
    const roleIdList = primaryRoleId ? [primaryRoleId] : [];

    if (isEditMode) {
      const updatePayload: EmployeeUpdateRequest = {
        empName:      data.empName      || undefined,
        empEmail:     data.empEmail     || undefined,
        empDesignation: data.empDesignation || undefined,
        empCostCenter:  data.empCostCenter  || undefined,
        departmentId: data.departmentId || undefined,
        locationId:   data.locationId   || undefined,
        companyId:    data.companyId    || undefined,
        plantName:    data.plantName    || undefined,
        empStatus:    data.empStatus,
        empJoinDate:  data.empJoinDate  || undefined,
        roleIds:      roleIdList,
        reportingManagerId: reportingManagerId ?? undefined,
      };
      updateMutation.mutate(updatePayload);
    } else {
      const createPayload: EmployeeCreateRequest = {
        empId:        data.empId,
        empName:      data.empName,
        empEmail:     data.empEmail,
        empDesignation: data.empDesignation || undefined,
        empCostCenter:  data.empCostCenter  || undefined,
        departmentId: data.departmentId || undefined,
        locationId:   data.locationId   || undefined,
        companyId:    data.companyId    || undefined,
        plantName:    data.plantName    || undefined,
        empStatus:    data.empStatus,
        empJoinDate:  data.empJoinDate  || undefined,
        password:     authType === 'non-ldap' ? (data.password || undefined) : undefined,
        roleIds:      roleIdList,
        reportingManagerId: reportingManagerId ?? undefined,
      };
      createMutation.mutate(createPayload);
    }
  };

  if (isEditMode && loadingEmployee) return <LoadingSpinner text="Loading employee..." />;

  const isPending = createMutation.isPending || updateMutation.isPending || isSubmitting;

  // ─────────────────────────────────────────────────────────────────────────
  // Render
  // ─────────────────────────────────────────────────────────────────────────
  return (
    <div>
      <PageHeader
        title={isEditMode ? 'Edit Employee' : 'Add Employee'}
        subtitle={isEditMode ? `Editing: ${employee?.empName}` : 'Create a new employee record'}
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Masters', path: '/masters' },
          { label: 'Employees', path: '/masters/employees' },
          { label: isEditMode ? 'Edit' : 'Add' },
        ]}
      />

      <Form onSubmit={handleSubmit(onSubmit)}>

        {/* ── Section 1: Employee Details ─────────────────────────────── */}
        <Card className="mb-3">
          <Card.Header className="d-flex align-items-center gap-2">
            <FaUser className="text-primary" />
            <span className="fw-bold">Employee Details</span>
          </Card.Header>
          <Card.Body>
            <Row>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Employee ID <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="e.g., EMP001"
                    {...register('empId', { required: 'Employee ID is required' })}
                    isInvalid={!!errors.empId}
                    disabled={isEditMode}
                  />
                  <Form.Control.Feedback type="invalid">{errors.empId?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Full Name <span className="text-danger">*</span></Form.Label>
                  <Form.Control
                    type="text"
                    placeholder="Enter full name"
                    {...register('empName', { required: 'Full name is required' })}
                    isInvalid={!!errors.empName}
                  />
                  <Form.Control.Feedback type="invalid">{errors.empName?.message}</Form.Control.Feedback>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Designation</Form.Label>
                  <Form.Control type="text" placeholder="e.g., Manager" {...register('empDesignation')} />
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Cost Center</Form.Label>
                  <Form.Control type="text" placeholder="e.g., CC001" {...register('empCostCenter')} />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Join Date</Form.Label>
                  <Form.Control type="date" {...register('empJoinDate')} />
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Status</Form.Label>
                  <Form.Select {...register('empStatus', { valueAsNumber: true })}>
                    <option value={1}>Active</option>
                    <option value={0}>Inactive</option>
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Company</Form.Label>
                  <Form.Select {...register('companyId', { setValueAs: v => v ? Number(v) : null })}>
                    <option value="">Select Company</option>
                    {companies.map((c: Company) => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Department</Form.Label>
                  <Form.Select {...register('departmentId', { setValueAs: v => v ? Number(v) : null })}>
                    <option value="">Select Department</option>
                    {departments.map((d: Department) => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Location</Form.Label>
                  <Form.Select {...register('locationId', { setValueAs: v => v ? Number(v) : null })}>
                    <option value="">Select Location</option>
                    {locations.map((l: Location) => <option key={l.id} value={l.id}>{l.name}</option>)}
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>
            <Row>
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>Plant Employee</Form.Label>
                  <Form.Check
                    type="switch"
                    id="is-plant-employee"
                    label={isPlantEmployee ? 'Yes — assign to plant' : 'No'}
                    checked={isPlantEmployee}
                    onChange={(e) => {
                      setIsPlantEmployee(e.target.checked);
                      if (!e.target.checked) setValue('plantName', '');
                    }}
                    className="mb-2"
                  />
                  {isPlantEmployee && (
                    <Form.Select
                      value={watch('plantName') || ''}
                      onChange={(e) => setValue('plantName', e.target.value)}
                    >
                      <option value="">Select Plant</option>
                      {plants.map((p: Plant) => (
                        <option key={p.id} value={p.code}>{p.name} ({p.code})</option>
                      ))}
                    </Form.Select>
                  )}
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* ── Section 2: Authentication ───────────────────────────────── */}
        <Card className="mb-3">
          <Card.Header className="d-flex align-items-center gap-2">
            <FaKey className="text-warning" />
            <span className="fw-bold">Authentication</span>
          </Card.Header>
          <Card.Body>
            {/* Auth type selector */}
            <Form.Group className="mb-3">
              <Form.Label className="fw-semibold">Authentication Method</Form.Label>
              <div className="d-flex gap-4 mt-1">
                <Form.Check
                  type="radio"
                  id="auth-ldap"
                  label="LDAP User"
                  checked={authType === 'ldap'}
                  onChange={() => setAuthType('ldap')}
                />
                <Form.Check
                  type="radio"
                  id="auth-non-ldap"
                  label="Non-LDAP User"
                  checked={authType === 'non-ldap'}
                  onChange={() => setAuthType('non-ldap')}
                />
              </div>
              <Form.Text className="text-muted">
                {authType === 'ldap'
                  ? 'LDAP users authenticate using their corporate email address.'
                  : 'Non-LDAP users authenticate using a local username and password.'}
              </Form.Text>
            </Form.Group>

            <Row>
              {/* Dynamic email / username field */}
              <Col md={4}>
                <Form.Group className="mb-3">
                  <Form.Label>
                    {authType === 'ldap' ? 'Email Address' : 'Username'}
                    {' '}<span className="text-danger">*</span>
                  </Form.Label>
                  <Form.Control
                    type="text"
                    placeholder={authType === 'ldap' ? 'user@company.com' : 'jsmith'}
                    {...register('empEmail', {
                      required: `${authType === 'ldap' ? 'Email address' : 'Username'} is required`,
                      validate: value => {
                        if (authType === 'ldap') {
                          return value.includes('@') || 'Email address must contain @';
                        }
                        return !value.includes('@') || 'Username must not contain @';
                      },
                    })}
                    isInvalid={!!errors.empEmail}
                  />
                  <Form.Control.Feedback type="invalid">{errors.empEmail?.message}</Form.Control.Feedback>
                  <Form.Text className="text-muted">
                    {authType === 'ldap'
                      ? 'Must contain @ — used as the login identifier.'
                      : 'Must not contain @ — used as the login identifier.'}
                  </Form.Text>
                </Form.Group>
              </Col>

              {/* Temporary password — Non-LDAP + CREATE only */}
              {authType === 'non-ldap' && !isEditMode && (
                <Col md={4}>
                  <Form.Group className="mb-3">
                    <Form.Label>
                      Temporary Password <span className="text-danger">*</span>
                    </Form.Label>
                    <Form.Control
                      type="password"
                      placeholder="Min 6 characters"
                      {...register('password', {
                        required: 'Temporary password is required for Non-LDAP users',
                        minLength: { value: 6, message: 'Password must be at least 6 characters' },
                      })}
                      isInvalid={!!errors.password}
                    />
                    <Form.Control.Feedback type="invalid">{errors.password?.message}</Form.Control.Feedback>
                    <Form.Text className="text-muted">
                      Employee will use this to log in for the first time.
                    </Form.Text>
                  </Form.Group>
                </Col>
              )}

              {/* Info for LDAP create or edit mode */}
              {(authType === 'ldap' && !isEditMode) && (
                <Col md={8} className="d-flex align-items-center">
                  <div className="text-muted small">
                    <FaKey className="me-1" />
                    LDAP users authenticate via the corporate directory. No local password required.
                  </div>
                </Col>
              )}
              {isEditMode && (
                <Col md={4} className="d-flex align-items-center">
                  <div className="text-muted small">
                    <FaKey className="me-1" />
                    Password changes are managed separately via User Management.
                  </div>
                </Col>
              )}
            </Row>
          </Card.Body>
        </Card>

        {/* ── Section 3: Role ─────────────────────────────────────────── */}
        <Card className="mb-3">
          <Card.Header className="d-flex align-items-center gap-2">
            <FaUserShield className="text-success" />
            <span className="fw-bold">Role</span>
            {primaryRoleId && (
              <Badge bg="success" className="ms-2">
                {allRoles.find(r => r.id === primaryRoleId)?.name ?? 'Selected'}
              </Badge>
            )}
          </Card.Header>
          <Card.Body>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Primary Role</Form.Label>
                  <Form.Select
                    value={primaryRoleId ?? ''}
                    onChange={e => setPrimaryRoleId(e.target.value ? Number(e.target.value) : null)}
                  >
                    <option value="">— No role assigned —</option>
                    {allRoles.map((role: Role) => (
                      <option key={role.id} value={role.id}>{role.name}</option>
                    ))}
                  </Form.Select>
                  <Form.Text className="text-muted">
                    The primary role determines default access and sidebar visibility.
                  </Form.Text>
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* ── Section 4: Reporting Manager ────────────────────────────── */}
        <Card className="mb-3">
          <Card.Header className="d-flex align-items-center gap-2">
            <FaSitemap className="text-info" />
            <span className="fw-bold">Reporting Manager</span>
            {!canEditReportingManager && isEditMode && (
              <Badge bg="secondary" className="ms-2">Read-only</Badge>
            )}
          </Card.Header>
          <Card.Body>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Reporting Manager</Form.Label>
                  <Form.Select
                    value={reportingManagerId ?? ''}
                    onChange={e => setReportingManagerId(e.target.value ? Number(e.target.value) : null)}
                    disabled={isEditMode && !canEditReportingManager}
                  >
                    <option value="">— No reporting manager —</option>
                    {employees.map(emp => (
                      <option key={emp.id} value={emp.id}>
                        {emp.empName} ({emp.empId})
                      </option>
                    ))}
                  </Form.Select>
                  {isEditMode && !canEditReportingManager && (
                    <Form.Text className="text-muted">
                      Only SUPERADMIN and ADMIN can change the reporting manager.
                    </Form.Text>
                  )}
                  {isEditMode && canEditReportingManager && (
                    <Form.Text className="text-muted">
                      Changing this will deactivate the current reporting relationship and create a new one.
                    </Form.Text>
                  )}
                </Form.Group>
              </Col>
            </Row>
          </Card.Body>
        </Card>

        {/* ── Section 5: Module Access (edit mode, ADMIN/SUPERADMIN only) ── */}
        {isEditMode && canManageModules && allModules.length > 0 && (
          <Card className="mb-3">
            <Card.Header className="d-flex align-items-center gap-2">
              <FaPuzzlePiece className="text-primary" />
              <span className="fw-bold">Module Access</span>
              <Badge bg="secondary" className="ms-2">
                {Object.values(moduleAccess).filter(Boolean).length} enabled
              </Badge>
            </Card.Header>
            <Card.Body>
              <Row className="g-3">
                {allModules.map(mod => {
                  const isFuture = mod.isFuture;
                  const currentEnabled = moduleAccess.hasOwnProperty(mod.moduleCode)
                    ? moduleAccess[mod.moduleCode]
                    : (!isFuture && mod.active);
                  const canToggle =
                    !isFuture &&
                    (hasAnyRole(['SUPERADMIN']) ||
                      (hasAnyRole(['ADMIN']) && mod.moduleCode !== 'ADMINISTRATION' && mod.moduleCode !== 'AUDIT_LOGS'));
                  return (
                    <Col key={mod.moduleCode} xs={12} sm={6} md={4}>
                      <div className={`d-flex align-items-center justify-content-between p-2 border rounded ${isFuture ? 'opacity-50 bg-light' : ''}`}>
                        <div>
                          <div className="fw-semibold" style={isFuture ? { fontStyle: 'italic', fontSize: '0.9em' } : { fontSize: '0.9em' }}>
                            {mod.moduleName}
                          </div>
                          <div className="text-muted" style={{ fontSize: '0.75em' }}>
                            {mod.moduleCode}{isFuture ? ' — coming soon' : ''}
                          </div>
                        </div>
                        <Form.Check
                          type="switch"
                          id={`mod-${mod.moduleCode}`}
                          checked={currentEnabled}
                          disabled={!canToggle}
                          onChange={e => setModuleAccess(prev => ({ ...prev, [mod.moduleCode]: e.target.checked }))}
                        />
                      </div>
                    </Col>
                  );
                })}
              </Row>
              <div className="mt-3 d-flex justify-content-end">
                <Button
                  variant="outline-primary"
                  size="sm"
                  onClick={handleSaveModules}
                  disabled={isSavingModules}
                >
                  {isSavingModules
                    ? <><Spinner as="span" animation="border" size="sm" className="me-2" />Saving...</>
                    : 'Save Module Access'}
                </Button>
              </div>
              <Form.Text className="text-muted d-block mt-1">
                Greyed-out toggles are locked for your role. Future modules cannot be enabled until implemented.
              </Form.Text>
            </Card.Body>
          </Card>
        )}

        {/* ── Section 6: Password Reset (edit mode, ADMIN/SUPERADMIN only) ── */}
        {isEditMode && hasAnyRole(['ADMIN', 'SUPERADMIN']) && (
          <Card className="mb-3 border-warning">
            <Card.Header className="d-flex align-items-center gap-2 bg-warning bg-opacity-10">
              <FaLock className="text-warning" />
              <span className="fw-bold">Reset Password</span>
            </Card.Header>
            <Card.Body>
              {passwordResetError && (
                <div className="alert alert-danger py-2 mb-3">{passwordResetError}</div>
              )}
              <Row className="g-3 align-items-end">
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>New Password</Form.Label>
                    <InputGroup>
                      <Form.Control
                        type={showNewPassword ? 'text' : 'password'}
                        placeholder="Min 8 characters"
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        disabled={isResettingPassword}
                      />
                      <Button
                        variant="outline-secondary"
                        onClick={() => setShowNewPassword((p) => !p)}
                        tabIndex={-1}
                        disabled={isResettingPassword}
                        aria-label={showNewPassword ? 'Hide password' : 'Show password'}
                      >
                        {showNewPassword ? <FaEyeSlash /> : <FaEye />}
                      </Button>
                    </InputGroup>
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Form.Group>
                    <Form.Label>Confirm Password</Form.Label>
                    <InputGroup>
                      <Form.Control
                        type={showConfirmPassword ? 'text' : 'password'}
                        placeholder="Repeat new password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        disabled={isResettingPassword}
                      />
                      <Button
                        variant="outline-secondary"
                        onClick={() => setShowConfirmPassword((p) => !p)}
                        tabIndex={-1}
                        disabled={isResettingPassword}
                        aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                      >
                        {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                      </Button>
                    </InputGroup>
                  </Form.Group>
                </Col>
                <Col md={4}>
                  <Button
                    variant="warning"
                    onClick={handlePasswordReset}
                    disabled={isResettingPassword || !newPassword || !confirmPassword}
                  >
                    {isResettingPassword ? (
                      <><Spinner as="span" animation="border" size="sm" className="me-2" />Resetting...</>
                    ) : (
                      <><FaLock className="me-2" />Reset Password</>
                    )}
                  </Button>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        )}

        {/* ── Actions ──────────────────────────────────────────────────── */}
        <div className="d-flex justify-content-end gap-2 mb-4">
          <Button variant="outline-secondary" onClick={() => navigate('/masters/employees')}>
            <FaTimes className="me-2" />Cancel
          </Button>
          <Button type="submit" variant="primary" disabled={isPending}>
            <FaSave className="me-2" />
            {isEditMode ? 'Update Employee' : 'Create Employee'}
          </Button>
        </div>

      </Form>
    </div>
  );
};

export default EmployeeFormPage;
