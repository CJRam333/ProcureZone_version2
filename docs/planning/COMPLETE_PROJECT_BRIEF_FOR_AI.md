# ProcureZone — Complete End-to-End Project Brief (Backend + Frontend)

> **Purpose:** This document is a complete, self-contained reference for the entire ProcureZone project (backend Spring Boot API + React frontend). Feed this to any AI assistant so it has full context to help debug, extend, and track the system.
>
> **Original Created:** February 2026
> **Last Updated:** April 18, 2026
> **Primary Developer (post-March 2026):** CJRam_NSL — janakiraama182@gmail.com
> **Earlier Contributor:** SaiHarsha502vvit (pre-March 2026)

---

## 1. PROJECT IDENTITY

| Field | Value |
|---|---|
| **Project Name** | ProcureZone |
| **Owner** | NSL India Pvt Ltd |
| **Type** | Enterprise Procurement & Manufacturing Operations System |
| **Backend Status** | Stable & Running — 248 endpoints, 28 controllers |
| **Frontend Status** | ~70% complete — React + TypeScript + Vite |
| **Backend Tech Stack** | Spring Boot 3.2.5, Java 21, MySQL 8.0, JWT Auth, Flyway, Quartz |
| **Frontend Tech Stack** | React 18, TypeScript, Vite, React Query, React Bootstrap, Zod |
| **Group ID** | `com.nslindia.procurezone` |
| **Artifact ID** | `procurezone-backend` |
| **Version** | `0.1.0-SNAPSHOT` |
| **Backend Port** | 8080 |
| **Frontend Port** | 5173 (Vite dev server) |
| **Database Name** | `seeds_indent_replica_1` (active) / `seeds_indent` (original) |
| **Git - Backend** | Committed under CJRam_NSL (janakiraama182@gmail.com) |
| **Git - Frontend** | Committed under CJRam_NSL (janakiraama182@gmail.com) |

---

## 2. WHAT THIS SYSTEM DOES

ProcureZone is an **enterprise procurement and manufacturing operations backend** with a dual-module architecture:

### Corporate Module (Office Procurement)
- **Indent Management** — Employees create material requisitions (indents) that flow through multi-level approval (HOD → Plant Manager → Procurement)
- **Purchase Order (PO) Management** — Approved indents become POs sent to vendors, with full lifecycle tracking (Draft → Approved → Sent → Received → Closed)
- **Goods Receipt Note (GRN)** — When materials arrive, GRN is created with quality inspection workflow
- **Issue Notes** — Internal material issuance from stores to departments

### Plant Module (Manufacturing Floor)
- **Plant Indents** — Manufacturing-specific material requisitions
- **SAP Integration** — Nightly CSV import of material master data from SAP ERP
- **Production Line Tracking** — Track materials by plant, line code, storage location

### Business Workflow (End-to-End Procurement Lifecycle)
```
Employee creates Indent
    → HOD approves (Level 1)
    → Plant Manager approves (Level 2)
    → Procurement team creates PO
    → PO sent to Vendor
    → Vendor delivers materials
    → Stores creates GRN (Goods Receipt)
    → Quality inspection (pass/fail)
    → Materials added to inventory
    → Departments raise Issue Notes to get materials from stores
```

---

## 3. TECHNOLOGY STACK (Dependencies from pom.xml)

### Core Framework
| Dependency | Version | Purpose |
|---|---|---|
| `spring-boot-starter-web` | 3.2.5 | REST API framework |
| `spring-boot-starter-data-jpa` | 3.2.5 | ORM / Database access (Hibernate) |
| `spring-boot-starter-security` | 3.2.5 | Authentication & Authorization |
| `spring-boot-starter-validation` | 3.2.5 | Request validation (`@Valid`) |
| `spring-boot-starter-actuator` | 3.2.5 | Health checks & monitoring |
| `spring-boot-starter-quartz` | 3.2.5 | Job scheduling |
| `spring-boot-starter-mail` | 3.2.5 | Email notifications (SMTP) |

### Security & Auth
| Dependency | Version | Purpose |
|---|---|---|
| `jjwt-api` | 0.12.5 | JWT token creation & parsing |
| `jjwt-impl` | 0.12.5 | JWT implementation |
| `jjwt-jackson` | 0.12.5 | JWT JSON serialization |

### Database
| Dependency | Version | Purpose |
|---|---|---|
| `mysql-connector-j` | (managed) | MySQL JDBC driver |
| `flyway-core` | (managed) | Database migration tool |
| `flyway-mysql` | (managed) | Flyway MySQL support |
| `h2` | (managed) | In-memory DB for tests |

### Document Generation
| Dependency | Version | Purpose |
|---|---|---|
| `poi-ooxml` | 5.2.5 | Excel report generation |
| `itext7-core` | 7.2.5 | PDF generation (Indent/PO PDFs) |
| `openpdf` | 1.3.43 | PDF generation (Reports) |
| `commons-csv` | 1.10.0 | CSV parsing (SAP import, bulk import) |

### Dev Tools
| Dependency | Purpose |
|---|---|
| `lombok` | Boilerplate reduction (@Data, @Builder, etc.) |

---

## 4. APPLICATION CONFIGURATION (application.yml)

```yaml
# Database
spring.datasource:
  url: jdbc:mysql://127.0.0.1:3306/seeds_indent
  username: ${PROCUREZONE_DB_USERNAME:root}
  password: ${PROCUREZONE_DB_PASSW}
  
# JPA - DDL managed by Flyway, not Hibernate
spring.jpa.hibernate.ddl-auto: none

# Flyway migrations
spring.flyway:
  enabled: true
  baseline-on-migrate: true
  baseline-version: 20
  locations: classpath:db/migration

# JWT Configuration
jwt:
  issuer: procurezone-backend
  secret: ${PROCUREZONE_JWT_SECRET}
  access-token.expiration: PT1H  # 1 hour

# SAP Import
app.sap:
  import.path: ${SAP_CSV_PATH:E:/sap-import}
  scheduler.cron: 0 30 1 * * ?  # Daily at 1:30 AM

# Email (Internal SMTP, no auth)
spring.mail:
  host: 172.16.65.65
  port: 25

# Server
server.port: 8080

# Actuator
management.endpoints.web.exposure.include: health,info,metrics
```

### Environment Variables Required
| Variable | Purpose | Default |
|---|---|---|
| `PROCUREZONE_DB_USERNAME` | MySQL username | `root` |
| `PROCUREZONE_DB_PASSW` | MySQL password | (none — required) |
| `PROCUREZONE_JWT_SECRET` | JWT signing key | dev key (change for prod) |
| `SAP_CSV_PATH` | SAP CSV import directory | `E:/sap-import` |
| `SAP_IMPORT_ENABLED` | Enable SAP import | `true` |
| `SAP_SCHEDULER_ENABLED` | Enable SAP scheduler | `true` |
| `SMTP_HOST` | SMTP server | `172.16.65.65` |

---

## 5. COMPLETE PROJECT STRUCTURE

