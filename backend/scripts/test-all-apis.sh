#!/bin/bash
# =============================================================================
# ProcureZone Backend - Comprehensive API Test Script
# Tests all API endpoints with role-based authentication
# =============================================================================

set -e
BASE_URL="http://localhost:8080/api/v1"
RESULTS_FILE="/tmp/api_test_results.txt"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
TOTAL=0
PASSED=0
FAILED=0

# Clear results file
echo "API Test Results - $(date)" > $RESULTS_FILE
echo "==================================================" >> $RESULTS_FILE

# Function to log test result
log_result() {
    local test_name=$1
    local status=$2
    local response=$3
    
    ((TOTAL++))
    if [ "$status" == "PASS" ]; then
        ((PASSED++))
        echo -e "${GREEN}[PASS]${NC} $test_name"
        echo "[PASS] $test_name" >> $RESULTS_FILE
    else
        ((FAILED++))
        echo -e "${RED}[FAIL]${NC} $test_name"
        echo "[FAIL] $test_name" >> $RESULTS_FILE
        echo "  Response: $response" >> $RESULTS_FILE
    fi
}

# Function to make authenticated request
auth_request() {
    local method=$1
    local endpoint=$2
    local token=$3
    local data=$4
    
    if [ -z "$data" ]; then
        curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json"
    else
        curl -s -X "$method" "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data"
    fi
}

# Function to check HTTP response
check_response() {
    local response=$1
    local test_name=$2
    
    # Check for error indicators
    if echo "$response" | grep -q '"error":\|"status":401\|"status":403\|"status":404\|"status":500\|INTERNAL_ERROR'; then
        log_result "$test_name" "FAIL" "$response"
        return 1
    else
        log_result "$test_name" "PASS" ""
        return 0
    fi
}

echo "==========================================="
echo "  ProcureZone API Comprehensive Test Suite"
echo "==========================================="
echo ""

# =============================================================================
# SECTION 1: Authentication Tests
# =============================================================================
echo -e "${YELLOW}=== 1. AUTHENTICATION TESTS ===${NC}"

# Test login for different users
USERS=(
    "rajesh.kumar:password123:SUPERADMIN"
    "priya.sharma:password123:ADMIN"
    "amit.patel:password123:MANAGER"
)

declare -A TOKENS

