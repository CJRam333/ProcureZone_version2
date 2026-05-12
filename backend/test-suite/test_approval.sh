#!/bin/bash
# Login and get token
response=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"vikram.singh","password":"password"}')

# Extract token
token=$(echo "$response" | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')
echo "Token obtained: ${token:0:50}..."

# Test approval workflow for indent 1003
echo ""
echo "Testing GET /api/v1/approval/indent/1003/workflow"
curl -s -X GET "http://localhost:8080/api/v1/approval/indent/1003/workflow" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $token" | head -c 500
