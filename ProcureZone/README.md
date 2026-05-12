# ProcureZone - NSL India Procurement Management System

## ⚠️ CRITICAL: DOCUMENTATION STATUS

**IMPORTANT:** Most documentation in the `docs/recreation/` folder describes the **TARGET STATE** of the ProcureZone system after the greenfield Spring Boot recreation. **This modern system is NOT YET IMPLEMENTED.**

### 🚨 Current Reality vs Documentation:

| Aspect                | Documentation Describes      | Current Production System                  |
| --------------------- | ---------------------------- | ------------------------------------------ |
| **Backend Framework** | Spring Boot 3.2 + REST APIs  | **Struts 2.0.14 + Struts Actions** ⚠️      |
| **Frontend**          | React 18 + Vite              | **JSP 2.0 (460+ pages)** ⚠️                |
| **Authentication**    | JWT + Spring Security        | **Session-based + MD5 passwords** 🔴       |
| **ORM**               | JPA with @Entity annotations | **Hibernate 3.x with XML mappings** ⚠️     |
| **Build Tool**        | Maven 3.9                    | **Ant (NetBeans)** ⚠️                      |
| **Testing**           | JUnit 5 + Mockito            | **Zero tests (0% coverage)** 🔴            |
| **Status**            | Planned (0% complete)        | **Running in Production (100% legacy)** ✅ |

### 📚 Documentation Guide - READ THIS FIRST:

#### **For New Developers:**

1. **START HERE:** [`docs/recreation/LEGACY-SYSTEM-DOCUMENTATION.md`](docs/recreation/LEGACY-SYSTEM-DOCUMENTATION.md)  
   → **800+ lines covering the ACTUAL Struts 2 system running in production**

2. **THEN READ:** [`docs/recreation/BACKEND-RECREATION-STRATEGY.md`](docs/recreation/BACKEND-RECREATION-STRATEGY.md)  
   → **Recreation roadmap detailing how each module will be rebuilt on Spring Boot**

3. **CONTEXT:** [`docs/recreation/CURRENT-STATE-AUDIT.md`](docs/recreation/CURRENT-STATE-AUDIT.md)  
   → **Gap analysis summarising the legacy system and recreation priorities**

4. **NEXT:** [`docs/recreation/Project-Recreation-Blueprint/`](docs/recreation/Project-Recreation-Blueprint/)  
   → **Target-state blueprints captured before the recreation effort**

#### **Quick Reference:**

| You Want To...                     | Read This Document                                          |
| ---------------------------------- | ----------------------------------------------------------- |
| Understand current production code | `LEGACY-SYSTEM-DOCUMENTATION.md`                            |
| Fix a bug in production            | `LEGACY-SYSTEM-DOCUMENTATION.md` Section 4 (code inventory) |
| Plan the recreation roadmap        | `BACKEND-RECREATION-STRATEGY.md`                            |
| See what we're recreating          | `Project-Recreation-Blueprint/` folder                      |
| Understand the gap                 | `CURRENT-STATE-AUDIT.md`                                    |
| See technical debt                 | `LEGACY-TECHNICAL-DEBT.md`                                  |

---

## 📖 Project Overview

ProcureZone is a comprehensive procurement and inventory management system designed for NSL India's seed procurement operations. The system manages the complete lifecycle of procurement from indent creation through goods receipt to issue notes, with integrated approval workflows and inventory tracking.

### Current System (Production):

**Technology Stack:**

- **Backend:** Struts 2.0.14 + Hibernate 3.x (XML mappings)
- **View Layer:** JSP 2.0 (460+ pages) + JavaScript
- **Database:** MySQL 8.0 (seeds_indent schema)
- **Authentication:** Session-based with MD5 password hashing 🔴 **Security Risk**
- **Build Tool:** Apache Ant (NetBeans project)
- **Server:** Apache Tomcat 7.x/8.x

**Code Statistics:**

- **Java Files:** 120+ (approximately 25,000 LOC)
- **JSP Pages:** 460+ pages
- **Database Tables:** 40+ tables
- **Largest File:** `IndentAction.java` (1,828 lines!) 🔴

### Business Modules:

1. **Authentication & Authorization**

   - LDAP integration
   - Role-based access control (6 roles)
   - Multi-company support

2. **Indent Management** (Core Module)

   - Indent creation with multi-material support
   - 7-state approval workflow (Draft → Final Approved)
   - Procurement assignment
   - PDF generation & Excel export

