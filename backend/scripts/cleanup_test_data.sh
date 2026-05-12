#!/bin/bash

##############################################################################
# ProcureZone Test Data Cleanup Script
# Date: October 15, 2025
# Purpose: Clean up test data created by comprehensive_api_test.sh
##############################################################################

DB_USER="root"
DB_PASS="password"
DB_NAME="seeds_indent"

echo "=========================================="
echo "Cleaning up test data..."
echo "=========================================="

# Clean up test employees and users
mysql -u $DB_USER -p$DB_PASS $DB_NAME -e "
    -- Delete test users
    DELETE FROM tbl_user_master WHERE user_name LIKE 'test.%';
    
    -- Delete test employee roles
    DELETE FROM tbl_map_emp_roles WHERE emp_number IN (
        SELECT emp_number FROM tbl_emp_master WHERE emp_id LIKE 'TEST%'
    );
    
    -- Delete test employees
    DELETE FROM tbl_emp_master WHERE emp_id LIKE 'TEST%';
    
    -- Delete test vendors
    DELETE FROM tbl_vendors WHERE vendor_name LIKE 'Test%' OR vendor_name LIKE '%API Test%';
    
    -- Delete test indents (and related data via cascade)
    DELETE FROM tbl_indent_details WHERE indent_id IN (
        SELECT indent_id FROM tbl_indent_master WHERE indent_no LIKE 'TEST-%'
    );
    DELETE FROM tbl_indent_master WHERE indent_no LIKE 'TEST-%';
    
    -- Delete test POs
    DELETE FROM tbl_po_details WHERE po_id IN (
        SELECT id FROM tbl_purchase_orders WHERE po_number LIKE 'TEST-%'
    );
    DELETE FROM tbl_purchase_orders WHERE po_number LIKE 'TEST-%';
    
    -- Delete test master data
    DELETE FROM tbl_company_master WHERE comp_name LIKE 'Test%';
    DELETE FROM tbl_department_master WHERE dept_name LIKE 'Test%';
    DELETE FROM tbl_material_master WHERE material_name LIKE 'Test%';
    DELETE FROM tbl_uom_master WHERE umo_name LIKE 'Test%';
"

echo "✓ Test data cleanup complete!"
echo ""
