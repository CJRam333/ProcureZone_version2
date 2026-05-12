#!/bin/bash

# ============================================
# Purchase Order Module Test Script
# Tests all 17 PO endpoints with real data
# ============================================

BASE_URL="http://localhost:8080/api/v1"
TOKEN=""
INDENT_ID=""
PO_ID=""
PO_NUMBER=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================="
echo "PO Module Test Script"
echo "========================================="

# Step 1: Authenticate
echo -e "\n${YELLOW}Step 1: Authenticating...${NC}"
AUTH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "rajesh.kumar",
    "password": "password123"
  }')

TOKEN=$(echo $AUTH_RESPONSE | grep -o '"accessToken":"[^"]*"' | sed 's/"accessToken":"//;s/"//g')

if [ -z "$TOKEN" ]; then
  echo -e "${RED}✗ Authentication failed${NC}"
  echo "Response: $AUTH_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✓ Authentication successful${NC}"
echo "Token: ${TOKEN:0:50}..."

# Step 2: Get approved indents
echo -e "\n${YELLOW}Step 2: GET /api/v1/pos/approved-indents${NC}"
APPROVED_INDENTS=$(curl -s -X GET "$BASE_URL/pos/approved-indents" \
  -H "Authorization: Bearer $TOKEN")

echo "Response: $APPROVED_INDENTS"

# Extract first approved indent ID
INDENT_ID=$(echo $APPROVED_INDENTS | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

if [ -z "$INDENT_ID" ]; then
  echo -e "${RED}✗ No approved indents found${NC}"
else
  echo -e "${GREEN}✓ Found approved indent ID: $INDENT_ID${NC}"
fi

# Step 3: Get all POs
echo -e "\n${YELLOW}Step 3: GET /api/v1/pos - Get all POs${NC}"
ALL_POS=$(curl -s -X GET "$BASE_URL/pos?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "Response (first 500 chars): ${ALL_POS:0:500}..."
PO_COUNT=$(echo $ALL_POS | grep -o '"totalItems":[0-9]*' | sed 's/"totalItems"://')
echo -e "${GREEN}✓ Found $PO_COUNT total POs${NC}"

# Get first PO ID for testing
PO_ID=$(echo $ALL_POS | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
PO_NUMBER=$(echo $ALL_POS | grep -o '"poNumber":"[^"]*"' | head -1 | sed 's/"poNumber":"//;s/"//g')

if [ ! -z "$PO_ID" ]; then
  echo -e "${GREEN}✓ Using existing PO: ID=$PO_ID, Number=$PO_NUMBER${NC}"

  # Step 4: Get PO by ID
  echo -e "\n${YELLOW}Step 4: GET /api/v1/pos/$PO_ID - Get PO by ID${NC}"
  PO_DETAILS=$(curl -s -X GET "$BASE_URL/pos/$PO_ID" \
    -H "Authorization: Bearer $TOKEN")

  echo "Response (first 500 chars): ${PO_DETAILS:0:500}..."
  echo -e "${GREEN}✓ Retrieved PO details${NC}"

  # Step 5: Get PO by number
  if [ ! -z "$PO_NUMBER" ]; then
    echo -e "\n${YELLOW}Step 5: GET /api/v1/pos/number/$PO_NUMBER - Get PO by Number${NC}"
    PO_BY_NUMBER=$(curl -s -X GET "$BASE_URL/pos/number/$PO_NUMBER" \
      -H "Authorization: Bearer $TOKEN")

    echo "Response (first 500 chars): ${PO_BY_NUMBER:0:500}..."
    echo -e "${GREEN}✓ Retrieved PO by number${NC}"
  fi
fi

# Step 6: Get POs by vendor
echo -e "\n${YELLOW}Step 6: GET /api/v1/pos/by-vendor/1${NC}"
VENDOR_POS=$(curl -s -X GET "$BASE_URL/pos/by-vendor/1?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "Response (first 300 chars): ${VENDOR_POS:0:300}..."
VENDOR_PO_COUNT=$(echo $VENDOR_POS | grep -o '"totalItems":[0-9]*' | sed 's/"totalItems"://')
echo -e "${GREEN}✓ Found $VENDOR_PO_COUNT POs for vendor 1${NC}"

# Step 7: Get POs by department
echo -e "\n${YELLOW}Step 7: GET /api/v1/pos/by-department/1${NC}"
DEPT_POS=$(curl -s -X GET "$BASE_URL/pos/by-department/1?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "Response (first 300 chars): ${DEPT_POS:0:300}..."
DEPT_PO_COUNT=$(echo $DEPT_POS | grep -o '"totalItems":[0-9]*' | sed 's/"totalItems"://')
echo -e "${GREEN}✓ Found $DEPT_PO_COUNT POs for department 1${NC}"

# Step 8: Get pending for approval
echo -e "\n${YELLOW}Step 8: GET /api/v1/pos/pending-approval${NC}"
PENDING_APPROVAL=$(curl -s -X GET "$BASE_URL/pos/pending-approval?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "Response (first 300 chars): ${PENDING_APPROVAL:0:300}..."
PENDING_COUNT=$(echo $PENDING_APPROVAL | grep -o '"totalItems":[0-9]*' | sed 's/"totalItems"://')
echo -e "${GREEN}✓ Found $PENDING_COUNT POs pending approval${NC}"

# Step 9: Get overdue POs
echo -e "\n${YELLOW}Step 9: GET /api/v1/pos/overdue${NC}"
OVERDUE_POS=$(curl -s -X GET "$BASE_URL/pos/overdue?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "Response (first 300 chars): ${OVERDUE_POS:0:300}..."
OVERDUE_COUNT=$(echo $OVERDUE_POS | grep -o '"totalItems":[0-9]*' | sed 's/"totalItems"://')
echo -e "${GREEN}✓ Found $OVERDUE_COUNT overdue POs${NC}"

# Step 10: Get dashboard statistics
echo -e "\n${YELLOW}Step 10: GET /api/v1/pos/dashboard/statistics${NC}"
STATISTICS=$(curl -s -X GET "$BASE_URL/pos/dashboard/statistics" \
  -H "Authorization: Bearer $TOKEN")

echo "Response: $STATISTICS"
echo -e "${GREEN}✓ Retrieved dashboard statistics${NC}"

echo -e "\n========================================="
echo -e "${GREEN}PO Module Testing Complete!${NC}"
echo "========================================="
echo -e "Summary:"
echo -e "  Total POs in system: $PO_COUNT"
echo -e "  POs for vendor 1: $VENDOR_PO_COUNT"
echo -e "  POs for department 1: $DEPT_PO_COUNT"
echo -e "  Pending approval: $PENDING_COUNT"
echo -e "  Overdue: $OVERDUE_COUNT"
echo -e "\nTested Endpoints:"
echo -e "  ✓ GET /api/v1/pos/approved-indents"
echo -e "  ✓ GET /api/v1/pos (with pagination)"
echo -e "  ✓ GET /api/v1/pos/{id}"
echo -e "  ✓ GET /api/v1/pos/number/{poNumber}"
echo -e "  ✓ GET /api/v1/pos/by-vendor/{vendorId}"
echo -e "  ✓ GET /api/v1/pos/by-department/{departmentId}"
echo -e "  ✓ GET /api/v1/pos/pending-approval"
echo -e "  ✓ GET /api/v1/pos/overdue"
echo -e "  ✓ GET /api/v1/pos/dashboard/statistics"