3. **Goods Receipt Notes (GRN)**

   - GRN creation against indents
   - Quality control workflow
   - Stores acceptance
   - Stock updates

4. **Issue Notes**

   - Issue note creation
   - Approval workflow
   - Stores issuance
   - Stock reduction

5. **Master Data Management**

   - 10+ master tables (Companies, Plants, Materials, Employees, etc.)
   - Hierarchical relationships
   - Stock management mappings

6. **Plant Module**
   - Plant-specific indent requests
   - Quality control
   - Receipt confirmations

---

## 🚀 Getting Started (Current System)

### Prerequisites:

- **Java:** JDK 8 (legacy requirement)
- **Database:** MySQL 8.0+
- **IDE:** Apache NetBeans (project configured for it)
- **Server:** Apache Tomcat 7.x/8.x

### Setup Steps:

1. **Clone Repository:**

   ```bash
   git clone <repository-url>
   cd ProcureZone
   ```

2. **Database Setup:**

   ```bash
   mysql -u root -p < database/seeds_indent_schema.sql
   mysql -u root -p seeds_indent < database/SEED_BASELINE_DATA.sql
   ```

3. **Configure Database Connection:**
   Edit `src/java/hibernate.cfg.xml`:

   ```xml
   <property name="connection.url">jdbc:mysql://localhost:3306/seeds_indent</property>
   <property name="connection.username">your_username</property>
   <property name="connection.password">your_password</property>
   ```

4. **Build with Ant:**

   ```bash
   ant clean
   ant compile
   ant dist
   ```

5. **Deploy to Tomcat:**
   - Copy `dist/ProcureZone.war` to `TOMCAT_HOME/webapps/`
   - Start Tomcat: `catalina.sh start` (Linux) or `catalina.bat start` (Windows)
   - Access: `http://localhost:8080/ProcureZone`

### Default Login:

- **Username:** Check `tbl_emp_master` table
- **Password:** (MD5 hashed in database) 🔴

---

## 📁 Project Structure

```
ProcureZone/
├── src/
│   ├── java/
│   │   ├── seeds/               ← Core application code
│   │   │   ├── indent/
│   │   │   │   ├── action/      ← Struts Actions (IndentAction.java 1828 lines!)
│   │   │   │   ├── dao/         ← Data Access Objects
│   │   │   │   └── service/     ← Business logic
│   │   │   ├── login/           ← Authentication
│   │   │   ├── masters/         ← Master data modules
│   │   │   ├── stores/          ← GRN, Issue Notes
│   │   │   └── plant/           ← Plant module
│   │   ├── pojo/                ← Hibernate POJOs (50+ entities)
│   │   │   ├── *.java           ← Java classes
│   │   │   └── *.hbm.xml        ← Hibernate XML mappings
│   │   └── com/myapp/           ← Utilities
│   └── conf/
│       └── MANIFEST.MF
├── web/                         ← Web resources
│   ├── WEB-INF/
│   │   ├── jsp/                 ← 460+ JSP pages!
│   │   ├── lib/                 ← JAR dependencies
│   │   ├── struts-config.xml    ← Struts routing
│   │   ├── tiles-defs.xml       ← Layout tiles
│   │   └── web.xml              ← Web application config
│   ├── images/                  ← Images
│   ├── script/                  ← JavaScript
│   └── uploads/                 ← User uploads
├── database/                    ← Database scripts
│   ├── seeds_indent_schema.sql  ← Schema definition
│   └── *.md                     ← Database documentation
├── docs/
│   └── recreation/              ← Recreation documentation
│       ├── LEGACY-SYSTEM-DOCUMENTATION.md      ← READ THIS FIRST
│       ├── BACKEND-RECREATION-STRATEGY.md      ← Recreation roadmap
│       ├── CURRENT-STATE-AUDIT.md
│       ├── LEGACY-TECHNICAL-DEBT.md
│       └── Project-Recreation-Blueprint/       ← Target state docs
├── build.xml                    ← Ant build script
└── README.md                    ← This file
```

---

## 🔧 Development Workflow (Current System)

### Making Changes:

1. **Edit code in NetBeans** (project configured)
2. **Build:** `ant compile`
3. **Deploy:** Copy WAR to Tomcat or use NetBeans deploy
4. **Test manually** (no automated tests exist 🔴)

### Common Tasks:

- **Add new Struts Action:**

  1. Create `*Action.java` extending `ActionSupport`
  2. Add action mapping to `web/WEB-INF/struts-config.xml`
  3. Create JSP view in `web/WEB-INF/jsp/`

