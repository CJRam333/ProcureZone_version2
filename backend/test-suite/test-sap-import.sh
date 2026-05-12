#!/bin/bash
# ============================================================================
# SAP Import Test Script
# Tests the SAP Material CSV Import functionality
# ============================================================================

echo "========================================"
echo "SAP Import Test Script"
echo "========================================"

BASE_URL="http://localhost:8080"
API_URL="${BASE_URL}/api/v1"
SAP_URL="${BASE_URL}/api/sap"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Step 1: Login
echo -e "${YELLOW}Step 1: Logging in as SUPERADMIN...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${API_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "vikram.singh", "password": "password123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo -e "${RED}❌ Login failed${NC}"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

echo -e "${GREEN}✅ Login successful${NC}"

# Step 2: Check Import Status
echo -e "${YELLOW}Step 2: Checking import status...${NC}"
STATUS_RESPONSE=$(curl -s -X GET "${SAP_URL}/status" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Import Status:"
echo $STATUS_RESPONSE | python -m json.tool 2>/dev/null || echo $STATUS_RESPONSE

# Step 3: Create Test CSV (matching legacy 51-column format)
echo -e "${YELLOW}Step 3: Creating test CSV file...${NC}"
cat > test_sap_import.csv << 'EOF'
1001,301,WH-01,SAP-TEST-001,Test Cotton BG2 Premium,KG,BATCH001,1500.50,SEED,101,Cotton Seeds,BG2,Bollgard II,Cotton,Fiber,PASS,0.5,98.5,NEG,1,1001,A,B,C,2024-01-15,8.5,98.2,0.3,0,0,0,0,0,0.4,45.2,12.5,92,95,96,94,7,93,98.5,97.2,98.1,YES,YES,NO,NO,STP1
1001,301,WH-01,SAP-TEST-002,Test Maize Hybrid,KG,BATCH002,2000.00,SEED,102,Maize Seeds,Hybrid,Single Cross,Maize,Cereal,PASS,0.3,97.8,NEG,1,1002,A,B,C,2024-01-15,9.2,97.5,0.5,0,0,0,0,0,0.3,42.1,15.2,90,93,94,92,7,91,97.5,96.8,97.2,NO,NO,NO,NO,STP1
1002,302,WH-02,SAP-TEST-003,Test Rice Basmati,KG,BATCH003,500.25,SEED,103,Rice Seeds,Basmati,Premium,Rice,Cereal,PASS,0.2,99.1,NEG,1,1003,A,B,C,2024-01-15,10.1,99.0,0.2,0,0,0,0,0,0.2,38.5,25.3,94,96,97,95,7,94,99.0,98.5,98.8,NO,NO,NO,NO,STP1
EOF

echo -e "${GREEN}✅ Test CSV created with 3 records (51 columns each)${NC}"

# Step 4: Upload CSV
echo -e "${YELLOW}Step 4: Uploading CSV file...${NC}"
IMPORT_RESPONSE=$(curl -s -X POST "${SAP_URL}/materials/import" \
  -H "Authorization: Bearer ${TOKEN}" \
  -F "file=@test_sap_import.csv" \
  -F "truncateBeforeImport=true")

echo "Import Response:"
echo $IMPORT_RESPONSE | python -m json.tool 2>/dev/null || echo $IMPORT_RESPONSE

# Step 5: Verify Import
echo -e "${YELLOW}Step 5: Verifying import...${NC}"
VERIFY_RESPONSE=$(curl -s -X GET "${SAP_URL}/materials?page=0&size=10" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Materials after import:"
echo $VERIFY_RESPONSE | python -m json.tool 2>/dev/null || echo $VERIFY_RESPONSE

# Step 6: Check Status Again
echo -e "${YELLOW}Step 6: Final status check...${NC}"
FINAL_STATUS=$(curl -s -X GET "${SAP_URL}/status" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Final Status:"
echo $FINAL_STATUS | python -m json.tool 2>/dev/null || echo $FINAL_STATUS

# Step 7: Get Materials by Plant Code
echo -e "${YELLOW}Step 7: Get materials by plant code 301...${NC}"
PLANT_RESPONSE=$(curl -s -X GET "${SAP_URL}/materials/plant/301" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Materials for Plant 301:"
echo $PLANT_RESPONSE | python -m json.tool 2>/dev/null || echo $PLANT_RESPONSE

# Step 8: Search Materials
echo -e "${YELLOW}Step 8: Search for 'cotton'...${NC}"
SEARCH_RESPONSE=$(curl -s -X GET "${SAP_URL}/materials/search?keyword=cotton" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Search Results:"
echo $SEARCH_RESPONSE | python -m json.tool 2>/dev/null || echo $SEARCH_RESPONSE

# Cleanup
rm -f test_sap_import.csv

echo ""
echo "========================================"
echo -e "${GREEN}SAP Import Test Complete!${NC}"
echo "========================================"
