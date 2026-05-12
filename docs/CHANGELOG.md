# ProcureZone – Changelog & Version History

All notable changes to this project are documented here.
Format: `[Date] – Author – Description`

---

## [Unreleased] – Current Working State (as of 2026-05-12)

### Frontend (unstaged changes)
- `frontend/src/App.tsx` – Route and layout updates
- `frontend/src/components/layout/MainLayout.tsx/.css` – Layout restructure
- `frontend/src/components/layout/Sidebar.tsx` – Navigation updates
- `frontend/src/pages/indents/IndentDetailPage.tsx` – Detail view fixes
- `frontend/src/pages/indents/IndentFormPage.tsx` – Form improvements
- `frontend/src/pages/mappings/*` – All 5 mapping pages updated (CompanyDept, CompanyLocation, CompanyLocationMaterial, CompanyPlantMaterial, EmployeeRole, ReportingHierarchy)
- `frontend/src/pages/masters/*` – Companies, Employees, Locations, Plants, MasterData pages updated
- `frontend/src/pages/plant-indent/*` – PlantIndentDetail and PlantIndentForm pages
- `frontend/src/pages/profile/SettingsPage.tsx` – Settings page updates
- `frontend/src/pages/purchase-orders/*` – PODetail and POForm pages
- `frontend/src/pages/admin/EmailTemplateManagerPage.tsx` – Email template admin
- `frontend/src/routes/router.tsx` – Route configuration
- `frontend/src/contexts/SettingsContext.tsx` – New: Settings context (untracked)

### Backend (unstaged changes)
- `CompanyDepartmentController.java` – Batch mapping support
- `CompanyLocationController.java` – Location hierarchy fixes
- `CompanyLocationMaterialMapRepository.java` / `Service.java` / `dto/` – Material mapping overhaul
- `CompanyPlantMaterialController.java` – Plant material mapping
- `CompanyDepartmentRepository.java` / `CompanyLocationRepository.java` – Query improvements
- `JwtAuthenticationFilter.java` – Security fix (filter chain correction)
- `CompanyDepartmentService.java` / `CompanyLocationService.java` – Service layer updates
- `EmployeeReportingService.java` – Reporting hierarchy service
- `application.yml` – Config updates (excluded from commit – see `application.yml.example`)

---

## [2026-04-01] – CJRam_NSL – Root commit `0c7704e`
**"Major frontend development updates"**

### Added
- Plant Indent module (`PlantIndentListPage`, `PlantIndentDetailPage`, `PlantIndentFormPage`)
- Settings/Profile page (`SettingsPage.tsx`)
- Company-Plant-Material mapping page
- Reporting Hierarchy management page
- Employee Role Mapping page

### Changed
- Sidebar navigation updated for new modules
- Main layout CSS refinements
- Email Template Manager admin page

---

## [2026-03-30] – CJRam_NSL – Backend commit `30482a7`
**"Major development updates by CJRam_NSL"**

### Added / Changed
- `pom.xml` – Dependency updates
- `auth/controller/AuthController.java` – Auth improvements
- `auth/dto/AuthenticatedUser.java` – DTO update
- `auth/service/AuthService.java` – Service layer fix
- `BACKEND-STATUS-AND-COMPLETION-PLAN.md` – Status update
- `logs/server.log` – (excluded by .gitignore)

---

## [2026-03-30] – CJRam_NSL – Backend commit `adb42f8`
**"Major development updates by CJRam_NSL"**

### Added
- `qodana.yaml` – Code quality config
- `V31__fix_email_tables_fk_and_schema_alignment.sql` – Email table schema alignment migration

---

## [2026-03-04] – CJRam_NSL – Frontend Integration Sprint
*(Documented in `docs/integration/INTEGRATION_COMPLETION_REPORT.txt`)*

