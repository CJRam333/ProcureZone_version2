// Extended page response types that include additional statistics
// These extend the base PageResponse to include count summaries returned by the backend

import { PageResponse } from './materials';

// Extended response for materials list with stats
export interface MaterialsListResponse<T> extends PageResponse<T> {
    activeCount?: number;
    categoryCount?: number;
    lowStockCount?: number;
}

// Extended response for vendors list with stats
export interface VendorsListResponse<T> extends PageResponse<T> {
    activeCount?: number;
    withGstCount?: number;
    newThisMonth?: number;
}

// Extended response for issue notes list with stats
export interface IssueNotesListResponse<T> extends PageResponse<T> {
    pendingCount?: number;
    issuedTodayCount?: number;
    returnedCount?: number;
    thisMonthCount?: number;
}

// Extended response for GRN list with stats
export interface GRNListResponse<T> extends PageResponse<T> {
    pendingQcCount?: number;
    qcApprovedCount?: number;
    completedCount?: number;
    rejectedCount?: number;
}

// Extended response for PO list with stats
export interface POListResponse<T> extends PageResponse<T> {
    pendingCount?: number;
    sentCount?: number;
    inProgressCount?: number;
    completedCount?: number;
}

// Extended response for Indents list with stats
export interface IndentsListResponse<T> extends PageResponse<T> {
    pendingCount?: number;
    approvedCount?: number;
    rejectedCount?: number;
    thisMonthCount?: number;
}

// Extended response for Inventory list with stats
export interface InventoryListResponse<T> extends PageResponse<T> {
    totalItems?: number;
    lowStockCount?: number;
    outOfStockCount?: number;
    totalValue?: number;
}
