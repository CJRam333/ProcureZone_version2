import React from 'react';
import { Table, Pagination, Form } from 'react-bootstrap';
import LoadingSpinner from './LoadingSpinner';

export interface Column<T> {
  key: string;
  label: string;
  sortable?: boolean;
  width?: string;
  render?: (item: T, index: number) => React.ReactNode;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyField: keyof T;
  loading?: boolean;
  emptyMessage?: string;
  
  // Pagination
  totalItems?: number;
  currentPage?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (size: number) => void;
  
  // Sorting
  sortField?: string;
  sortDirection?: 'asc' | 'desc';
  onSort?: (field: string) => void;
  
  // Selection
  selectable?: boolean;
  selectedItems?: T[];
  onSelectionChange?: (items: T[]) => void;
  
  // Row click
  onRowClick?: (item: T) => void;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function DataTable<T extends Record<string, any>>({
  columns,
  data,
  keyField,
  loading = false,
  emptyMessage = 'No data available',
  totalItems = 0,
  currentPage = 0,
  pageSize = 10,
  onPageChange,
  onPageSizeChange,
  sortField,
  sortDirection,
  onSort,
  selectable = false,
  selectedItems = [],
  onSelectionChange,
  onRowClick,
}: DataTableProps<T>) {
  const totalPages = Math.ceil(totalItems / pageSize);
  
  const handleSelectAll = (checked: boolean) => {
    if (onSelectionChange) {
      onSelectionChange(checked ? [...data] : []);
    }
  };
  
  const handleSelectItem = (item: T, checked: boolean) => {
    if (onSelectionChange) {
      if (checked) {
        onSelectionChange([...selectedItems, item]);
      } else {
        onSelectionChange(selectedItems.filter((i) => i[keyField] !== item[keyField]));
      }
    }
  };
  
  const isSelected = (item: T) =>
    selectedItems.some((i) => i[keyField] === item[keyField]);
  
  const allSelected = data.length > 0 && data.every((item) => isSelected(item));

  if (loading) {
    return <LoadingSpinner text="Loading data..." />;
  }

  return (
    <div className="data-table">
      <div className="table-responsive">
        <Table hover>
          <thead>
            <tr>
              {selectable && (
                <th style={{ width: '40px' }}>
                  <Form.Check
                    type="checkbox"
                    checked={allSelected}
                    onChange={(e) => handleSelectAll(e.target.checked)}
                  />
                </th>
              )}
              {columns.map((column) => (
                <th
                  key={column.key}
                  style={{ width: column.width, cursor: column.sortable ? 'pointer' : 'default' }}
                  onClick={() => column.sortable && onSort && onSort(column.key)}
                >
                  <div className="d-flex align-items-center gap-1">
                    {column.label}
                    {column.sortable && sortField === column.key && (
                      <span>{sortDirection === 'asc' ? '↑' : '↓'}</span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.length === 0 ? (
              <tr>
                <td
                  colSpan={columns.length + (selectable ? 1 : 0)}
                  className="text-center text-muted py-4"
                >
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              data.map((item, index) => (
                <tr
                  key={String(item[keyField])}
                  onClick={() => onRowClick && onRowClick(item)}
                  style={{ cursor: onRowClick ? 'pointer' : 'default' }}
                >
                  {selectable && (
                    <td onClick={(e) => e.stopPropagation()}>
                      <Form.Check
                        type="checkbox"
                        checked={isSelected(item)}
                        onChange={(e) => handleSelectItem(item, e.target.checked)}
                      />
                    </td>
                  )}
                  {columns.map((column) => (
                    <td key={column.key}>
                      {column.render
                        ? column.render(item, index)
                        : String(item[column.key] ?? '-')}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </Table>
      </div>

      {totalItems > 0 && (
        <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 p-3 border-top">
          <div className="d-flex align-items-center gap-2">
            <span className="text-muted">Rows per page:</span>
            <Form.Select
              size="sm"
              style={{ width: 'auto' }}
              value={pageSize}
              onChange={(e) => onPageSizeChange && onPageSizeChange(Number(e.target.value))}
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </Form.Select>
            <span className="text-muted">
              Showing {currentPage * pageSize + 1} to{' '}
              {Math.min((currentPage + 1) * pageSize, totalItems)} of {totalItems}
            </span>
          </div>

          <Pagination className="mb-0">
            <Pagination.First
              disabled={currentPage === 0}
              onClick={() => onPageChange && onPageChange(0)}
            />
            <Pagination.Prev
              disabled={currentPage === 0}
              onClick={() => onPageChange && onPageChange(currentPage - 1)}
            />
            
            {[...Array(Math.min(5, totalPages))].map((_, i) => {
              let pageNum: number;
              if (totalPages <= 5) {
                pageNum = i;
              } else if (currentPage < 3) {
                pageNum = i;
              } else if (currentPage > totalPages - 4) {
                pageNum = totalPages - 5 + i;
              } else {
                pageNum = currentPage - 2 + i;
              }
              
              return (
                <Pagination.Item
                  key={pageNum}
                  active={pageNum === currentPage}
                  onClick={() => onPageChange && onPageChange(pageNum)}
                >
                  {pageNum + 1}
                </Pagination.Item>
              );
            })}
            
            <Pagination.Next
              disabled={currentPage >= totalPages - 1}
              onClick={() => onPageChange && onPageChange(currentPage + 1)}
            />
            <Pagination.Last
              disabled={currentPage >= totalPages - 1}
              onClick={() => onPageChange && onPageChange(totalPages - 1)}
            />
          </Pagination>
        </div>
      )}
    </div>
  );
}

export default DataTable;
