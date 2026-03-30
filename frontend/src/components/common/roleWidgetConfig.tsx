// Role-based widget configurations for Dashboard
// Separate file for constants to allow fast refresh in component files

import React from "react";
import {
    FaFileAlt,
    FaClipboardCheck,
    FaFlask,
    FaTimesCircle,
    FaUserTie,
    FaCheckDouble,
    FaExclamationTriangle,
    FaHourglassHalf,
    FaUserCog,
    FaTruck,
    FaBoxOpen,
    FaClipboardList,
    FaChartLine,
    FaClock,
} from "react-icons/fa";

// Stats type definition for type safety
export interface DashboardStats {
    indentStats?: {
        total?: number;
        pending?: number;
        approved?: number;
        rejected?: number;
    };
    poStats?: {
        total?: number;
        pending?: number;
        delivered?: number;
    };
    grnStats?: {
        total?: number;
        pending?: number;
    };
    inventoryStats?: {
        totalItems?: number;
        lowStock?: number;
        outOfStock?: number;
    };
    // Additional stats
    pendingL1Approvals?: number;
    pendingL2Approvals?: number;
    teamIndents?: number;
    departmentIndents?: number;
    pendingQC?: number;
    qcRejected?: number;
    pendingGRN?: number;
    pendingIssues?: number;
    lowStockCount?: number;
    approvedIndents?: number;
    pendingPOs?: number;
    overduePOs?: number;
    floorIssues?: number;
    pendingIssueConfirmation?: number;
    rejectedIndents?: number;
    activeUsers?: number;
}

interface WidgetConfig {
    id: string;
    title: string;
    icon: React.ReactNode;
    color: string;
    description?: string;
    linkTo: string;
    getCount: (stats: DashboardStats | null | undefined) => number;
}

interface RoleWidgetConfig {
    role: string[];
    widgets: WidgetConfig[];
}