```
backend/src/main/java/com/nslindia/procurezone/
│
├── auth/                          # Authentication module
│   ├── controller/AuthController.java     # Login, logout, /me
│   └── service/AuthService.java           # Auth business logic
│
├── security/                      # Security infrastructure
│   ├── JwtAuthenticationFilter.java       # JWT filter in security chain
│   ├── JwtService.java                    # JWT create/validate/parse
│   ├── PasswordService.java              # BCrypt + MD5 legacy support
│   ├── PlantSecurityService.java         # Plant-level access control
│   ├── TokenBlacklistService.java        # Token revocation (logout)
│   └── UserPrincipal.java                # Spring Security principal
│
├── config/                        # Spring configuration
│   ├── SecurityConfig.java               # Security filter chain, CORS, STATELESS sessions
│   ├── WebConfig.java                    # Web MVC config
│   └── JwtProperties.java               # JWT config properties
│
├── indent/                        # Indent (requisition) module
│   ├── IndentController.java             # 11 endpoints
│   ├── IndentService.java                # Indent business logic
│   ├── IndentRepository.java             # JPA repository
│   ├── IndentDetailRepository.java       # Line items repo
│   ├── ApprovalController.java           # 7 approval endpoints
│   └── ApprovalWorkflowRepository.java   # Approval history repo
│
├── po/                            # Purchase Order module
│   ├── POController.java                 # 17 endpoints
│   ├── POService.java                    # PO business logic
│   ├── PORepository.java                 # PO header repo
│   ├── PODetailRepository.java           # PO line items repo
│   └── POAmendmentRepository.java        # PO amendments repo
│
├── grn/                           # Goods Receipt Note module
│   ├── GRNController.java                # 12 endpoints
│   ├── GRNService.java                   # GRN business logic
│   ├── GrnQcController.java             # QC inspection endpoints
│   ├── GrnQcService.java                # Quality check logic
│   ├── GoodsReceiptRepository.java       # GRN repo
│   └── GrnQcResultRepository.java        # QC results repo
│
├── issuenote/                     # Issue Note module
│   ├── IssueNoteController.java          # 15 endpoints
│   ├── IssueNoteService.java             # Issue note logic
│   ├── IssueNoteRepository.java          # Issue note repo
│   └── IssueNoteDetailsRepository.java   # Line items repo
│
├── inventory/                     # Inventory management
│   ├── InventoryController.java          # 8 endpoints
│   └── InventoryService.java             # Stock tracking
│
├── vendor/                        # Vendor management
│   ├── VendorController.java             # 9 endpoints
│   ├── VendorService.java                # Vendor logic
│   └── VendorRepository.java             # Vendor repo
│
├── employee/                      # Employee management
│   ├── EmployeeController.java           # 12 endpoints
│   └── EmployeeService.java              # Employee logic
│
├── user/                          # User account management
│   ├── UserController.java               # 8 endpoints
│   └── UserService.java                  # User logic
│
├── masterdata/                    # Master data modules
│   ├── controller/
│   │   ├── CompanyController.java        # 7 endpoints
│   │   ├── DepartmentController.java     # 7 endpoints
│   │   ├── LocationController.java       # 7 endpoints
│   │   ├── PlantController.java          # 7 endpoints
│   │   ├── MaterialController.java       # 7 endpoints
│   │   ├── SectionController.java        # Section CRUD
│   │   └── UnitOfMeasureController.java  # 7 endpoints
│   ├── service/
│   │   ├── CompanyService.java
│   │   ├── DepartmentService.java
│   │   ├── LocationService.java
│   │   ├── PlantService.java
│   │   ├── MaterialService.java
│   │   ├── SectionService.java
│   │   └── UnitOfMeasureService.java
│   └── repository/
│       ├── CompanyRepository.java
│       ├── DepartmentRepository.java
│       ├── MaterialRepository.java
│       ├── SectionRepository.java
│       └── UnitOfMeasureRepository.java
│
├── mapping/                       # Organization mapping
│   ├── CompanyLocationMaterialController.java   # 9 endpoints
│   ├── CompanyPlantMaterialController.java      # 9 endpoints
│   ├── CompanyLocationMaterialService.java
│   └── CompanyPlantMaterialService.java
│
├── controller/                    # Additional mapping controllers
│   ├── CompanyDepartmentController.java         # 9 endpoints
│   ├── CompanyEmployeeController.java           # 11 endpoints
│   ├── CompanyLocationController.java           # 9 endpoints
│   ├── EmployeeReportingController.java         # 16 endpoints
│   └── EmployeeRoleController.java              # 11 endpoints
│
├── service/                       # Mapping services
│   ├── CompanyDepartmentService.java
│   ├── CompanyEmployeeService.java
│   ├── CompanyLocationService.java
│   ├── EmployeeReportingService.java
│   └── EmployeeRoleService.java
│
├── dashboard/                     # Dashboard & analytics
│   ├── controller/DashboardController.java      # 13 endpoints
│   └── service/DashboardService.java
│
├── document/                      # PDF generation
│   └── service/
│       ├── IndentPdfService.java         # Indent PDF docs
│       └── PurchaseOrderPdfService.java  # PO PDF docs
│
├── report/                        # Reports module
│   ├── controller/
│   │   ├── ComprehensiveReportController.java
│   │   ├── MaterialReportController.java        # 3 endpoints
│   │   ├── PdfReportController.java
│   │   └── ReportApiController.java
│   └── service/
│       ├── ExcelReportService.java       # Excel exports
│       ├── IndentReportService.java
│       ├── InventoryReportService.java
│       ├── MaterialQuantityReportService.java
│       ├── POReportService.java
│       ├── PdfReportService.java         # PDF reports (OpenPDF)
│       └── VendorPerformanceService.java
│
├── notification/                  # Email notifications
│   ├── service/
│   │   ├── EmailService.java              # Core SMTP service
│   │   ├── IndentNotificationService.java
│   │   ├── InventoryNotificationService.java
│   │   └── PONotificationService.java
│   └── repository/
│       ├── EmailLogRepository.java
│       └── EmailTemplateRepository.java
│
├── bulkimport/                    # Bulk data import
│   ├── BulkImportController.java         # 2 endpoints
│   └── MaterialBulkImportService.java
│
├── integration/sap/              # SAP ERP integration
│   ├── controller/SapMaterialImportController.java  # 2 endpoints
│   └── service/
│       ├── CsvParserService.java
│       └── MaterialImportService.java
│
├── sap/                           # SAP scheduled import
│   ├── controller/SapImportController.java
│   ├── service/SapImportService.java
│   └── repository/
│       ├── SapImportLogRepository.java
│       └── SapScheduleMaterialRepository.java
│
├── plantindent/                   # Plant-specific indents
│   ├── PlantIndentController.java
│   ├── PlantIndentService.java
│   ├── PlantIndentRepository.java
│   ├── PlantIndentDetailRepository.java
│   └── repository/
│       ├── CropGroupRepository.java
│       ├── EmployeePlantMapRepository.java
│       ├── PlantLineCodeRepository.java
│       ├── PlantOrderTypeRepository.java
│       ├── PlantProcessPackingRepository.java
│       ├── PlantStorageLocationRepository.java
│       └── SapMaterialMasterRepository.java
│
├── scheduler/                     # Job scheduling
│   └── service/JobSchedulerService.java
│
├── audit/                         # Audit logging
│   ├── AuditService.java
│   └── AuditLogRepository.java
│
├── common/                        # Shared utilities
│   └── service/DocumentNumberService.java  # FY-based doc numbering (IND/2024-25/0001)
│
├── identity/                      # Identity/auth domain
│   ├── EmployeeRepository.java
│   ├── EmployeeRoleRepository.java
│   ├── EmployeeReportingHierarchyRepository.java
│   ├── RoleRepository.java
│   ├── UserAccountRepository.java
│   ├── UserRepository.java
│   └── ReportingHierarchyService.java
│
├── domain/                        # Domain entities
│   └── inventory/
│       ├── Inventory.java
│       └── InventoryTransaction.java
│
├── entity/                        # JPA entities (mapping tables)
│   ├── CompanyDepartment.java
│   ├── CompanyEmployee.java
│   ├── CompanyLocation.java
│   ├── EmployeeReporting.java
│   └── EmployeeRole.java
│
├── dto/                           # Data transfer objects
│   ├── CompanyDepartmentResponse.java
│   ├── CompanyEmployeeResponse.java
│   ├── CompanyLocationResponse.java
│   ├── EmployeeReportingResponse.java
│   ├── EmployeeRoleResponse.java
│   ├── OrgChartNode.java
│   ├── Create*Request.java (5 types)
│   ├── Update*Request.java (5 types)
│   ├── inventory/
│   │   ├── InventoryResponse.java
│   │   ├── InventoryStatisticsDTO.java
│   │   ├── InventoryTransactionResponse.java
│   │   └── StockAdjustmentRequest.java
│   └── issuenote/
│       ├── IssueNoteRequest.java
│       ├── IssueNoteResponse.java
│       ├── IssueNoteDetailsDTO.java
│       └── IssueNoteStatisticsDTO.java
│
└── exception/                     # Global exception handling
```

---

## 6. COMPLETE API ENDPOINTS (225 Total)

### Authentication (`/api/v1/auth`) — 3 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/auth/login` | Login with username/password, returns JWT |
| GET | `/api/v1/auth/me` | Get current user info from JWT |
| POST | `/api/v1/auth/logout` | Logout, blacklist JWT token |

### Indent Management (`/api/v1/indents`) — 11 endpoints
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/indents` | List all indents (paginated) |
| GET | `/api/v1/indents/{id}` | Get indent by ID |
| POST | `/api/v1/indents` | Create new indent |
| PUT | `/api/v1/indents/{id}` | Update indent |
| DELETE | `/api/v1/indents/{id}` | Delete indent |
| POST | `/api/v1/indents/{id}/submit` | Submit for approval |
| GET | `/api/v1/indents/status/{status}` | Filter by status |
| GET | `/api/v1/indents/employee/{empId}` | Indents by employee |
| GET | `/api/v1/indents/search` | Search indents |
| GET | `/api/v1/indents/pending-approval` | Pending approvals |
| GET | `/api/v1/indents/statistics` | Indent stats |

### Approval Workflow (`/api/v1/approvals`) — 7 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/approvals/{indentId}/approve` | Approve indent |
| POST | `/api/v1/approvals/{indentId}/reject` | Reject indent |
| GET | `/api/v1/approvals/{indentId}/history` | Approval history |
| GET | `/api/v1/approvals/pending` | Pending approvals for current user |
| POST | `/api/v1/approvals/{indentId}/final-approve` | Final (Level 2) approval |
| POST | `/api/v1/approvals/{indentId}/final-reject` | Final rejection |
| POST | `/api/v1/approvals/{indentId}/request-info` | Request more info from creator |

