# ProcureZone Spring Boot Backend

**Enterprise Procurement & Manufacturing Operations System**  
**Version**: 1.0.0  
**Tech Stack**: Spring Boot 3.2.5 | Java 17 | MySQL 8.0 | JWT Auth  
**Last Updated**: November 2025

---

## 🚀 QUICK START

### Prerequisites

- Java 17+
- MySQL 8.0+
- Maven 3.8+

### Setup

```bash
# 1. Clone and navigate
cd backend

# 2. Configure database
# Edit src/main/resources/application.properties with your MySQL credentials

# 3. Run database setup
mysql -u root -p < ../ProcureZone/database/final-DB_Data_Schema/DEFINITIVE_PROCUREZONE_SCHEMA.sql
mysql -u root -p < ../ProcureZone/database/final-DB_Data_Schema/LOGIN_CREDENTIALS_CORRECTED.sql

# 4. Build and run
mvn clean install
mvn spring-boot:run
```

### Test

```bash
# API will be available at http://localhost:8080
# Test with: bash scripts/comprehensive_api_test.sh
```

---

## 📚 DOCUMENTATION INDEX

### 🎯 **START HERE**

- **[Current Implementation Status](doc/PHASE1-2-COMPLETE-SUMMARY.md)** - What's built, what's missing
- **[Complete Project Roadmap](doc/PROJECT-COMPLETION-PLAN.md)** - 20-week implementation plan
- **[Master Action Plan](doc/DEVELOPMENT-ACTION-PLAN.md)** - Prioritized task breakdown

### 🏗️ **ARCHITECTURE & DESIGN**

- **[System Architecture Discovery](LEGACY-VS-REALITY-COMPREHENSIVE-ANALYSIS.md)** - Real vs documented features
- **[Plant Operations Module](doc/PLANT-OPERATIONS-DISCOVERY.md)** - Manufacturing floor integration
- **[Database Schema Guide](../ProcureZone/database/final-DB_Data_Schema/Explaination_about_tables.md)** - All 54 tables explained
- **[Implementation Roadmap](IMPLEMENTATION-ROADMAP.md)** - Feature-by-feature breakdown

### 📋 **USER REQUIREMENTS**

- **[Super Admin Role](doc/user-stories/2.Admin%20Role.md)** - User & master data management (18 stories)
- **[Employee Role](doc/user-stories/3.Employee%20Role.md)** - Indent creation & tracking (12 stories)
- **[Department Head Role](doc/user-stories/5.Department%20Head%20Role.md)** - Approval workflows (15 stories)
- **[Procurement Role](doc/user-stories/6.Procurement%20Role.md)** - PO & vendor management (22 stories)
- **[Stores Manager Role](doc/user-stories/9.Stores%20Manager%20Role.md)** - Inventory & GRN (18 stories)
- **[Quality Manager Role](doc/user-stories/12.13.14.VERIFICATION-QUALITY-ROLES.md)** - Quality inspection (25 stories)

### 🔌 **API DOCUMENTATION**

- **[Complete API Reference](doc/Api-Documentation/COMPLETE-API-DOCUMENTATION.md)** - All endpoints documented
- **[Postman Collection](ProcureZone-Auth.postman_collection.json)** - Import for testing
- **[API Test Script](scripts/comprehensive_api_test.sh)** - Automated testing (214 tests)

### 🛠️ **DEVELOPMENT GUIDES**

- **[Feature Implementation Guide](doc/FEATURE-IMPLEMENTATION-ROADMAP-15Oct.md)** - How to add new features
- **[Database Migration Guide](database/README-DATABASE-MIGRATION.md)** - Schema changes
- **[Testing Guide](doc/COMPLETE-TEST-SCRIPT-READY.md)** - Test execution & validation

### 📊 **PROGRESS TRACKING**

