# 🚀 DEPLOYMENT GUIDE - Customizable Document Tracking System

## 📊 Current Deployment Status

### ✅ Services Running Successfully:
- **User Service** (Port 8081) - Authentication & User Management
- **Document Service** (Port 8082) - Document CRUD Operations  
- **Workflow Service** (Port 8083) - Pipeline & Task Management
- **Tracking Service** (Port 8084) - Document Tracking & Audit
- **React Frontend** (Port 5173) - User Interface

### 🌐 Application Access:
- **Main Application**: http://localhost:5173
- **API Services**: http://localhost:8081-8084

## 🛠️ Quick Deployment Commands

### Start/Restart All Services:
```bash
./deploy.sh
```

### Check System Status:
```bash
./status.sh
```

### Stop All Services:
```bash
./stop.sh
```

## 🐳 Docker Deployment (Production)

For production deployment, use Docker Compose:

```bash
# Build and start all services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop all services
docker-compose -f docker-compose.prod.yml down
```

## 📋 Service Architecture

```
┌─────────────────┐    ┌──────────────────┐
│   React Frontend │    │   Nginx Proxy    │
│   (Port 5173)   │────│   (Port 80/443)  │
└─────────────────┘    └──────────────────┘
         │                       │
         └───────────────────────┘
                    │
        ┌───────────┼───────────┐
        │           │           │
   ┌────▼────┐ ┌────▼────┐ ┌────▼────┐
   │User Svc │ │Doc Svc  │ │Workflow │
   │ :8081   │ │ :8082   │ │ :8083   │
   └────┬────┘ └────┬────┘ └────┬────┘
        │           │           │
        └───────────┼───────────┘
                    │
            ┌───────▼───────┐
            │  PostgreSQL   │
            │   Database    │
            └───────────────┘
```

## 🔧 Manual Service Management

### Start Individual Services:

```bash
# User Service
cd backend/user-service
mvn clean package -DskipTests
java -jar target/user-service-*.jar &

# Document Service
cd backend/document-service
mvn clean package -DskipTests
java -jar target/document-service-*.jar &

# Workflow Service
cd backend/workflow-service
mvn clean package -DskipTests
java -jar target/workflow-service-*.jar &

# Tracking Service
cd backend/tracking-service
mvn clean package -DskipTests
java -jar target/tracking-service-*.jar &

# Frontend
cd frontend
npm install
npm run dev
```

## 🗄️ Database Setup

### PostgreSQL Database Creation:
```sql
CREATE DATABASE user_db;
CREATE DATABASE document_db;
CREATE DATABASE workflow_db;
CREATE DATABASE tracking_db;
```

### Connection Strings:
- User Service: `jdbc:postgresql://localhost:5432/user_db`
- Document Service: `jdbc:postgresql://localhost:5432/document_db`
- Workflow Service: `jdbc:postgresql://localhost:5432/workflow_db`
- Tracking Service: `jdbc:postgresql://localhost:5432/tracking_db`

## 🔐 Security Configuration

### Default Admin User:
- **Username**: `adminuser`
- **Password**: `AdminPass123`
- **Role**: `ADMIN`

### Default Staff User:
- **Username**: `staff`
- **Password**: `TestPass123`
- **Role**: `USER`

## 📊 Monitoring & Logs

### View Service Logs:
```bash
# All logs
tail -f backend/logs/*.log

# Specific service
tail -f backend/logs/user-service.log
tail -f backend/logs/document-service.log
tail -f backend/logs/workflow-service.log
tail -f backend/logs/tracking-service.log
tail -f backend/logs/frontend.log
```

### Health Check Endpoints:
- User Service: http://localhost:8081/api/health
- Document Service: http://localhost:8082/api/health
- Workflow Service: http://localhost:8083/api/health
- Tracking Service: http://localhost:8084/api/health

## 🚨 Troubleshooting

### Port Conflicts:
```bash
# Check what's using a port
lsof -i :8081

# Kill process on port
kill -9 $(lsof -ti:8081)
```

### Service Not Starting:
1. Check logs in `backend/logs/`
2. Verify PostgreSQL is running
3. Check database connections
4. Ensure Java 21 is installed
5. Verify Node.js version for frontend

### Database Connection Issues:
```bash
# Start PostgreSQL (macOS with Homebrew)
brew services start postgresql@15

# Create missing databases
createdb user_db
createdb document_db
createdb workflow_db
createdb tracking_db
```

## 🌟 Production Recommendations

1. **Use HTTPS** - Configure SSL certificates
2. **Environment Variables** - Store secrets securely
3. **Load Balancing** - Use Nginx for production
4. **Monitoring** - Add application monitoring
5. **Backups** - Regular database backups
6. **Logging** - Centralized logging solution

## 🎯 Next Steps

1. **Test all features** in the deployed application
2. **Create additional users** and departments
3. **Upload test documents** and create workflows
4. **Monitor performance** and optimize as needed
5. **Set up production environment** with proper security

---

🎉 **Your Customizable Document Tracking System is successfully deployed and ready for use!**

Access your application at: **http://localhost:5173**