### Purchase Orders (`/api/v1/pos`) — 17 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/pos` | Create PO from indent |
| GET | `/api/v1/pos` | List all POs |
| GET | `/api/v1/pos/{id}` | Get PO by ID |
| PUT | `/api/v1/pos/{id}` | Update PO |
| POST | `/api/v1/pos/{id}/submit` | Submit PO for approval |
| POST | `/api/v1/pos/{id}/approve` | Approve PO |
| POST | `/api/v1/pos/{id}/reject` | Reject PO |
| POST | `/api/v1/pos/{id}/send-to-vendor` | Send PO to vendor |
| POST | `/api/v1/pos/{id}/cancel` | Cancel PO |
| POST | `/api/v1/pos/{id}/close` | Close PO |
| GET | `/api/v1/pos/vendor/{vendorId}` | POs by vendor |
| GET | `/api/v1/pos/department/{deptId}` | POs by department |
| GET | `/api/v1/pos/pending-approval` | Pending PO approvals |
| GET | `/api/v1/pos/overdue` | Overdue POs |
| GET | `/api/v1/pos/statistics` | PO statistics |
| GET | `/api/v1/pos/{id}/pdf` | Generate PO PDF |
| GET | `/api/v1/pos/search` | Search POs |

### Goods Receipt Note (`/api/v1/grns`) — 12 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/grns` | Create GRN |
| GET | `/api/v1/grns` | List all GRNs |
| GET | `/api/v1/grns/{id}` | Get GRN by ID |
| POST | `/api/v1/grns/{id}/approve` | Approve GRN (Level 1) |
| POST | `/api/v1/grns/{id}/reject` | Reject GRN |
| POST | `/api/v1/grns/{id}/final-approve` | Final approve (Level 2) |
| POST | `/api/v1/grns/{id}/store` | Store goods |
| GET | `/api/v1/grns/pending-approval` | Pending approvals |
| GET | `/api/v1/grns/pending-store` | Pending store confirmations |
| GET | `/api/v1/grns/statistics` | GRN statistics |
| POST | `/api/v1/grns/{id}/qc` | Quality check |
| GET | `/api/v1/grns/{id}/qc-results` | QC results |

### Issue Notes (`/api/v1/issue-notes`) — 15 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/issue-notes` | Create issue note |
| GET | `/api/v1/issue-notes` | List all |
| GET | `/api/v1/issue-notes/{id}` | Get by ID |
| PUT | `/api/v1/issue-notes/{id}` | Update |
| DELETE | `/api/v1/issue-notes/{id}` | Delete |
| POST | `/api/v1/issue-notes/{id}/submit` | Submit for approval |
| POST | `/api/v1/issue-notes/{id}/approve` | RM approves |
| POST | `/api/v1/issue-notes/{id}/reject` | RM rejects |
| POST | `/api/v1/issue-notes/{id}/issue` | Stores issues material |
| POST | `/api/v1/issue-notes/{id}/stores-reject` | Stores rejects |
| GET | `/api/v1/issue-notes/my` | Current user's notes |
| GET | `/api/v1/issue-notes/department/{deptId}` | By department |
| GET | `/api/v1/issue-notes/pending-approval` | Pending approvals |
| GET | `/api/v1/issue-notes/pending-issue` | Pending store issues |
| GET | `/api/v1/issue-notes/statistics` | Statistics |

### Inventory (`/api/v1/inventory`) — 8 endpoints
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/inventory` | List all stock |
| GET | `/api/v1/inventory/{materialId}` | Stock for material |
| GET | `/api/v1/inventory/low-stock` | Low stock alerts |
| GET | `/api/v1/inventory/critical` | Critical stock |
| POST | `/api/v1/inventory/adjust` | Stock adjustment |
| GET | `/api/v1/inventory/transactions` | Transaction history |
| GET | `/api/v1/inventory/check/{materialId}` | Availability check |
| GET | `/api/v1/inventory/statistics` | Inventory stats |

### Vendor Management (`/api/v1/vendors`) — 9 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/vendors` | Create vendor |
| GET | `/api/v1/vendors` | List all |
| GET | `/api/v1/vendors/{id}` | Get by ID |
| PUT | `/api/v1/vendors/{id}` | Update |
| DELETE | `/api/v1/vendors/{id}` | Delete |
| GET | `/api/v1/vendors/active` | Active vendors |
| GET | `/api/v1/vendors/search` | Search |
| PUT | `/api/v1/vendors/{id}/rating` | Update rating |
| GET | `/api/v1/vendors/{id}/performance` | Performance metrics |

### User Management (`/api/v1/users`) — 8 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/users` | Create user account |
| GET | `/api/v1/users` | List all users |
| GET | `/api/v1/users/{id}` | Get user by ID |
| PUT | `/api/v1/users/{id}` | Update user |
| PUT | `/api/v1/users/{id}/password` | Change password |
| POST | `/api/v1/users/{id}/reset-password` | Reset password |
| POST | `/api/v1/users/{id}/lock` | Lock account |
| POST | `/api/v1/users/{id}/unlock` | Unlock account |

### Employee Management (`/api/v1/employees`) — 12 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/employees` | Create employee |
| GET | `/api/v1/employees` | List all |
| GET | `/api/v1/employees/{id}` | Get by ID |
| PUT | `/api/v1/employees/{id}` | Update |
| DELETE | `/api/v1/employees/{id}` | Delete |
| GET | `/api/v1/employees/active` | Active employees |
| GET | `/api/v1/employees/search` | Search |
| GET | `/api/v1/employees/department/{deptId}` | By department |
| POST | `/api/v1/employees/{id}/activate` | Activate |
| POST | `/api/v1/employees/{id}/deactivate` | Deactivate |
| GET | `/api/v1/employees/{id}/roles` | Get employee roles |
| POST | `/api/v1/employees/{id}/roles` | Assign roles |

### Master Data (6 entities × ~7 endpoints each = 42 endpoints)
Each master data entity (Company, Department, Location, Plant, Material, UnitOfMeasure) has:
| Method | Path Pattern | Purpose |
|---|---|---|
| POST | `/api/v1/{entity}` | Create |
| GET | `/api/v1/{entity}` | List all (paginated) |
| GET | `/api/v1/{entity}/{id}` | Get by ID |
| PUT | `/api/v1/{entity}/{id}` | Update |
| DELETE | `/api/v1/{entity}/{id}` | Delete |
| GET | `/api/v1/{entity}/active` | Active only |
| GET | `/api/v1/{entity}/search` | Search |

**Base paths:** `/api/v1/companies`, `/api/v1/departments`, `/api/v1/locations`, `/api/v1/plants`, `/api/v1/materials`, `/api/v1/uom`

### Mapping Endpoints (74 endpoints)
| Controller | Endpoints | Purpose |
|---|---|---|
| CompanyDepartmentController | 9 | Which departments belong to which company |
| CompanyEmployeeController | 11 | Which employees belong to which company |
| CompanyLocationController | 9 | Which locations belong to which company |
| EmployeeRoleController | 11 | Which roles are assigned to which employee |
| EmployeeReportingController | 16 | Reporting hierarchy (subordinate → supervisor) + org chart |
| CompanyPlantMaterialController | 9 | Material-plant-company inventory mapping |
| CompanyLocationMaterialController | 9 | Material-location-company inventory mapping |

### Dashboard (`/api/v1/dashboard`) — 13 endpoints
| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/dashboard/statistics` | Comprehensive stats |
| GET | `/api/v1/dashboard/summary` | Summary cards |
| GET | `/api/v1/dashboard/indents/stats` | Indent stats |
| GET | `/api/v1/dashboard/purchase-orders/stats` | PO stats |
| GET | `/api/v1/dashboard/goods-receipts/stats` | GRN stats |
| GET | `/api/v1/dashboard/issue-notes/stats` | Issue note stats |
| GET | `/api/v1/dashboard/inventory/stats` | Inventory stats |
| GET | `/api/v1/dashboard/vendors/stats` | Vendor stats |
| GET | `/api/v1/dashboard/trends/monthly` | Monthly trends |
| GET | `/api/v1/dashboard/breakdown/department` | Dept breakdown |
| GET | `/api/v1/dashboard/top-materials` | Top materials |
| GET | `/api/v1/dashboard/pending-approvals` | All pending items |
| GET | `/api/v1/dashboard/alerts` | System alerts |

### Utility Endpoints — 7 endpoints
| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/bulk-import/materials` | Bulk material import (CSV) |
| GET | `/api/v1/bulk-import/template` | Download CSV template |
| POST | `/api/v1/sap/import` | Trigger SAP material import |
| GET | `/api/v1/sap/status` | SAP import status |
| GET | `/api/v1/reports/material-quantity` | Material quantity report |
| GET | `/api/v1/reports/material-quantity/export` | Export to Excel |
| GET | `/api/v1/reports/material-quantity/pdf` | Export to PDF |

