#!/bin/bash
# Quick test to verify login and authorization

# Test admin login
echo "Testing admin login..."
RESULT=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
echo "Result: $RESULT"

echo ""
echo "Testing stores.manager login..."
RESULT=$(curl -s -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"stores.manager","password":"admin123"}')
echo "Result: $RESULT"
