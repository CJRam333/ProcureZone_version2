import React from 'react';
import { Spinner } from 'react-bootstrap';

interface LoadingSpinnerProps {
  size?: 'sm' | undefined;
  variant?: string;
  text?: string;
  fullPage?: boolean;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size,
  variant = 'primary',
  text = 'Loading...',
  fullPage = false,
}) => {
  if (fullPage) {
    return (
      <div className="loading-overlay">
        <div className="text-center">
          <Spinner animation="border" variant={variant} />
          {text && <p className="mt-2 text-muted">{text}</p>}
        </div>
      </div>
    );
  }

  return (
    <div className="d-flex align-items-center justify-content-center p-4">
      <Spinner animation="border" variant={variant} size={size} />
      {text && <span className="ms-2">{text}</span>}
    </div>
  );
};

export default LoadingSpinner;
