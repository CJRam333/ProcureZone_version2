#!/bin/bash
# Test EMPLOYEE role (swati.bhatt) for authorization fixes

BASE_URL="http://localhost:8080/api/v1"
USERNAME="swati.bhatt"
PASSWORD="Seeds@2024"

echo "=== EMPLOYEE ROLE VERIFICATION (swati.bhatt) ==="
echo "Testing endpoints that previously failed due to authorization..."
echo ""

# Login
echo "1. Login..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Login FAILED"
  echo "$LOGIN_RESPONSE"
  exit 1
fi
echo "✅ Login successful"

# Test endpoints that were failing before
echo ""
echo "2. Testing /dashboard/statistics (was failing)..."
RESP=$(curl -s -w "%{http_code}" -o /tmp/resp.json -H "Authorization: Bearer $TOKEN" "${BASE_URL}/dashboard/statistics")
if [ "$RESP" = "200" ]; then
  echo "✅ PASS - Dashboard statistics"
else
  echo "❌ FAIL - Dashboard statistics (HTTP $RESP)"
fi

echo ""
echo "3. Testing /indents (was failing)..."
RESP=$(curl -s -w "%{http_code}" -o /tmp/resp.json -H "Authorization: Bearer $TOKEN" "${BASE_URL}/indents")
if [ "$RESP" = "200" ]; then
  echo "✅ PASS - Indents list"
else
  echo "❌ FAIL - Indents list (HTTP $RESP)"
fi

echo ""
echo "4. Testing /pos (was failing)..."
RESP=$(curl -s -w "%{http_code}" -o /tmp/resp.json -H "Authorization: Bearer $TOKEN" "${BASE_URL}/pos")
if [ "$RESP" = "200" ]; then
  echo "✅ PASS - POs list"
else
  echo "❌ FAIL - POs list (HTTP $RESP)"
fi

echo ""
echo "5. Testing /grn..."
RESP=$(curl -s -w "%{http_code}" -o /tmp/resp.json -H "Authorization: Bearer $TOKEN" "${BASE_URL}/grn")
if [ "$RESP" = "200" ]; then
  echo "✅ PASS - GRN list"
else
  echo "❌ FAIL - GRN list (HTTP $RESP) - Note: EMPLOYEE may not need GRN access"
fi

echo ""
echo "6. Testing /issue-notes (was failing)..."
RESP=$(curl -s -w "%{http_code}" -o /tmp/resp.json -H "Authorization: Bearer $TOKEN" "${BASE_URL}/issue-notes")
if [ "$RESP" = "200" ]; then
  echo "✅ PASS - Issue notes list"
else
  echo "❌ FAIL - Issue notes list (HTTP $RESP)"
fi

echo ""
echo "=== EMPLOYEE VERIFICATION COMPLETE ==="
