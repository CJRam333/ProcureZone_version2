# Database Schema Reference - seeds_indent

**Generated:** November 7, 2025  
**Database:** seeds_indent (MySQL 8.0)  
**Total Tables:** 56

---

## Table of Contents

- [System Tables](#system-tables)
- [Master Data Tables](#master-data-tables)
- [Email & Notification Tables](#email--notification-tables)
- [Indent & Procurement Tables](#indent--procurement-tables)
- [Purchase Order Tables](#purchase-order-tables)
- [Goods Receipt & Issue Tables](#goods-receipt--issue-tables)
- [Mapping Tables](#mapping-tables)
- [Legacy/PZ Tables](#legacypz-tables)
- [Quick Reference by Entity Type](#quick-reference-by-entity-type)

---

## System Tables

### flyway_schema_history

**Purpose:** Flyway migration tracking  
**Primary Key:** installed_rank (int)

| Column         | Type          | Null | Key | Default           | Notes                              |
| -------------- | ------------- | ---- | --- | ----------------- | ---------------------------------- |
| installed_rank | int           | NO   | PRI |                   | Migration sequence                 |
| version        | varchar(50)   | YES  |     |                   | Migration version (e.g., "1", "2") |
| description    | varchar(200)  | NO   |     |                   | Migration description              |
| type           | varchar(20)   | NO   |     |                   | Migration type (SQL, JDBC)         |
| script         | varchar(1000) | NO   |     |                   | Script filename                    |
| checksum       | int           | YES  |     |                   | File checksum for validation       |
| installed_by   | varchar(100)  | NO   |     |                   | User who ran migration             |
| installed_on   | timestamp     | NO   |     | CURRENT_TIMESTAMP | Execution timestamp                |
| execution_time | int           | NO   |     |                   | Time in milliseconds               |
| success        | tinyint(1)    | NO   | MUL |                   | 1=success, 0=failed                |

**Indexes:** success (MUL)

---

### tbl_audit_log

**Purpose:** System-wide audit trail for all CRUD operations  
**Primary Key:** audit_id (bigint, auto_increment)

| Column            | Type         | Null | Key | Default           | Notes                                    |
| ----------------- | ------------ | ---- | --- | ----------------- | ---------------------------------------- |
| audit_id          | bigint       | NO   | PRI |                   | Auto-incrementing ID                     |
| audit_entity_type | varchar(100) | NO   | MUL |                   | Entity name (e.g., "Indent", "Material") |
| audit_entity_id   | varchar(100) | YES  |     |                   | ID of affected record                    |
| audit_action      | varchar(50)  | NO   | MUL |                   | CREATE/UPDATE/DELETE/LOGIN               |
| audit_details     | text         | YES  |     |                   | Human-readable description               |
| audit_status      | varchar(20)  | YES  |     | SUCCESS           | SUCCESS/FAILURE/PENDING                  |
| audit_old_value   | mediumtext   | YES  |     |                   | JSON of previous state                   |
| audit_new_value   | mediumtext   | YES  |     |                   | JSON of new state                        |
| audit_user_id     | int          | NO   | MUL |                   | FK to tbl_emp_master.emp_number          |
| audit_user_name   | varchar(100) | YES  |     |                   | Cached user name                         |
| audit_username    | varchar(100) | YES  |     |                   | Login username                           |
| audit_ip_address  | varchar(50)  | YES  |     |                   | Client IP                                |
| audit_timestamp   | datetime     | NO   | MUL | CURRENT_TIMESTAMP | When action occurred                     |
| audit_remarks     | text         | YES  |     |                   | Additional notes                         |

**Indexes:** audit_entity_type, audit_action, audit_user_id, audit_timestamp  
**Foreign Keys:** audit_user_id → tbl_emp_master.emp_number

---

## Master Data Tables

### tbl_emp_master

**Purpose:** Employee information (users of the system)  
**Primary Key:** emp_number (int, auto_increment)

| Column          | Type         | Null | Key | Default | Notes                             |
| --------------- | ------------ | ---- | --- | ------- | --------------------------------- |
| emp_number      | int          | NO   | PRI |         | System-generated ID               |
| emp_id          | varchar(100) | NO   | UNI |         | **Business key** (e.g., "EMP001") |
| emp_name        | varchar(100) | NO   |     |         | Full name                         |
| emp_email       | varchar(100) | NO   | UNI |         | Email (unique)                    |
| emp_password    | varchar(500) | YES  |     |         | BCrypt hashed password            |
| emp_join_date   | date         | YES  |     |         | Date of joining                   |
| emp_designation | varchar(100) | NO   |     |         | Job title                         |
| emp_cost_center | varchar(500) | YES  |     |         | Cost center code                  |
| emp_path        | mediumtext   | YES  |     |         | File path for documents           |
| emp_status      | int          | NO   | MUL |         | 1=Active, 0=Inactive              |
| emp_lmd         | date         | NO   |     |         | Last modified date                |
| emp_department  | int          | NO   | MUL |         | FK to tbl_department_master       |
| emp_location    | int          | NO   | MUL |         | FK to tbl_location_master         |
| emp_company     | int          | NO   | MUL |         | FK to tbl_company_master          |
| emp_plant       | varchar(100) | YES  |     |         | Plant codes (comma-separated?)    |

**Indexes:** emp_id (UNI), emp_email (UNI), emp_status, emp_department, emp_location, emp_company  
**Foreign Keys:**

- emp_department → tbl_department_master.dept_id
- emp_location → tbl_location_master.loc_id
- emp_company → tbl_company_master.comp_id

---

### tbl_company_master

**Purpose:** Company/organization entities  
**Primary Key:** comp_id (int, auto_increment)

| Column      | Type         | Null | Key | Default | Notes                             |
| ----------- | ------------ | ---- | --- | ------- | --------------------------------- |
| comp_id     | int          | NO   | PRI |         | Auto-increment                    |
| comp_code   | varchar(100) | NO   | UNI |         | Business key (e.g., "NSL", "SSL") |
| comp_name   | varchar(100) | NO   |     |         | Company name                      |
| comp_status | int          | NO   | MUL |         | 1=Active, 0=Inactive              |
| comp_lmd    | date         | NO   |     |         | Last modified date                |
| comp_lmu    | int          | YES  | MUL |         | FK to tbl_emp_master.emp_number   |

**Indexes:** comp_code (UNI), comp_status, comp_lmu

---

### tbl_plant_master

**Purpose:** Plant/facility locations  
**Primary Key:** plant_id (int, auto_increment)

| Column       | Type         | Null | Key | Default | Notes                           |
| ------------ | ------------ | ---- | --- | ------- | ------------------------------- |
| plant_id     | int          | NO   | PRI |         | Auto-increment                  |
| plant_code   | varchar(100) | NO   | UNI |         | Business key                    |
| plant_name   | varchar(100) | NO   |     |         | Plant name                      |
| plant_status | int          | NO   | MUL |         | 1=Active, 0=Inactive            |
| plant_lmd    | date         | NO   |     |         | Last modified date              |
| plant_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** plant_code (UNI), plant_status, plant_lmu

---

### tbl_location_master

**Purpose:** Geographic locations  
**Primary Key:** loc_id (int, auto_increment)

| Column     | Type         | Null | Key | Default | Notes                           |
| ---------- | ------------ | ---- | --- | ------- | ------------------------------- |
| loc_id     | int          | NO   | PRI |         | Auto-increment                  |
| loc_code   | varchar(100) | NO   | UNI |         | Business key                    |
| loc_name   | varchar(100) | NO   |     |         | Location name                   |
| loc_status | int          | NO   | MUL |         | 1=Active, 0=Inactive            |
| loc_lmd    | date         | NO   |     |         | Last modified date              |
| loc_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** loc_code (UNI), loc_status, loc_lmu

---

### tbl_department_master

**Purpose:** Organizational departments  
**Primary Key:** dept_id (int, auto_increment)

| Column      | Type         | Null | Key | Default | Notes                           |
| ----------- | ------------ | ---- | --- | ------- | ------------------------------- |
| dept_id     | int          | NO   | PRI |         | Auto-increment                  |
| dept_code   | varchar(100) | NO   | UNI |         | Business key                    |
| dept_name   | varchar(100) | NO   |     |         | Department name                 |
| dept_status | int          | NO   | MUL |         | 1=Active, 0=Inactive            |
| dept_lmd    | date         | NO   |     |         | Last modified date              |
| dept_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** dept_code (UNI), dept_status, dept_lmu

---

### tbl_section_master

**Purpose:** Sub-divisions within departments  
**Primary Key:** sec_id (int, auto_increment)

| Column     | Type         | Null | Key | Default | Notes                           |
| ---------- | ------------ | ---- | --- | ------- | ------------------------------- |
| sec_id     | int          | NO   | PRI |         | Auto-increment                  |
| sec_code   | varchar(100) | NO   | UNI |         | Business key                    |
| sec_name   | varchar(100) | NO   |     |         | Section name                    |
| sec_status | int          | NO   | MUL |         | 1=Active, 0=Inactive            |
| sec_lmd    | date         | NO   |     |         | Last modified date              |
| sec_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** sec_code (UNI), sec_status, sec_lmu

---

### tbl_material_master

**Purpose:** Material/product catalog  
**Primary Key:** material_id (int, auto_increment)

| Column                 | Type          | Null | Key | Default | Notes                           |
| ---------------------- | ------------- | ---- | --- | ------- | ------------------------------- |
| material_id            | int           | NO   | PRI |         | Auto-increment                  |
| material_code          | varchar(100)  | NO   | UNI |         | Business key (e.g., "MAT001")   |
| material_name          | varchar(100)  | NO   | MUL |         | Material name                   |
| material_desc          | text          | YES  |     |         | Description                     |
| material_price         | decimal(20,2) | YES  |     | 0.00    | Current price                   |
| material_unit_cost     | decimal(20,2) | YES  |     | 0.00    | Cost per unit                   |
| material_min_order_qty | decimal(20,2) | YES  |     | 1.00    | Minimum order quantity          |
| material_status        | int           | NO   | MUL |         | 1=Active, 0=Inactive            |
| material_lmd           | date          | NO   |     |         | Last modified date              |
| material_lmu           | int           | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** material_code (UNI), material_name, material_status, material_lmu

---

### tbl_umo_master

**Purpose:** Unit of Measure definitions  
**Primary Key:** umo_id (int, auto_increment)

| Column     | Type         | Null | Key | Default | Notes                            |
| ---------- | ------------ | ---- | --- | ------- | -------------------------------- |
| umo_id     | int          | NO   | PRI |         | Auto-increment                   |
| umo_code   | varchar(100) | NO   | UNI |         | Business key (e.g., "KG", "LTR") |
| umo_name   | varchar(100) | NO   |     |         | UOM name (Kilogram, Liter)       |
| umo_status | int          | NO   | MUL |         | 1=Active, 0=Inactive             |
| umo_lmd    | date         | NO   |     |         | Last modified date               |
| umo_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number  |

**Indexes:** umo_code (UNI), umo_status, umo_lmu

---

### tbl_crop_master

**Purpose:** Crop types for agricultural operations  
**Primary Key:** crop_id (int, auto_increment)

| Column      | Type         | Null | Key | Default | Notes                           |
| ----------- | ------------ | ---- | --- | ------- | ------------------------------- |
| crop_id     | int          | NO   | PRI |         | Auto-increment                  |
| crop_code   | varchar(100) | NO   | UNI |         | Business key                    |
| crop_name   | varchar(100) | NO   |     |         | Crop name                       |
| crop_status | int          | NO   | MUL |         | 1=Active, 0=Inactive            |
| crop_lmd    | date         | NO   |     |         | Last modified date              |
| crop_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** crop_code (UNI), crop_status, crop_lmu

---

### tbl_vendor_master

**Purpose:** Vendor/supplier information  
**Primary Key:** vendor_id (int, auto_increment)

| Column                | Type         | Null | Key | Default | Notes                           |
| --------------------- | ------------ | ---- | --- | ------- | ------------------------------- |
| vendor_id             | int          | NO   | PRI |         | Auto-increment                  |
| vendor_code           | varchar(100) | NO   | UNI |         | Business key                    |
| vendor_name           | varchar(200) | NO   | MUL |         | Vendor name                     |
| vendor_contact_person | varchar(100) | YES  |     |         | Contact person                  |
| vendor_email          | varchar(100) | YES  |     |         | Email                           |
| vendor_phone          | varchar(20)  | YES  |     |         | Phone                           |
| vendor_address        | text         | YES  |     |         | Full address                    |
| vendor_city           | varchar(100) | YES  |     |         | City                            |
| vendor_state          | varchar(100) | YES  |     |         | State                           |
| vendor_pincode        | varchar(10)  | YES  |     |         | PIN code                        |
| vendor_gstin          | varchar(20)  | YES  |     |         | GST number                      |
| vendor_pan            | varchar(20)  | YES  |     |         | PAN number                      |
| vendor_rating         | decimal(3,2) | YES  |     | 0.00    | Rating (0.00-5.00)              |
| vendor_status         | int          | NO   | MUL | 1       | 1=Active, 0=Inactive            |
| vendor_lmd            | date         | NO   |     |         | Last modified date              |
| vendor_lmu            | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** vendor_code (UNI), vendor_name, vendor_status, vendor_lmu

---

### tbl_vendors (Enhanced)

**Purpose:** Extended vendor information with metrics  
**Primary Key:** id (int, auto_increment)

| Column                | Type          | Null | Key | Default           | Notes                |
| --------------------- | ------------- | ---- | --- | ----------------- | -------------------- |
| id                    | int           | NO   | PRI |                   | Auto-increment       |
| vendor_code           | varchar(50)   | NO   | UNI |                   | Business key         |
| vendor_name           | varchar(200)  | NO   | MUL |                   | Vendor name          |
| vendor_type           | varchar(50)   | YES  |     |                   | Type/category        |
| contact_person        | varchar(100)  | YES  |     |                   | Contact person       |
| contact_phone         | varchar(20)   | YES  |     |                   | Phone                |
| contact_email         | varchar(100)  | YES  |     |                   | Email                |
| address_line1         | varchar(200)  | YES  |     |                   | Address line 1       |
| address_line2         | varchar(200)  | YES  |     |                   | Address line 2       |
| city                  | varchar(100)  | YES  | MUL |                   | City                 |
| state                 | varchar(100)  | YES  |     |                   | State                |
| country               | varchar(100)  | YES  |     |                   | Country              |
| pincode               | varchar(20)   | YES  |     |                   | PIN code             |
| gst_number            | varchar(50)   | YES  | UNI |                   | GST number (unique)  |
| pan_number            | varchar(20)   | YES  |     |                   | PAN number           |
| payment_terms         | varchar(100)  | YES  |     |                   | Payment terms        |
| credit_period_days    | int           | YES  |     |                   | Credit period        |
| rating                | decimal(3,2)  | YES  | MUL | 0.00              | Overall rating       |
| total_orders          | int           | YES  |     | 0                 | Total orders placed  |
| total_order_value     | decimal(15,2) | YES  |     | 0.00              | Total value          |
| on_time_delivery_rate | decimal(5,2)  | YES  |     | 0.00              | Percentage (0-100)   |
| quality_rating        | decimal(3,2)  | YES  |     | 0.00              | Quality score        |
| status                | int           | NO   | MUL | 1                 | 1=Active, 0=Inactive |
| remarks               | text          | YES  |     |                   | Additional notes     |
| registration_date     | date          | YES  |     |                   | Registration date    |
| last_order_date       | date          | YES  |     |                   | Last order date      |
| created_by            | int           | YES  |     |                   | Creator employee     |
| created_date          | timestamp     | YES  |     | CURRENT_TIMESTAMP | Creation timestamp   |
| last_modified_by      | int           | YES  |     |                   | Last modifier        |
| last_modified_date    | timestamp     | YES  |     | CURRENT_TIMESTAMP | Auto-updates         |

**Indexes:** vendor_code (UNI), vendor_name, gst_number (UNI), city, rating, status  
**Note:** Has ON UPDATE CURRENT_TIMESTAMP on last_modified_date

---

### tbl_roles_master

**Purpose:** Role definitions with CRUD permissions  
**Primary Key:** role_id (int, auto_increment)

| Column      | Type         | Null | Key | Default | Notes                           |
| ----------- | ------------ | ---- | --- | ------- | ------------------------------- |
| role_id     | int          | NO   | PRI |         | Auto-increment                  |
| role_code   | varchar(100) | NO   | UNI |         | Business key (e.g., "ADMIN")    |
| role_name   | varchar(100) | NO   |     |         | Display name                    |
| role_view   | tinyint(1)   | NO   | MUL | 0       | View permission                 |
| role_add    | tinyint(1)   | NO   |     | 0       | Add permission                  |
| role_edit   | tinyint(1)   | NO   |     | 0       | Edit permission                 |
| role_delete | tinyint(1)   | NO   |     | 0       | Delete permission               |
| role_status | int          | NO   | MUL | 1       | 1=Active, 0=Inactive            |
| role_lmd    | date         | NO   |     |         | Last modified date              |
| role_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** role_code (UNI), role_view, role_status, role_lmu

---

### tbl_user_master

**Purpose:** User authentication (links to employees)  
**Primary Key:** user_id (int, auto_increment)

| Column        | Type         | Null | Key | Default | Notes                           |
| ------------- | ------------ | ---- | --- | ------- | ------------------------------- |
| user_id       | int          | NO   | PRI |         | Auto-increment                  |
| user_name     | varchar(100) | NO   | UNI |         | Username (unique)               |
| user_password | varchar(500) | NO   |     |         | BCrypt hashed                   |
| user_login_ip | varchar(100) | YES  |     |         | Last login IP                   |
| user_status   | int          | NO   | MUL |         | 1=Active, 0=Inactive            |
| user_lmd      | date         | NO   |     |         | Last modified date              |
| emp_number    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |
| user_lmu      | int          | NO   | MUL |         | Last modified by employee       |

**Indexes:** user_name (UNI), user_status, emp_number, user_lmu  
**Foreign Keys:** emp_number → tbl_emp_master.emp_number

---

### tbl_ldap_config

**Purpose:** LDAP/AD integration configuration  
**Primary Key:** config_id (int, auto_increment)

| Column        | Type         | Null | Key | Default | Notes                           |
| ------------- | ------------ | ---- | --- | ------- | ------------------------------- |
| config_id     | int          | NO   | PRI |         | Auto-increment                  |
| config_name   | varchar(100) | NO   |     |         | Config key name                 |
| config_value  | text         | YES  |     |         | Config value                    |
| config_status | int          | NO   |     |         | 1=Active, 0=Inactive            |
| config_lmd    | date         | NO   |     |         | Last modified date              |
| config_lmu    | int          | NO   | MUL |         | FK to tbl_emp_master.emp_number |

**Indexes:** config_lmu

---

## Email & Notification Tables

### tbl_email_template

**Purpose:** Email templates with placeholders  
**Primary Key:** template_id (int, auto_increment)

| Column                | Type         | Null | Key | Default      | Notes                                     |
| --------------------- | ------------ | ---- | --- | ------------ | ----------------------------------------- |
| template_id           | int          | NO   | PRI |              | Auto-increment                            |
| template_code         | varchar(50)  | NO   | UNI |              | Business key (e.g., "SAP_IMPORT_SUCCESS") |
| template_name         | varchar(200) | NO   |     |              | Display name                              |
| template_subject      | varchar(500) | NO   |     |              | Email subject                             |
| template_body         | text         | NO   |     |              | Email body with {{placeholders}}          |
| template_type         | varchar(20)  | NO   | MUL | NOTIFICATION | NOTIFICATION/ALERT/REPORT                 |
| template_category     | varchar(50)  | YES  | MUL |              | Category grouping                         |
| template_description  | text         | YES  |     |              | Purpose description                       |
| template_placeholders | text         | YES  |     |              | JSON list of available placeholders       |
| is_html               | tinyint(1)   | YES  |     | 1            | 1=HTML, 0=Plain text                      |
| template_status       | int          | NO   | MUL | 1            | 1=Active, 0=Inactive                      |
| template_lmd          | date         | NO   |     |              | Last modified date                        |
| template_lmu          | int          | NO   |     |              | FK to tbl_emp_master.emp_number           |

**Indexes:** template_code (UNI), template_type, template_category, template_status  
**Foreign Keys:** template_lmu → tbl_emp_master.emp_number

**Default Templates:**

1. SAP_IMPORT_SUCCESS
2. SAP_IMPORT_FAILURE
3. MATERIAL_QUANTITY_REPORT
4. LOW_STOCK_ALERT

---

## Indent & Procurement Tables

### tbl_indent_master

**Purpose:** Indent/requisition header  
**Primary Key:** indent_id (int, auto_increment)

| Column                    | Type         | Null | Key | Default | Notes                          |
| ------------------------- | ------------ | ---- | --- | ------- | ------------------------------ |
| indent_id                 | int          | NO   | PRI |         | Auto-increment                 |
| indent_no                 | varchar(100) | NO   | MUL |         | Business key                   |
| indent_year               | varchar(45)  | NO   |     |         | Financial year                 |
| indent_date               | datetime     | NO   | MUL |         | Indent creation date           |
| indent_company            | int          | NO   | MUL |         | FK to tbl_company_master       |
| indent_dept               | int          | NO   | MUL |         | FK to tbl_department_master    |
| indent_sec                | int          | NO   | MUL |         | FK to tbl_section_master       |
| indent_plant              | int          | NO   | MUL |         | FK to tbl_plant_master         |
| indent_emp                | int          | NO   | MUL |         | FK to tbl_emp_master (creator) |
| indent_comments           | text         | YES  |     |         | Comments                       |
| indent_delivery_date      | datetime     | YES  |     |         | Expected delivery              |
| indent_po_number          | varchar(100) | YES  |     |         | Related PO number              |
| indent_createdby          | int          | NO   | MUL |         | FK to tbl_emp_master           |
| indent_approvedby         | int          | YES  | MUL |         | Level 1 approver               |
| indent_approvedby_date    | datetime     | YES  |     |         | Level 1 approval date          |
| indent_final_approvedby   | int          | YES  | MUL |         | Level 2 approver               |
| indent_final_date         | datetime     | YES  |     |         | Level 2 approval date          |
| indent_procurementby      | int          | YES  | MUL |         | Procurement officer            |
| indent_remarks            | mediumtext   | YES  |     |         | Level 1 remarks                |
| indent_final_remarks      | mediumtext   | YES  |     |         | Level 2 remarks                |
| indent_status             | int          | NO   | MUL |         | Workflow status                |
| indent_approved_status    | int          | YES  | MUL |         | Level 1 status                 |
| indent_final_status       | int          | YES  | MUL |         | Level 2 status                 |
| indent_procurement_status | int          | YES  | MUL |         | Procurement status             |
| indent_lmd                | datetime     | NO   |     |         | Last modified date             |
| indent_lmu                | int          | NO   | MUL |         | FK to tbl_emp_master           |

**Indexes:** indent_no, indent_date, indent_company, indent_dept, indent_sec, indent_plant, indent_emp, indent_createdby, indent_approvedby, indent_final_approvedby, indent_procurementby, indent_status, indent_approved_status, indent_final_status, indent_procurement_status, indent_lmu

---

### tbl_indent_details

**Purpose:** Indent line items (materials requested)  
**Primary Key:** indent_details_id (int, auto_increment)

| Column                    | Type          | Null | Key | Default | Notes                     |
| ------------------------- | ------------- | ---- | --- | ------- | ------------------------- |
| indent_details_id         | int           | NO   | PRI |         | Auto-increment            |
| indent_id                 | int           | NO   | MUL |         | FK to tbl_indent_master   |
| indent_details_material   | int           | NO   | MUL |         | FK to tbl_material_master |
| indent_details_umo        | int           | NO   | MUL |         | FK to tbl_umo_master      |
| indent_details_qty        | decimal(20,2) | NO   |     |         | Requested quantity        |
| indent_details_rm_qty     | decimal(20,2) | YES  |     |         | RM quantity               |
| indent_details_dept_qty   | decimal(20,2) | YES  |     |         | Department quantity       |
| indent_details_stock_aval | decimal(20,2) | YES  |     |         | Available stock           |
| indent_details_pricing    | decimal(20,2) | YES  |     |         | Unit price                |
| indent_details_purpose    | mediumtext    | YES  |     |         | Purpose/justification     |
| indent_details_vendor     | varchar(200)  | YES  |     |         | Suggested vendor          |
| indent_details_status     | int           | NO   |     |         | Line status               |
| indent_details_lmd        | datetime      | NO   |     |         | Last modified date        |
| indent_details_lmu        | int           | NO   | MUL |         | FK to tbl_emp_master      |

**Indexes:** indent_id, indent_details_material, indent_details_umo, indent_details_lmu  
**Foreign Keys:**

- indent_id → tbl_indent_master.indent_id
- indent_details_material → tbl_material_master.material_id
- indent_details_umo → tbl_umo_master.umo_id

---

### tbl_indent_status

**Purpose:** Indent status codes  
**Primary Key:** indent_status_id (int, auto_increment)

| Column             | Type         | Null | Key | Default | Notes                |
| ------------------ | ------------ | ---- | --- | ------- | -------------------- |
| indent_status_id   | int          | NO   | PRI |         | Auto-increment       |
| indent_status_name | varchar(100) | NO   |     |         | Status name          |
| indent_status      | int          | NO   | MUL |         | Status code          |
| indent_lmd         | date         | NO   |     |         | Last modified date   |
| indent_lmu         | int          | NO   | MUL |         | FK to tbl_emp_master |

**Indexes:** indent_status, indent_lmu

---

### tbl_indent_procurement_logs

**Purpose:** Procurement activity logs for indents  
**Primary Key:** indent_procurement_id (int, auto_increment)

| Column                      | Type     | Null | Key | Default | Notes                   |
| --------------------------- | -------- | ---- | --- | ------- | ----------------------- |
| indent_procurement_id       | int      | NO   | PRI |         | Auto-increment          |
| indent_procurement_indent   | int      | NO   | MUL |         | FK to tbl_indent_master |
| indent_procurement_emp      | int      | NO   | MUL |         | FK to tbl_emp_master    |
| indent_procurement_comments | text     | YES  |     |         | Comments                |
| indent_procurement_date     | datetime | YES  |     |         | Activity date           |
| indent_procurement_lmd      | datetime | NO   |     |         | Last modified date      |
| indent_procurement_lmu      | int      | NO   | MUL |         | FK to tbl_emp_master    |

**Indexes:** indent_procurement_indent, indent_procurement_emp, indent_procurement_lmu

---

### tbl_approval_workflow

**Purpose:** Tracks approval actions for indents  
**Primary Key:** workflow_id (int, auto_increment)

| Column              | Type        | Null | Key | Default | Notes                            |
| ------------------- | ----------- | ---- | --- | ------- | -------------------------------- |
| workflow_id         | int         | NO   | PRI |         | Auto-increment                   |
| indent_id           | int         | NO   | MUL |         | FK to tbl_indent_master          |
| approver_emp_number | int         | NO   | MUL |         | FK to tbl_emp_master             |
| action              | varchar(50) | NO   | MUL |         | APPROVED/REJECTED/INFO_REQUESTED |
| action_date         | datetime    | NO   | MUL |         | Action timestamp                 |
| remarks             | text        | YES  |     |         | Approver remarks                 |
| level               | int         | NO   |     |         | Approval level (1,2,3...)        |
| info_requested      | text        | YES  |     |         | Information requested            |

**Indexes:** indent_id, approver_emp_number, action, action_date  
**Foreign Keys:**

- indent_id → tbl_indent_master.indent_id
- approver_emp_number → tbl_emp_master.emp_number

---

## Purchase Order Tables

### tbl_purchase_orders

**Purpose:** Purchase order header (enhanced)  
**Primary Key:** id (int, auto_increment)

| Column                 | Type          | Null | Key | Default           | Notes                       |
| ---------------------- | ------------- | ---- | --- | ----------------- | --------------------------- |
| id                     | int           | NO   | PRI |                   | Auto-increment              |
| po_number              | varchar(50)   | NO   | UNI |                   | Business key                |
| po_date                | date          | NO   | MUL |                   | PO date                     |
| indent_id              | int           | NO   | MUL |                   | FK to tbl_indent_master     |
| vendor_id              | int           | NO   | MUL |                   | FK to tbl_vendors           |
| department_id          | int           | YES  | MUL |                   | FK to tbl_department_master |
| po_status              | int           | NO   | MUL | 1                 | Status code                 |
| total_amount           | decimal(20,2) | NO   |     | 0.00              | Pre-tax total               |
| tax_amount             | decimal(20,2) | NO   |     | 0.00              | Tax amount                  |
| discount_amount        | decimal(20,2) | NO   |     | 0.00              | Discount                    |
| net_amount             | decimal(20,2) | NO   |     | 0.00              | Final amount                |
| currency               | varchar(10)   | YES  |     | INR               | Currency code               |
| payment_terms          | varchar(100)  | YES  |     |                   | Payment terms               |
| delivery_address       | varchar(1000) | YES  |     |                   | Delivery address            |
| delivery_date          | date          | YES  |     |                   | Requested delivery          |
| expected_delivery_date | date          | YES  | MUL |                   | Expected delivery           |
| actual_delivery_date   | date          | YES  |     |                   | Actual delivery             |
| terms_conditions       | text          | YES  |     |                   | T&C                         |
| notes                  | text          | YES  |     |                   | Additional notes            |
| priority               | varchar(20)   | YES  |     | Medium            | HIGH/MEDIUM/LOW             |
| approved_by            | int           | YES  |     |                   | Approver                    |
| approved_date          | datetime      | YES  |     |                   | Approval date               |
| sent_to_vendor_by      | int           | YES  |     |                   | Who sent to vendor          |
| sent_to_vendor_date    | datetime      | YES  |     |                   | Send date                   |
| cancelled_by           | int           | YES  |     |                   | Who cancelled               |
| cancelled_date         | datetime      | YES  |     |                   | Cancel date                 |
| cancellation_reason    | varchar(500)  | YES  |     |                   | Why cancelled               |
| closed_by              | int           | YES  |     |                   | Who closed                  |
| closed_date            | datetime      | YES  |     |                   | Close date                  |
| created_by             | int           | NO   |     |                   | Creator                     |
| created_date           | datetime      | NO   | MUL | CURRENT_TIMESTAMP | Creation timestamp          |
| last_modified_by       | int           | YES  |     |                   | Last modifier               |
| last_modified_date     | datetime      | YES  |     |                   | Last modified timestamp     |

**Indexes:** po_number (UNI), po_date, indent_id, vendor_id, department_id, po_status, expected_delivery_date, created_date

---

### tbl_purchase_order_details

**Purpose:** PO line items  
**Primary Key:** id (int, auto_increment)

| Column                 | Type          | Null | Key | Default           | Notes                     |
| ---------------------- | ------------- | ---- | --- | ----------------- | ------------------------- |
| id                     | int           | NO   | PRI |                   | Auto-increment            |
| purchase_order_id      | int           | NO   | MUL |                   | FK to tbl_purchase_orders |
| line_number            | int           | NO   | MUL |                   | Line sequence             |
| indent_detail_id       | int           | YES  |     |                   | FK to tbl_indent_details  |
| material_id            | int           | NO   | MUL |                   | FK to tbl_material_master |
| material_code          | varchar(100)  | NO   |     |                   | Cached material code      |
| material_name          | varchar(255)  | NO   |     |                   | Cached material name      |
| material_description   | text          | YES  |     |                   | Description               |
| quantity               | decimal(20,2) | NO   |     |                   | Order quantity            |
| unit_of_measure        | varchar(50)   | NO   |     |                   | UOM                       |
| unit_price             | decimal(20,2) | NO   |     |                   | Price per unit            |
| tax_rate               | decimal(5,2)  | NO   |     | 0.00              | Tax percentage            |
| tax_amount             | decimal(20,2) | NO   |     | 0.00              | Tax amount                |
| discount_rate          | decimal(5,2)  | NO   |     | 0.00              | Discount percentage       |
| discount_amount        | decimal(20,2) | NO   |     | 0.00              | Discount amount           |
| line_total             | decimal(20,2) | NO   |     |                   | Net line total            |
| received_quantity      | decimal(20,2) | NO   |     | 0.00              | Received so far           |
| pending_quantity       | decimal(20,2) | NO   |     | 0.00              | Pending quantity          |
| rejected_quantity      | decimal(20,2) | NO   |     | 0.00              | Rejected quantity         |
| delivery_status        | int           | NO   | MUL | 1                 | Delivery status           |
| expected_delivery_date | date          | YES  | MUL |                   | Expected delivery         |
| actual_delivery_date   | date          | YES  |     |                   | Actual delivery           |
| notes                  | text          | YES  |     |                   | Line notes                |
| created_date           | datetime      | NO   |     | CURRENT_TIMESTAMP | Creation timestamp        |
| last_modified_date     | datetime      | YES  |     |                   | Last modified timestamp   |

**Indexes:** purchase_order_id, line_number, material_id, delivery_status, expected_delivery_date

---

### tbl_po_header (Legacy)

**Purpose:** Legacy PO header  
**Primary Key:** po_id (int, auto_increment)

| Column              | Type          | Null | Key | Default | Notes                   |
| ------------------- | ------------- | ---- | --- | ------- | ----------------------- |
| po_id               | int           | NO   | PRI |         | Auto-increment          |
| po_number           | varchar(100)  | NO   | UNI |         | Business key            |
| po_date             | datetime      | NO   | MUL |         | PO date                 |
| indent_id           | int           | NO   | MUL |         | FK to tbl_indent_master |
| vendor_id           | int           | NO   | MUL |         | FK to tbl_vendor_master |
| po_total_amount     | decimal(20,2) | YES  |     | 0.00    | Total                   |
| po_discount         | decimal(20,2) | YES  |     | 0.00    | Discount                |
| po_tax_amount       | decimal(20,2) | YES  |     | 0.00    | Tax                     |
| po_net_amount       | decimal(20,2) | YES  |     | 0.00    | Net amount              |
| po_payment_terms    | varchar(200)  | YES  |     |         | Payment terms           |
| po_delivery_date    | datetime      | YES  |     |         | Delivery date           |
| po_delivery_address | text          | YES  |     |         | Address                 |
| po_remarks          | text          | YES  |     |         | Remarks                 |
| po_createdby        | int           | NO   | MUL |         | Creator                 |
| po_created_date     | datetime      | NO   |     |         | Creation date           |
| po_approvedby       | int           | YES  | MUL |         | Approver                |
| po_approved_date    | datetime      | YES  |     |         | Approval date           |
| po_status_id        | int           | NO   | MUL |         | FK to tbl_po_status     |
| po_lmd              | datetime      | NO   |     |         | Last modified date      |
| po_lmu              | int           | NO   | MUL |         | Last modifier           |

**Indexes:** po_number (UNI), po_date, indent_id, vendor_id, po_createdby, po_approvedby, po_status_id, po_lmu

---

### tbl_po_details (Legacy)

**Purpose:** Legacy PO line items  
**Primary Key:** po_detail_id (int, auto_increment)

| Column               | Type          | Null | Key | Default | Notes                     |
| -------------------- | ------------- | ---- | --- | ------- | ------------------------- |
| po_detail_id         | int           | NO   | PRI |         | Auto-increment            |
| po_id                | int           | NO   | MUL |         | FK to tbl_po_header       |
| indent_detail_id     | int           | NO   | MUL |         | FK to tbl_indent_details  |
| material_id          | int           | NO   | MUL |         | FK to tbl_material_master |
| uom_id               | int           | NO   | MUL |         | FK to tbl_umo_master      |
| po_quantity          | decimal(20,2) | NO   |     |         | Quantity                  |
| po_unit_price        | decimal(20,2) | NO   |     |         | Unit price                |
| po_tax_percent       | decimal(5,2)  | YES  |     | 0.00    | Tax %                     |
| po_line_total        | decimal(20,2) | NO   |     |         | Line total                |
| po_received_quantity | decimal(20,2) | YES  |     | 0.00    | Received                  |
| po_pending_quantity  | decimal(20,2) | NO   |     |         | Pending                   |
| po_detail_remarks    | text          | YES  |     |         | Remarks                   |
| po_detail_status     | int           | NO   |     | 1       | Status                    |
| po_detail_lmd        | datetime      | NO   |     |         | Last modified date        |
| po_detail_lmu        | int           | NO   | MUL |         | Last modifier             |

**Indexes:** po_id, indent_detail_id, material_id, uom_id, po_detail_lmu

---

### tbl_po_status

**Purpose:** PO status codes  
**Primary Key:** po_status_id (int, auto_increment)

| Column         | Type         | Null | Key | Default | Notes              |
| -------------- | ------------ | ---- | --- | ------- | ------------------ |
| po_status_id   | int          | NO   | PRI |         | Auto-increment     |
| po_status_name | varchar(100) | NO   |     |         | Status name        |
| po_status_code | varchar(20)  | NO   | UNI |         | Status code        |
| po_status      | int          | NO   | MUL | 1       | Numeric status     |
| po_lmd         | date         | NO   |     |         | Last modified date |
| po_lmu         | int          | NO   | MUL |         | Last modifier      |

**Indexes:** po_status_code (UNI), po_status, po_lmu

---

## Goods Receipt & Issue Tables

### tbl_goods_receipt

**Purpose:** Goods receipt notes (GRN)  
**Primary Key:** goods_receipt_id (int, auto_increment)

| Column                                 | Type          | Null | Key | Default | Notes                    |
| -------------------------------------- | ------------- | ---- | --- | ------- | ------------------------ |
| goods_receipt_id                       | int           | NO   | PRI |         | Auto-increment           |
| goods_receipt_no                       | varchar(100)  | NO   | UNI |         | GRN number               |
| goods_receipt_date                     | datetime      | NO   |     |         | Receipt date             |
| indent_id                              | int           | NO   | MUL |         | FK to tbl_indent_master  |
| indent_details_id                      | int           | NO   | MUL |         | FK to tbl_indent_details |
| goods_receipt_quantity                 | decimal(20,2) | NO   |     |         | Received quantity        |
| goods_receipt_issued_quantity          | decimal(20,2) | YES  |     |         | Issued quantity          |
| goods_receipt_balance_inventory        | decimal(20,2) | YES  |     |         | Balance                  |
| goods_receipt_opening_quantity         | decimal(20,2) | YES  |     |         | Opening stock            |
| goods_receipt_rate                     | decimal(20,2) | YES  |     |         | Rate                     |
| goods_receipt_amount                   | decimal(20,2) | YES  |     |         | Amount                   |
| goods_receipt_vendor                   | varchar(200)  | YES  |     |         | Vendor                   |
| goods_receipt_comments                 | text          | YES  |     |         | Comments                 |
| goods_receipt_createdby                | int           | NO   | MUL |         | Creator                  |
| goods_receipt_created_date             | datetime      | YES  |     |         | Creation date            |
| goods_receipt_created_remarks          | text          | YES  |     |         | Creation remarks         |
| goods_receipt_approvedby               | int           | YES  | MUL |         | Level 1 approver         |
| goods_receipt_approvedby_date          | datetime      | YES  |     |         | Level 1 date             |
| goods_receipt_approvedby_remarks       | text          | YES  |     |         | Level 1 remarks          |
| goods_receipt_final_approvedby         | int           | YES  | MUL |         | Level 2 approver         |
| goods_receipt_final_approvedby_date    | datetime      | YES  |     |         | Level 2 date             |
| goods_receipt_final_approvedby_remarks | text          | YES  |     |         | Level 2 remarks          |
| goods_receipt_requested_quantity       | decimal(20,2) | YES  |     |         | Requested quantity       |
| goods_receipt_balance_quantity_stores  | decimal(20,2) | YES  |     |         | Stores balance           |
| goods_receipt_storesby                 | int           | YES  | MUL |         | Stores officer           |
| goods_receipt_storesby_date            | datetime      | YES  |     |         | Stores date              |
| goods_receipt_storesby_remarks         | text          | YES  |     |         | Stores remarks           |
| goods_receipt_status                   | int           | NO   | MUL |         | Status                   |
| goods_receipt_approved_status          | int           | YES  | MUL |         | Level 1 status           |
| goods_receipt_final_status             | int           | YES  | MUL |         | Level 2 status           |
| goods_receipt_storesby_status          | int           | YES  | MUL |         | Stores status            |
| goods_receipt_lmd                      | datetime      | NO   |     |         | Last modified date       |
| goods_receipt_lmu                      | int           | NO   | MUL |         | Last modifier            |

**Indexes:** goods_receipt_no (UNI), indent_id, indent_details_id, goods_receipt_createdby, goods_receipt_approvedby, goods_receipt_final_approvedby, goods_receipt_storesby, goods_receipt_status, goods_receipt_approved_status, goods_receipt_final_status, goods_receipt_storesby_status, goods_receipt_lmu

---

### tbl_issue_note

**Purpose:** Material issue notes  
**Primary Key:** issue_note_id (int, auto_increment)

| Column                     | Type         | Null | Key | Default | Notes                       |
| -------------------------- | ------------ | ---- | --- | ------- | --------------------------- |
| issue_note_id              | int          | NO   | PRI |         | Auto-increment              |
| issue_note_no              | varchar(100) | NO   | UNI |         | Issue note number           |
| issue_note_date            | datetime     | NO   |     |         | Issue date                  |
| issue_note_company         | int          | NO   | MUL |         | FK to tbl_company_master    |
| issue_note_dept            | int          | NO   | MUL |         | FK to tbl_department_master |
| issue_note_sec             | int          | NO   | MUL |         | FK to tbl_section_master    |
| issue_note_plant           | int          | NO   | MUL |         | FK to tbl_plant_master      |
| issue_note_issued_to       | varchar(200) | YES  |     |         | Issued to person            |
| issue_note_purpose         | text         | YES  |     |         | Purpose                     |
| issue_note_comments        | text         | YES  |     |         | Comments                    |
| issue_note_createdby       | int          | NO   | MUL |         | Creator                     |
| issue_note_approvedby      | int          | YES  | MUL |         | Approver                    |
| issue_note_approvedby_date | datetime     | YES  |     |         | Approval date               |
| issue_note_storesby        | int          | YES  | MUL |         | Stores officer              |
| issue_note_storesby_date   | datetime     | YES  |     |         | Stores date                 |
| issue_note_status          | int          | NO   | MUL |         | Status                      |
| issue_note_approved_status | int          | YES  | MUL |         | Approval status             |
| issue_note_storesby_status | int          | YES  | MUL |         | Stores status               |
| issue_note_lmd             | datetime     | NO   |     |         | Last modified date          |
| issue_note_lmu             | int          | NO   | MUL |         | Last modifier               |

**Indexes:** issue_note_no (UNI), issue_note_company, issue_note_dept, issue_note_sec, issue_note_plant, issue_note_createdby, issue_note_approvedby, issue_note_storesby, issue_note_status, issue_note_approved_status, issue_note_storesby_status, issue_note_lmu

---

### tbl_issue_note_details

**Purpose:** Issue note line items  
**Primary Key:** issue_note_details_id (int, auto_increment)

| Column                      | Type          | Null | Key | Default | Notes                     |
| --------------------------- | ------------- | ---- | --- | ------- | ------------------------- |
| issue_note_details_id       | int           | NO   | PRI |         | Auto-increment            |
| issue_note_id               | int           | NO   | MUL |         | FK to tbl_issue_note      |
| issue_note_details_material | int           | NO   | MUL |         | FK to tbl_material_master |
| issue_note_details_umo      | int           | NO   | MUL |         | FK to tbl_umo_master      |
| issue_note_details_qty      | decimal(20,2) | NO   |     |         | Quantity                  |
| issue_note_details_rate     | decimal(20,2) | YES  |     |         | Rate                      |
| issue_note_details_amount   | decimal(20,2) | YES  |     |         | Amount                    |
| issue_note_details_purpose  | text          | YES  |     |         | Purpose                   |
| issue_note_details_status   | int           | NO   | MUL |         | Status                    |
| issue_note_details_lmd      | datetime      | NO   |     |         | Last modified date        |
| issue_note_details_lmu      | int           | NO   | MUL |         | Last modifier             |

**Indexes:** issue_note_id, issue_note_details_material, issue_note_details_umo, issue_note_details_status, issue_note_details_lmu

---

## Mapping Tables

### tbl_map_company_department

**Purpose:** Company-Department relationships  
**Primary Key:** map_id (int, auto_increment)

| Column     | Type | Null | Key | Default | Notes                       |
| ---------- | ---- | ---- | --- | ------- | --------------------------- |
| map_id     | int  | NO   | PRI |         | Auto-increment              |
| map_comp   | int  | NO   | MUL |         | FK to tbl_company_master    |
| map_dept   | int  | NO   | MUL |         | FK to tbl_department_master |
| map_status | int  | NO   | MUL |         | 1=Active, 0=Inactive        |
| map_lmd    | date | NO   | MUL |         | Last modified date          |
| map_lmu    | int  | NO   | MUL |         | Last modifier               |

**Indexes:** map_comp, map_dept, map_status, map_lmd, map_lmu

---

### tbl_map_company_emp

**Purpose:** Company-Employee relationships  
**Primary Key:** map_id (int, auto_increment)

| Column     | Type | Null | Key | Default | Notes                    |
| ---------- | ---- | ---- | --- | ------- | ------------------------ |
| map_id     | int  | NO   | PRI |         | Auto-increment           |
| map_comp   | int  | NO   | MUL |         | FK to tbl_company_master |
| map_emp    | int  | NO   | MUL |         | FK to tbl_emp_master     |
| map_status | int  | NO   | MUL |         | 1=Active, 0=Inactive     |
| map_lmd    | date | NO   | MUL |         | Last modified date       |
| map_lmu    | int  | NO   | MUL |         | Last modifier            |

**Indexes:** map_comp, map_emp, map_status, map_lmd, map_lmu

---

### tbl_map_company_location

**Purpose:** Company-Location relationships  
**Primary Key:** map_id (int, auto_increment)

| Column     | Type | Null | Key | Default | Notes                     |
| ---------- | ---- | ---- | --- | ------- | ------------------------- |
| map_id     | int  | NO   | PRI |         | Auto-increment            |
| map_comp   | int  | NO   | MUL |         | FK to tbl_company_master  |
| map_loc    | int  | NO   | MUL |         | FK to tbl_location_master |
| map_status | int  | NO   | MUL |         | 1=Active, 0=Inactive      |
| map_lmd    | date | NO   | MUL |         | Last modified date        |
| map_lmu    | int  | NO   | MUL |         | Last modifier             |

**Indexes:** map_comp, map_loc, map_status, map_lmd, map_lmu

---

### tbl_map_company_location_material

**Purpose:** Material inventory by company-location  
**Primary Key:** map_id (int, auto_increment)

| Column            | Type          | Null | Key | Default | Notes                     |
| ----------------- | ------------- | ---- | --- | ------- | ------------------------- |
| map_id            | int           | NO   | PRI |         | Auto-increment            |
| map_comp          | int           | NO   | MUL |         | FK to tbl_company_master  |
| map_loc           | int           | NO   | MUL |         | FK to tbl_location_master |
| map_material      | int           | NO   | MUL |         | FK to tbl_material_master |
| map_quantity      | decimal(20,2) | YES  | MUL |         | Current stock             |
| map_reorder_level | decimal(20,2) | YES  |     |         | Reorder trigger           |
| map_max_level     | decimal(20,2) | YES  |     |         | Maximum stock             |
| map_status        | int           | NO   | MUL |         | 1=Active, 0=Inactive      |
| map_lmd           | date          | NO   | MUL |         | Last modified date        |
| map_lmu           | int           | NO   | MUL |         | Last modifier             |

**Indexes:** map_comp, map_loc, map_material, map_quantity, map_status, map_lmd, map_lmu

---

### tbl_map_company_plant_material

**Purpose:** Material inventory by company-plant  
**Primary Key:** map_id (int, auto_increment)

| Column              | Type          | Null | Key | Default | Notes                     |
| ------------------- | ------------- | ---- | --- | ------- | ------------------------- |
| map_id              | int           | NO   | PRI |         | Auto-increment            |
| map_comp            | int           | NO   | MUL |         | FK to tbl_company_master  |
| map_plant           | int           | NO   | MUL |         | FK to tbl_plant_master    |
| map_material        | int           | NO   | MUL |         | FK to tbl_material_master |
| map_quantity_stores | decimal(20,2) | YES  |     | 0.00    | Stores quantity           |
| map_reorder_level   | decimal(20,2) | YES  |     |         | Reorder level             |
| map_max_level       | decimal(20,2) | YES  |     |         | Max level                 |
| map_status          | int           | NO   | MUL |         | 1=Active, 0=Inactive      |
| map_lmd             | date          | NO   |     |         | Last modified date        |
| map_lmu             | int           | NO   | MUL |         | Last modifier             |

**Indexes:** map_comp, map_plant, map_material, map_status, map_lmu

---

### tbl_map_emp_reporting

**Purpose:** Employee reporting hierarchy  
**Primary Key:** report_id (int, auto_increment)

| Column                | Type | Null | Key | Default | Notes                              |
| --------------------- | ---- | ---- | --- | ------- | ---------------------------------- |
| report_id             | int  | NO   | PRI |         | Auto-increment                     |
| report_sub            | int  | NO   | MUL |         | FK to tbl_emp_master (subordinate) |
| report_sup            | int  | NO   | MUL |         | FK to tbl_emp_master (supervisor)  |
| report_effective_date | date | YES  | MUL |         | Effective from                     |
| report_status         | int  | NO   | MUL |         | 1=Active, 0=Inactive               |
| report_lmd            | date | NO   | MUL |         | Last modified date                 |
| report_lmu            | int  | NO   | MUL |         | Last modifier                      |

**Indexes:** report_sub, report_sup, report_effective_date, report_status, report_lmd, report_lmu

---

### tbl_map_emp_roles

**Purpose:** Employee-Role assignments  
**Primary Key:** emp_roles_id (int, auto_increment)

| Column                  | Type     | Null | Key | Default | Notes                  |
| ----------------------- | -------- | ---- | --- | ------- | ---------------------- |
| emp_roles_id            | int      | NO   | PRI |         | Auto-increment         |
| emp_number              | int      | NO   | MUL |         | FK to tbl_emp_master   |
| role_id                 | int      | NO   | MUL |         | FK to tbl_roles_master |
| emp_roles_status        | int      | NO   | MUL |         | 1=Active, 0=Inactive   |
| emp_roles_assigned_by   | int      | YES  | MUL |         | Who assigned           |
| emp_roles_assigned_date | datetime | YES  | MUL |         | Assignment date        |
| emp_roles_remarks       | text     | YES  |     |         | Assignment remarks     |
| emp_roles_removed_by    | int      | YES  |     |         | Who removed            |
| emp_roles_removed_date  | datetime | YES  |     |         | Removal date           |
| emp_roles_lmd           | date     | NO   | MUL |         | Last modified date     |
| emp_roles_lmu           | int      | NO   | MUL |         | Last modifier          |

**Indexes:** emp_number, role_id, emp_roles_status, emp_roles_assigned_by, emp_roles_assigned_date, emp_roles_lmd, emp_roles_lmu

---

## Legacy/PZ Tables

### pz_crop_group

**Purpose:** Legacy crop group  
**Primary Key:** crop_id (int, auto_increment)

| Column      | Type         | Null | Key | Default | Notes              |
| ----------- | ------------ | ---- | --- | ------- | ------------------ |
| crop_id     | int          | NO   | PRI |         | Auto-increment     |
| crop_code   | varchar(100) | NO   | UNI |         | Business key       |
| crop_name   | varchar(100) | NO   |     |         | Crop name          |
| crop_status | int          | NO   | MUL |         | Status             |
| crop_lmd    | date         | NO   |     |         | Last modified date |
| crop_lmu    | int          | NO   | MUL |         | Last modifier      |

---

### pz_sap_material_masters

**Purpose:** SAP material master data  
**Primary Key:** id (int, auto_increment)

| Column         | Type         | Null | Key | Default | Notes           |
| -------------- | ------------ | ---- | --- | ------- | --------------- |
| id             | int          | NO   | PRI |         | Auto-increment  |
| material_code  | varchar(100) | NO   | UNI |         | Material code   |
| material_desc  | varchar(200) | YES  |     |         | Description     |
| uom            | varchar(20)  | YES  |     |         | Unit of measure |
| material_type  | varchar(50)  | YES  | MUL |         | Material type   |
| plant          | int          | YES  |     |         | Plant ID        |
| materialgroup  | varchar(100) | YES  |     |         | Material group  |
| materialstatus | int          | NO   |     |         | Status          |
| plant_name     | varchar(200) | YES  |     |         | Plant name      |

**Indexes:** material_code (UNI), material_type

---

### pz_schedule_sap_material_master

**Purpose:** Scheduled SAP import staging table (51 columns)  
**Primary Key:** company_id (int, auto_increment)

Contains quality control fields: STL, ODV, GOT, ELISA, MOISTURE, GERM_NORMAL, etc.

---

## Quick Reference by Entity Type

### Core Master Data

- **Employees:** tbl_emp_master (emp_id VARCHAR(100))
- **Companies:** tbl_company_master (comp_code VARCHAR(100))
- **Plants:** tbl_plant_master (plant_code VARCHAR(100))
- **Locations:** tbl_location_master (loc_code VARCHAR(100))
- **Departments:** tbl_department_master (dept_code VARCHAR(100))
- **Sections:** tbl_section_master (sec_code VARCHAR(100))
- **Materials:** tbl_material_master (material_code VARCHAR(100))
- **UOM:** tbl_umo_master (umo_code VARCHAR(100))
- **Crops:** tbl_crop_master (crop_code VARCHAR(100))

### Procurement Flow

1. **Indent:** tbl_indent_master + tbl_indent_details
2. **Approval:** tbl_approval_workflow
3. **Purchase Order:** tbl_purchase_orders + tbl_purchase_order_details
4. **Goods Receipt:** tbl_goods_receipt
5. **Issue Note:** tbl_issue_note + tbl_issue_note_details

### Security

- **Users:** tbl_user_master (links to emp_number)
- **Roles:** tbl_roles_master
- **Role Assignments:** tbl_map_emp_roles
- **Audit:** tbl_audit_log

### Inventory

- **Location Stock:** tbl_map_company_location_material
- **Plant Stock:** tbl_map_company_plant_material

### Foreign Key Patterns

- **emp_id:** VARCHAR(100) in tbl_emp_master
- **emp_number:** INT in tbl_emp_master (used for FKs)
- **comp_id:** INT in tbl_company_master
- **plant_id:** INT in tbl_plant_master
- **material_id:** INT in tbl_material_master
- **indent_id:** INT in tbl_indent_master

### Common Patterns

- **Status Fields:** 1=Active, 0=Inactive
- **Audit Fields:** \_lmd (last modified date), \_lmu (last modified user)
- **Workflow Fields:** \_createdby, \_approvedby, \_final_approvedby

---

## Notes

1. **Type Mismatch Warning:** tbl_email_template.template_lmu is INT, but should be VARCHAR(100) to match emp_id. Migration V21 attempts to fix this.

2. **Flyway Migration Status:** Check flyway_schema_history.success column to see which migrations succeeded.

3. **Dual Vendor Tables:** Both tbl_vendor_master and tbl_vendors exist. tbl_vendors appears to be enhanced version with metrics.

4. **Dual PO Tables:** Both tbl_po_header/tbl_po_details and tbl_purchase_orders/tbl_purchase_order_details exist.

5. **Legacy PZ Tables:** Tables prefixed with `pz_` appear to be from previous system version.

---

**Last Updated:** November 7, 2025  
**Maintainer:** Development Team  
**Version:** 1.0