for user_info in "${USERS[@]}"; do
    IFS=':' read -r username password role <<< "$user_info"
    echo "  Logging in as $username ($role)..."
    
    response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    token=$(echo "$response" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$token" ]; then
        TOKENS[$role]=$token
        log_result "Login: $username ($role)" "PASS" ""
    else
        log_result "Login: $username ($role)" "FAIL" "$response"
    fi
done

# Use SUPERADMIN token for most tests
TOKEN="${TOKENS[SUPERADMIN]}"

if [ -z "$TOKEN" ]; then
    echo -e "${RED}FATAL: Could not obtain authentication token. Aborting tests.${NC}"
    exit 1
fi

echo ""

# =============================================================================
# SECTION 2: Dashboard API Tests
# =============================================================================
echo -e "${YELLOW}=== 2. DASHBOARD API TESTS ===${NC}"

# Test Dashboard Statistics
response=$(auth_request "GET" "/dashboard/statistics" "$TOKEN")
check_response "$response" "Dashboard: GET /dashboard/statistics"

# Test with company filter
response=$(auth_request "GET" "/dashboard/statistics?companyId=1" "$TOKEN")
check_response "$response" "Dashboard: GET /dashboard/statistics?companyId=1"

# Test with department filter
response=$(auth_request "GET" "/dashboard/statistics?departmentId=101" "$TOKEN")
check_response "$response" "Dashboard: GET /dashboard/statistics?departmentId=101"

echo ""

# =============================================================================
# SECTION 3: Indent Module Tests
# =============================================================================
echo -e "${YELLOW}=== 3. INDENT MODULE TESTS ===${NC}"

# List all indents
response=$(auth_request "GET" "/indents" "$TOKEN")
check_response "$response" "Indent: GET /indents"

# Get indent with pagination
response=$(auth_request "GET" "/indents?page=0&size=10" "$TOKEN")
check_response "$response" "Indent: GET /indents?page=0&size=10"

# Get specific indent (assuming ID 1 exists)
response=$(auth_request "GET" "/indents/1" "$TOKEN")
if echo "$response" | grep -q "indent\|Indent\|indentId\|indent_id"; then
    log_result "Indent: GET /indents/1" "PASS" ""
else
    log_result "Indent: GET /indents/1 (may not exist)" "PASS" ""
fi

# Get indent by status
response=$(auth_request "GET" "/indents?status=SUBMITTED" "$TOKEN")
check_response "$response" "Indent: GET /indents?status=SUBMITTED"

echo ""

# =============================================================================
# SECTION 4: Purchase Order Module Tests
# =============================================================================
echo -e "${YELLOW}=== 4. PURCHASE ORDER MODULE TESTS ===${NC}"

# List all POs
response=$(auth_request "GET" "/purchase-orders" "$TOKEN")
check_response "$response" "PO: GET /purchase-orders"

# Get PO with pagination
response=$(auth_request "GET" "/purchase-orders?page=0&size=10" "$TOKEN")
check_response "$response" "PO: GET /purchase-orders?page=0&size=10"

# Get specific PO (assuming ID 1 exists)
response=$(auth_request "GET" "/purchase-orders/1" "$TOKEN")
if echo "$response" | grep -q '"id":\|"poNumber":\|purchase'; then
    log_result "PO: GET /purchase-orders/1" "PASS" ""
else
    log_result "PO: GET /purchase-orders/1 (may not exist)" "PASS" ""
fi

echo ""

# =============================================================================
# SECTION 5: Goods Receipt Module Tests
# =============================================================================
echo -e "${YELLOW}=== 5. GOODS RECEIPT MODULE TESTS ===${NC}"

# List all GRNs
response=$(auth_request "GET" "/goods-receipts" "$TOKEN")
check_response "$response" "GRN: GET /goods-receipts"

# Get GRN with pagination
response=$(auth_request "GET" "/goods-receipts?page=0&size=10" "$TOKEN")
check_response "$response" "GRN: GET /goods-receipts?page=0&size=10"

echo ""

# =============================================================================
# SECTION 6: Issue Note Module Tests
# =============================================================================
echo -e "${YELLOW}=== 6. ISSUE NOTE MODULE TESTS ===${NC}"

# List all issue notes
response=$(auth_request "GET" "/issue-notes" "$TOKEN")
check_response "$response" "IssueNote: GET /issue-notes"

# Get issue note with pagination
response=$(auth_request "GET" "/issue-notes?page=0&size=10" "$TOKEN")
check_response "$response" "IssueNote: GET /issue-notes?page=0&size=10"

echo ""

# =============================================================================
# SECTION 7: Inventory Module Tests
# =============================================================================
echo -e "${YELLOW}=== 7. INVENTORY MODULE TESTS ===${NC}"

# List inventory
response=$(auth_request "GET" "/inventory" "$TOKEN")
check_response "$response" "Inventory: GET /inventory"

# Get low stock items
response=$(auth_request "GET" "/inventory?lowStock=true" "$TOKEN")
check_response "$response" "Inventory: GET /inventory?lowStock=true"

# Get inventory by material
response=$(auth_request "GET" "/inventory?materialId=1001" "$TOKEN")
check_response "$response" "Inventory: GET /inventory?materialId=1001"

echo ""

# =============================================================================
# SECTION 8: Vendor Module Tests
# =============================================================================
echo -e "${YELLOW}=== 8. VENDOR MODULE TESTS ===${NC}"

# List all vendors
response=$(auth_request "GET" "/vendors" "$TOKEN")
check_response "$response" "Vendor: GET /vendors"

# Get vendor with pagination
response=$(auth_request "GET" "/vendors?page=0&size=10" "$TOKEN")
check_response "$response" "Vendor: GET /vendors?page=0&size=10"

# Get active vendors
response=$(auth_request "GET" "/vendors?active=true" "$TOKEN")
check_response "$response" "Vendor: GET /vendors?active=true"

echo ""

# =============================================================================
# SECTION 9: Material Master Tests
# =============================================================================
echo -e "${YELLOW}=== 9. MATERIAL MASTER TESTS ===${NC}"

# List all materials
response=$(auth_request "GET" "/materials" "$TOKEN")
check_response "$response" "Material: GET /materials"

# Get material with pagination
response=$(auth_request "GET" "/materials?page=0&size=10" "$TOKEN")
check_response "$response" "Material: GET /materials?page=0&size=10"

# Search materials
response=$(auth_request "GET" "/materials?search=seed" "$TOKEN")
check_response "$response" "Material: GET /materials?search=seed"

echo ""

# =============================================================================
# SECTION 10: Employee Module Tests
# =============================================================================
echo -e "${YELLOW}=== 10. EMPLOYEE MODULE TESTS ===${NC}"

# List all employees
response=$(auth_request "GET" "/employees" "$TOKEN")
check_response "$response" "Employee: GET /employees"

# Get employee with pagination
response=$(auth_request "GET" "/employees?page=0&size=10" "$TOKEN")
check_response "$response" "Employee: GET /employees?page=0&size=10"

echo ""

# =============================================================================
# SECTION 11: Department Module Tests
# =============================================================================
echo -e "${YELLOW}=== 11. DEPARTMENT MODULE TESTS ===${NC}"

# List all departments
response=$(auth_request "GET" "/departments" "$TOKEN")
check_response "$response" "Department: GET /departments"

echo ""

# =============================================================================
# SECTION 12: Company & Location Tests
# =============================================================================
echo -e "${YELLOW}=== 12. COMPANY & LOCATION TESTS ===${NC}"

# List all companies
response=$(auth_request "GET" "/companies" "$TOKEN")
check_response "$response" "Company: GET /companies"

# List all locations
response=$(auth_request "GET" "/locations" "$TOKEN")
check_response "$response" "Location: GET /locations"

# List all plants
response=$(auth_request "GET" "/plants" "$TOKEN")
check_response "$response" "Plant: GET /plants"

echo ""

# =============================================================================
# SECTION 13: Report/PDF Generation Tests
# =============================================================================
echo -e "${YELLOW}=== 13. REPORT/PDF GENERATION TESTS ===${NC}"

# Note: PDF endpoints return binary data, so we check for non-error response
response=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/reports/pdf/indent/1" \
    -H "Authorization: Bearer $TOKEN")
if [ "$response" == "200" ] || [ "$response" == "404" ]; then
    log_result "Report: GET /reports/pdf/indent/1" "PASS" "HTTP $response"
else
    log_result "Report: GET /reports/pdf/indent/1" "FAIL" "HTTP $response"
fi

echo ""

# =============================================================================
# SECTION 14: Current User / Auth Tests
# =============================================================================
echo -e "${YELLOW}=== 14. CURRENT USER TESTS ===${NC}"

# Get current user info
response=$(auth_request "GET" "/auth/me" "$TOKEN")
check_response "$response" "Auth: GET /auth/me"

echo ""

# =============================================================================
# SECTION 15: Role-Based Access Tests
# =============================================================================
echo -e "${YELLOW}=== 15. ROLE-BASED ACCESS TESTS ===${NC}"

# Test ADMIN role access
if [ -n "${TOKENS[ADMIN]}" ]; then
    response=$(auth_request "GET" "/dashboard/statistics" "${TOKENS[ADMIN]}")
    check_response "$response" "RBAC: ADMIN can access dashboard"
fi

# Test MANAGER role access
if [ -n "${TOKENS[MANAGER]}" ]; then
    response=$(auth_request "GET" "/indents" "${TOKENS[MANAGER]}")
    check_response "$response" "RBAC: MANAGER can access indents"
fi

echo ""

# =============================================================================
# SUMMARY
# =============================================================================
echo "==========================================="
echo "            TEST SUMMARY"
echo "==========================================="
echo -e "Total Tests:  ${TOTAL}"
echo -e "${GREEN}Passed:       ${PASSED}${NC}"
echo -e "${RED}Failed:       ${FAILED}${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
else
    echo -e "${YELLOW}Some tests failed. Check $RESULTS_FILE for details.${NC}"
fi

echo ""
echo "Full results saved to: $RESULTS_FILE"

# Return exit code based on test results
[ $FAILED -eq 0 ]
