#!/bin/bash

# Status Check Script for Customizable Document Tracking System

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📊 Customizable Document Tracking System - Service Status${NC}"
echo "=================================================================="

# Function to check service status
check_service() {
    local service_name=$1
    local port=$2
    local endpoint=$3
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null; then
        local pid=$(lsof -ti:$port)
        echo -e "${GREEN}✅ $service_name${NC} - Running on port $port (PID: $pid)"
        
        # Test HTTP endpoint if provided
        if [ ! -z "$endpoint" ]; then
            if curl -s "$endpoint" >/dev/null 2>&1; then
                echo -e "   🌐 HTTP endpoint responding: $endpoint"
            else
                echo -e "${YELLOW}   ⚠️  HTTP endpoint not responding: $endpoint${NC}"
            fi
        fi
    else
        echo -e "${RED}❌ $service_name${NC} - Not running on port $port"
    fi
}

echo ""
echo -e "${BLUE}Backend Services:${NC}"
check_service "User Service" 8081 "http://localhost:8081/api/health"
check_service "Document Service" 8082 "http://localhost:8082/api/health"
check_service "Workflow Service" 8083 "http://localhost:8083/api/health"
check_service "Tracking Service" 8084 "http://localhost:8084/api/health"
check_service "Notification Service" 8085 "http://localhost:8085/api/health"

echo ""
echo -e "${BLUE}Frontend:${NC}"
check_service "React Frontend" 5173 "http://localhost:5173"

echo ""
echo -e "${BLUE}Database Connections:${NC}"
# Check PostgreSQL
if pgrep -x "postgres" > /dev/null; then
    echo -e "${GREEN}✅ PostgreSQL${NC} - Running"
    
    # Check specific databases
    for db in user_db document_db workflow_db tracking_db; do
        if psql -lqt | cut -d \| -f 1 | grep -qw $db; then
            echo -e "   📊 Database: $db - Available"
        else
            echo -e "${YELLOW}   ⚠️  Database: $db - Not found${NC}"
        fi
    done
else
    echo -e "${RED}❌ PostgreSQL${NC} - Not running"
fi

echo ""
echo -e "${BLUE}System Resources:${NC}"
echo -e "💾 Memory Usage: $(ps aux | awk 'NR>1{sum+=$6} END {printf "%.1f MB", sum/1024}')"
echo -e "🖥️  CPU Load: $(uptime | awk -F'load average:' '{print $2}')"

echo ""
echo -e "${BLUE}Quick Actions:${NC}"
echo -e "  🚀 Deploy:        ${YELLOW}./deploy.sh${NC}"
echo -e "  🛑 Stop all:      ${YELLOW}./stop.sh${NC}"
echo -e "  📜 View logs:     ${YELLOW}tail -f backend/logs/*.log${NC}"
echo -e "  🌐 Open app:      ${YELLOW}open http://localhost:5173${NC}"

echo ""
echo "=================================================================="