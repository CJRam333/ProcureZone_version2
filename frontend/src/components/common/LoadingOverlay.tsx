import React from 'react';
import { Spinner } from 'react-bootstrap';

interface LoadingOverlayProps {
  text?: string;
}

const LoadingOverlay: React.FC<LoadingOverlayProps> = ({ text = 'Loading...' }) => {
  return (
    <div className="loading-overlay">
      <div className="text-center">
        <Spinner animation="border" role="status" />
        <p className="mt-3 text-muted">{text}</p>
      </div>
    </div>
  );
};

export default LoadingOverlay;

