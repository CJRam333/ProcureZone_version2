# Database Migration Guide - ProcureZone

## ✅ Migration Status

**Date**: January 15, 2025  
**Database**: `seeds_indent`  
**Status**: **TABLES CREATED SUCCESSFULLY**

---

## 📊 Created Tables

### 1. ✅ tbl_purchase_orders

- **Status**: Created
- **Rows**: 0 (empty)
- **Migration File**: `12_create_purchase_orders.sql`
- **Foreign Keys**:
  - `vendor_id` → `tbl_vendors(id)`
  - `indent_id` → `tbl_indent_master(indent_id)`

### 2. ✅ tbl_purchase_order_details

- **Status**: Created
- **Rows**: 0 (empty)
- **Migration File**: `13_create_po_details.sql`
- **Foreign Keys**:
  - `purchase_order_id` → `tbl_purchase_orders(id)` ON DELETE CASCADE

---

## 🔧 Database Configuration

**Connection Details** (from `database_detailes.txt`):

```
username = root
password = password
database = seeds_indent
```

---

## 📝 Migration Execution Log

### Step 1: Vendor Table (Already Exists)

```bash
✅ Table: tbl_vendors - ALREADY EXISTS
✅ Sample Data: 7 vendors (5 active, 1 inactive, 1 blacklisted)
```

### Step 2: Purchase Orders Table

```bash
✅ Command: mysql -u root -ppassword seeds_indent < database/12_create_purchase_orders.sql
✅ Result: Table created successfully
❌ Sample Data: Failed (invalid indent_id references)
   - Sample data references indent IDs that don't exist
   - Table structure is intact and ready for use
```

### Step 3: Purchase Order Details Table

```bash
✅ Command: mysql -u root -ppassword seeds_indent < database/13_create_po_details.sql
✅ Result: Table created successfully
❌ Sample Data: Failed (no parent PO records)
   - Sample data requires PO records to exist first
   - Table structure is intact and ready for use
```

---

## 🚀 How to Use

### Option 1: Use via Spring Boot Application (Recommended)

The tables are now ready. Use the REST APIs to create POs:

```bash
# 1. Get approved indents
GET /api/v1/po/approved-indents

# 2. Create PO from indent
POST /api/v1/po
Content-Type: application/json
{
  "indentId": <valid_indent_id>,
  "vendorId": <valid_vendor_id>,
  "lineItems": [...]
}
```

### Option 2: Insert Sample Data Manually

**Step 1: Find valid indent IDs**

```sql
SELECT indent_id, indent_no, indent_final_status
FROM tbl_indent_master
WHERE indent_final_status = 5
LIMIT 5;
```

**Step 2: Find valid vendor IDs**

```sql
SELECT id, vendor_code, vendor_name, status
FROM tbl_vendors
WHERE status = 1
LIMIT 5;
```

**Step 3: Insert sample PO**

```sql
INSERT INTO tbl_purchase_orders (
    po_number, po_date, indent_id, vendor_id,
    po_status, net_amount, created_by
) VALUES (
    'PO202501001',
    CURDATE(),
    <valid_indent_id>,
    <valid_vendor_id>,
    1, -- Draft
    100000.00,
    1
);
```

**Step 4: Insert line items**

```sql
INSERT INTO tbl_purchase_order_details (
    purchase_order_id, line_number, material_id,
    material_code, material_name, quantity,
    unit_of_measure, unit_price, line_total
) VALUES (
    LAST_INSERT_ID(),
    1,
    <valid_material_id>,
    'MAT001',
    'Sample Material',
    100,
    'KG',
    500.00,
    50000.00
);
```

---

## 🔍 Table Structure Verification

### Verify Tables Created

```sql
-- Check if tables exist
SHOW TABLES LIKE 'tbl_purchase%';

-- Check row counts
SELECT 'tbl_purchase_orders' AS table_name, COUNT(*) AS row_count
FROM tbl_purchase_orders
UNION ALL
SELECT 'tbl_purchase_order_details', COUNT(*)
FROM tbl_purchase_order_details;
```

### View Table Structure

