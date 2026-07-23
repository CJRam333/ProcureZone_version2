import React, { useState } from 'react';
import { Button, Spinner } from 'react-bootstrap';
import { FaFileCsv, FaFileExcel } from 'react-icons/fa';

type ExportFormat = 'csv' | 'xlsx';

interface ExportButtonsProps {
  /**
   * Called with the chosen format. Should hit the backend export endpoint and
   * resolve with the response Blob (the component triggers the browser download).
   * May also handle the download itself and resolve void.
   */
  onExport: (format: ExportFormat) => Promise<Blob | void>;
  /** Base name for the downloaded file (extension is appended). Default: 'export'. */
  filenameBase?: string;
  /** Disable both buttons (e.g. while the underlying list is loading). */
  disabled?: boolean;
}

/**
 * Two small "Export CSV" / "Export Excel" buttons for operational list pages.
 * Shows a spinner and disables while a download is in flight, then triggers the
 * browser download from the returned Blob via a temporary object-URL anchor.
 *
 * Note: a plain Blob carries no Content-Disposition header, so the filename
 * falls back to `${filenameBase}.${ext}`.
 */
const ExportButtons: React.FC<ExportButtonsProps> = ({
  onExport,
  filenameBase = 'export',
  disabled = false,
}) => {
  const [busy, setBusy] = useState<ExportFormat | null>(null);

  const handleExport = async (format: ExportFormat) => {
    if (busy) return;
    setBusy(format);
    try {
      const result = await onExport(format);
      if (result instanceof Blob) {
        const url = URL.createObjectURL(result);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${filenameBase}.${format === 'csv' ? 'csv' : 'xlsx'}`;
        a.click();
        URL.revokeObjectURL(url);
      }
    } catch {
      alert('Export failed. You may not have permission.');
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className="d-flex gap-2">
      <Button
        variant="outline-secondary"
        size="sm"
        disabled={disabled || busy !== null}
        onClick={() => handleExport('csv')}
      >
        {busy === 'csv' ? (
          <Spinner animation="border" size="sm" className="me-1" />
        ) : (
          <FaFileCsv className="me-1" />
        )}
        Export CSV
      </Button>
      <Button
        variant="outline-secondary"
        size="sm"
        disabled={disabled || busy !== null}
        onClick={() => handleExport('xlsx')}
      >
        {busy === 'xlsx' ? (
          <Spinner animation="border" size="sm" className="me-1" />
        ) : (
          <FaFileExcel className="me-1" />
        )}
        Export Excel
      </Button>
    </div>
  );
};

export default ExportButtons;
