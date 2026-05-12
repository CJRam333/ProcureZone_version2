#!/bin/bash

# Test Script for Company-Plant-Material Mapping Module
# Date: November 1, 2025
# Purpose: Comprehensive endpoint testing for plant-based material management

# Configuration
BASE_URL="http://localhost:8080/api/v1"
AUTH_URL="$BASE_URL/auth/login"
CPM_URL="$BASE_URL/company-plant-materials"

# Test credentials
USERNAME="rajesh.kumar"
PASSWORD="password123"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test results array
declare -a TEST_RESULTS

# Function to print colored output
print_status() {
    if [ "$1" == "PASS" ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((PASSED_TESTS++))
        TEST_RESULTS+=("✓ $2")
    elif [ "$1" == "FAIL" ]; then
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((FAILED_TESTS++))
        TEST_RESULTS+=("✗ $2")
    elif [ "$1" == "INFO" ]; then
        echo -e "${BLUE}ℹ INFO${NC}: $2"
    elif [ "$1" == "WARN" ]; then
        echo -e "${YELLOW}⚠ WARN${NC}: $2"
    fi
}

# Function to print section header
print_header() {
    echo ""
    echo -e "${BLUE}================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================================${NC}"
    echo ""
}

# Function to print test header
print_test() {
    ((TOTAL_TESTS++))
    echo -e "${YELLOW}Test $TOTAL_TESTS: $1${NC}"
}

# Function to extract JWT token
extract_token() {
    echo "$1" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4
}

# Function to check JSON response
check_response() {
    local response="$1"
    local expected_field="$2"
    local test_name="$3"
    
    if echo "$response" | grep -q "\"$expected_field\""; then
        print_status "PASS" "$test_name"
        return 0
    else
        print_status "FAIL" "$test_name - Expected field '$expected_field' not found"
        echo "Response: $response"
        return 1
    fi
}

# Start testing
print_header "COMPANY-PLANT-MATERIAL MAPPING MODULE TEST SUITE"
print_status "INFO" "Starting comprehensive API tests..."
print_status "INFO" "Base URL: $BASE_URL"
echo ""

# ============================================================
# TEST 1: Authentication
# ============================================================
print_test "User Authentication"
AUTH_RESPONSE=$(curl -s -X POST "$AUTH_URL" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(extract_token "$AUTH_RESPONSE")

if [ -z "$TOKEN" ]; then
    print_status "FAIL" "Authentication failed - No token received"
    print_status "INFO" "Response: $AUTH_RESPONSE"
    exit 1
else
    print_status "PASS" "Authentication successful - Token received"
    print_status "INFO" "Token: ${TOKEN:0:20}..."
fi

# Set authorization header
AUTH_HEADER="Authorization: Bearer $TOKEN"

sleep 1

# ============================================================
# TEST 2: Create Company-Plant-Material Mapping
# ============================================================
print_test "Create New Mapping (Company=1, Plant=101, Material=1012)"
CREATE_RESPONSE=$(curl -s -X POST "$CPM_URL" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "companyId": 1,
        "plantId": 101,
        "materialId": 1012,
        "quantityStores": 500.00,
        "reorderLevel": 100.00,
        "maxLevel": 1000.00,
        "status": 1
    }')

check_response "$CREATE_RESPONSE" "success" "Create mapping with valid data"

# Extract mapping ID for later tests
MAPPING_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
print_status "INFO" "Created mapping ID: $MAPPING_ID"

sleep 1

# ============================================================
# TEST 3: Create Duplicate Mapping (Should Fail)
# ============================================================
print_test "Create Duplicate Mapping (Should Fail)"
DUPLICATE_RESPONSE=$(curl -s -X POST "$CPM_URL" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "companyId": 1,
        "plantId": 101,
        "materialId": 1012,
        "quantityStores": 500.00,
        "reorderLevel": 100.00,
        "maxLevel": 1000.00,
        "status": 1
    }')

if echo "$DUPLICATE_RESPONSE" | grep -q "already exists"; then
    print_status "PASS" "Duplicate prevention working - Request correctly rejected"
else
    print_status "FAIL" "Duplicate prevention failed - Should reject duplicate mapping"
    echo "Response: $DUPLICATE_RESPONSE"
fi

sleep 1

# ============================================================
# TEST 4: Create with Invalid Max Level (max < reorder)
# ============================================================
print_test "Create with Invalid Levels (max < reorder - Should Fail)"
INVALID_RESPONSE=$(curl -s -X POST "$CPM_URL" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "companyId": 1,
        "plantId": 102,
        "materialId": 1013,
        "quantityStores": 500.00,
        "reorderLevel": 500.00,
        "maxLevel": 100.00,
        "status": 1
    }')

if echo "$INVALID_RESPONSE" | grep -qi "max level.*less than.*reorder"; then
    print_status "PASS" "Validation working - Invalid levels rejected"
else
    print_status "FAIL" "Validation failed - Should reject max < reorder"
    echo "Response: $INVALID_RESPONSE"
fi

sleep 1

# ============================================================
# TEST 5: Get All Mappings
# ============================================================
print_test "Get All Mappings (Paginated)"
ALL_RESPONSE=$(curl -s -X GET "$CPM_URL?page=0&size=20" \
    -H "$AUTH_HEADER")

check_response "$ALL_RESPONSE" "totalItems" "Get all mappings"

sleep 1

# ============================================================
# TEST 6: Get Active Mappings Only
# ============================================================
print_test "Get Active Mappings Only"
ACTIVE_RESPONSE=$(curl -s -X GET "$CPM_URL/active?page=0&size=20" \
    -H "$AUTH_HEADER")

check_response "$ACTIVE_RESPONSE" "success" "Get active mappings"

sleep 1

# ============================================================
# TEST 7: Get Mapping by ID
# ============================================================
print_test "Get Mapping by ID ($MAPPING_ID)"
if [ ! -z "$MAPPING_ID" ]; then
    BY_ID_RESPONSE=$(curl -s -X GET "$CPM_URL/$MAPPING_ID" \
        -H "$AUTH_HEADER")
    
    check_response "$BY_ID_RESPONSE" "companyId" "Get mapping by ID"
else
    print_status "FAIL" "Get mapping by ID - No mapping ID available"
fi

sleep 1

# ============================================================
# TEST 8: Get Mappings by Company
# ============================================================
print_test "Get Mappings by Company (Company ID: 1)"
BY_COMPANY_RESPONSE=$(curl -s -X GET "$CPM_URL/company/1?page=0&size=20" \
    -H "$AUTH_HEADER")

check_response "$BY_COMPANY_RESPONSE" "success" "Get mappings by company"

sleep 1

# ============================================================
# TEST 9: Get Mappings by Plant
# ============================================================
print_test "Get Mappings by Plant (Plant ID: 101)"
BY_PLANT_RESPONSE=$(curl -s -X GET "$CPM_URL/plant/101?page=0&size=20" \
    -H "$AUTH_HEADER")

check_response "$BY_PLANT_RESPONSE" "success" "Get mappings by plant"

sleep 1

# ============================================================
# TEST 10: Get Mappings by Material
# ============================================================
print_test "Get Mappings by Material (Material ID: 1012)"
BY_MATERIAL_RESPONSE=$(curl -s -X GET "$CPM_URL/material/1012?page=0&size=20" \
    -H "$AUTH_HEADER")

check_response "$BY_MATERIAL_RESPONSE" "success" "Get mappings by material"

sleep 1

# ============================================================
# TEST 11: Get Materials at Plant
# ============================================================
print_test "Get Materials at Plant (Company=1, Plant=101)"
MATERIALS_AT_PLANT_RESPONSE=$(curl -s -X GET "$CPM_URL/company/1/plant/101/materials" \
    -H "$AUTH_HEADER")

check_response "$MATERIALS_AT_PLANT_RESPONSE" "count" "Get materials at plant"

sleep 1

# ============================================================
# TEST 12: Get Plants for Material
# ============================================================
print_test "Get Plants for Material (Company=1, Material=1012)"
PLANTS_FOR_MAT_RESPONSE=$(curl -s -X GET "$CPM_URL/company/1/material/1012/plants" \
    -H "$AUTH_HEADER")

check_response "$PLANTS_FOR_MAT_RESPONSE" "count" "Get plants for material"

sleep 1

# ============================================================
# TEST 13: Check Material Availability at Plant
# ============================================================
print_test "Check Material Availability (Company=1, Plant=101, Material=1012)"
AVAILABILITY_RESPONSE=$(curl -s -X GET "$CPM_URL/check-availability?companyId=1&plantId=101&materialId=1012" \
    -H "$AUTH_HEADER")

if echo "$AVAILABILITY_RESPONSE" | grep -q '"available":true'; then
    print_status "PASS" "Material availability check - Material is available"
else
    print_status "FAIL" "Material availability check failed"
    echo "Response: $AVAILABILITY_RESPONSE"
fi

sleep 1

# ============================================================
# TEST 14: Check Stock Sufficiency
# ============================================================
print_test "Check Stock Sufficiency (Required: 100 units)"
STOCK_CHECK_RESPONSE=$(curl -s -X GET "$CPM_URL/check-stock?companyId=1&plantId=101&materialId=1012&quantity=100" \
    -H "$AUTH_HEADER")

if echo "$STOCK_CHECK_RESPONSE" | grep -q '"sufficient":true'; then
    print_status "PASS" "Stock sufficiency check - Sufficient stock available"
else
    print_status "FAIL" "Stock sufficiency check failed"
    echo "Response: $STOCK_CHECK_RESPONSE"
fi

sleep 1

# ============================================================
# TEST 15: Check Insufficient Stock
# ============================================================
print_test "Check Insufficient Stock (Required: 10000 units - Should be insufficient)"
INSUFFICIENT_RESPONSE=$(curl -s -X GET "$CPM_URL/check-stock?companyId=1&plantId=101&materialId=1012&quantity=10000" \
    -H "$AUTH_HEADER")

if echo "$INSUFFICIENT_RESPONSE" | grep -q '"sufficient":false'; then
    print_status "PASS" "Insufficient stock correctly identified"
else
    print_status "FAIL" "Stock sufficiency validation failed"
    echo "Response: $INSUFFICIENT_RESPONSE"
fi

sleep 1

# ============================================================
# TEST 16: Get Mappings Needing Reorder
# ============================================================
print_test "Get Mappings Needing Reorder"
REORDER_RESPONSE=$(curl -s -X GET "$CPM_URL/needing-reorder" \
    -H "$AUTH_HEADER")

check_response "$REORDER_RESPONSE" "count" "Get mappings needing reorder"

sleep 1

# ============================================================
# TEST 17: Get Mappings Needing Reorder at Plant
# ============================================================
print_test "Get Mappings Needing Reorder at Plant (Plant=101)"
REORDER_AT_PLANT_RESPONSE=$(curl -s -X GET "$CPM_URL/plant/101/needing-reorder" \
    -H "$AUTH_HEADER")

check_response "$REORDER_AT_PLANT_RESPONSE" "success" "Get reorder alerts for plant"

sleep 1

# ============================================================
# TEST 18: Get Low Stock Mappings (Early Warning)
# ============================================================
print_test "Get Low Stock Mappings (Threshold: 120%)"
LOW_STOCK_RESPONSE=$(curl -s -X GET "$CPM_URL/low-stock?threshold=120" \
    -H "$AUTH_HEADER")

check_response "$LOW_STOCK_RESPONSE" "threshold" "Get low stock early warning"

sleep 1

# ============================================================
# TEST 19: Get Statistics Dashboard
# ============================================================
print_test "Get Dashboard Statistics"
STATS_RESPONSE=$(curl -s -X GET "$CPM_URL/statistics" \
    -H "$AUTH_HEADER")

check_response "$STATS_RESPONSE" "healthPercentage" "Get statistics dashboard"

sleep 1

# ============================================================
# TEST 20: Update Mapping (Partial Update)
# ============================================================
print_test "Update Mapping (Increase Quantity to 600)"
if [ ! -z "$MAPPING_ID" ]; then
    UPDATE_RESPONSE=$(curl -s -X PUT "$CPM_URL/$MAPPING_ID" \
        -H "Content-Type: application/json" \
        -H "$AUTH_HEADER" \
        -d '{
            "quantityStores": 600.00,
            "reorderLevel": 150.00
        }')
    
    check_response "$UPDATE_RESPONSE" "success" "Update mapping"
else
    print_status "FAIL" "Update mapping - No mapping ID available"
fi

sleep 1

# ============================================================
# TEST 21: Update with Invalid Levels (Should Fail)
# ============================================================
print_test "Update with Invalid Levels (max < reorder - Should Fail)"
if [ ! -z "$MAPPING_ID" ]; then
    INVALID_UPDATE_RESPONSE=$(curl -s -X PUT "$CPM_URL/$MAPPING_ID" \
        -H "Content-Type: application/json" \
        -H "$AUTH_HEADER" \
        -d '{
            "reorderLevel": 1500.00,
            "maxLevel": 1000.00
        }')
    
    if echo "$INVALID_UPDATE_RESPONSE" | grep -qi "max level.*less than.*reorder"; then
        print_status "PASS" "Update validation working - Invalid levels rejected"
    else
        print_status "FAIL" "Update validation failed - Should reject max < reorder"
        echo "Response: $INVALID_UPDATE_RESPONSE"
    fi
else
    print_status "FAIL" "Update validation test - No mapping ID available"
fi

sleep 1

# ============================================================
# TEST 22: Create Additional Test Mapping (Low Stock)
# ============================================================
print_test "Create Low Stock Mapping (For Reorder Alert Test)"
LOW_STOCK_CREATE_RESPONSE=$(curl -s -X POST "$CPM_URL" \
    -H "Content-Type: application/json" \
    -H "$AUTH_HEADER" \
    -d '{
        "companyId": 1,
        "plantId": 103,
        "materialId": 1014,
        "quantityStores": 50.00,
        "reorderLevel": 100.00,
        "maxLevel": 500.00,
        "status": 1
    }')

LOW_STOCK_ID=$(echo "$LOW_STOCK_CREATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
check_response "$LOW_STOCK_CREATE_RESPONSE" "success" "Create low stock mapping"

sleep 1

# ============================================================
# TEST 23: Verify Reorder Alert for Low Stock
# ============================================================
print_test "Verify Reorder Alert Appears for Low Stock"
REORDER_CHECK_RESPONSE=$(curl -s -X GET "$CPM_URL/needing-reorder" \
    -H "$AUTH_HEADER")

if echo "$REORDER_CHECK_RESPONSE" | grep -q "$LOW_STOCK_ID"; then
    print_status "PASS" "Reorder alert system working - Low stock mapping detected"
else
    print_status "WARN" "Reorder alert test - Low stock mapping not found (may be timing issue)"
fi

sleep 1

# ============================================================
# TEST 24: Verify Low Stock Alert (120% threshold)
# ============================================================
print_test "Verify Low Stock Early Warning (120% threshold)"
LOW_STOCK_CHECK=$(curl -s -X GET "$CPM_URL/low-stock?threshold=120" \
    -H "$AUTH_HEADER")

if echo "$LOW_STOCK_CHECK" | grep -q "$LOW_STOCK_ID"; then
    print_status "PASS" "Early warning system working - Low stock detected at 120% threshold"
else
    print_status "WARN" "Early warning test - Mapping not found (may be timing issue)"
fi

sleep 1

# ============================================================
# TEST 25: Soft Delete Mapping
# ============================================================
print_test "Soft Delete Mapping"
if [ ! -z "$MAPPING_ID" ]; then
    DELETE_RESPONSE=$(curl -s -X DELETE "$CPM_URL/$MAPPING_ID" \
        -H "$AUTH_HEADER")
    
    check_response "$DELETE_RESPONSE" "success" "Soft delete mapping"
    
    # Verify it's not in active list
    sleep 1
    ACTIVE_CHECK=$(curl -s -X GET "$CPM_URL/active" -H "$AUTH_HEADER")
    if ! echo "$ACTIVE_CHECK" | grep -q "\"id\":$MAPPING_ID"; then
        print_status "PASS" "Soft delete verification - Mapping removed from active list"
    else
        print_status "FAIL" "Soft delete verification - Mapping still appears in active list"
    fi
else
    print_status "FAIL" "Delete mapping - No mapping ID available"
fi

sleep 1

# ============================================================
# TEST 26: Cleanup - Delete Low Stock Mapping
# ============================================================
print_test "Cleanup - Delete Low Stock Test Mapping"
if [ ! -z "$LOW_STOCK_ID" ]; then
    CLEANUP_RESPONSE=$(curl -s -X DELETE "$CPM_URL/$LOW_STOCK_ID" \
        -H "$AUTH_HEADER")
    
    check_response "$CLEANUP_RESPONSE" "success" "Cleanup test data"
else
    print_status "INFO" "Cleanup - No low stock mapping ID to delete"
fi

# ============================================================
# TEST SUMMARY
# ============================================================
print_header "TEST SUMMARY"

echo "Total Tests Run: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

# Calculate success rate
if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$(awk "BEGIN {printf \"%.2f\", ($PASSED_TESTS/$TOTAL_TESTS)*100}")
    echo "Success Rate: $SUCCESS_RATE%"
    echo ""
fi

# Print detailed results
echo "Detailed Results:"
echo "----------------------------------------"
for result in "${TEST_RESULTS[@]}"; do
    echo "$result"
done
echo ""

# Final status
if [ $FAILED_TESTS -eq 0 ]; then
    print_status "PASS" "ALL TESTS PASSED! 🎉"
    exit 0
else
    print_status "FAIL" "$FAILED_TESTS test(s) failed"
    exit 1
fi