---

## 7. DATABASE SCHEMA (56 Tables in `seeds_indent`)

### Core Master Data Tables
| Table | Purpose | Primary Key |
|---|---|---|
| `tbl_emp_master` | Employee info (users of the system) | `emp_number` (INT, auto) |
| `tbl_company_master` | Company/organization entities | `comp_id` (INT, auto) |
| `tbl_plant_master` | Plant/facility locations | `plant_id` (INT, auto) |
| `tbl_location_master` | Geographic locations | `loc_id` (INT, auto) |
| `tbl_department_master` | Organizational departments | `dept_id` (INT, auto) |
| `tbl_section_master` | Sub-divisions within departments | `sec_id` (INT, auto) |
| `tbl_material_master` | Material/product catalog | `material_id` (INT, auto) |
| `tbl_umo_master` | Unit of Measure definitions | `umo_id` (INT, auto) |
| `tbl_crop_master` | Crop types (agricultural) | `crop_id` (INT, auto) |
| `tbl_vendor_master` | Vendor/supplier basic info | `vendor_id` (INT, auto) |
| `tbl_vendors` | Enhanced vendors with metrics | `id` (INT, auto) |
| `tbl_roles_master` | Role definitions with CRUD permissions | `role_id` (INT, auto) |
| `tbl_user_master` | User authentication (links to employee) | `user_id` (INT, auto) |
| `tbl_ldap_config` | LDAP/AD integration config | `config_id` (INT, auto) |

### Procurement Flow Tables
| Table | Purpose | Key Relationships |
|---|---|---|
| `tbl_indent_master` | Indent/requisition header | → company, dept, section, plant, employee |
| `tbl_indent_details` | Indent line items | → indent_master, material, UOM |
| `tbl_indent_status` | Indent status codes | Lookup table |
| `tbl_indent_procurement_logs` | Procurement activity logs | → indent, employee |
| `tbl_approval_workflow` | Approval actions history | → indent, approver employee |
| `tbl_purchase_orders` | PO header (enhanced) | → indent, vendor, department |
| `tbl_purchase_order_details` | PO line items | → PO, material |
| `tbl_po_header` | Legacy PO header | → indent, vendor |
| `tbl_po_details` | Legacy PO line items | → PO, indent detail, material, UOM |
| `tbl_po_status` | PO status codes | Lookup table |

### Goods Receipt & Issue Tables
| Table | Purpose |
|---|---|
| `tbl_goods_receipt` | GRN with multi-level approval |
| `tbl_issue_note` | Material issue notes |
| `tbl_issue_note_details` | Issue note line items |

### Mapping Tables (Organization Relationships)
| Table | Purpose |
|---|---|
| `tbl_map_company_department` | Company ↔ Department |
| `tbl_map_company_emp` | Company ↔ Employee |
| `tbl_map_company_location` | Company ↔ Location |
| `tbl_map_company_location_material` | Material inventory at company-location (with stock levels) |
| `tbl_map_company_plant_material` | Material inventory at company-plant |
| `tbl_map_emp_reporting` | Employee reporting hierarchy |
| `tbl_map_emp_roles` | Employee ↔ Role assignments |

### Email & Notification Tables
| Table | Purpose |
|---|---|
| `tbl_email_template` | Email templates with `{{placeholders}}` |
| (email_log) | Email sending history |

### SAP Integration Tables
| Table | Purpose |
|---|---|
| `pz_sap_material_masters` | SAP material master data |
| `pz_schedule_sap_material_master` | Scheduled SAP import staging (51 columns) |
| `pz_crop_group` | Legacy crop group data |

### System Tables
| Table | Purpose |
|---|---|
| `flyway_schema_history` | Migration tracking |
| `tbl_audit_log` | System-wide audit trail |

### Key Database Patterns
- **Status fields:** `1` = Active, `0` = Inactive (across all tables)
- **Audit fields:** Every table has `_lmd` (last modified date) and `_lmu` (last modified user/employee)
- **Workflow fields:** `_createdby`, `_approvedby`, `_final_approvedby` pattern for multi-level approval
- **Business keys:** Each entity has a unique code field (e.g., `emp_id`, `comp_code`, `material_code`)
- **Foreign keys:** Most FKs reference `tbl_emp_master.emp_number` for audit tracking
- **Dual tables:** Both `tbl_vendor_master` and `tbl_vendors` exist (enhanced version); both `tbl_po_header` and `tbl_purchase_orders` exist

---

## 8. SECURITY ARCHITECTURE

### Authentication Flow
```
1. POST /api/v1/auth/login (username, password)
2. Server validates credentials (BCrypt or legacy MD5)
3. Server returns JWT token (1 hour expiry)
4. Client sends: Authorization: Bearer <token>
5. JwtAuthenticationFilter validates token on every request
6. On logout: token is blacklisted (TokenBlacklistService)
```

### Security Configuration
- **Session:** STATELESS (no server-side sessions)
- **CSRF:** Disabled (API-only, JWT-based)
- **CORS:** Configured for frontend origins
- **Public endpoints:** `/api/v1/auth/**`, `/actuator/**`, `/health`
- **All other endpoints:** Require valid JWT
- **Method security:** `@PreAuthorize` annotations for role-based access

### Role System
| Role | Permissions | Purpose |
|---|---|---|
| SUPERADMIN | Full CRUD | System administration |
| DEPTHEAD | View, Edit, Approve | Department management, approval |
| EMPLOYEE | View, Add | Create indents, view own data |
| PROCUREMENT | View, Add, Edit | PO creation, vendor management |
| STORES | View, Add, Edit | GRN, inventory, issue notes |
| QUALITY | View, Add | Quality inspection |

### Test Credentials
```
SUPERADMIN:  rajesh.kumar   / password123
DEPTHEAD:    suresh.reddy   / password123
EMPLOYEE:    priya.sharma   / password123
```

---

## 9. CROSS-CUTTING FEATURES

### Audit Logging (`AuditService`)
- Logs all CRUD operations to `tbl_audit_log`
- Captures: entity type, entity ID, action, old/new values (JSON), user, IP, timestamp

### Email Notifications (`EmailService`)
- Template-based emails with `{{placeholder}}` support
- Triggers: indent submit/approve/reject, PO creation, GRN receipt, issue note events
- Internal SMTP server (no auth required)
- Email logging and retry

### Document Numbering (`DocumentNumberService`)
- Indian financial year format: `IND/2024-25/0001`
- Auto-reset counter at FY start (April)
- Company-based prefixes

### PDF Generation
- **IndentPdfService** — Indent documents with line items (iText 7)
- **PurchaseOrderPdfService** — PO documents for vendors (iText 7)
- **PdfReportService** — General PDF reports (OpenPDF)

### Excel Reports (`ExcelReportService`)
- Material quantity reports
- Inventory reports
- Vendor performance reports

### SAP Integration
- **Scheduled job:** Runs daily at 1:30 AM
- **Process:** Reads CSV files from SAP export directory → parses → imports material master data
- **Archive:** Processed files moved to archive directory

### Bulk Import
- CSV-based material bulk import
- Downloadable template

---

## 10. FLYWAY MIGRATIONS

Migrations are in `src/main/resources/db/migration/`:

| Migration | Purpose | Status |
|---|---|---|
| `V0__baseline.sql` | Initial schema baseline | ✅ Applied |
| `V20__create_email_tables.sql` | Email template/log tables | ✅ Applied |
| `V21__create_email_tables.sql` | Email table fixes | ✅ Applied |
| `V22__create_inventory_tables.sql` | Inventory & transaction tables | ✅ Applied |
| `V25__create_company_location_material_mapping.sql` | Location-material mapping | ✅ Applied |
| `V27__create_employee_reporting_hierarchy.sql` | Reporting hierarchy table | ✅ Applied |
| `V28__create_company_plant_material_table.sql` | Plant-material mapping | ✅ Applied |
| `V29__seed_indent_and_po_data.sql` | Seed data for testing | ✅ Applied |
| `V30__create_sap_integration_tables.sql` | SAP import tables (51 cols) | ✅ Applied |
| `V31__fix_email_tables_fk_and_schema_alignment.sql` | Schema alignment fixes | ✅ Applied |
| `V32__create_missing_tables.sql` | tbl_audit_log, tbl_goods_receipt, tbl_purchase_orders, tbl_purchase_order_details | ✅ Applied Mar 2026 |
| `V33__create_vendors_table.sql` | tbl_vendors (enhanced vendor table) | ✅ Applied Mar 2026 |
| `V34__add_issue_note_comments.sql` | Added issue_note_comments column to tbl_issue_note | ✅ Applied Mar 2026 |
| `V35__add_issue_note_missing_columns.sql` | Additional missing columns for Issue Note module | ✅ Applied Apr 2026 |

