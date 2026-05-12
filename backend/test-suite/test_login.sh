#!/bin/bash
echo "Testing login..."
curl -v -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"vikram.singh","password":"password"}' 2>&1
