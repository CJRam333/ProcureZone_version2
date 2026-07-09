import React, { useState } from 'react';
import { Card, Form, Row, Col, InputGroup, Button, Badge } from 'react-bootstrap';
import { useQuery } from '@tanstack/react-query';
import { FaSearch, FaSyncAlt } from 'react-icons/fa';
import { PageHeader, DataTable, Column } from '../../components/common';
import { inventoryApi, getErrorMessage } from '../../api';
import type { InventoryStockRow } from '../../api/inventory';

/**
 * Read-only Inventory — a view-only list of materials with their available stock, sourced from
 * tbl_map_company_plant_material.map_quantity_stores (the same source the material dropdown and
 * issue-note creation use). No create/edit/delete. Distinct from Masters → Materials (editable).
 */
const InventoryStockPage: React.FC = () => {
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const pageSize = 20;

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['inventory-stock-view', search, page],
    queryFn: () => inventoryApi.stockView({ search: search || undefined, page, size: pageSize }),
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearch(searchInput.trim());
    setPage(0);
  };

  const columns: Column<InventoryStockRow>[] = [
    {
      key: 'materialCode',
      label: 'Material Code',
      render: (row) => <strong className="text-primary">{row.materialCode}</strong>,
    },
    { key: 'materialName', label: 'Name', render: (row) => row.materialName || '—' },
    {
      key: 'materialDescription',
      label: 'Description',
      render: (row) => row.materialDescription || '—',
    },
    { key: 'companyName', label: 'Company', render: (row) => row.companyName || '—' },
    { key: 'plantName', label: 'Plant', render: (row) => row.plantName || '—' },
    {
      key: 'stockQuantity',
      label: 'Available Stock',
      render: (row) => {
        const qty = row.stockQuantity ?? 0;
        return <Badge bg={qty > 0 ? 'success' : 'secondary'}>{qty}</Badge>;
      },
    },
  ];

  return (
    <div>
      <PageHeader
        title="Inventory"
        subtitle="Available stock by material (view only)"
        breadcrumbs={[
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Inventory' },
        ]}
      />

      <Card className="mb-3">
        <Card.Body>
          <Form onSubmit={handleSearch}>
            <Row className="g-3 align-items-end">
              <Col lg={5} md={8}>
                <InputGroup>
                  <Form.Control
                    placeholder="Search by material code, name or description..."
                    value={searchInput}
                    onChange={(e) => setSearchInput(e.target.value)}
                  />
                  <Button type="submit" variant="primary"><FaSearch /></Button>
                </InputGroup>
              </Col>
              <Col lg="auto">
                <div className="d-flex gap-2">
                  <Button variant="outline-secondary" onClick={() => refetch()}>
                    <FaSyncAlt />
                  </Button>
                  {search && (
                    <Button
                      variant="outline-danger"
                      onClick={() => { setSearchInput(''); setSearch(''); setPage(0); }}
                    >
                      Clear
                    </Button>
                  )}
                </div>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      <Card>
        <Card.Body className="p-0">
          {error ? (
            <div className="p-4 text-center text-danger">{getErrorMessage(error)}</div>
          ) : (
            <DataTable
              columns={columns as unknown as Column<Record<string, unknown>>[]}
              data={(data?.content || []) as unknown as Record<string, unknown>[]}
              keyField="materialId"
              loading={isLoading}
              totalItems={data?.totalElements || 0}
              currentPage={page}
              pageSize={pageSize}
              onPageChange={setPage}
              emptyMessage="No materials found"
            />
          )}
        </Card.Body>
      </Card>
    </div>
  );
};

export default InventoryStockPage;