**Flyway config:** Baseline version = 20, validate-on-migrate = false

**Note on V32-V34:** These migrations were created during the March 2026 integration phase when 5 missing tables caused dashboard SQL errors. V34 initially failed due to MySQL's unsupported `IF NOT EXISTS` in `ALTER TABLE` — this was fixed and re-applied after cleaning from `flyway_schema_history`.

---

## 11. HOW TO BUILD & RUN

```bash
# Prerequisites: Java 21+, MySQL 8.0+, Maven 3.8+

# 1. Start MySQL, create database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS seeds_indent;"

# 2. Set environment variables
set PROCUREZONE_DB_PASSW=your_mysql_password
set PROCUREZONE_JWT_SECRET=your-production-secret-key

# 3. Build
cd d:\New_ProcureZone\Net-Beans\Net-Beans\backend
mvn clean install

# 4. Run
mvn spring-boot:run

# 5. Test
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rajesh.kumar","password":"password123"}'

# 6. Run API tests
bash scripts/comprehensive_api_test.sh
```

---

## 12. KEY FILE LOCATIONS

| What | Path |
|---|---|
| **Main config** | `src/main/resources/application.yml` |
| **Security config** | `src/main/java/.../config/SecurityConfig.java` |
| **JWT filter** | `src/main/java/.../security/JwtAuthenticationFilter.java` |
| **Root package** | `src/main/java/com/nslindia/procurezone/` |
| **Flyway migrations** | `src/main/resources/db/migration/` |
| **API tests** | `scripts/comprehensive_api_test.sh` |
| **Database docs** | `DATABASE_SCHEMA_REFERENCE.md` |
| **Status doc** | `BACKEND-STATUS-AND-COMPLETION-PLAN.md` |
| **pom.xml** | `pom.xml` (project root) |
| **Test suite** | `test-suite/` directory |

---

## 13. CODING PATTERNS & CONVENTIONS

### Package Organization
Each module follows the pattern: `Controller → Service → Repository → Entity/DTO`

### Naming Conventions
- **Controllers:** `*Controller.java` with `@RestController`
- **Services:** `*Service.java` with `@Service`
- **Repositories:** `*Repository.java` extends `JpaRepository<Entity, Integer>`
- **DTOs:** `*Request.java` (input), `*Response.java` (output), `*DTO.java` (transfer)
- **Entities:** Named after domain concept (e.g., `CompanyDepartment`, `Inventory`)

### API Conventions
- Base path: `/api/v1/`
- Pagination: query params (page, size, sort)
- Response pattern: Consistent JSON structure
- Error handling: Global exception handler
- Validation: `@Valid` on request bodies

### Status Codes Used
- `1` = Active, `0` = Inactive (database)
- Indent statuses: Draft(1) → Submitted(2) → L1 Approved(3) → L2 Approved(4) → Rejected(5) → Procurement(6)
- PO statuses: Draft → Submitted → Approved → Sent to Vendor → Received → Closed → Cancelled

---

## 14. SUMMARY TABLE OF ALL COMPONENTS

