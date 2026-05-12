#!/bin/bash
# ProcureZone API Test Script
# Tests all API endpoints folder by folder

BASE_URL="http://localhost:8080/api/v1"
RESULTS_FILE="/tmp/api_test_results.txt"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
PASS=0
FAIL=0
BUGS=()

log_result() {
  local test_name=$1
  local status=$2
  local details=$3
  
  if [[ "$status" == "PASS" ]]; then
    echo -e "${GREEN}✅ PASS${NC} - $test_name"
    ((PASS++))
  else
    echo -e "${RED}❌ FAIL${NC} - $test_name"
    echo "   Details: $details"
    ((FAIL++))
    BUGS+=("$test_name: $details")
  fi
}

# Get admin token
get_token() {
  local response=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"priya.sharma","password":"password123"}' 2>/dev/null)
  echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4
}

echo "=============================================="
echo "       ProcureZone API Test Suite"
echo "=============================================="
echo ""

# Get token first
echo "Getting authentication token..."
TOKEN=$(get_token)
if [[ -z "$TOKEN" ]]; then
  echo -e "${RED}FATAL: Could not get auth token${NC}"
  exit 1
fi
echo -e "${GREEN}Token obtained successfully${NC}"
echo ""

# ===============================
# 01-AUTHENTICATION TESTS
# ===============================
echo "=== 01-AUTHENTICATION TESTS ==="

# AUTH-01: Login Success
response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"priya.sharma","password":"password123"}' 2>/dev/null)
if echo "$response" | grep -q '"accessToken"'; then
  log_result "AUTH-01: Login Success" "PASS"
else
  log_result "AUTH-01: Login Success" "FAIL" "No token returned"
fi

# AUTH-02: Login Invalid Password
response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"priya.sharma","password":"wrongpassword"}' 2>/dev/null)
if echo "$response" | grep -qi "error\|invalid\|unauthorized"; then
  log_result "AUTH-02: Login Invalid Password" "PASS"
else
  log_result "AUTH-02: Login Invalid Password" "FAIL" "Should reject: $response"
fi

# AUTH-03: Login Non-existent User
response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"nobody.exists","password":"password123"}' 2>/dev/null)
if echo "$response" | grep -qi "error\|invalid\|not found"; then
  log_result "AUTH-03: Login Non-existent User" "PASS"
else
  log_result "AUTH-03: Login Non-existent User" "FAIL" "Should reject: $response"
fi

# AUTH-04: Login Empty Payload
response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{}' 2>/dev/null)
if echo "$response" | grep -qi "error\|required\|invalid\|bad"; then
  log_result "AUTH-04: Login Empty Payload" "PASS"
else
  log_result "AUTH-04: Login Empty Payload" "FAIL" "Should reject empty: $response"
fi

