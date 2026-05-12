-- Temporarily disable foreign key checks for H2
SET REFERENTIAL_INTEGRITY FALSE;

-- Insert supporting master data first
INSERT INTO
    tbl_company_master (
        comp_id,
        comp_code,
        comp_name,
        comp_status,
        comp_lmd,
        comp_lmu
    )
VALUES (
        1,
        'NSL',
        'NSL India',
        1,
        CURRENT_DATE(),
        0
    );

INSERT INTO
    tbl_department_master (
        dept_id,
        dept_code,
        dept_name,
        dept_status,
        dept_lmd,
        dept_lmu
    )
VALUES (
        1,
        'IT',
        'Information Technology',
        1,
        CURRENT_DATE(),
        0
    );

INSERT INTO
    tbl_location_master (
        loc_id,
        loc_code,
        loc_name,
        loc_status,
        loc_lmd,
        loc_lmu
    )
VALUES (
        1,
        'HQ',
        'Head Office',
        1,
        CURRENT_DATE(),
        0
    );

-- Insert role
INSERT INTO
    tbl_roles_master (
        role_id,
        role_code,
        role_name,
        role_status,
        role_lmd,
        role_lmu
    )
VALUES (
        3001,
        'ADMIN',
        'Administrator',
        1,
        CURRENT_DATE(),
        0
    );

-- Insert employee with all required fields
INSERT INTO
    tbl_emp_master (
        emp_number,
        emp_id,
        emp_name,
        emp_email,
        emp_password,
        emp_join_date,
        emp_designation,
        emp_cost_center,
        emp_path,
        emp_status,
        emp_lmd,
        emp_department,
        emp_location,
        emp_company,
        emp_plant
    )
VALUES (
        2001,
        'E-1001',
        'Legacy User',
        'legacy.user@nsl.com',
        '482c811da5d5b4bc6d497ffa98491e38',
        CURRENT_DATE(),
        'System Administrator',
        'CC-001',
        NULL,
        1,
        CURRENT_DATE(),
        1,
        1,
        1,
        NULL
    );

-- Insert user account
INSERT INTO
    tbl_user_master (
        user_id,
        user_name,
        user_password,
        user_login_ip,
        user_status,
        user_lmd,
        emp_number,
        user_lmu
    )
VALUES (
        1001,
        'legacy.user',
        '482c811da5d5b4bc6d497ffa98491e38',
        NULL,
        1,
        CURRENT_DATE(),
        2001,
        0
    );

-- Map employee to role
INSERT INTO
    tbl_map_emp_roles (
        emp_roles_id,
        emp_number,
        role_id,
        emp_roles_status,
        emp_roles_assigned_date,
        emp_roles_assigned_by,
        emp_roles_removed_date,
        emp_roles_removed_by,
        emp_roles_remarks,
        emp_roles_lmd,
        emp_roles_lmu
    )
VALUES (
        4001,
        2001,
        3001,
        1,
        CURRENT_DATE(),
        'SYSTEM',
        NULL,
        NULL,
        'Initial role assignment',
        CURRENT_TIMESTAMP(),
        'SYSTEM'
    );

-- Re-enable foreign key checks
SET REFERENTIAL_INTEGRITY TRUE;