| Category | Count | Details |
|---|---|---|
| **Java Packages** | 36 | Organized by feature/module |
| **Controllers** | 28 | REST API controllers |
| **Services** | 51 | Business logic |
| **Repositories** | 49 | JPA data access |
| **Entities/Domain** | 7+ | JPA entities |
| **DTOs** | 24+ | Request/response objects |
| **API Endpoints** | 248 | Complete REST API |
| **Database Tables** | 56+ | MySQL schema (+ V32-V35 additions) |
| **Flyway Migrations** | 14 | V0, V20-V35 |
| **User Roles** | 14 | Full RBAC role set |
| **Frontend Pages** | 70+ | React pages across all modules |
| **Frontend API Modules** | 25 | src/api/*.ts files |
| **Test Users** | 3 | Pre-seeded credentials |

---

> **Instructions for AI Assistants:** When helping with this project, remember:
> 1. This is **Spring Boot 3.2.5 / Java 21** on the backend, **React 18 / TypeScript / Vite** on the frontend
> 2. Database DDL is managed entirely by **Flyway** — Hibernate `ddl-auto: none`
> 3. Auth is **stateless JWT** via `JwtAuthenticationFilter`. No session, no refresh token endpoint
> 4. Every module: **Controller → Service → Repository → Entity/DTO**
> 5. There are **two PO table sets**: `tbl_po_header` (legacy) + `tbl_purchase_orders` (new)
> 6. There are **two Plant Indent table sets**: `pz_tbl_indent_master` (wrong) vs `pz_tbl_indent_masterb` (correct)
> 7. Status fields: `1/0` = Active/Inactive in master tables; workflow statuses are 1-based integers
> 8. Active database: `seeds_indent_replica_1` (MySQL 8.0, localhost:3306)
> 9. All endpoints require JWT Bearer token except `/api/v1/auth/**` and `/actuator/**`
> 10. SAP integration runs as scheduled Quartz job at **1:30 AM daily**
> 11. **Rate (₹) columns** are intentionally commented out in frontend (IndentForm, IndentDetail, PlantIndentForm, PlantIndentDetail, POForm, PODetail) — do NOT uncomment without business approval
> 12. **Mock vendors** in `POFormPage.tsx` are a testing fallback — real vendors come from `/api/v1/vendors?isActive=true`
> 13. Developer **CJRam_NSL** is responsible for all work from March 21, 2026 onward
> 14. Flyway migrations **V32–V35** were applied during the March 2026 integration phase
> 15. `pz_crop_type` table has been seeded with 11 records for Plant Indent dropdown

---

## 15. FRONTEND ARCHITECTURE (Added April 2026)

The frontend is a **React 18 + TypeScript + Vite** single-page application located at `frontend/`.

### Directory Structure
```
frontend/src/
├── api/                   # 25 API modules (one per backend module)
│   ├── auth.ts            # Login, logout, me
│   ├── indents.ts         # Full indent CRUD + approval
│   ├── purchaseOrders.ts  # PO lifecycle endpoints
│   ├── grn.ts             # GRN + inspection workflow
│   ├── issueNotes.ts      # Issue note workflow
│   ├── inventory.ts       # Stock management
│   ├── vendors.ts         # Vendor management
│   ├── dashboard.ts       # All 13 dashboard endpoints
│   ├── companies.ts       # Company master CRUD
│   ├── departments.ts     # Department master CRUD
│   ├── plants.ts          # Plant master CRUD
│   ├── materials.ts       # Material master CRUD
│   ├── employees.ts       # Employee management
│   ├── users.ts           # User account management
│   ├── roles.ts           # Role management
│   └── client.ts          # Axios instance with JWT interceptor
│
├── pages/
│   ├── auth/              # LoginPage.tsx
│   ├── dashboard/         # DashboardPage.tsx (cards, charts)
│   ├── indents/           # IndentListPage, IndentFormPage, IndentDetailPage
│   ├── plant-indent/      # PlantIndentListPage, PlantIndentFormPage, PlantIndentDetailPage
│   ├── purchase-orders/   # POListPage, POFormPage, PODetailPage
│   ├── grn/               # GRNListPage, GRNFormPage, GRNDetailPage
│   ├── issue-notes/       # IssueNoteListPage, IssueNoteFormPage, IssueNoteDetailPage
│   ├── inventory/         # InventoryListPage
│   ├── masters/           # 25 files — Company, Plant, Dept, Section, UOM, Crop, Material, Vendor, Employee, User, Role (List + Form each)
│   ├── approvals/         # ApprovalPage.tsx (pending approval queue)
│   ├── mappings/          # EmployeeRoleMappingPage, etc.
│   └── reports/           # ReportsPage.tsx
│
├── components/
│   ├── layout/            # AppLayout, Sidebar, Topbar
│   ├── common/            # PageHeader, LoadingSpinner, ConfirmModal
│   └── charts/            # Recharts-based dashboard charts
│
├── contexts/
│   └── AuthContext.tsx     # JWT storage, user info, hasAnyRole() helper
│
├── routes/
│   └── router.tsx          # React Router v6 — all app routes
│
└── styles/                # Global CSS
```

### Key Frontend Conventions
- **State Management:** React Query (`@tanstack/react-query`) for server state; React state for UI
- **Forms:** React Hook Form + Zod validation schemas
- **HTTP Client:** Axios in `client.ts` with JWT auto-attach interceptor
- **Pagination:** Spring Page format — 0-indexed page, `content[]`, `totalPages`, `totalElements`
- **Status Enums:** All workflow statuses are **1-based integers** matching backend
- **Role Checks:** `hasAnyRole(['SUPERADMIN', 'ADMIN', ...])` from `useAuth()` context
- **Routing:** Nested routes under AppLayout; unauthenticated users redirect to /login

### Frontend Page Completion Status
| Module | Pages | Status |
|---|---|---|
| Auth | LoginPage | ✅ Complete |
| Dashboard | DashboardPage | ✅ Complete |
| Indent | List, Form, Detail | ✅ Complete |
| Plant Indent | List, Form, Detail | ✅ Complete (Mar 2026) |
| Purchase Orders | List, Form, Detail | ✅ Complete |
| GRN | List, Form, Detail | ✅ Complete |
| Issue Notes | List, Form, Detail | ✅ Complete |
| Inventory | List | ✅ Complete |
| Approvals | ApprovalPage | ✅ Complete |
| Master Data | 25 pages (List + Form each) | ✅ Complete |
| Reports | ReportsPage | 🔄 Partial |
| Mappings | EmployeeRoleMappingPage | 🔄 Partial |

---

## 16. PLANT INDENT MODULE — DETAILED REFERENCE (March 2026)

This module covers manufacturing-specific indents for the plant floor. It was heavily reworked in March 2026 by CJRam_NSL.

### Backend Entity: `PlantIndent.java`
- **JPA Table:** `pz_tbl_indent_masterb` ← this is the correct legacy table
- **WRONG table** (do NOT use): `pz_tbl_indent_master`
- **Key Fields:** `id`, `employeeId` (INT FK), `plantId` (INT FK), `cropTypeId` (INT FK), `uomId` (INT FK), `outputMaterial` (crop type text), `packProcess` (crop group text), `outputDescription` (delivery date/notes), `indentNumber`, `status` (INT 1-8), `batchNumber`, `remarks`
- **Design Decision:** Uses direct integer ID fields (NOT `@ManyToOne`) to avoid JPA relationship issues with the legacy schema

### PlantIndent Status Lifecycle
```
1 = Draft
2 = Pending DEO Approval
3 = Pending Manager Approval
4 = Approved
5 = Processing
6 = Processing Complete
7 = Completed
8 = Rejected
```

### Backend: `PlantIndentService.java`
- `mapToResponse()` uses `entityManager.find()` to dynamically resolve:
  - `plantId` → `CompanyPlant` → `plantName`
  - `cropTypeId` → `CropType` → `cropTypeName`
  - `uomId` → `UnitOfMeasure` → `uomCode`
  - `employeeId` → `Employee` → employee name
- Null-safety: every `entityManager.find()` is guarded with `id > 0` check

### Backend: `PlantIndentRepository.java`
- Extends `JpaRepository<PlantIndent, Long>`
- NO `@EntityGraph` (stripped — caused JDBC "Unknown column" errors)
- JPQL queries use simple field access: `WHERE p.status = :status`, `WHERE p.employeeId = :employeeId`

### Frontend: `PlantIndentFormPage.tsx`
- `transformFormData()` maps form fields to backend fields:
  - `cropType` → `outputMaterial`
  - `cropGroup` → `packProcess`
  - `deliveryDate` → `outputDescription`
- Zod Schema ONLY validates: `employeeId`, `plantId`, `items[]` (quantity > 0)
- UOM auto-populates when user selects a material (via `useEffect` on material change)
- Submit sends `POST /api/v1/plant-indent` (create) or `PUT /api/v1/plant-indent/{id}` (edit)

### Frontend: `PlantIndentListPage.tsx`
- Columns: #, Indent No, Employee, Plant, Crop Type, UOM, Status, Created Date, Actions
- UOM resolved from `items[0]?.uomCode`
- Crop Type resolved from `outputMaterial` or `cropTypeName` fallback
- Edit button → `/plant-indent/:id/edit` (form in edit mode)
- Delete button → DELETE API call with confirmation modal
- Status-aware: Edit/Delete disabled for approved/completed indents

### Frontend: `PlantIndentDetailPage.tsx`
- Created from scratch (read-only view)
- Sections: Plant & Employee Info, Crop/Production Details, QC Parameters (STL, ODV, GOT, ELISA, Moisture, Pure Seed, Germ Normal), Material Line Items table
- Edit/Delete buttons with status guards

### Routing for Plant Indent
```
/plant-indent           → PlantIndentListPage (list)
/plant-indent/new       → PlantIndentFormPage (create)
/plant-indent/:id       → PlantIndentDetailPage (read-only)  ← was wrong before fix
/plant-indent/:id/edit  → PlantIndentFormPage (edit mode)    ← fixed Mar 2026
```

### Database: `pz_crop_type` Table
Seeded with 11 records for the Crop Type dropdown:
Cotton, Maize, Rice, Soybean, Wheat, Vegetables, Oilseeds, Pulses, Sugarcane, Fruits, Spices

---

## 17. KNOWN BUGS FIXED (March–April 2026)

### Bug 1: Plant Indent — Wrong Table Mapping
- **Symptom:** Plant indent form submitted successfully but no record appeared in the list
- **Root Cause:** `PlantIndent.java` had `@Table(name = "pz_tbl_indent_master")` but correct table is `pz_tbl_indent_masterb`
- **Fix:** Changed `@Table` annotation; also removed all broken `@ManyToOne` joins
- **Files:** `PlantIndent.java`

### Bug 2: Plant Indent — "Unable to find UnitOfMeasure" on Submit
- **Symptom:** 500 error on form submit with Hibernate exception
- **Root Cause:** `@ManyToOne` relationship to `UnitOfMeasure` tried to do a JOIN on wrong column
- **Fix:** Replaced all entity relationships with plain integer ID fields; dynamic lookup in service
- **Files:** `PlantIndent.java`, `PlantIndentService.java`

### Bug 3: Plant Indent — Empty Columns in List View
- **Symptom:** Crop Type and Plant columns showed empty/null in list
- **Root Cause:** Frontend was looking for `cropTypeName` but backend returned data in `outputMaterial`
- **Fix:** Frontend uses `outputMaterial || cropTypeName` fallback
- **Files:** `PlantIndentListPage.tsx`

### Bug 4: Indent Status — "Rejected" Shows as "PO_CREATED"
- **Symptom:** Rejected indents displayed wrong status text
- **Root Cause:** Backend returned status name as `"PO_CREATED"` for status ID 5; frontend was passing this through without override
- **Fix:** Added local `STATUS_LABEL_MAP` in `IndentListPage.tsx` and `ApprovalPage.tsx` that always overrides backend-provided status name
- **Files:** `IndentListPage.tsx`, `ApprovalPage.tsx`

### Bug 5: JwtAuthenticationFilter — Compile Error (Server Won't Start)
- **Symptom:** `mvn compile` fails with `JwtAuthenticationFilter.java:96:131: ')' expected`
- **Root Cause:** Stray extra `(` parenthesis in `logger.debug(` call — `logger.debug( ("JWT token valid...", args)`
- **Fix:** Removed the extra opening parenthesis
- **Files:** `JwtAuthenticationFilter.java`

### Bug 6: Flyway V34 — Migration Failure
- **Symptom:** Application startup fails — `V34 migration failed`
- **Root Cause:** Used `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` which MySQL does not support
- **Fix:** Removed `IF NOT EXISTS` clause. Manually deleted the failed migration row from `flyway_schema_history` and re-ran
- **Files:** `V34__add_issue_note_comments.sql`

### Bug 7: Plant Indent Form — Silent Submission Failure
- **Symptom:** Form appeared to submit (no error shown) but nothing happened — no API call
- **Root Cause:** Zod validation schema required `companyId` and `departmentId` which were not in the form, causing silent validation failure before submit
- **Fix:** Removed `companyId` and `departmentId` from Zod schema (not needed for Plant Indent)
- **Files:** `PlantIndentFormPage.tsx`

---

## 18. RATE COLUMNS STATUS (April 2026)

Rate (₹) columns have been **intentionally hidden** across the frontend. They are commented out with JSX comments, not deleted, so they can be restored.

| File | What Is Hidden |
|---|---|
| `IndentFormPage.tsx` | `<th>Rate (₹)</th>` header; `estimatedRate` input `<td>` per row |
| `IndentDetailPage.tsx` | `<th>Est. Rate (₹)</th>` header; rate value `<td>` per row |
| `PlantIndentFormPage.tsx` | `<th>Est. Rate</th>` header; `estimatedRate` input `<td>` per row |
| `PlantIndentDetailPage.tsx` | `<th>Rate</th>` header |
| `POFormPage.tsx` | `<th>Rate (₹)</th>` header; `unitRate` input `<td>` per row |
| `PODetailPage.tsx` | `<th>Rate (₹)</th>` header; rate value `<td>` per row |

**Side Effect:** The "Est. Value" / total cells still exist. They will show ₹0 or calculate against 0 rate since the rate input is hidden.

**To Restore:** Remove the `{/* ... */}` JSX comments wrapping the hidden elements.

---

## 19. MOCK VENDOR DATA (POFormPage.tsx)

When the backend vendor API returns an empty list (e.g., during testing when `tbl_vendors` is empty), `POFormPage.tsx` falls back to 5 mock vendors:

```typescript
const MOCK_VENDORS = [
  { id: 1, vendorName: 'Agro Seeds India Ltd',       vendorCode: 'AGRO001' },
  { id: 2, vendorName: 'Bharat Fertilizers Pvt Ltd', vendorCode: 'BFA002' },
  { id: 3, vendorName: 'Green Crop Suppliers',       vendorCode: 'GCS003' },
  { id: 4, vendorName: 'National Agro Traders',      vendorCode: 'NAT004' },
  { id: 5, vendorName: 'Pioneer Seed Corporation',   vendorCode: 'PSC005' },
];
```

**Logic:**
```typescript
const vendors = vendorsData?.content?.length
  ? vendorsData.content   // real backend data
  : MOCK_VENDORS;         // fallback for testing
