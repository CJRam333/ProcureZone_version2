import React, { useState, useRef } from 'react';
import { Card, Form, Row, Col, Button, Table, Alert, ProgressBar, Badge } from 'react-bootstrap';
import { useQueryClient } from '@tanstack/react-query';
import {
  FaFileUpload, FaDownload, FaCheck, FaTimes, FaExclamationTriangle,
  FaFileExcel, FaFileCsv, FaSpinner, FaArrowLeft
} from 'react-icons/fa';
import { PageHeader } from '../../components/common';
import { materialsApi, getErrorMessage } from '../../api';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';

interface ImportRow {
  rowNumber: number;
  code: string;
  name: string;
  description: string;
  status: number;
  importStatus: 'pending' | 'success' | 'error';
  error?: string;
}

const MaterialBulkImportPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  
  const [file, setFile] = useState<File | null>(null);
  const [parsedData, setParsedData] = useState<ImportRow[]>([]);
  const [importProgress, setImportProgress] = useState(0);
  const [isImporting, setIsImporting] = useState(false);
  const [importResults, setImportResults] = useState<{ success: number; failed: number }>({ success: 0, failed: 0 });
  const [showResults, setShowResults] = useState(false);

  // Sample template data - matches backend CreateMaterialRequest
  const templateHeaders = ['Code', 'Name', 'Description', 'Status'];
  const sampleData = [
    ['MAT-001', 'Sample Material 1', 'Description for material 1', '1'],
    ['MAT-002', 'Sample Material 2', 'Description for material 2', '1'],
  ];

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (!selectedFile) return;

    // Validate file type
    const validTypes = ['text/csv', 'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'];
    if (!validTypes.includes(selectedFile.type) && !selectedFile.name.match(/\.(csv|xlsx?)$/i)) {
      toast.error('Please upload a CSV or Excel file');
      return;
    }

    setFile(selectedFile);
    parseFile(selectedFile);
  };

  const parseFile = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      const lines = text.split('\n').filter(line => line.trim());
      
      // Skip header row
      const dataRows = lines.slice(1);
      const parsed: ImportRow[] = dataRows.map((line, index) => {
        const cols = line.split(',').map(c => c.trim().replace(/^"|"$/g, ''));
        return {
          rowNumber: index + 2, // +2 for 1-based and header
          code: cols[0]?.toUpperCase() || '',
          name: cols[1] || '',
          description: cols[2] || '',
          status: cols[3] ? Number(cols[3]) : 1,
          importStatus: 'pending',
        };
      });

      // Validate rows
      parsed.forEach(row => {
        if (!row.code) row.error = 'Material code is required';
        else if (!/^[A-Z0-9_-]+$/.test(row.code)) row.error = 'Code must be uppercase letters, numbers, underscores, and hyphens only';
        else if (!row.name) row.error = 'Material name is required';
        else if (row.status !== 0 && row.status !== 1) row.error = 'Status must be 0 (inactive) or 1 (active)';
      });

      setParsedData(parsed);
    };
    reader.readAsText(file);
  };

  const downloadTemplate = () => {
    const csvContent = [
      templateHeaders.join(','),
      ...sampleData.map(row => row.join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'material_import_template.csv';
    a.click();
  };

  const startImport = async () => {
    setIsImporting(true);
    setShowResults(false);
    let successCount = 0;
    let failCount = 0;

    const validRows = parsedData.filter(row => !row.error);
    
    for (let i = 0; i < validRows.length; i++) {
      const row = validRows[i];
      try {
        await materialsApi.create({
          code: row.code,
          name: row.name,
          description: row.description,
          status: row.status,
        });
        
        // Update status
        setParsedData(prev => prev.map(r => 
          r.rowNumber === row.rowNumber ? { ...r, importStatus: 'success' } : r
        ));
        successCount++;
      } catch (err) {
        setParsedData(prev => prev.map(r => 
          r.rowNumber === row.rowNumber ? { ...r, importStatus: 'error', error: getErrorMessage(err) } : r
        ));
        failCount++;
      }
      
      setImportProgress(Math.round(((i + 1) / validRows.length) * 100));
    }

    setImportResults({ success: successCount, failed: failCount });
    setShowResults(true);
    setIsImporting(false);
    queryClient.invalidateQueries({ queryKey: ['materials'] });
    
    if (successCount > 0) {
      toast.success(`Successfully imported ${successCount} materials`);
    }
    if (failCount > 0) {
      toast.warning(`${failCount} materials failed to import`);
    }
  };

  const clearData = () => {
    setFile(null);
    setParsedData([]);
    setImportProgress(0);
    setShowResults(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const validRowCount = parsedData.filter(r => !r.error).length;
  const errorRowCount = parsedData.filter(r => r.error).length;

  return (
    <div className="material-bulk-import">
      <PageHeader
        title="Bulk Import Materials"
        subtitle="Import multiple materials from CSV or Excel file"
        breadcrumbs={[
          { label: 'Masters', path: '/masters' },
          { label: 'Materials', path: '/masters/materials' },
          { label: 'Bulk Import', path: '' },
        ]}
      />

      {/* Instructions Card */}
      <Card className="mb-3 shadow-sm">
        <Card.Header className="bg-info text-white">
          <FaFileExcel className="me-2" /> Import Instructions
        </Card.Header>
        <Card.Body>
          <Row>
            <Col md={8}>
              <h6>File Requirements:</h6>
              <ul className="mb-0">
                <li>Supported formats: CSV (.csv), Excel (.xlsx, .xls)</li>
                <li>First row should contain column headers</li>
                <li>Required columns: Material Code, Description, UOM ID</li>
                <li>Optional columns: Category ID, Min Stock Level, Max Stock Level, Reorder Level</li>
                <li>Material codes must be unique</li>
              </ul>
            </Col>
            <Col md={4} className="d-flex align-items-center justify-content-end">
              <Button variant="outline-primary" onClick={downloadTemplate}>
                <FaDownload className="me-1" /> Download Template
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* File Upload */}
      <Card className="mb-3 shadow-sm">
        <Card.Body>
          <Row className="align-items-center">
            <Col md={6}>
              <Form.Group>
                <Form.Label>Select File</Form.Label>
                <Form.Control
                  type="file"
                  ref={fileInputRef}
                  accept=".csv,.xlsx,.xls"
                  onChange={handleFileSelect}
                  disabled={isImporting}
                />
              </Form.Group>
            </Col>
            <Col md={6} className="text-end">
              {file && (
                <div className="d-flex gap-2 justify-content-end align-items-center">
                  <Badge bg="secondary" className="fs-6 py-2 px-3">
                    <FaFileCsv className="me-1" /> {file.name}
                  </Badge>
                  <Button variant="outline-danger" size="sm" onClick={clearData} disabled={isImporting}>
                    <FaTimes className="me-1" /> Clear
                  </Button>
                </div>
              )}
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Preview Data */}
      {parsedData.length > 0 && (
        <Card className="mb-3 shadow-sm">
          <Card.Header className="d-flex justify-content-between align-items-center">
            <span>Preview Data ({parsedData.length} rows)</span>
            <div>
              {validRowCount > 0 && (
                <Badge bg="success" className="me-2">
                  <FaCheck className="me-1" /> {validRowCount} Valid
                </Badge>
              )}
              {errorRowCount > 0 && (
                <Badge bg="danger">
                  <FaTimes className="me-1" /> {errorRowCount} Errors
                </Badge>
              )}
            </div>
          </Card.Header>
          <Card.Body className="p-0">
            <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
              <Table striped bordered hover size="sm" className="mb-0">
                <thead className="table-light sticky-top">
                  <tr>
                    <th style={{ width: '50px' }}>#</th>
                    <th>Code</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Status</th>
                    <th style={{ width: '100px' }}>Import Status</th>
                  </tr>
                </thead>
                <tbody>
                  {parsedData.map((row) => (
                    <tr key={row.rowNumber} className={row.error ? 'table-danger' : row.importStatus === 'success' ? 'table-success' : ''}>
                      <td>{row.rowNumber}</td>
                      <td>{row.code}</td>
                      <td>{row.name}</td>
                      <td>{row.description}</td>
                      <td>{row.status === 1 ? 'Active' : 'Inactive'}</td>
                      <td>
                        {row.error && (
                          <span className="text-danger small" title={row.error}>
                            <FaExclamationTriangle className="me-1" />
                            Error
                          </span>
                        )}
                        {row.importStatus === 'success' && (
                          <span className="text-success">
                            <FaCheck className="me-1" /> OK
                          </span>
                        )}
                        {row.importStatus === 'pending' && !row.error && (
                          <span className="text-muted">Pending</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </div>
          </Card.Body>
        </Card>
      )}

      {/* Import Progress */}
      {isImporting && (
        <Card className="mb-3 shadow-sm">
          <Card.Body>
            <div className="d-flex align-items-center mb-2">
              <FaSpinner className="fa-spin me-2 text-primary" />
              <span>Importing materials... {importProgress}%</span>
            </div>
            <ProgressBar animated now={importProgress} />
          </Card.Body>
        </Card>
      )}

      {/* Results */}
      {showResults && (
        <Alert variant={importResults.failed > 0 ? 'warning' : 'success'}>
          <h5>Import Complete</h5>
          <p className="mb-0">
            <FaCheck className="text-success me-1" /> {importResults.success} materials imported successfully
            {importResults.failed > 0 && (
              <>
                <br />
                <FaTimes className="text-danger me-1" /> {importResults.failed} materials failed to import
              </>
            )}
          </p>
        </Alert>
      )}

      {/* Actions */}
      <div className="d-flex justify-content-between">
        <Button variant="outline-secondary" onClick={() => navigate('/masters/materials')}>
          <FaArrowLeft className="me-1" /> Back to Materials
        </Button>
        {parsedData.length > 0 && validRowCount > 0 && (
          <Button
            variant="primary"
            onClick={startImport}
            disabled={isImporting || showResults}
          >
            {isImporting ? (
              <>
                <FaSpinner className="fa-spin me-1" /> Importing...
              </>
            ) : (
              <>
                <FaFileUpload className="me-1" /> Import {validRowCount} Materials
              </>
            )}
          </Button>
        )}
      </div>
    </div>
  );
};

export default MaterialBulkImportPage;