- **[Phase 0 Progress](PHASE-0-PROGRESS.md)** - Foundation & stabilization
- **[Phase 1 Implementation Plan](PHASE-1-IMPLEMENTATION-PLAN.md)** - Core features (51-64 hrs)
- **[Location & Plant Controllers](LOCATION-PLANT-CONTROLLERS-COMPLETE.md)** - Master data completion

---

## 🎯 IMPLEMENTATION STATUS (55% Complete)

### ✅ **COMPLETED MODULES**

#### **Authentication & Security (100%)**

- JWT-based authentication with token blacklisting
- Dual password support (MD5 legacy + BCrypt)
- Role-based authorization (`@PreAuthorize`)
- Audit logging for authentication events
- **Endpoints**: 3/3 (Login, Me, Logout)

#### **Indent Management (100%)**

- Complete CRUD operations
- Multi-level approval workflow
- Submit/Approve/Reject with history
- Search & filtering with pagination
- **Endpoints**: 12/12 (100% test pass rate)

#### **Master Data - Partial (55%)**

- ✅ Company CRUD (5 endpoints)
- ✅ Department CRUD (5 endpoints)
- ✅ Material CRUD (7 endpoints)
- ✅ Unit of Measure CRUD (5 endpoints)
- ✅ Location CRUD (6 endpoints)
- ✅ Plant CRUD (6 endpoints)
- **Endpoints**: 34/34 master data

### 🔴 **CRITICAL GAPS (Must Implement)**

#### **User Management (0%)** - Priority 0

- Employee CRUD (8 endpoints)
- User account management (6 endpoints)
- Role assignment (3 endpoints)
- **Timeline**: Weeks 1-3

#### **Procurement Workflow (0%)** - Priority 1

- Vendor management (9 endpoints)
- Purchase Order workflow (16 endpoints)
- **Timeline**: Weeks 4-8

#### **Inventory Management (0%)** - Priority 2

- Goods Receipt Note (8 endpoints)
- Issue Note (8 endpoints)
- Stock tracking (6 endpoints)
- **Timeline**: Weeks 9-12

#### **Plant Operations (0%)** - Priority 3

- Plant indents (5 endpoints)
- SAP integration (4 endpoints)
- Production line tracking (4 endpoints)
- **Timeline**: Weeks 13-16

---

## 🏭 SYSTEM ARCHITECTURE

### **Dual-Module Design**

```
┌─────────────────────────────────────────────────────────────┐
│                    PROCUREZONE SYSTEM                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────┐    ┌────────────────────────┐   │
│  │  CORPORATE MODULE    │    │  PLANT MODULE          │   │
│  │  (Office Procurement)│    │  (Manufacturing Floor) │   │
│  └──────────────────────┘    └────────────────────────┘   │
│           │                            │                    │
│           ├─ Indent Management         ├─ Plant Indents    │
│           ├─ Approval Workflow         ├─ Issue Notes      │
│           ├─ PO Generation             ├─ SAP Integration  │
│           ├─ Vendor Management         ├─ Production Sched │
│           └─ GRN (Goods Receipt)       └─ Line Tracking    │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         SCHEDULED JOBS (Background Tasks)            │  │
│  ├──────────────────────────────────────────────────────┤  │
│  │  • SAP CSV Import (Nightly @ 2 AM)                   │  │
│  │  • Material Master Sync                              │  │
│  │  • Production Schedule Updates                       │  │
│  │  • Email Notifications (Approval reminders)          │  │
│  │  • Report Generation                                 │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### **Technology Stack**

- **Backend**: Spring Boot 3.2.5, Spring Security 6.x, Spring Data JPA
- **Database**: MySQL 8.0 (54 tables, 25+ stored procedures)
- **Authentication**: JWT (RS256), BCrypt password hashing
- **Scheduling**: Spring @Scheduled + Quartz (legacy SAP sync)
- **Integration**: SAP ERP (CSV exports), LDAP/AD (planned)
- **Testing**: JUnit 5, MockMvc, 214 API test cases

---

## 📦 PROJECT STRUCTURE

```
backend/
├── src/main/java/com/nslindia/procurezone/
│   ├── auth/                   # JWT authentication & security
│   ├── indent/                 # Indent management module
│   ├── master/                 # Master data (Company, Dept, Material, UOM)
│   ├── location/               # Location master
│   ├── plant/                  # Plant master
│   ├── approval/               # Approval workflow service
│   ├── config/                 # Spring configuration
│   ├── exception/              # Global exception handling
│   └── util/                   # Utilities (JWT, validation)
│
├── src/main/resources/
│   ├── application.properties  # Database & JWT config
│   └── schema.sql              # H2 test database (optional)
│
├── doc/                        # All documentation
├── database/                   # SQL migration scripts
├── scripts/                    # Test & utility scripts
└── archive/                    # Archived outdated docs
```

---

## 🔑 AUTHENTICATION

### **Test Credentials**

```
SUPERADMIN:
  Username: rajesh.kumar
  Password: password123

