#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base URL
BASE_URL="http://localhost:8080/api/v1"
AUTH_URL="http://localhost:8080/api/v1/auth"

# Test credentials
USERNAME="rajesh.kumar"
PASSWORD="password123"

# Counter for tests
PASSED=0
FAILED=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Issue Note Module Test Suite${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to make API calls
call_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    
    if [ -z "$data" ]; then
        curl -s -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            "$endpoint"
    else
        curl -s -X "$method" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $token" \
            -d "$data" \
            "$endpoint"
    fi
}

# Function to check response
check_response() {
    local response=$1
    local test_name=$2
    
    if echo "$response" | grep -q '"success":true\|"content"\|\['; then
        echo -e "${GREEN}✓ PASS${NC}: $test_name"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC}: $test_name"
        echo -e "${YELLOW}Response: $response${NC}"
        ((FAILED++))
        return 1
    fi
}

# Test 1: Login and get token
echo -e "\n${BLUE}Test 1: Authentication${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$AUTH_URL/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ FAIL: Authentication failed${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo -e "${GREEN}✓ PASS: Authentication successful${NC}"
echo "Token: ${TOKEN:0:50}..."
((PASSED++))

# Get employee number from token payload
EMPLOYEE_NUM=$(echo "$TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | grep -o '"employeeNumber":[0-9]*' | cut -d':' -f2)
echo "Employee Number: $EMPLOYEE_NUM"

# Initialize test stock data
echo -e "\n${BLUE}Initializing Test Stock Data${NC}"
INIT_STOCK_RESPONSE=$(call_api "POST" "$BASE_URL/inventory/init-test-stock" "" "$TOKEN")
if echo "$INIT_STOCK_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✓${NC} Stock initialized successfully"
else
    echo -e "${YELLOW}⚠${NC} Stock initialization response: $INIT_STOCK_RESPONSE"
fi

# Test 2: Get Statistics (Dashboard)
echo -e "\n${BLUE}Test 2: Get Issue Note Statistics${NC}"
STATS_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/statistics" "" "$TOKEN")
check_response "$STATS_RESPONSE" "Get Statistics"

# Test 3: Create Issue Note
echo -e "\n${BLUE}Test 3: Create New Issue Note${NC}"
CREATE_DATA='{
  "companyId": 1,
  "departmentId": 101,
  "sectionId": 401,
  "plantId": 301,
  "issuedTo": "Production Department",
  "purpose": "Monthly material requirement for production",
  "comments": "Urgent requirement",
  "lineItems": [
    {
      "materialId": 1012,
      "unitOfMeasureId": 501,
      "quantity": 100,
      "rate": 50.00,
      "purpose": "Assembly line requirement"
    },
    {
      "materialId": 1003,
      "unitOfMeasureId": 502,
      "quantity": 50,
      "rate": 75.00,
      "purpose": "Quality testing"
    }
  ]
}'

CREATE_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes" "$CREATE_DATA" "$TOKEN")
if check_response "$CREATE_RESPONSE" "Create Issue Note"; then
    ISSUE_NOTE_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    ISSUE_NOTE_NUMBER=$(echo "$CREATE_RESPONSE" | grep -o '"issueNoteNumber":"[^"]*"' | cut -d'"' -f4)
    echo "Created Issue Note ID: $ISSUE_NOTE_ID"
    echo "Issue Note Number: $ISSUE_NOTE_NUMBER"
fi

# Test 4: Get All Issue Notes
echo -e "\n${BLUE}Test 4: Get All Issue Notes (Paginated)${NC}"
ALL_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes?page=0&size=10" "" "$TOKEN")
check_response "$ALL_RESPONSE" "Get All Issue Notes"

# Test 5: Get Issue Note by ID
if [ ! -z "$ISSUE_NOTE_ID" ]; then
    echo -e "\n${BLUE}Test 5: Get Issue Note by ID${NC}"
    GET_BY_ID_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/$ISSUE_NOTE_ID" "" "$TOKEN")
    check_response "$GET_BY_ID_RESPONSE" "Get Issue Note by ID"
fi

# Test 6: Get Issue Note by Number
if [ ! -z "$ISSUE_NOTE_NUMBER" ]; then
    echo -e "\n${BLUE}Test 6: Get Issue Note by Number${NC}"
    GET_BY_NUMBER_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/by-number?issueNoteNumber=$ISSUE_NOTE_NUMBER" "" "$TOKEN")
    check_response "$GET_BY_NUMBER_RESPONSE" "Get Issue Note by Number"
