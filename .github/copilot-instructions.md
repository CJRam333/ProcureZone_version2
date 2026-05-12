# =============================================================================

# PROCUREZONE - PROJECT DOCUMENTATION & AI INSTRUCTIONS

# =============================================================================

#

# 📦 Project: ProcureZone (Procurement Management System)

# 🔄 Migration: Legacy Struts → Modern React + Spring Boot

# 📅 Last Updated: December 9, 2024

#

# =============================================================================

## 📋 TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Architecture](#2-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Database Configuration](#4-database-configuration)
5. [User Credentials](#5-user-credentials)
6. [Database Schema](#6-database-schema)
7. [API Endpoints](#7-api-endpoints)
8. [Workflow Definitions](#8-workflow-definitions)
9. [Frontend Structure](#9-frontend-structure)
10. [Backend Structure](#10-backend-structure)
11. [Development Guidelines](#11-development-guidelines)
12. [Quick Reference](#12-quick-reference)

---

# 1. PROJECT OVERVIEW

## What is ProcureZone?

ProcureZone is an enterprise **Procurement Management System** designed for managing:

-   📝 **Indents/Requisitions** - Material request workflow
-   🛒 **Purchase Orders** - Vendor order management
-   📦 **GRN (Goods Receipt Notes)** - Incoming goods tracking
-   📤 **Issue Notes** - Material issuance to departments
-   📊 **Inventory Management** - Stock tracking and control

## Project Status

| Component                       | Status            | Technology                   |
| ------------------------------- | ----------------- | ---------------------------- |
| **Legacy App** (`ProcureZone/`) | 📦 Reference Only | Struts 1.x + JSP             |
| **New Backend** (`backend/`)    | ✅ 90% Complete   | Spring Boot 3 + Java 21      |
| **New Frontend** (`frontend/`)  | 🔄 55% Complete   | React 18 + TypeScript + Vite |

---

# 2. ARCHITECTURE

## Directory Structure

```
e:\Net-Beans\
├── 📁 ProcureZone/          # 🔴 LEGACY (Struts - Reference Only)
│   └── src/                 # Old JSP/Struts code (do not modify)
│
├── 📁 backend/              # 🟢 NEW Spring Boot Backend
│   └── src/main/java/com/nslindia/procurezone/
│       ├── controller/      # REST API Controllers
│       ├── service/         # Business Logic
│       ├── repository/      # Data Access Layer
│       ├── entity/          # JPA Entities
│       ├── dto/             # Data Transfer Objects
│       └── config/          # Configuration Classes
│
├── 📁 frontend/             # 🟢 NEW React Frontend
│   └── src/
│       ├── pages/           # Page Components
│       ├── components/      # Reusable UI Components
│       ├── api/             # API Client Functions
│       ├── contexts/        # React Contexts
│       ├── hooks/           # Custom Hooks
│       ├── routes/          # Routing Configuration
│       └── types/           # TypeScript Types
│
├── 📁 database/             # SQL Scripts & Migrations
├── 📁 .github/              # GitHub Configuration
└── 📄 PROJECT_TODO.md       # Task Tracking
```

## Data Flow

```
[React Frontend] → [Axios HTTP] → [Spring Boot API] → [JPA/Hibernate] → [MySQL]
     :5173              REST            :8080                              :3306
```

---

# 3. TECHNOLOGY STACK

## Frontend (New)

| Technology      | Version | Purpose        |
| --------------- | ------- | -------------- |
| React           | 18.x    | UI Framework   |
| TypeScript      | 5.x     | Type Safety    |
| Vite            | 5.x     | Build Tool     |
| Bootstrap       | 5.x     | CSS Framework  |
| React Router    | 6.x     | Navigation     |
| TanStack Query  | 5.x     | Data Fetching  |
| Axios           | 1.x     | HTTP Client    |
| React Hook Form | 7.x     | Form Handling  |
| React Icons     | 5.x     | Icon Library   |
| date-fns        | 3.x     | Date Utilities |

## Backend (New)

| Technology      | Version  | Purpose               |
| --------------- | -------- | --------------------- |
| Java            | 21 (LTS) | Runtime               |
| Spring Boot     | 3.2.5    | Framework             |
| Spring Security | 6.x      | Authentication        |
| Spring Data JPA | 3.x      | ORM                   |
| MySQL Connector | 8.x      | Database Driver       |
| JWT (jjwt)      | 0.12.x   | Token Auth            |
| Lombok          | 1.18.x   | Boilerplate Reduction |
| MapStruct       | 1.5.x    | Object Mapping        |
| Flyway          | 9.x      | DB Migrations         |

## Database

| Property | Value          |
| -------- | -------------- |
| Database | MySQL 8.0      |
| Schema   | `seeds_indent` |
| Host     | `localhost`    |
| Port     | `3306`         |

---

# 4. DATABASE CONFIGURATION

## Connection Details

```yaml
# backend/src/main/resources/application.yml
spring:
    datasource:
        url: jdbc:mysql://localhost:3306/seeds_indent
        username: root
        password: password
        driver-class-name: com.mysql.cj.jdbc.Driver
```

| Property | Value                                      |
| -------- | ------------------------------------------ |
| Host     | `localhost`                                |
| Port     | `3306`                                     |
| Database | `seeds_indent`                             |
| Username | `root`                                     |
| Password | `password`                                 |
| JDBC URL | `jdbc:mysql://localhost:3306/seeds_indent` |

---

# 5. USER CREDENTIALS

## Default Password

All users: `password123` (BCrypt hashed in database)

## Key Users by Role

| Username          | Employee Name   | Primary Role(s)          |
| ----------------- | --------------- | ------------------------ |
| `rajesh.kumar`    | Rajesh Kumar    | SUPERADMIN, PLANTMANAGER |
| `priya.sharma`    | Priya Sharma    | ADMIN, PLANTMANAGER      |
| `amit.patel`      | Amit Patel      | ADMIN, PLANTMANAGER      |
| `vikram.singh`    | Vikram Singh    | SUPERADMIN, DEPTHEAD     |
| `rahul.joshi`     | Rahul Joshi     | DEPTHEAD, PROCUREMENT    |
| `neha.gupta`      | Neha Gupta      | PROCUREMENT              |
| `anjali.mehta`    | Anjali Mehta    | DEPTHEAD, FINANCE        |
| `ravi.chandra`    | Ravi Chandra    | SUPERADMIN, STOREKEEPER  |
| `lakshmi.nambiar` | Lakshmi Nambiar | STOREKEEPER              |
| `manoj.tiwari`    | Manoj Tiwari    | VIEWER                   |

## Available Roles (14)

| Code             | Name                | Permissions          |
| ---------------- | ------------------- | -------------------- |
| `SUPERADMIN`     | Super Administrator | Full Access          |
| `ADMIN`          | Administrator       | Admin Access         |
| `PLANTMANAGER`   | Plant Manager       | Plant Management     |
| `DEPTHEAD`       | Department Head     | Department Approval  |
| `PROCUREMENT`    | Procurement Officer | PO Management        |
| `FINANCE`        | Finance Manager     | Financial Oversight  |
| `QUALITY`        | Quality Manager     | QC Functions         |
| `STOREKEEPER`    | Store Keeper        | Inventory Management |
| `EMPLOYEE`       | Regular Employee    | Create Indents       |
| `VIEWER`         | View Only           | Read Access          |
| `FLOORINCHARGE`  | Floor Incharge      | Floor Operations     |
| `SUPERVISOR`     | Supervisor          | Team Supervision     |
| `AUDITOR`        | Auditor             | Audit Access         |
| `QUALITYMANAGER` | Quality Manager     | QC Management        |

---

# 6. DATABASE SCHEMA

## Overview

-   **Total Tables**: 51
-   **Active Tables**: 36 (`tbl_*` prefix)
-   **Legacy Tables**: 15 (`pz_*` prefix - all empty, DO NOT USE)

## Core Master Tables

| Table                   | Rows | Purpose              |
| ----------------------- | ---- | -------------------- |
| `tbl_emp_master`        | 25   | Employee information |
| `tbl_user_master`       | 25   | User authentication  |
| `tbl_company_master`    | 9    | Company entities     |
| `tbl_plant_master`      | 5    | Plant locations      |
| `tbl_location_master`   | 7    | Geographic locations |
| `tbl_department_master` | 16   | Departments          |
| `tbl_section_master`    | 4    | Department sections  |
| `tbl_material_master`   | 19   | Material catalog     |
| `tbl_umo_master`        | 12   | Units of measure     |
| `tbl_vendors`           | 41   | Vendor/Supplier info |
| `tbl_roles_master`      | 14   | Role definitions     |

## Transaction Tables

| Table                        | Rows | Purpose            |
| ---------------------------- | ---- | ------------------ |
| `tbl_indent_master`          | 75   | Indent headers     |
| `tbl_indent_details`         | 110  | Indent line items  |
| `tbl_purchase_orders`        | 26   | PO headers         |
| `tbl_purchase_order_details` | 41   | PO line items      |
| `tbl_goods_receipt`          | 0    | GRN records        |
| `tbl_issue_note`             | 28   | Issue note headers |
| `tbl_issue_note_details`     | 31   | Issue note items   |

## Inventory Tables

| Table                       | Rows | Purpose              |
| --------------------------- | ---- | -------------------- |
| `tbl_inventory_balance`     | 1    | Current stock levels |
| `tbl_inventory_transaction` | 2    | Stock movements      |

## Mapping Tables

| Table                        | Rows | Purpose                   |
| ---------------------------- | ---- | ------------------------- |
| `tbl_map_company_department` | 17   | Company-Dept mapping      |
| `tbl_map_company_emp`        | 25   | Company-Employee mapping  |
| `tbl_map_company_location`   | 9    | Company-Location mapping  |
| `tbl_map_emp_reporting`      | 21   | Reporting hierarchy       |
| `tbl_map_emp_roles`          | 50   | Employee-Role assignments |

## System Tables

| Table                   | Rows | Purpose           |
| ----------------------- | ---- | ----------------- |
| `tbl_audit_log`         | 1659 | Audit trail       |
| `tbl_approval_workflow` | 207  | Approval tracking |
| `tbl_email_template`    | 16   | Email templates   |
| `tbl_email_log`         | 0    | Email history     |

---

# 7. API ENDPOINTS

## Base URL

`http://localhost:8080/api/v1`

## Authentication

| Method | Endpoint       | Description       |
| ------ | -------------- | ----------------- |
| POST   | `/auth/login`  | User login        |
| POST   | `/auth/logout` | User logout       |
| GET    | `/auth/me`     | Current user info |

## Master Data

| Resource    | Endpoints      |
| ----------- | -------------- |
| Employees   | `/employees`   |
| Companies   | `/companies`   |
| Plants      | `/plants`      |
| Locations   | `/locations`   |
| Departments | `/departments` |
| Sections    | `/sections`    |
| Materials   | `/materials`   |
| Vendors     | `/vendors`     |
| UOM         | `/uom`         |
| Roles       | `/roles`       |

## Procurement

| Resource        | Endpoints      |
| --------------- | -------------- |
| Indents         | `/indents`     |
| Purchase Orders | `/pos`         |
| GRN             | `/grn`         |
| Issue Notes     | `/issue-notes` |

## Inventory

| Resource  | Endpoints    |
| --------- | ------------ |
| Inventory | `/inventory` |

---

# 8. WORKFLOW DEFINITIONS

## Indent Workflow

```
0=Draft → 1=Submitted → 2=L1_Approved → 4=L2_Approved → 6=PO_Created → 7=Closed
                     ↘ 3=L1_Rejected   ↘ 5=L2_Rejected
```

| Status      | Code | Description              |
| ----------- | ---- | ------------------------ |
| Draft       | 0    | Initial creation         |
| Submitted   | 1    | Pending L1 approval      |
| L1 Approved | 2    | RM/Section Head approved |
| L1 Rejected | 3    | RM/Section Head rejected |
| L2 Approved | 4    | Dept Head approved       |
| L2 Rejected | 5    | Dept Head rejected       |
| PO Created  | 6    | Purchase order generated |
| Closed      | 7    | Completed                |

## PO Workflow

| Status             | Code | Description          |
| ------------------ | ---- | -------------------- |
| Created            | 1    | PO generated         |
| Approved           | 2    | PO confirmed         |
| Sent to Vendor     | 3    | Dispatched to vendor |
| Partially Received | 4    | Some goods received  |
| Fully Received     | 5    | All goods received   |
| Closed             | 6    | Completed            |
| Cancelled          | 7    | Cancelled            |

## GRN Workflow

| Status      | Code | Description        |
| ----------- | ---- | ------------------ |
| Created     | 1    | GRN registered     |
| L1 Approved | 2    | QC passed          |
| L2 Approved | 3    | Stores accepted    |
| Stored      | 4    | Added to inventory |
| Closed      | 5    | Completed          |

## Issue Note Workflow

| Status   | Code | Description        |
| -------- | ---- | ------------------ |
| Created  | 1    | Request created    |
| Approved | 2    | Dept Head approved |
| Issued   | 3    | Material issued    |
| Returned | 4    | Material returned  |
| Closed   | 5    | Completed          |

---

# 9. FRONTEND STRUCTURE

## File Organization

```
frontend/src/
├── api/                 # API client functions
│   ├── client.ts        # Axios instance
│   ├── index.ts         # Exports
│   ├── indents.ts       # Indent API
│   ├── purchaseOrders.ts
│   ├── grn.ts
│   ├── issueNotes.ts
│   ├── inventory.ts
│   ├── vendors.ts
│   ├── materials.ts
│   └── ...
├── components/
│   ├── common/          # Reusable components
│   │   ├── DataTable.tsx
│   │   ├── PageHeader.tsx
│   │   ├── LoadingSpinner.tsx
│   │   └── StatusBadge.tsx
│   └── layout/          # Layout components
│       ├── MainLayout.tsx
│       ├── Sidebar.tsx
│       └── Header.tsx
├── contexts/
│   └── AuthContext.tsx  # Authentication state
├── pages/
│   ├── auth/            # Login pages
│   ├── dashboard/       # Dashboard
│   ├── indents/         # Indent management
│   ├── purchase-orders/ # PO management
│   ├── grn/             # GRN management
│   ├── issue-notes/     # Issue note management
│   ├── inventory/       # Inventory management
│   ├── vendors/         # Vendor management
│   ├── materials/       # Material management
│   ├── masters/         # Master data pages
│   ├── mappings/        # Mapping pages
│   ├── reports/         # Reports
│   └── profile/         # User profile
├── routes/
│   ├── router.tsx       # Route definitions
│   └── ProtectedRoute.tsx
└── styles/
    └── index.css        # Global styles
```

## Key Libraries

-   **UI**: React-Bootstrap 2.x
-   **Icons**: React-Icons (Feather, FontAwesome)
-   **Forms**: React Hook Form + Zod validation
-   **Tables**: Custom DataTable component
-   **Toasts**: React-Toastify
-   **Dates**: date-fns

---

# 10. BACKEND STRUCTURE

## Package Organization

```
com.nslindia.procurezone/
├── ProcurezoneApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── JwtConfig.java
├── controller/
│   ├── AuthController.java
│   ├── IndentController.java
│   ├── POController.java
│   └── ...
├── service/
│   ├── AuthService.java
│   ├── IndentService.java
│   └── ...
├── repository/
│   ├── IndentRepository.java
│   └── ...
├── entity/
│   ├── Indent.java
│   ├── PurchaseOrder.java
│   └── ...
├── dto/
│   ├── request/
│   └── response/
├── security/
│   ├── JwtUtils.java
│   └── UserDetailsServiceImpl.java
└── exception/
    └── GlobalExceptionHandler.java
```

---

# 11. DEVELOPMENT GUIDELINES

## AI Assistant Rules

### DO ✅

-   Read config files before making changes
-   Verify database schema with queries
-   Use exact credentials from this document
-   Make minimal, targeted code changes
-   Test changes before suggesting

### DON'T ❌

-   Invent API endpoints
-   Invent database tables/columns
-   Invent role names or credentials
-   Invent workflow states
-   Make full-class rewrites
-   Use placeholder code

## Commands

```bash
# Start Backend (port 8080)
cd backend && mvn spring-boot:run

# Start Frontend (port 5173)
cd frontend && npm run dev

# Build Frontend
cd frontend && npm run build

# MySQL Access
mysql -u root -ppassword seeds_indent

# Check Table
mysql -u root -ppassword seeds_indent -e "DESCRIBE table_name"
```

---

# 12. QUICK REFERENCE

## Ports

| Service        | Port |
| -------------- | ---- |
| Frontend (Dev) | 5173 |
| Backend API    | 8080 |
| MySQL          | 3306 |

## URLs

| Resource | URL                                   |
| -------- | ------------------------------------- |
| Frontend | http://localhost:5173                 |
| API Base | http://localhost:8080/api/v1          |
| API Docs | http://localhost:8080/swagger-ui.html |

## Status Codes (General)

| Code | Meaning  |
| ---- | -------- |
| 1    | Active   |
| 0    | Inactive |

## Theme

| Property      | Value      |
| ------------- | ---------- |
| Primary Color | `#2563eb`  |
| Background    | `#f8fafc`  |
| Theme         | Light Only |

---

# Important

Always give tab space before the command for CMD commands

like for Exmaple

Example
Expected :
some space and then  ls

Not:
ls

Simlarly for others :

Example

Expected :
some space and then  mvn spring-boot:run

Not:
mvn spring-boot:run

# =============================================================================

# END OF DOCUMENTATION

# =============================================================================