DEPTHEAD:
  Username: suresh.reddy
  Password: password123

EMPLOYEE:
  Username: priya.sharma
  Password: password123
```

### **JWT Token Usage**

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rajesh.kumar","password":"password123"}'

# Response: { "accessToken": "eyJhbGc...", "user": {...} }

# 2. Use token in requests
curl http://localhost:8080/api/v1/indents \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## 🧪 TESTING

### **Run All API Tests**

```bash
bash scripts/comprehensive_api_test.sh
```

### **Test Coverage**

- **Authentication**: 3/3 ✅
- **Indent Management**: 12/12 ✅
- **Master Data**: 34/34 ✅
- **User Management**: 0/11 ❌
- **Procurement**: 0/25 ❌
- **Total**: 49/94 endpoints (52%)

---

## 📈 ROADMAP

### **Phase 1: Foundation (Complete)**

- ✅ Authentication & JWT security
- ✅ Indent management workflow
- ✅ Master data CRUD operations

### **Phase 2: User Management (Weeks 1-3)**

- Employee & user account management
- Role assignment & permissions
- User activation/deactivation

### **Phase 3: Procurement (Weeks 4-8)**

- Vendor management
- Purchase Order creation & approval
- PO-to-vendor workflow

### **Phase 4: Inventory (Weeks 9-12)**

- Goods Receipt Note (GRN)
- Quality inspection
- Material issue notes

### **Phase 5: Plant Operations (Weeks 13-16)**

- Plant indent workflow
- SAP integration (CSV import)
- Production line tracking

### **Phase 6: Advanced Features (Weeks 17-20)**

- Dashboard analytics
- LDAP/AD integration
- Email notifications
- Advanced reporting

---

## 🤝 CONTRIBUTING

### **Adding New Endpoints**

1. Study existing patterns: `IndentController.java`, `CompanyController.java`
2. Create DTOs: `XxxRequest.java`, `XxxResponse.java`
3. Implement service: `XxxService.java`
4. Add controller: `XxxController.java`
5. Add tests: Update `comprehensive_api_test.sh`
6. Update documentation: `COMPLETE-API-DOCUMENTATION.md`

### **Code Standards**

- Follow Spring Boot best practices
- Use DTOs for all API requests/responses
- Implement proper exception handling
- Add audit logging for critical operations
- Write integration tests for all endpoints

---

## 📞 SUPPORT

### **Common Issues**

- **Database connection fails**: Check `application.properties` MySQL credentials
- **JWT token invalid**: Token expires after 24 hours, re-login
- **Port 8080 in use**: Change `server.port` in `application.properties`
- **Tests fail**: Ensure MySQL is running and database is seeded

### **Documentation Resources**

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security JWT](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- [MySQL 8.0 Reference](https://dev.mysql.com/doc/refman/8.0/en/)

---

## 📄 LICENSE

Proprietary - NSL India Pvt Ltd

---

**Last Updated**: November 1, 2025  
**Maintainer**: Development Team  
**Status**: 🟢 Active Development (55% Complete)