- **Add new database entity:**

  1. Create Java POJO in `src/java/pojo/`
  2. Create Hibernate mapping `*.hbm.xml`
  3. Update `src/java/hibernate.cfg.xml`

- **Add new JSP page:**
  1. Create JSP in `web/WEB-INF/jsp/`
  2. Use Tiles layout: `<tiles:insert definition="layout">`

---

## ⚠️ Known Issues & Technical Debt

### 🔴 CRITICAL Security Vulnerabilities:

1. **MD5 Password Hashing** - Cryptographically broken, easily cracked
2. **SQL Injection Risk** - Dynamic HQL/SQL in many DAOs
3. **XSS Vulnerabilities** - Unescaped user input in JSPs
4. **Session Fixation** - No session regeneration after login
5. **No HTTPS Enforcement** - HTTP allowed in production
6. **Directory Traversal** - File upload paths not validated
7. **Sensitive Data Exposure** - Passwords logged in some places

See [`docs/recreation/LEGACY-TECHNICAL-DEBT.md`](docs/recreation/LEGACY-TECHNICAL-DEBT.md) for full inventory.

### 🟡 Code Quality Issues:

- **Massive Action Classes:** `IndentAction.java` (1,828 lines), `GoodsReceiptAction.java` (1,200+ lines)
- **No Service Layer:** Business logic embedded in Actions
- **No Unit Tests:** 0% test coverage
- **Manual Transaction Management:** Error-prone
- **Duplicate Code:** Copy-paste across DAOs
- **Magic Strings:** Status values hardcoded everywhere
- **Poor Error Handling:** Generic catch blocks

### 🟡 Performance Issues:

- **N+1 Query Problem:** Lazy loading without query optimization
- **Large Result Sets:** No pagination in many queries
- **Session Leaks:** Unclosed Hibernate sessions
- **Synchronous Email:** Blocks request threads

---

## 🎯 Future Recreation Plan

**Status:** Planning Complete → Ready to Start Implementation

**Timeline:** 32 weeks (8 months)

**Approach:** Strangler Fig Pattern (incremental replacement)

### Recreation Phases:

| Phase       | Duration    | Focus                                   | Status         |
| ----------- | ----------- | --------------------------------------- | -------------- |
| **Phase 1** | Weeks 1-8   | Foundation & Pilot (Company Master)     | ⏸️ Not Started |
| **Phase 2** | Weeks 9-16  | Core Modules (Indent, GRN, Issue Notes) | ⏸️ Not Started |
| **Phase 3** | Weeks 17-24 | Master Data & Plant Module              | ⏸️ Not Started |
| **Phase 4** | Weeks 25-28 | Testing & Parallel Run                  | ⏸️ Not Started |
| **Phase 5** | Weeks 29-32 | Production Cutover                      | ⏸️ Not Started |

### Target Technology Stack (After Recreation):

- **Backend:** Spring Boot 3.2 + Spring Data JPA
- **Frontend:** React 18 + Vite + React Router
- **API:** REST (105 endpoints documented)
- **Authentication:** JWT + Spring Security
- **Password Hashing:** BCrypt
- **Build:** Maven 3.9
- **Testing:** JUnit 5 + Mockito + Playwright
- **Database:** MySQL 8.0 (no schema changes needed ✅)

See [`docs/recreation/BACKEND-RECREATION-STRATEGY.md`](docs/recreation/BACKEND-RECREATION-STRATEGY.md) for the complete recreation plan.

---

## 📞 Support & Contact

**For Technical Issues:**

- Check `docs/recreation/LEGACY-SYSTEM-DOCUMENTATION.md` for architecture details
- Review `docs/recreation/LEGACY-TECHNICAL-DEBT.md` for known issues

**For Recreation Planning:**

- See `docs/recreation/BACKEND-RECREATION-STRATEGY.md`
- Review `docs/recreation/CURRENT-STATE-AUDIT.md`

---

## 📄 License

**Proprietary** - NSL India Internal Use Only

---

## 🗂️ Document Changelog

| Version | Date        | Author      | Changes                                          |
| ------- | ----------- | ----------- | ------------------------------------------------ |
| 1.0     | Oct 2, 2025 | DevOps Team | Initial README with documentation clarifications |

---

**Remember:** Always read `LEGACY-SYSTEM-DOCUMENTATION.md` first before making any code changes! 🚀