fi

# Test 7: Get My Issue Notes
echo -e "\n${BLUE}Test 7: Get My Issue Notes${NC}"
MY_NOTES_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/my-issue-notes?page=0&size=10" "" "$TOKEN")
check_response "$MY_NOTES_RESPONSE" "Get My Issue Notes"

# Test 8: Submit for Approval
if [ ! -z "$ISSUE_NOTE_ID" ]; then
    echo -e "\n${BLUE}Test 8: Submit Issue Note for Approval${NC}"
    SUBMIT_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes/$ISSUE_NOTE_ID/submit" "" "$TOKEN")
    if check_response "$SUBMIT_RESPONSE" "Submit for Approval"; then
        echo "Status changed: Created → Pending Approval"
    fi
fi

# Test 9: Get Pending Approval Queue
echo -e "\n${BLUE}Test 9: Get Pending Approval Queue${NC}"
PENDING_APPROVAL_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/pending-approval?page=0&size=10" "" "$TOKEN")
check_response "$PENDING_APPROVAL_RESPONSE" "Get Pending Approval Queue"

# Test 10: Approve Issue Note (Manager action)
if [ ! -z "$ISSUE_NOTE_ID" ]; then
    echo -e "\n${BLUE}Test 10: Approve Issue Note${NC}"
    APPROVE_DATA='{"remarks": "Approved for material issuance"}'
    APPROVE_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes/$ISSUE_NOTE_ID/approve" "$APPROVE_DATA" "$TOKEN")
    if check_response "$APPROVE_RESPONSE" "Approve Issue Note"; then
        echo "Status changed: Pending Approval → Approved"
    fi
fi

# Test 11: Get Pending Issue Queue
echo -e "\n${BLUE}Test 11: Get Pending Issue Queue (Store Keeper)${NC}"
PENDING_ISSUE_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/pending-issue?page=0&size=10" "" "$TOKEN")
check_response "$PENDING_ISSUE_RESPONSE" "Get Pending Issue Queue"

# Test 12: Issue Goods (Store Keeper action)
if [ ! -z "$ISSUE_NOTE_ID" ]; then
    echo -e "\n${BLUE}Test 12: Issue Goods from Stores${NC}"
    ISSUE_DATA='{"remarks": "Materials issued successfully"}'
    ISSUE_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes/$ISSUE_NOTE_ID/issue" "$ISSUE_DATA" "$TOKEN")
    if check_response "$ISSUE_RESPONSE" "Issue Goods"; then
        echo "Status changed: Approved → Issued"
    fi
fi

# Test 13: Create another Issue Note for rejection testing
echo -e "\n${BLUE}Test 13: Create Issue Note for Rejection Test${NC}"
CREATE_DATA_2='{
  "companyId": 1,
  "departmentId": 101,
  "sectionId": 401,
  "plantId": 301,
  "issuedTo": "Maintenance Department",
  "purpose": "Emergency spare parts",
  "comments": "Test rejection workflow",
  "lineItems": [
    {
      "materialId": 1004,
      "unitOfMeasureId": 501,
      "quantity": 10,
      "rate": 100.00,
      "purpose": "Equipment repair"
    }
  ]
}'

CREATE_RESPONSE_2=$(call_api "POST" "$BASE_URL/issue-notes" "$CREATE_DATA_2" "$TOKEN")
if check_response "$CREATE_RESPONSE_2" "Create Issue Note for Rejection"; then
    REJECT_NOTE_ID=$(echo "$CREATE_RESPONSE_2" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "Created Issue Note ID for rejection: $REJECT_NOTE_ID"
    
    # Submit it
    call_api "POST" "$BASE_URL/issue-notes/$REJECT_NOTE_ID/submit" "" "$TOKEN" > /dev/null
    echo "Submitted for approval"
fi

# Test 14: Reject by Manager
if [ ! -z "$REJECT_NOTE_ID" ]; then
    echo -e "\n${BLUE}Test 14: Reject Issue Note (Manager)${NC}"
    REJECT_DATA='{"reason": "Insufficient budget allocation"}'
    REJECT_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes/$REJECT_NOTE_ID/reject" "$REJECT_DATA" "$TOKEN")
    if check_response "$REJECT_RESPONSE" "Reject by Manager"; then
        echo "Status changed: Pending Approval → Rejected by Manager"
    fi
fi

# Test 15: Create Issue Note for cancellation
echo -e "\n${BLUE}Test 15: Create and Cancel Issue Note${NC}"
CREATE_DATA_3='{
  "companyId": 1,
  "departmentId": 101,
  "sectionId": 401,
  "plantId": 301,
  "issuedTo": "R&D Department",
  "purpose": "Research materials",
  "comments": "Test cancellation",
  "lineItems": [
    {
      "materialId": 1012,
      "unitOfMeasureId": 501,
      "quantity": 5,
      "rate": 50.00,
      "purpose": "Testing"
    }
  ]
}'

