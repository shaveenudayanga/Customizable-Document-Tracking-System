#!/bin/bash
# Test script for API integration

echo "==================================="
echo "Frontend API Integration Test"
echo "==================================="
echo ""

# Check if backend is running
echo "1. Checking backend services..."
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✓ API Gateway is running (port 8080)"
else
    echo "✗ API Gateway is NOT running"
    echo "  Start with: cd backend/api-gateway && ./mvnw spring-boot:run"
fi

if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "✓ User Service is running (port 8081)"
else
    echo "✗ User Service is NOT running"
    echo "  Start with: cd backend/user-service && ./mvnw spring-boot:run"
fi

echo ""
echo "2. Testing authentication endpoint..."
# Test login endpoint
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' 2>&1)

if echo "$RESPONSE" | grep -q "token"; then
    echo "✓ Login endpoint is working"
    TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo "  Token received: ${TOKEN:0:20}..."
    
    echo ""
    echo "3. Testing profile endpoint with JWT..."
    PROFILE=$(curl -s http://localhost:8080/api/auth/profile \
      -H "Authorization: Bearer $TOKEN")
    
    if echo "$PROFILE" | grep -q "username"; then
        echo "✓ Profile endpoint is working"
        echo "  Response: $PROFILE"
    else
        echo "✗ Profile endpoint failed"
        echo "  Response: $PROFILE"
    fi
else
    echo "✗ Login endpoint failed"
    echo "  Response: $RESPONSE"
fi

echo ""
echo "4. Frontend build status..."
if [ -d "frontend/dist" ]; then
    echo "✓ Frontend is built (dist folder exists)"
    echo "  Size: $(du -sh frontend/dist 2>/dev/null | cut -f1)"
else
    echo "✗ Frontend not built"
    echo "  Run: cd frontend && npm run build"
fi

echo ""
echo "5. Frontend dev server check..."
if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo "✓ Frontend dev server is running (port 5173)"
else
    echo "✗ Frontend dev server is NOT running"
    echo "  Start with: cd frontend && npm run dev"
fi

echo ""
echo "==================================="
echo "Test Complete"
echo "==================================="
echo ""
echo "To test the integration manually:"
echo "1. Open http://localhost:5173 in your browser"
echo "2. Login with username: admin, password: admin123"
echo "3. Navigate to Admin Profile page"
echo "4. Verify profile data loads from backend API"
echo ""
