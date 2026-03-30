import React from 'react';
import { Alert, Button } from 'react-bootstrap';
import { FaExclamationTriangle, FaRedo } from 'react-icons/fa';

interface ErrorAlertProps {
  title?: string;
  message: string;
  onRetry?: () => void;
  variant?: 'danger' | 'warning';
}

const ErrorAlert: React.FC<ErrorAlertProps> = ({
  title = 'Error',
  message,
  onRetry,
  variant = 'danger',
}) => {
  return (
    <Alert variant={variant} className="d-flex align-items-start gap-3">
      <FaExclamationTriangle size={24} className="flex-shrink-0 mt-1" />
      <div className="flex-grow-1">
        <Alert.Heading className="h6 mb-1">{title}</Alert.Heading>
        <p className="mb-0">{message}</p>
        {onRetry && (
          <Button
            variant={`outline-${variant}`}
            size="sm"
            className="mt-2"
            onClick={onRetry}
          >
            <FaRedo className="me-1" /> Retry
          </Button>
        )}
      </div>
    </Alert>
  );
};

export default ErrorAlert;