# AUTH-05: Get Current User
response=$(curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" 2>/dev/null)
if echo "$response" | grep -qi "priya\|sharma\|EMP002"; then
  log_result "AUTH-05: Get Current User" "PASS"
else
  log_result "AUTH-05: Get Current User" "FAIL" "Response: $response"
fi

echo ""

# ===============================
# 02-USERS TESTS
# ===============================
echo "=== 02-USERS TESTS ==="

# USER-01: Get All Users
response=$(curl -s -X GET "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|userId\|\[\]"; then
  log_result "USER-01: Get All Users" "PASS"
else
  log_result "USER-01: Get All Users" "FAIL" "Response: ${response:0:100}"
fi

# USER-02: Get User by ID
response=$(curl -s -X GET "$BASE_URL/users/2" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -qi "userId\|priya"; then
  log_result "USER-02: Get User by ID (2)" "PASS"
else
  log_result "USER-02: Get User by ID (2)" "FAIL" "Response: ${response:0:100}"
fi

# USER-03: Get User by Username
response=$(curl -s -X GET "$BASE_URL/users/username/priya.sharma" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -qi "userId\|priya"; then
  log_result "USER-03: Get User by Username" "PASS"
else
  log_result "USER-03: Get User by Username" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 03-EMPLOYEES TESTS
# ===============================
echo "=== 03-EMPLOYEES TESTS ==="

# EMP-01: Get All Employees
response=$(curl -s -X GET "$BASE_URL/employees" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|employeeId\|\[\]"; then
  log_result "EMP-01: Get All Employees" "PASS"
else
  log_result "EMP-01: Get All Employees" "FAIL" "Response: ${response:0:100}"
fi

# EMP-02: Get Employee by ID
response=$(curl -s -X GET "$BASE_URL/employees/2" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -qi "employeeId\|EMP"; then
  log_result "EMP-02: Get Employee by ID (2)" "PASS"
else
  log_result "EMP-02: Get Employee by ID (2)" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 05-COMPANIES TESTS
# ===============================
echo "=== 05-COMPANIES TESTS ==="

# COMP-01: Get All Companies
response=$(curl -s -X GET "$BASE_URL/companies" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|companyId\|companyCode\|\[\]"; then
  log_result "COMP-01: Get All Companies" "PASS"
else
  log_result "COMP-01: Get All Companies" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 06-MATERIALS TESTS
# ===============================
echo "=== 06-MATERIALS TESTS ==="

# MAT-01: Get All Materials
response=$(curl -s -X GET "$BASE_URL/materials" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|materialId\|materialCode\|\[\]"; then
  log_result "MAT-01: Get All Materials" "PASS"
else
  log_result "MAT-01: Get All Materials" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 07-DEPARTMENTS TESTS
# ===============================
echo "=== 07-DEPARTMENTS TESTS ==="

# DEPT-01: Get All Departments
response=$(curl -s -X GET "$BASE_URL/departments" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|departmentId\|deptCode\|\[\]"; then
  log_result "DEPT-01: Get All Departments" "PASS"
else
  log_result "DEPT-01: Get All Departments" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 09-PLANTS TESTS
# ===============================
echo "=== 09-PLANTS TESTS ==="

# PLANT-01: Get All Plants
response=$(curl -s -X GET "$BASE_URL/plants" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|plantId\|plantCode\|\[\]"; then
  log_result "PLANT-01: Get All Plants" "PASS"
else
  log_result "PLANT-01: Get All Plants" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 10-LOCATIONS TESTS
# ===============================
echo "=== 10-LOCATIONS TESTS ==="

# LOC-01: Get All Locations
response=$(curl -s -X GET "$BASE_URL/locations" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|locationId\|locationCode\|\[\]"; then
  log_result "LOC-01: Get All Locations" "PASS"
else
  log_result "LOC-01: Get All Locations" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 11-UOMs TESTS
# ===============================
echo "=== 11-UOMs TESTS ==="

# UOM-01: Get All UOMs
response=$(curl -s -X GET "$BASE_URL/uoms" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|uomId\|uomCode\|\[\]"; then
  log_result "UOM-01: Get All UOMs" "PASS"
else
  log_result "UOM-01: Get All UOMs" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 12-VENDORS TESTS
# ===============================
echo "=== 12-VENDORS TESTS ==="

# VENDOR-01: Get All Vendors
response=$(curl -s -X GET "$BASE_URL/vendors" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|vendorId\|vendorCode\|\[\]"; then
  log_result "VENDOR-01: Get All Vendors" "PASS"
else
  log_result "VENDOR-01: Get All Vendors" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 13-INDENTS TESTS
# ===============================
echo "=== 13-INDENTS TESTS ==="

# IND-01: Get All Indents
response=$(curl -s -X GET "$BASE_URL/indents" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|indentId\|indentNumber\|\[\]"; then
  log_result "IND-01: Get All Indents" "PASS"
else
  log_result "IND-01: Get All Indents" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 14-PURCHASE-ORDERS TESTS
# ===============================
echo "=== 14-PURCHASE-ORDERS TESTS ==="

# PO-01: Get All Purchase Orders
response=$(curl -s -X GET "$BASE_URL/purchase-orders" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|poId\|poNumber\|\[\]"; then
  log_result "PO-01: Get All Purchase Orders" "PASS"
else
  log_result "PO-01: Get All Purchase Orders" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 15-GRN TESTS
# ===============================
echo "=== 15-GRN TESTS ==="

# GRN-01: Get All GRNs
response=$(curl -s -X GET "$BASE_URL/grn" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|grnId\|grnNumber\|\[\]"; then
  log_result "GRN-01: Get All GRNs" "PASS"
else
  log_result "GRN-01: Get All GRNs" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 16-ISSUE-NOTES TESTS
# ===============================
echo "=== 16-ISSUE-NOTES TESTS ==="

# ISN-01: Get All Issue Notes
response=$(curl -s -X GET "$BASE_URL/issue-notes" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|issueNoteId\|issueNumber\|\[\]"; then
  log_result "ISN-01: Get All Issue Notes" "PASS"
else
  log_result "ISN-01: Get All Issue Notes" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 17-INVENTORY TESTS
# ===============================
echo "=== 17-INVENTORY TESTS ==="

# INV-01: Get All Inventory
response=$(curl -s -X GET "$BASE_URL/inventory" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -q "content\|inventoryId\|quantity\|\[\]"; then
  log_result "INV-01: Get All Inventory" "PASS"
else
  log_result "INV-01: Get All Inventory" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# 18-DASHBOARD TESTS
# ===============================
echo "=== 18-DASHBOARD TESTS ==="

# DASH-01: Get Dashboard Summary
response=$(curl -s -X GET "$BASE_URL/dashboard/summary" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
if echo "$response" | grep -qi "indent\|pending\|total\|count"; then
  log_result "DASH-01: Dashboard Summary" "PASS"
else
  log_result "DASH-01: Dashboard Summary" "FAIL" "Response: ${response:0:100}"
fi

echo ""

# ===============================
# SUMMARY
# ===============================
echo "=============================================="
echo "              TEST SUMMARY"
echo "=============================================="
echo -e "Total Tests: $((PASS + FAIL))"
echo -e "${GREEN}Passed: $PASS${NC}"
echo -e "${RED}Failed: $FAIL${NC}"
echo ""

if [[ ${#BUGS[@]} -gt 0 ]]; then
  echo "BUGS/ISSUES FOUND:"
  for bug in "${BUGS[@]}"; do
    echo "  - $bug"
  done
fi

echo ""
echo "Test completed at: $(date)"