### Fixed – Status Enum Alignment (0-based → 1-based)
- Indent Status: `1=Draft` through `8=Completed`
- PO Status: `1=Draft` through `8=Closed`
- GRN Status: `1=Created` through `7=Rejected`
- Issue Note Status: `1` through `10` (10-state workflow)

### Fixed – API Path Corrections
- GRN paths corrected
- Auth endpoints aligned to backend contracts
- Indent, PO, Issue Notes, Inventory paths fixed

### Added
- `frontend/src/api/dashboard.ts` – New Dashboard API module
- All 9 backend roles mapped to frontend: `SUPERADMIN`, `ADMIN`, `DEPTHEAD`, `EMPLOYEE`, `PROCUREMENT`, `STOREKEEPER`, `PLANTMANAGER`, `VIEWER`, `AUDITOR`

### Changed
- 20 frontend page components updated
- Barrel file (`api/index.ts`) updated
- UI branding: logo, sidebar, favicon

---

## [2026-03-03] – CJRam_NSL – Frontend Integration Brief
*(Documented in `docs/integration/INTEGRATION_CHANGES_BRIEF.txt`)*

### Fixed
- Role alignment: 16 frontend roles → 9 backend roles
- Auth refresh logic corrected (no refresh endpoint; JWT is 1-hour fixed)
- Inventory response wrapper support (`CustomPage<T>` instead of Spring `Page<T>`)
- Pagination defaults standardized

---

## [2025-12-11] – CJRam_NSL – Backend Session
*(Documented in `docs/status/BACKEND_IMPLEMENTATION_PROGRESS_DEC11.md`)*

### Added – Enhanced Reporting System
- `IndentReportService.java`
- `POReportService.java`
- `InventoryReportService.java`
- `VendorPerformanceService.java`
- `ComprehensiveReportController.java` – 10+ new report endpoints

### Added – PDF Document Generation
- `IndentPdfService.java`
- `PurchaseOrderPdfService.java`
- Download + preview endpoints

### Added – Email Notification System
- `IndentNotificationService.java`
- `PONotificationService.java`
- `InventoryNotificationService.java`
- 12 email triggers with HTML templates

### Stats
- 19 new API endpoints
- ~2,500 lines of production code
- Backend completion: 85% → 92%

---

## [2025-11-29] – SaiHarsha502vvit – Backend commit `08dc4bf`
**"some changes" – Initial project scaffolding**

### Added
- Maven project structure (`pom.xml`)
- IntelliJ IDEA / Eclipse project files
- Initial Spring Boot application skeleton
- Base entity and repository structure
- Flyway migration baseline (V1–V20)

---

## Development Sessions Summary
*(Full session-by-session log: `docs/status/OVERALL_WORK_SUMMARY.txt`)*

| Session | Date | Key Work |
|---------|------|----------|
| 1 | Feb 24, 2026 | Project discovery, codebase analysis |
| 2 | Feb 25, 2026 | Integration guide creation |
| 3 | Feb 26, 2026 | Frontend debugging |
| 4 | Feb 27, 2026 | Dashboard fixes |
| 5 | Feb 28, 2026 | API alignment |
| 6 | Mar 1, 2026 | Role-based routing |
| 7 | Mar 2, 2026 | Issue Note workflow |
| 8 | Mar 3, 2026 | Integration sprint (20 files) |
| 9 | Mar 21, 2026 | Plant Indent module overhaul |
| 10 | Mar 25, 2026 | Indent status display bug fix |
| 11 | Mar 28, 2026 | Git identity setup (CJRam_NSL) |
| 12 | Mar 30, 2026 | JWT filter security fix |
| 13 | Apr 1, 2026 | Rate columns removal, mock vendors |

**Cumulative:** 336 files created/modified across 13 sessions

---

## How to Use This File

- When you make changes, add an entry under `[Unreleased]`
- On each commit/push, move `[Unreleased]` items under a new dated heading
- Format: `## [YYYY-MM-DD] – AuthorName – Commit message`
