#!/bin/bash

# Stop Script for Customizable Document Tracking System

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🛑 Stopping Customizable Document Tracking System...${NC}"

# Function to kill process on port
kill_port() {
    local port=$1
    local service_name=$2
    
    local pid=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$pid" ]; then
        echo -e "${YELLOW}🔄 Stopping $service_name on port $port (PID: $pid)${NC}"
        kill -9 $pid 2>/dev/null || true
        sleep 1
        
        # Verify it's stopped
        if ! lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name stopped${NC}"
        else
            echo -e "${RED}❌ Failed to stop $service_name${NC}"
        fi
    else
        echo -e "${YELLOW}⚪ $service_name not running on port $port${NC}"
    fi
}

echo ""
echo -e "${BLUE}Stopping Backend Services:${NC}"
kill_port 8081 "User Service"
kill_port 8082 "Document Service"
kill_port 8083 "Workflow Service"
kill_port 8084 "Tracking Service"
kill_port 8085 "Notification Service"

echo ""
echo -e "${BLUE}Stopping Frontend:${NC}"
kill_port 5173 "React Frontend"

echo ""
echo -e "${BLUE}Cleaning up additional processes:${NC}"

# Kill any remaining Java processes related to our services
echo -e "${YELLOW}🔄 Cleaning up Java processes...${NC}"
pkill -f "user-service" 2>/dev/null || true
pkill -f "document-service" 2>/dev/null || true
pkill -f "workflow-service" 2>/dev/null || true
pkill -f "tracking-service" 2>/dev/null || true
pkill -f "notification-service" 2>/dev/null || true

# Kill any remaining Node processes for the frontend
echo -e "${YELLOW}🔄 Cleaning up Node processes...${NC}"
pkill -f "vite" 2>/dev/null || true

echo ""
echo -e "${GREEN}🎉 All services stopped successfully!${NC}"
echo ""
echo -e "${BLUE}To restart the system:${NC}"
echo -e "  🚀 Run: ${YELLOW}./deploy.sh${NC}"
echo ""