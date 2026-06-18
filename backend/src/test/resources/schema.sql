-- H2 Test Database Schema for ProcureZone
-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS tbl_audit_log;

DROP TABLE IF EXISTS tbl_map_emp_roles;

DROP TABLE IF EXISTS tbl_user_master;

DROP TABLE IF EXISTS tbl_indent_master;

DROP TABLE IF EXISTS tbl_emp_master;

DROP TABLE IF EXISTS tbl_roles_master;

DROP TABLE IF EXISTS tbl_material_master;

DROP TABLE IF EXISTS tbl_uom_master;

DROP TABLE IF EXISTS tbl_department_master;

DROP TABLE IF EXISTS tbl_location_master;

DROP TABLE IF EXISTS tbl_plant_master;

DROP TABLE IF EXISTS tbl_company_master;

DROP TABLE IF EXISTS tbl_vendor_master;

-- Master Data Tables
CREATE TABLE tbl_company_master (
    comp_id INT PRIMARY KEY,
    comp_code VARCHAR(50) NOT NULL,
    comp_name VARCHAR(200) NOT NULL,
    comp_status INT NOT NULL,
    comp_lmd DATE,
    comp_lmu INT
);

CREATE TABLE tbl_department_master (
    dept_id INT PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL,
    dept_name VARCHAR(200) NOT NULL,
    dept_status INT NOT NULL,
    dept_lmd DATE,
    dept_lmu INT
);

CREATE TABLE tbl_location_master (
    loc_id INT PRIMARY KEY,
    loc_code VARCHAR(50) NOT NULL,
    loc_name VARCHAR(200) NOT NULL,
    loc_status INT NOT NULL,
    loc_lmd DATE,
    loc_lmu INT
);

CREATE TABLE tbl_plant_master (
    plant_id INT PRIMARY KEY,
    plant_code VARCHAR(50) NOT NULL,
    plant_name VARCHAR(200) NOT NULL,
    plant_status INT NOT NULL,
    plant_lmd DATE,
    plant_lmu INT
);

CREATE TABLE tbl_uom_master (
    uom_id INT PRIMARY KEY,
    uom_code VARCHAR(50) NOT NULL,
    uom_name VARCHAR(100) NOT NULL,
    uom_status INT NOT NULL,
    uom_lmd DATE,
    uom_lmu INT
);

CREATE TABLE tbl_material_master (
    mat_id INT PRIMARY KEY,
    mat_code VARCHAR(50) NOT NULL,
    mat_name VARCHAR(200) NOT NULL,
    mat_desc VARCHAR(500),
    mat_uom INT,
    mat_status INT NOT NULL,
    mat_lmd DATE,
    mat_lmu INT
);

CREATE TABLE tbl_vendor_master (
    vendor_id INT PRIMARY KEY,
    vendor_code VARCHAR(50) NOT NULL,
    vendor_name VARCHAR(200) NOT NULL,
    vendor_gst_number VARCHAR(50),
    vendor_email VARCHAR(100),
    vendor_phone VARCHAR(20),
    vendor_address VARCHAR(500),
    vendor_status INT NOT NULL,
    vendor_lmd DATE,
    vendor_lmu INT
);

-- Employee & Identity Tables
CREATE TABLE tbl_emp_master (
    emp_number BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_id VARCHAR(100) NOT NULL UNIQUE,
    emp_name VARCHAR(100) NOT NULL,
    emp_email VARCHAR(100) NOT NULL UNIQUE,
    emp_password VARCHAR(500) NOT NULL,
    emp_join_date DATE,
    emp_designation VARCHAR(100) NOT NULL,
    emp_cost_center VARCHAR(500),
    emp_path VARCHAR(500),
    emp_status INT NOT NULL,
    emp_lmd DATE NOT NULL,
    emp_department INT NOT NULL,
    emp_location INT NOT NULL,
    emp_company INT,
    emp_plant VARCHAR(100),
    CONSTRAINT fk_emp_dept FOREIGN KEY (emp_department) REFERENCES tbl_department_master (dept_id),
    CONSTRAINT fk_emp_loc FOREIGN KEY (emp_location) REFERENCES tbl_location_master (loc_id)
);

CREATE TABLE tbl_roles_master (
    role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(100) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    role_view VARCHAR(100),
    role_add VARCHAR(100),
    role_edit VARCHAR(100),
    role_delete VARCHAR(100),
    role_status INT NOT NULL,
    role_lmd DATE,
    role_lmu INT
);

CREATE TABLE tbl_user_master (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(100) NOT NULL,
    user_password VARCHAR(500) NOT NULL,
    user_login_ip VARCHAR(100),
    user_status INT NOT NULL,
    user_lmd DATE,
    emp_number BIGINT NOT NULL,
    user_lmu INT,
    CONSTRAINT fk_user_emp FOREIGN KEY (emp_number) REFERENCES tbl_emp_master (emp_number)
);

CREATE TABLE tbl_map_emp_roles (
    emp_roles_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_number BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    emp_roles_status INT NOT NULL,
    emp_roles_assigned_date DATE,
    emp_roles_assigned_by VARCHAR(100),
    emp_roles_removed_date DATE,
    emp_roles_removed_by VARCHAR(100),
    emp_roles_remarks VARCHAR(500),
    emp_roles_lmd TIMESTAMP NOT NULL,
    emp_roles_lmu VARCHAR(100) NOT NULL,
    CONSTRAINT fk_emp_role_emp FOREIGN KEY (emp_number) REFERENCES tbl_emp_master (emp_number),
    CONSTRAINT fk_emp_role_role FOREIGN KEY (role_id) REFERENCES tbl_roles_master (role_id)
);

-- Audit Log Table
CREATE TABLE tbl_audit_log (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    audit_action VARCHAR(100) NOT NULL,
    audit_entity_type VARCHAR(100),
    audit_entity_id VARCHAR(100),
    audit_user_id INT,
    audit_username VARCHAR(100),
    audit_ip_address VARCHAR(45),
    audit_timestamp TIMESTAMP NOT NULL,
    audit_details TEXT,
    audit_status VARCHAR(50)
);

-- Transaction Tables
CREATE TABLE tbl_indent_master (
    indent_id INT PRIMARY KEY,
    indent_number VARCHAR(50) NOT NULL,
    indent_date DATE NOT NULL,
    indent_status VARCHAR(50) NOT NULL,
    indent_created_by BIGINT,
    indent_department INT,
    indent_location INT,
    indent_company INT,
    indent_lmd TIMESTAMP,
    indent_lmu INT
);