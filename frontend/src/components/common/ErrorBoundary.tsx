import React from 'react';
import { Button } from 'react-bootstrap';

type ErrorBoundaryProps = {
  children: React.ReactNode;
};

type ErrorBoundaryState = {
  hasError: boolean;
  message?: string;
};

class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, message: error.message };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Uncaught error:', error, errorInfo);
  }

  handleRetry = () => {
    this.setState({ hasError: false, message: undefined });
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="loading-overlay">
          <div className="loading-card">
            <h4 className="mb-2">Something went wrong</h4>
            <p className="mb-3 text-muted">{this.state.message || 'An unexpected error occurred.'}</p>
            <div className="d-flex gap-2 justify-content-center">
              <Button variant="primary" onClick={this.handleRetry}>
                Try Again
              </Button>
              <Button variant="light" onClick={() => window.location.reload()}>
                Reload App
              </Button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