export const roleWidgetConfigs: RoleWidgetConfig[] = [
    // Employee / Section Team Member
    {
        role: ["EMPLOYEE"],
        widgets: [
            {
                id: "l1-approvals",
                title: "My Approval Queue",
                icon: <FaClipboardCheck size={18} />,
                color: "primary",
                description: "Indents pending your approval",
                linkTo: "/indents/approvals?level=L1",
                getCount: (stats) => stats?.pendingL1Approvals || 0,
            },
            {
                id: "team-indents",
                title: "Team Indents",
                icon: <FaFileAlt size={18} />,
                color: "info",
                description: "Indents from your team",
                linkTo: "/indents?filter=team",
                getCount: (stats) => stats?.teamIndents || 0,
            },
        ],
    },
    // Department Head - L2 Approver
    {
        role: ["DEPTHEAD"],
        widgets: [
            {
                id: "l2-approvals",
                title: "Department Approvals",
                icon: <FaUserTie size={18} />,
                color: "primary",
                description: "L1 approved, awaiting your action",
                linkTo: "/indents/approvals?level=L2",
                getCount: (stats) => stats?.pendingL2Approvals || 0,
            },
            {
                id: "dept-summary",
                title: "Department Summary",
                icon: <FaChartLine size={18} />,
                color: "success",
                description: "This month's activity",
                linkTo: "/analytics",
                getCount: (stats) => stats?.departmentIndents || 0,
            },
        ],
    },
    // Quality Control
    {
        role: ["QUALITY"],
        widgets: [
            {
                id: "qc-pending",
                title: "QC Pending",
                icon: <FaFlask size={18} />,
                color: "warning",
                description: "GRNs awaiting quality check",
                linkTo: "/grn?status=pending-qc",
                getCount: (stats) => stats?.pendingQC || 0,
            },
            {
                id: "qc-rejected",
                title: "QC Rejected",
                icon: <FaTimesCircle size={18} />,
                color: "danger",
                description: "Items rejected in QC",
                linkTo: "/grn?status=qc-rejected",
                getCount: (stats) => stats?.qcRejected || 0,
            },
        ],
    },
    // Store Keeper
    {
        role: ["STOREKEEPER"],
        widgets: [
            {
                id: "pending-grn",
                title: "Pending GRN",
                icon: <FaTruck size={18} />,
                color: "info",
                description: "GRNs to process",
                linkTo: "/grn?status=pending",
                getCount: (stats) => stats?.pendingGRN || 0,
            },
            {
                id: "issue-requests",
                title: "Issue Requests",
                icon: <FaBoxOpen size={18} />,
                color: "warning",
                description: "Issue notes to fulfill",
                linkTo: "/issue-notes?status=pending",
                getCount: (stats) => stats?.pendingIssues || 0,
            },
            {
                id: "low-stock",
                title: "Low Stock Alerts",
                icon: <FaExclamationTriangle size={18} />,
                color: "danger",
                description: "Items below reorder level",
                linkTo: "/inventory?lowStock=true",
                getCount: (stats) => stats?.lowStockCount || 0,
            },
        ],
    },
    // Procurement
    {
        role: ["PROCUREMENT"],
        widgets: [
            {
                id: "approved-indents",
                title: "Approved Indents",
                icon: <FaClipboardList size={18} />,
                color: "success",
                description: "Ready for PO creation",
                linkTo: "/indents?status=approved",
                getCount: (stats) => stats?.approvedIndents || 0,
            },
            {
                id: "pending-pos",
                title: "Pending POs",
                icon: <FaHourglassHalf size={18} />,
                color: "warning",
                description: "POs awaiting delivery",
                linkTo: "/purchase-orders?status=pending",
                getCount: (stats) => stats?.pendingPOs || 0,
            },
            {
                id: "overdue-pos",
                title: "Overdue Deliveries",
                icon: <FaClock size={18} />,
                color: "danger",
                description: "Past delivery date",
                linkTo: "/purchase-orders?status=overdue",
                getCount: (stats) => stats?.overduePOs || 0,
            },
        ],
    },
    // Removed FLOORINCHARGE section — merged into EMPLOYEE above
    // Admin/SuperAdmin - Overview
    {
        role: ["SUPERADMIN", "ADMIN"],
        widgets: [
            {
                id: "total-indents",
                title: "Total Indents",
                icon: <FaFileAlt size={18} />,
                color: "primary",
                description: "All indents this month",
                linkTo: "/indents",
                getCount: (stats) => stats?.indentStats?.total || 0,
            },
            {
                id: "all-pending",
                title: "All Pending Approvals",
                icon: <FaClipboardCheck size={18} />,
                color: "warning",
                description: "Across all levels",
                linkTo: "/indents/approvals",
                getCount: (stats) => stats?.indentStats?.pending || 0,
            },
            {
                id: "rejected-indents",
                title: "Rejected Indents",
                icon: <FaTimesCircle size={18} />,
                color: "danger",
                description: "This month",
                linkTo: "/indents?status=rejected",
                getCount: (stats) => stats?.indentStats?.rejected || 0,
            },
            {
                id: "system-users",
                title: "Active Users",
                icon: <FaUserCog size={18} />,
                color: "info",
                description: "System users",
                linkTo: "/masters/users",
                getCount: (stats) => stats?.activeUsers || 0,
            },
        ],
    },
];

// Helper to get widgets for a user based on their roles
export const getWidgetsForRoles = (userRoles: string[]): WidgetConfig[] => {
    const allWidgets: WidgetConfig[] = [];
    const addedIds = new Set<string>();

    roleWidgetConfigs.forEach((config) => {
        const hasRole = config.role.some((r) => userRoles.includes(r));
        if (hasRole) {
            config.widgets.forEach((widget) => {
                if (!addedIds.has(widget.id)) {
                    allWidgets.push(widget);
                    addedIds.add(widget.id);
                }
            });
        }
    });

    return allWidgets;
};
