#!/bin/bash

# Customizable Document Tracking System Deployment Script
# This script builds and starts all microservices and the frontend

set -e

echo "🚀 Starting Customizable Document Tracking System Deployment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="/Users/sadishshamal/Docu_trace/Customizable-Document-Tracking-System"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"

# Java and Node paths
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"

echo -e "${BLUE}📋 Deployment Configuration:${NC}"
echo -e "  Project Root: $PROJECT_ROOT"
echo -e "  Backend Dir: $BACKEND_DIR"
echo -e "  Frontend Dir: $FRONTEND_DIR"
echo ""

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Function to kill process on port
kill_port() {
    local port=$1
    local pid=$(lsof -ti:$port)
    if [ ! -z "$pid" ]; then
        echo -e "${YELLOW}🔄 Killing existing process on port $port (PID: $pid)${NC}"
        kill -9 $pid 2>/dev/null || true
        sleep 2
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local port=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service_name to start on port $port...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            echo -e "${GREEN}✅ $service_name is ready on port $port${NC}"
            return 0
        fi
        echo -e "   Attempt $attempt/$max_attempts..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start on port $port${NC}"
    return 1
}

# Function to build and start a microservice
start_microservice() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local jar_pattern=$4
    
    echo -e "${BLUE}🔨 Building and starting $service_name...${NC}"
    
    cd "$service_dir"
    
    # Kill existing process
    kill_port $port
    
    # Build the service
    echo -e "${YELLOW}📦 Building $service_name...${NC}"
    mvn clean package -DskipTests -q
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Failed to build $service_name${NC}"
        return 1
    fi
    
    # Find and start the JAR
    local jar_file=$(find target -name "$jar_pattern" -type f | head -1)
    
    if [ -z "$jar_file" ]; then
        echo -e "${RED}❌ JAR file not found for $service_name${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}🚀 Starting $service_name with: $jar_file${NC}"
    nohup java -jar "$jar_file" > "../logs/${service_name}.log" 2>&1 &
    
    # Wait for service to be ready
    wait_for_service $port "$service_name"
}

# Create logs directory
mkdir -p "$BACKEND_DIR/logs"

echo -e "${BLUE}🗄️  Setting up databases...${NC}"
# Create databases if they don't exist
createdb user_db 2>/dev/null || echo "Database user_db already exists"
createdb document_db 2>/dev/null || echo "Database document_db already exists"
createdb workflow_db 2>/dev/null || echo "Database workflow_db already exists"
createdb tracking_db 2>/dev/null || echo "Database tracking_db already exists"

echo -e "${BLUE}🎯 Starting Backend Services...${NC}"

# Start User Service (Port 8081)
start_microservice "User Service" "$BACKEND_DIR/user-service" 8081 "user-service-*.jar"

# Start Document Service (Port 8082)
start_microservice "Document Service" "$BACKEND_DIR/document-service" 8082 "document-service-*.jar"

# Start Workflow Service (Port 8083)
start_microservice "Workflow Service" "$BACKEND_DIR/workflow-service" 8083 "workflow-service-*.jar"

# Start Tracking Service (Port 8084)
start_microservice "Tracking Service" "$BACKEND_DIR/tracking-service" 8084 "tracking-service-*.jar"

echo -e "${BLUE}🌐 Starting Frontend...${NC}"

cd "$FRONTEND_DIR"

# Kill existing frontend process
kill_port 5173

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}📦 Installing frontend dependencies...${NC}"
    npm install
fi

# Start frontend in background
echo -e "${YELLOW}🚀 Starting React frontend...${NC}"
nohup npm run dev > "../backend/logs/frontend.log" 2>&1 &

# Wait for frontend to be ready
wait_for_service 5173 "Frontend"

echo -e "${GREEN}🎉 DEPLOYMENT COMPLETE!${NC}"
echo ""
echo -e "${BLUE}📊 Service Status:${NC}"
echo -e "  ✅ User Service:      http://localhost:8081"
echo -e "  ✅ Document Service:  http://localhost:8082"
echo -e "  ✅ Workflow Service:  http://localhost:8083"
echo -e "  ✅ Tracking Service:  http://localhost:8084"
echo -e "  ✅ Frontend:          http://localhost:5173"
echo ""
echo -e "${BLUE}📱 Access your application:${NC}"
echo -e "  🌐 Main Application: ${GREEN}http://localhost:5173${NC}"
echo ""
echo -e "${BLUE}📋 Management Commands:${NC}"
echo -e "  📊 Check status:  ${YELLOW}./status.sh${NC}"
echo -e "  🛑 Stop all:      ${YELLOW}./stop.sh${NC}"
echo -e "  📜 View logs:     ${YELLOW}tail -f backend/logs/*.log${NC}"
echo ""
echo -e "${GREEN}🎊 Your Customizable Document Tracking System is now running!${NC}"