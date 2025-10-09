#!/bin/bash

# Test script to verify Authorization header is passed correctly
# This script tests the authentication flow and profile endpoint

echo "=========================================="
echo "Testing Authentication & Authorization"
echo "=========================================="
echo ""

API_BASE="http://localhost:8080/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Login
echo "Test 1: User Login"
echo "-------------------"
LOGIN_RESPONSE=$(curl -s -X POST "${API_BASE}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "Login Response:"
echo "$LOGIN_RESPONSE" | jq '.'
echo ""

# Extract token
TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // empty')

if [ -z "$TOKEN" ]; then
  echo -e "${RED}❌ Login failed - no token received${NC}"
  exit 1
else
  echo -e "${GREEN}✅ Login successful - token received${NC}"
  echo "Token: ${TOKEN:0:50}..."
fi

echo ""
echo "=========================================="
echo ""

# Test 2: Get Profile WITH Authorization Header
echo "Test 2: Get Profile (WITH Authorization header)"
echo "------------------------------------------------"
PROFILE_RESPONSE=$(curl -s -X GET "${API_BASE}/auth/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}")

echo "Profile Response:"
echo "$PROFILE_RESPONSE" | jq '.'
echo ""

if echo "$PROFILE_RESPONSE" | jq -e '.username' > /dev/null 2>&1; then
  echo -e "${GREEN}✅ Profile retrieved successfully with Authorization header${NC}"
  USERNAME=$(echo "$PROFILE_RESPONSE" | jq -r '.username')
  EMAIL=$(echo "$PROFILE_RESPONSE" | jq -r '.email')
  ROLE=$(echo "$PROFILE_RESPONSE" | jq -r '.role')
  echo "Username: $USERNAME"
  echo "Email: $EMAIL"
  echo "Role: $ROLE"
else
  echo -e "${RED}❌ Failed to retrieve profile${NC}"
fi

echo ""
echo "=========================================="
echo ""

# Test 3: Get Profile WITHOUT Authorization Header (should fail)
echo "Test 3: Get Profile (WITHOUT Authorization header - should fail)"
echo "-----------------------------------------------------------------"
PROFILE_NO_AUTH=$(curl -s -X GET "${API_BASE}/auth/profile" \
  -H "Content-Type: application/json")

echo "Response:"
echo "$PROFILE_NO_AUTH" | jq '.'
echo ""

if echo "$PROFILE_NO_AUTH" | jq -e '.username' > /dev/null 2>&1; then
  echo -e "${RED}❌ Security Issue: Profile retrieved without authorization!${NC}"
else
  echo -e "${GREEN}✅ Correctly rejected unauthorized request${NC}"
fi

echo ""
echo "=========================================="
echo ""

# Test 4: Get Profile with INVALID token (should fail)
echo "Test 4: Get Profile (with INVALID token - should fail)"
echo "-------------------------------------------------------"
PROFILE_BAD_TOKEN=$(curl -s -X GET "${API_BASE}/auth/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid_token_12345")

echo "Response:"
echo "$PROFILE_BAD_TOKEN" | jq '.'
echo ""

if echo "$PROFILE_BAD_TOKEN" | jq -e '.username' > /dev/null 2>&1; then
  echo -e "${RED}❌ Security Issue: Profile retrieved with invalid token!${NC}"
else
  echo -e "${GREEN}✅ Correctly rejected invalid token${NC}"
fi

echo ""
echo "=========================================="
echo "Summary"
echo "=========================================="
echo ""
echo "The Authorization header must be in format:"
echo -e "${YELLOW}Authorization: Bearer <JWT_TOKEN>${NC}"
echo ""
echo "Frontend automatically adds this header via api.js"
echo "See API_AUTH_FLOW.md for detailed flow documentation"
echo ""