```sql
-- Purchase Orders structure
DESCRIBE tbl_purchase_orders;

-- Purchase Order Details structure
DESCRIBE tbl_purchase_order_details;

-- Check indexes
SHOW INDEX FROM tbl_purchase_orders;
SHOW INDEX FROM tbl_purchase_order_details;

-- Check foreign keys
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'seeds_indent'
  AND TABLE_NAME IN ('tbl_purchase_orders', 'tbl_purchase_order_details')
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

---

## 📋 Table Relationships

```
tbl_indent_master (indent_id)
    ↓
tbl_purchase_orders (indent_id) ←── FK: fk_purchase_order_indent
    ↓ (id)
    ├── tbl_purchase_order_details (purchase_order_id) ←── FK: CASCADE DELETE
    │
    └── tbl_vendors (id) ←── FK: vendor_id
```

---

## ⚠️ Known Issues & Solutions

### Issue 1: Sample Data Foreign Key Violations

**Problem**: Sample INSERT statements reference non-existent records

```
ERROR 1452: Cannot add or update a child row: a foreign key constraint fails
```

**Solution**:

- Tables are created successfully ✅
- Sample data needs valid references
- Use Spring Boot APIs to create records (they handle relationships correctly)
- Or manually query for valid IDs before inserting

### Issue 2: Constraint Name Conflicts

**Problem**: Original migration used `fk_po_indent` which conflicted

```
ERROR 1826: Duplicate foreign key constraint name 'fk_po_indent'
```

**Solution**: ✅ Changed to `fk_purchase_order_indent` (unique name)

### Issue 3: Wrong Table Reference

**Problem**: Original migration referenced `tbl_indent` (doesn't exist)

```
ERROR 1824: Failed to open the referenced table 'tbl_indent'
```

**Solution**: ✅ Changed to `tbl_indent_master` (actual table name)

---

## 🎯 Next Steps

### 1. Start Spring Boot Application

```bash
cd backend
mvn spring-boot:run
```

### 2. Test PO Module APIs

Use the comprehensive documentation in:

- `backend/doc/WEEK4-6-PO-MODULE-COMPLETE.md`

### 3. Or Insert Test Data

Follow "How to Use > Option 2" above

---

## 📊 Database Schema Summary

| Table                      | Columns | Indexes      | Foreign Keys | Status     |
| -------------------------- | ------- | ------------ | ------------ | ---------- |
| tbl_purchase_orders        | 31      | 8            | 2            | ✅ Created |
| tbl_purchase_order_details | 25      | 5 + 1 UNIQUE | 1            | ✅ Created |

### Key Points:

- ✅ All table structures created successfully
- ✅ All indexes created
- ✅ All foreign key constraints active
- ✅ Ready for production use
- ⚠️ No sample data (will be created via APIs)

---

## 🔐 Security Notes

1. **Password Security**: The error messages show:

   ```
   mysql: [Warning] Using a password on the command line interface can be insecure.
   ```

   **Recommendation**: Use MySQL config file instead:

   ```bash
   # Create ~/.my.cnf
   [client]
   user=root
   password=password

   # Then run without -p flag
   mysql seeds_indent < migration.sql
   ```

2. **Foreign Key Constraints**: All tables have proper referential integrity
   - Cannot delete vendors with active POs
   - Deleting PO cascades to line items
   - Cannot delete indents with POs

---

## 📝 Migration Files Summary

### 11_create_vendors.sql

- ✅ Already executed (table exists with 7 records)
- Creates tbl_vendors table
- Inserts 7 sample vendors

### 12_create_purchase_orders.sql

- ✅ Table created successfully
- ❌ Sample data skipped (invalid references)
- **Fixed Issues**:
  - Changed `tbl_indent` → `tbl_indent_master`
  - Changed `fk_po_indent` → `fk_purchase_order_indent`

### 13_create_po_details.sql

- ✅ Table created successfully
- ❌ Sample data skipped (no parent POs)
- Ready for use

---

**Last Updated**: January 15, 2025  
**Author**: NSL India Development Team  
**Status**: ✅ **MIGRATION COMPLETE - READY FOR USE**
