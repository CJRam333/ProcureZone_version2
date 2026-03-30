import React from 'react';
import { Modal, Button, Spinner } from 'react-bootstrap';
import { FaExclamationTriangle } from 'react-icons/fa';

interface ConfirmDialogProps {
  show: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'danger' | 'warning' | 'primary';
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  show,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'danger',
  loading = false,
  onConfirm,
  onCancel,
}) => {
  return (
    <Modal show={show} onHide={onCancel} centered>
      <Modal.Header closeButton>
        <Modal.Title className="d-flex align-items-center gap-2">
          {variant === 'danger' && (
            <FaExclamationTriangle className="text-danger" />
          )}
          {title}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>{message}</Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onCancel} disabled={loading}>
          {cancelLabel}
        </Button>
        <Button variant={variant} onClick={onConfirm} disabled={loading}>
          {loading && (
            <Spinner
              as="span"
              animation="border"
              size="sm"
              role="status"
              className="me-2"
            />
          )}
          {confirmLabel}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ConfirmDialog;