```

Real vendor API: `GET /api/v1/vendors?isActive=true&page=0&size=100`

---

## 20. DEVELOPMENT CHANGELOG (March 21 – April 1, 2026)

All work below was done by **CJRam_NSL** (janakiraama182@gmail.com).

| Date | Area | File / Component | Change Summary |
|------|------|-----------------|----------------|
| Mar 21 | Backend | `PlantIndent.java` | Re-mapped `@Table` to `pz_tbl_indent_masterb`; removed all `@ManyToOne` relationships |
| Mar 21 | Backend | `PlantIndentRepository.java` | Stripped all `@EntityGraph`; simplified JPQL queries |
| Mar 21 | Backend | `PlantIndentService.java` | Full rebuild: null-safety guards; dynamic `entityManager.find()` in `mapToResponse()` |
| Mar 21 | Database | `pz_crop_type` | Seeded 11 crop type records via direct SQL INSERT |
| Mar 21 | Frontend | `PlantIndentFormPage.tsx` | Fixed `transformFormData` field mapping; removed dead Zod validations; added UOM auto-fill |
| Mar 22 | Frontend | `PlantIndentListPage.tsx` | Restored Crop Type, Plant, UOM columns; added Edit/Delete action buttons |
| Mar 22 | Frontend | `PlantIndentDetailPage.tsx` | Created from scratch — read-only detail view with all fields and QC parameters |
| Mar 22 | Frontend | `plant-indent/index.ts` | Added `PlantIndentDetailPage` export |
| Mar 22 | Frontend | `pages/index.tsx` | Added `PlantIndentDetailPage` export |
| Mar 22 | Frontend | `router.tsx` | Updated: `/plant-indent/:id` → Detail page; `/plant-indent/:id/edit` → Form |
| Mar 22 | Frontend | `IndentListPage.tsx` | Fixed status display: added local `STATUS_LABEL_MAP` to override backend status names |
| Mar 22 | Frontend | `ApprovalPage.tsx` | Same status display fix applied |
| Mar 30 | Backend | `JwtAuthenticationFilter.java` | Fixed stray `(` parenthesis at line 96 that blocked compilation |
| Mar 30 | Git | Backend + Frontend repos | All above changes committed under `CJRam_NSL` identity |
| Apr 1 | Frontend | `IndentFormPage.tsx` | Commented out Rate (₹) column header and `estimatedRate` input cell |
| Apr 1 | Frontend | `IndentDetailPage.tsx` | Commented out Est. Rate (₹) column header and rate value cell |
| Apr 1 | Frontend | `PlantIndentFormPage.tsx` | Commented out Est. Rate column header and `estimatedRate` input cell |
| Apr 1 | Frontend | `PlantIndentDetailPage.tsx` | Commented out Rate column header |
| Apr 1 | Frontend | `POFormPage.tsx` | Commented out Rate (₹) header and `unitRate` input cell; added 5 mock vendor fallback |
| Apr 1 | Frontend | `PODetailPage.tsx` | Commented out Rate (₹) column header and rate value cell |
| Apr 1 | Database | `V35__add_issue_note_missing_columns.sql` | Applied migration for additional Issue Note columns |
| Apr 1 | Docs | All 5 documentation files | Updated all project docs (OVERALL_WORK_SUMMARY, INTEGRATION_STATUS, FINAL_READINESS, BACKEND_STATUS, COMPREHENSIVE_AUDIT) |

---

## 21. FULL ROLE LIST (14 Roles)

| Role ID | Code | Name | Key Permissions |
|---|---|---|---|
| 1 | SUPERADMIN | Super Administrator | Full CRUD + Delete on all modules |
| 2 | ADMIN | Administrator | Full CRUD, no delete |
| 3 | PLANTMANAGER | Plant Manager | Full CRUD, no delete; Plant Indent approval |
| 4 | DEPTHEAD | Department Head | View + Edit + Approve (Indent L1) |
| 5 | PROCUREMENT | Procurement Officer | Create PO, manage vendors |
| 6 | FINANCE | Finance Manager | Financial approval |
| 7 | QUALITY | Quality Manager | GRN inspection |
| 8 | STOREKEEPER | Store Keeper | GRN, inventory, issue note execution |
| 9 | EMPLOYEE | Regular Employee | Create indents, view own data |
| 10 | VIEWER | View Only | Read-only all modules |
| 11 | FLOORINCHARGE | Floor Incharge | Plant floor operations |
| 12 | SUPERVISOR | Supervisor | Team-level approvals |
| 13 | AUDITOR | Auditor | Read-only + audit logs |
| 14 | QUALITYMANAGER | Quality Manager | Full quality workflow |

### Role Usage in Frontend `hasAnyRole()`
```typescript
// Examples from codebase:
canEdit   = hasAnyRole(['SUPERADMIN', 'ADMIN', 'EMPLOYEE', 'DEPTHEAD', 'PLANTMANAGER'])
canApprove = hasAnyRole(['SUPERADMIN', 'ADMIN', 'DEPTHEAD'])
canCreatePO = hasAnyRole(['SUPERADMIN', 'ADMIN', 'PROCUREMENT'])
canGRN    = hasAnyRole(['SUPERADMIN', 'ADMIN', 'STOREKEEPER'])
```

---

## 22. WORKFLOW STATUS REFERENCE (All Modules)

### Indent Status (1-based)
| ID | Name | Notes |
|---|---|---|
| 1 | Draft | Employee can edit/delete |
| 2 | Submitted | Awaiting Level 1 (Dept Head) approval |
| 3 | Dept Head Approved | Awaiting Level 2 (Plant Manager) approval |
| 4 | Finance Approved | Awaiting Procurement approval |
| 5 | Procurement Approved | PO can now be created |
| 6 | Rejected | Any level can reject; reason stored in remarks |
| 7 | On Hold | Temporarily paused |
| 8 | Completed | PO created and fulfilled |

### Purchase Order Status (1-based)
| ID | Name | Frontend Enum Key |
|---|---|---|
| 1 | Draft | `POStatus.DRAFT` |
| 2 | Submitted / Pending Approval | `POStatus.SUBMITTED` |
| 3 | Approved | `POStatus.APPROVED` |
| 4 | Sent to Vendor | `POStatus.SENT_TO_VENDOR` |
| 5 | Partially Received | `POStatus.PARTIALLY_RECEIVED` |
| 6 | Fully Received | `POStatus.FULLY_RECEIVED` |
| 7 | Cancelled | `POStatus.CANCELLED` |
| 8 | Closed | `POStatus.CLOSED` |

### GRN Status (1-based)
| ID | Name |
|---|---|
| 1 | Created |
| 2 | Inspected |
| 3 | RM Approved |
| 4 | Approved |
| 5 | Final Approved |
| 6 | Stored |
| 7 | Rejected |

### Issue Note Status (1-based, 10 values)
| ID | Name |
|---|---|
| 1 | Created |
| 2 | Pending RM Approval |
| 3 | RM Approved |
| 4 | Approved |
| 5 | Rejected by RM |
| 6 | Rejected by Manager |
| 7 | Pending Store Issue |
| 8 | Issued |
| 9 | Rejected by Stores |
| 10 | Returned |

---

## 23. INTEGRATION ALIGNMENT STATUS (Frontend ↔ Backend)

### Modules 100% Aligned ✅
- **Auth:** `/api/v1/auth/login`, `/auth/logout`, `/auth/me`
- **Dashboard:** All 13 endpoints match
- **GRN:** All 14 endpoints match (note: base path is `/api/v1/grn` singular, NOT `/grns`)
- **Indent:** All CRUD + approval workflow endpoints match

### Modules With Known Path Mismatches ⚠️
| Frontend Call | Correct Backend Path | Status |
|---|---|---|
| `GET /pos/vendor/{id}` | `GET /pos/by-vendor/{id}` | Needs fix |
| `GET /pos/department/{id}` | `GET /pos/by-department/{id}` | Needs fix |
| `GET /pos/statistics` | `GET /pos/dashboard/statistics` | Needs fix |
| `GET /issue-notes/number/{n}` | `GET /issue-notes/by-number?issueNoteNumber=X` | Needs fix |
| `POST /issue-notes/{id}/stores-reject` | `POST /issue-notes/{id}/reject-stores` | Needs fix |
| `GET /issue-notes/my` | `GET /issue-notes/my-issue-notes` | Needs fix |
| `GET /issue-notes/department/{id}` | `GET /issue-notes/by-department/{id}` | Needs fix |

### Auth Endpoints That Do NOT Exist in Backend
- `POST /auth/refresh` — NO refresh token endpoint. JWT is 1 hour, then user must re-login.
- `POST /auth/change-password` — Not in backend.
- Frontend's dead refresh code in `client.ts` should be removed.

---

## 24. SESSION CHANGELOG — APRIL 2, 2026 (Bug Fixes & Feature Completions)

> **Session Summary:** Six modules fixed in one session — Mapping refresh, Plant-Material save error, Reporting Hierarchy display, Email Templates CRUD, Settings persistence, and Inventory investigation.

---

### 24.1 Company-Location-Material Mapping — List Not Refreshing After Save

**Problem:** New mappings saved successfully but did not appear in the Current Mapping list.

**Root Cause (2 layers):**
1. Backend `getAllMappings()` used `repository.findAll()` returning ALL records (including soft-deleted).
2. Frontend fetched without `size` param so Spring default page (20) was applied.

**Fixes:**
- `CompanyLocationMaterialMapRepository.java` → Added `findByIsActiveTrue(Pageable)` derived query.
- `CompanyLocationMaterialService.java` → Changed to `repository.findByIsActiveTrue(pageable)`.
- `CompanyLocationMaterialPage.tsx` → Added `params: { page: 0, size: 500 }` to the GET call.

---

### 24.2 Plant-Material Mapping — Three Separate Bugs Fixed

**Bug A — Material dropdown required 2 chars to load:**
- `enabled: materialSearch.length >= 2` → Fixed to `enabled: showMaterialDropdown` (loads on focus/click).

**Bug B — "companyId is required" on save:**
- Backend `CompanyPlantMaterialRequest` has `@NotNull companyId`.
- Frontend never included it.
- Fixed: Derive `companyId` from the selected plant object and include in POST payload.

**Bug C — "Required request header 'X-User-Id' not present":**
- `CompanyPlantMaterialController` used the old `@RequestHeader("X-User-Id")` pattern. Frontend never sends this header.
- Fixed: Replaced with `Authentication authentication` parameter + private `getUserId(Authentication)` helper extracting from `UserPrincipal`.
- **RULE ESTABLISHED:** All controllers in this project MUST use `Authentication` for user ID. `@RequestHeader("X-User-Id")` is forbidden.

---

### 24.3 Employee Reporting Hierarchy — "report_end cannot be null"

**Problem:** Saving a new reporting relationship: `Column 'report_end' cannot be null`.

**Fix (`EmployeeReportingService.java`):**
```java
.endDate(LocalDate.of(2099, 12, 31))  // Sentinel: no real end date
.status(request.getStatus() != null ? request.getStatus() : 1)
```

---

### 24.4 Employee Reporting Hierarchy — Current Structure Table Empty

**Problem:** Created relationships not showing in the table.

**Root Cause (3 issues in `ReportingHierarchyPage.tsx`):**
1. Controller wraps response as `{ success, data: [...] }` but frontend read `hierarchyData?.content` → Fixed to `hierarchyData?.data || []`.
2. Interface used `employeeId`/`reportingToId` but backend sends `subordinateEmployeeNumber`/`supervisorEmployeeNumber` → Fixed interface and all usages.
3. Search filter checked `relation.employeeName` (not in response) → Fixed to look up employee by ID from the loaded employees list.

---

### 24.5 Email Templates — Two Critical Bugs

**Bug A — All templates show "Inactive":**
- Frontend typed `isActive: boolean` but backend sends `status: Integer (1=Active, 0=Inactive)`.
- Fixed: Changed interface to `status: number`; badge checks `row.status === 1`.

**Bug B — "Resource not found" on PUT/GET by ID:**
- `EmailTemplateController` had only one endpoint (`GET /email-templates`). No `GET /{id}` or `PUT /{id}` existed.
- Fixed: Added both endpoints to `EmailTemplateController.java`.

**Also fixed:**
- Frontend field names: `templateCode`/`templateName` → `code`/`name` (actual Java field names).
- `updatedAt` → `lastModifiedDate`.

---

### 24.6 Settings Module — Complete Overhaul

**Problem:** Settings page was entirely fake. `handleSave` ran a `setTimeout` with a success message but saved nothing.

**Solution: New `SettingsContext` system.**

**New file: `frontend/src/contexts/SettingsContext.tsx`**
- Persists all preferences to `localStorage` (key: `procurezone_settings`).
- Applies theme via `document.documentElement.setAttribute('data-bs-theme', 'dark'|'light')` — Bootstrap 5 native dark mode.
- Applies compact mode via `document.body.classList.add('compact-mode')`.
- Listens to OS `prefers-color-scheme` changes when "System" is selected.
- Exports `useSettings()` hook.

**Updated files:**
- `MainLayout.css` — Added `body.compact-mode` CSS (reduced padding, table density, button sizes).
- `MainLayout.tsx` — Initialises `sidebarCollapsed` from `settings.sidebarCollapsed` saved value.
- `App.tsx` — Wrapped with `<SettingsProvider>` outside `<AuthProvider>`.
- `SettingsPage.tsx` — Completely rewritten to use `useSettings()`; Save button now calls `saveAllSettings()`.

---

### 24.7 Inventory Management — Investigation (Pending Full Fix)

**Problem:** No data in inventory list, search not working.

**Findings:**
1. `tbl_inventory_balance` — **0 rows**. Stock must be seeded via GRN approvals or manual adjustment.
2. Backend `GET /inventory` only filters by `companyId`, `plantId`, `materialId`, `lowStock`. Frontend's `search` and `stockStatus` params are silently ignored.
3. `InventoryRepository.findByFilters()` JPQL has no `LIKE` clause for material code/name.

**Status: PENDING.** Next session must:
- Insert mock inventory data (SQL prepared: material IDs 1-7, plant IDs 1-5).
- Add `search` text filter to `InventoryRepository.findByFilters()`.
- Wire `search` and `stockStatus` through service → controller.

---

### 24.8 Patterns & Rules Established This Session

| Pattern | Rule |
|---|---|
| **User ID in Controllers** | Use `Authentication` + `getUserId()` from `UserPrincipal`. Never `@RequestHeader("X-User-Id")`. |
| **Active-only Mapping Lists** | All mapping lists must use `findByIsActive(true)` derived queries, not `findAll()`. |
| **Response Shape Variance** | Some controllers return `{ success, data: [...] }` (custom). Others return Spring `Page<T>`. Check per controller. |
| **Status Field Type** | Entities use `Integer status` (1=Active, 0=Inactive). Frontend must NOT use `boolean isActive`. |
| **Not-Null Date Sentinels** | `report_end = '2099-12-31'` = "no expiry" convention for this project. |
| **Settings Persistence** | All UI preferences via `SettingsContext` + `localStorage`. No backend API involved. |
| **Frontend Pagination Size** | Mapping list pages should pass `size=500` to avoid entries being cut off by page limits. |

---

### 24.9 Complete File Change Log — April 2, 2026

**Backend:**
| File | Change |
|---|---|
| `mapping/CompanyLocationMaterialMapRepository.java` | Added `findByIsActiveTrue(Pageable)` |
| `mapping/CompanyLocationMaterialService.java` | `findAll` → `findByIsActiveTrue` |
| `mapping/CompanyPlantMaterialController.java` | `@RequestHeader` → `Authentication` on POST/PUT/DELETE |
| `service/EmployeeReportingService.java` | Set `endDate = 2099-12-31` on create |
| `notification/controller/EmailTemplateController.java` | Added `GET /{id}` + `PUT /{id}` |

**Frontend:**
| File | Change |
|---|---|
| `pages/mappings/CompanyLocationMaterialPage.tsx` | Added `size=500` GET param |
| `pages/mappings/CompanyPlantMaterialPage.tsx` | Fixed dropdown, `companyId`, field names |
| `pages/mappings/ReportingHierarchyPage.tsx` | Fixed `data` vs `content`, interface fields, search |
| `pages/admin/EmailTemplateManagerPage.tsx` | Fixed `status`, `code`, `name`, `lastModifiedDate` |
| `contexts/SettingsContext.tsx` | **NEW** — Full settings persistence + DOM application |
| `pages/profile/SettingsPage.tsx` | Rewritten to use `useSettings()` |
| `components/layout/MainLayout.tsx` | Reads `sidebarCollapsed` default from settings |
| `components/layout/MainLayout.css` | Added compact-mode CSS rules |
| `App.tsx` | Wrapped with `<SettingsProvider>` |

