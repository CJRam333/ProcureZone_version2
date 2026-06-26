import React, { useState } from 'react';
import { Card, Row, Col, Form, Button, Badge, Alert, Spinner, InputGroup } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { format } from 'date-fns';
import {
  FaUser,
  FaEnvelope,
  FaPhone,
  FaBuilding,
  FaSave,
  FaKey,
  FaCamera,
  FaEye,
  FaEyeSlash,
} from 'react-icons/fa';
import { PageHeader } from '../../components/common';
import { useAuth } from '../../contexts/AuthContext';
import { getErrorMessage, authApi, userApi } from '../../api';

interface ProfileFormData {
  fullName: string;
  email: string;
  phone?: string;
  department?: string;
  designation?: string;
  address?: string;
}

interface PasswordFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

const ProfilePage: React.FC = () => {
  const { user } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const {
    register: registerProfile,
    handleSubmit: handleProfileSubmit,
    formState: { errors: profileErrors },
  } = useForm<ProfileFormData>({
    defaultValues: {
      fullName: user?.displayName || '',
      email: user?.email || '',
      phone: '',
      department: user?.departmentName || '',
      designation: user?.designation || '',
      address: '',
    },
  });

  const {
    register: registerPassword,
    handleSubmit: handlePasswordSubmit,
    formState: { errors: passwordErrors },
    reset: resetPassword,
  } = useForm<PasswordFormData>();

  const profileMutation = useMutation({
    mutationFn: async (data: ProfileFormData) => {
      return userApi.updateProfile({
        fullName: data.fullName,
        email: data.email,
        phone: data.phone,
        department: data.department,
        designation: data.designation,
        address: data.address,
      });
    },
    onSuccess: () => {
      setSuccess('Profile updated successfully');
      setError(null);
    },
    onError: (err) => {
      setError(getErrorMessage(err));
      setSuccess(null);
    },
  });

  const passwordMutation = useMutation({
    mutationFn: async (data: PasswordFormData) => {
      if (data.newPassword !== data.confirmPassword) {
        throw new Error('New password and confirm password do not match');
      }
      return authApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
        confirmPassword: data.confirmPassword,
      });
    },
    onSuccess: () => {
      setSuccess('Password changed successfully');
      setError(null);
      resetPassword();
    },
    onError: (err) => {
      setError(getErrorMessage(err));
      setSuccess(null);
    },
  });

  const onProfileSubmit = (data: ProfileFormData) => {
    profileMutation.mutate(data);
  };

  const onPasswordSubmit = (data: PasswordFormData) => {
    passwordMutation.mutate(data);
  };

  const getRoleColor = (role: string): string => {
    const colors: Record<string, string> = {
      SUPERADMIN: 'danger',
      ADMIN: 'primary',
      MANAGER: 'info',
      PURCHASE: 'success',
      STORES: 'warning',
    };
    return colors[role] || 'secondary';
  };

  return (
    <div>
      <PageHeader
        title="My Profile"
        subtitle="Manage your account information and preferences"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Profile' },
        ]}
      />

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" dismissible onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      <Row>
        <Col lg={4}>
          {/* Profile Card */}
          <Card className="mb-4">
            <Card.Body className="text-center">
              <div className="position-relative d-inline-block mb-3">
                <div
                  className="rounded-circle bg-primary d-flex align-items-center justify-content-center mx-auto"
                  style={{ width: 100, height: 100 }}
                >
                  <span className="text-white display-6">
                    {user?.displayName?.charAt(0).toUpperCase() || 'U'}
                  </span>
                </div>
                <Button
                  variant="light"
                  size="sm"
                  className="position-absolute bottom-0 end-0 rounded-circle"
                  style={{ width: 32, height: 32 }}
                  title="Change Photo"
                >
                  <FaCamera size={12} />
                </Button>
              </div>
              <h5 className="mb-1">{user?.displayName}</h5>
              <p className="text-muted mb-2">{user?.employeeId || user?.email}</p>
              <Badge bg={getRoleColor(user?.roles?.[0] || '')} className="mb-3">
                {user?.roles?.[0]?.replace('_', ' ')}
              </Badge>
              <div className="text-muted small">
                <div className="mb-1">
                  <FaEnvelope className="me-2" />
                  {user?.email}
                </div>
                {user?.departmentName && (
                  <div>
                    <FaBuilding className="me-2" />
                    {user.departmentName}
                  </div>
                )}
              </div>
            </Card.Body>
          </Card>

          {/* Account Info */}
          <Card className="mb-4">
            <Card.Header>
              <h6 className="mb-0">Account Information</h6>
            </Card.Header>
            <Card.Body>
              <div className="mb-3">
                <small className="text-muted d-block">Employee Code</small>
                <strong>{user?.employeeId || 'N/A'}</strong>
              </div>
              <div className="mb-3">
                <small className="text-muted d-block">Email</small>
                <strong>{user?.email}</strong>
              </div>
              {user?.departmentName && (
                <div className="mb-3">
                  <small className="text-muted d-block">Department</small>
                  <strong>{user.departmentName}</strong>
                </div>
              )}
              {user?.companyName && (
                <div className="mb-3">
                  <small className="text-muted d-block">Company</small>
                  <strong>{user.companyName}</strong>
                </div>
              )}
              {user?.locationName && (
                <div className="mb-3">
                  <small className="text-muted d-block">Location</small>
                  <strong>{user.locationName}</strong>
                </div>
              )}
              <div>
                <small className="text-muted d-block">Roles</small>
                <strong>{user?.roles?.join(', ') || 'N/A'}</strong>
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col lg={8}>
          {/* Edit Profile */}
          <Card className="mb-4">
            <Card.Header>
              <h5 className="mb-0">
                <FaUser className="me-2" />
                Edit Profile
              </h5>
            </Card.Header>
            <Card.Body>
              <Form onSubmit={handleProfileSubmit(onProfileSubmit)}>
                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Full Name <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="text"
                        {...registerProfile('fullName')}
                        isInvalid={!!profileErrors.fullName}
                      />
                      <Form.Control.Feedback type="invalid">
                        {profileErrors.fullName?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Email <span className="text-danger">*</span></Form.Label>
                      <Form.Control
                        type="email"
                        {...registerProfile('email')}
                        isInvalid={!!profileErrors.email}
                      />
                      <Form.Control.Feedback type="invalid">
                        {profileErrors.email?.message}
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Phone</Form.Label>
                      <Form.Control
                        type="tel"
                        {...registerProfile('phone')}
                        placeholder="+91 98765 43210"
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Department</Form.Label>
                      <Form.Control
                        type="text"
                        {...registerProfile('department')}
                        disabled
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <Form.Label>Designation</Form.Label>
                      <Form.Control
                        type="text"
                        {...registerProfile('designation')}
                        placeholder="e.g., Senior Manager"
                      />
                    </Form.Group>
                  </Col>
                  <Col xs={12}>
                    <Form.Group className="mb-3">
                      <Form.Label>Address</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={2}
                        {...registerProfile('address')}
                        placeholder="Enter your address"
                      />
                    </Form.Group>
                  </Col>
                </Row>
                <Button
                  type="submit"
                  variant="primary"
                  disabled={profileMutation.isPending}
                >
                  {profileMutation.isPending ? (
                    <>
                      <Spinner as="span" animation="border" size="sm" className="me-2" />
                      Saving...
                    </>
                  ) : (
                    <>
                      <FaSave className="me-2" />
                      Save Changes
                    </>
                  )}
                </Button>
              </Form>
            </Card.Body>
          </Card>

          {/* Change Password */}
          <Card>
            <Card.Header>
              <h5 className="mb-0">
                <FaKey className="me-2" />
                Change Password
              </h5>
            </Card.Header>
            <Card.Body>
              <Form onSubmit={handlePasswordSubmit(onPasswordSubmit)}>
                <Row>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Current Password <span className="text-danger">*</span></Form.Label>
                      <InputGroup hasValidation>
                        <Form.Control
                          type={showCurrent ? 'text' : 'password'}
                          autoComplete="current-password"
                          {...registerPassword('currentPassword')}
                          isInvalid={!!passwordErrors.currentPassword}
                        />
                        <Button
                          variant="outline-secondary"
                          tabIndex={-1}
                          onClick={() => setShowCurrent(v => !v)}
                        >
                          {showCurrent ? <FaEyeSlash /> : <FaEye />}
                        </Button>
                        <Form.Control.Feedback type="invalid">
                          {passwordErrors.currentPassword?.message}
                        </Form.Control.Feedback>
                      </InputGroup>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>New Password <span className="text-danger">*</span></Form.Label>
                      <InputGroup hasValidation>
                        <Form.Control
                          type={showNew ? 'text' : 'password'}
                          autoComplete="new-password"
                          {...registerPassword('newPassword')}
                          isInvalid={!!passwordErrors.newPassword}
                        />
                        <Button
                          variant="outline-secondary"
                          tabIndex={-1}
                          onClick={() => setShowNew(v => !v)}
                        >
                          {showNew ? <FaEyeSlash /> : <FaEye />}
                        </Button>
                        <Form.Control.Feedback type="invalid">
                          {passwordErrors.newPassword?.message}
                        </Form.Control.Feedback>
                      </InputGroup>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <Form.Label>Confirm Password <span className="text-danger">*</span></Form.Label>
                      <InputGroup hasValidation>
                        <Form.Control
                          type={showConfirm ? 'text' : 'password'}
                          autoComplete="new-password"
                          {...registerPassword('confirmPassword')}
                          isInvalid={!!passwordErrors.confirmPassword}
                        />
                        <Button
                          variant="outline-secondary"
                          tabIndex={-1}
                          onClick={() => setShowConfirm(v => !v)}
                        >
                          {showConfirm ? <FaEyeSlash /> : <FaEye />}
                        </Button>
                        <Form.Control.Feedback type="invalid">
                          {passwordErrors.confirmPassword?.message}
                        </Form.Control.Feedback>
                      </InputGroup>
                    </Form.Group>
                  </Col>
                </Row>
                <Button
                  type="submit"
                  variant="warning"
                  disabled={passwordMutation.isPending}
                >
                  {passwordMutation.isPending ? (
                    <>
                      <Spinner as="span" animation="border" size="sm" className="me-2" />
                      Updating...
                    </>
                  ) : (
                    <>
                      <FaKey className="me-2" />
                      Update Password
                    </>
                  )}
                </Button>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default ProfilePage;