CREATE_RESPONSE_3=$(call_api "POST" "$BASE_URL/issue-notes" "$CREATE_DATA_3" "$TOKEN")
if check_response "$CREATE_RESPONSE_3" "Create Issue Note for Cancellation"; then
    CANCEL_NOTE_ID=$(echo "$CREATE_RESPONSE_3" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo "Created Issue Note ID for cancellation: $CANCEL_NOTE_ID"
    
    # Cancel it
    CANCEL_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes/$CANCEL_NOTE_ID/cancel" "" "$TOKEN")
    if check_response "$CANCEL_RESPONSE" "Cancel Issue Note"; then
        echo "Status changed: Created → Cancelled"
    fi
fi

# Test 16: Get Issue Notes by Department
echo -e "\n${BLUE}Test 16: Get Issue Notes by Department${NC}"
DEPT_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/by-department/101?page=0&size=10" "" "$TOKEN")
check_response "$DEPT_RESPONSE" "Get Issue Notes by Department"

# Test 17: Get Issue Notes with Status Filter
echo -e "\n${BLUE}Test 17: Get Issue Notes with Status Filter (Issued)${NC}"
FILTER_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes?status=6&page=0&size=10" "" "$TOKEN")
check_response "$FILTER_RESPONSE" "Get Issue Notes by Status"

# Test 18: Get Issue Notes with Company Filter
echo -e "\n${BLUE}Test 18: Get Issue Notes with Company Filter${NC}"
COMPANY_FILTER_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes?companyId=1&page=0&size=10" "" "$TOKEN")
check_response "$COMPANY_FILTER_RESPONSE" "Get Issue Notes by Company"

# Test 19: Create Issue Note for Stores Rejection
echo -e "\n${BLUE}Test 19: Test Store Keeper Rejection Workflow${NC}"
CREATE_DATA_4='{
  "companyId": 1,
  "departmentId": 101,
  "sectionId": 401,
  "plantId": 301,
  "issuedTo": "Quality Department",
  "purpose": "Testing materials",
  "comments": "Test stores rejection",
  "lineItems": [
    {
      "materialId": 1003,
      "unitOfMeasureId": 501,
      "quantity": 1000,
      "rate": 75.00,
      "purpose": "High quantity test"
    }
  ]
}'

CREATE_RESPONSE_4=$(call_api "POST" "$BASE_URL/issue-notes" "$CREATE_DATA_4" "$TOKEN")
if check_response "$CREATE_RESPONSE_4" "Create Issue Note for Stores Rejection"; then
    STORES_REJECT_ID=$(echo "$CREATE_RESPONSE_4" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    
    # Submit and approve
    call_api "POST" "$BASE_URL/issue-notes/$STORES_REJECT_ID/submit" "" "$TOKEN" > /dev/null
    APPROVE_DATA='{"remarks": "Approved"}'
    call_api "POST" "$BASE_URL/issue-notes/$STORES_REJECT_ID/approve" "$APPROVE_DATA" "$TOKEN" > /dev/null
    
    # Reject by stores
    STORES_REJECT_DATA='{"reason": "Insufficient stock available"}'
    STORES_REJECT_RESPONSE=$(call_api "POST" "$BASE_URL/issue-notes/$STORES_REJECT_ID/reject-stores" "$STORES_REJECT_DATA" "$TOKEN")
    if check_response "$STORES_REJECT_RESPONSE" "Reject by Store Keeper"; then
        echo "Status changed: Approved → Rejected by Stores"
    fi
fi

# Test 20: Verify Statistics Updated
echo -e "\n${BLUE}Test 20: Verify Statistics Updated${NC}"
FINAL_STATS_RESPONSE=$(call_api "GET" "$BASE_URL/issue-notes/statistics" "" "$TOKEN")
check_response "$FINAL_STATS_RESPONSE" "Final Statistics Check"

# Display summary
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Tests: $((PASSED + FAILED))"
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠ Some tests failed${NC}"
    exit 1
fi
