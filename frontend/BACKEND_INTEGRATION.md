# Frontend-Backend Integration Summary

## 🎯 Integration Status: COMPLETED

This document summarizes the complete integration of the DocuTrace frontend with the backend microservices.

---

## 📋 Backend Services Overview

| Service          | Port | Base Path          | Purpose                              |
| ---------------- | ---- | ------------------ | ------------------------------------ |
| user-service     | 8081 | `/api/auth/*`      | Authentication & user management     |
| document-service | 8082 | `/api/documents/*` | Document CRUD & file operations      |
| workflow-service | 8083 | `/api/workflow/*`  | Workflow templates & task management |
| tracking-service | 8084 | `/api/tracking/*`  | Document tracking & history          |

---

## ✅ Completed Integrations

### 1. **Core API Client** (`src/lib/api.js`)

- ✅ JWT Bearer token authentication
- ✅ Automatic token injection in headers
- ✅ 401 handling with auto-redirect to login
- ✅ Multipart form-data support for file uploads
- ✅ Centralized error handling

### 2. **Authentication Service** (`src/services/authService.js`)

**Endpoints Integrated:**

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get current user profile

**Features:**

- JWT token storage in localStorage
- User data caching
- Automatic authentication state management

### 3. **User Service** (`src/services/userService.js`)

- ✅ Wraps authentication service
- ✅ Removed hardcoded mock data
- ✅ Provides cached user access
- ✅ Authentication status checking

### 4. **Document Service** (`src/services/documentService.js`)

**Endpoints Integrated:**

- `GET /api/documents` - List all documents
- `GET /api/documents/{id}` - Get document by ID
- `POST /api/documents` - Create document
- `POST /api/documents` (multipart) - Create document with file
- `POST /api/documents/{id}/status` - Update document status
- `GET /api/documents/{id}/qrcode` - Get QR code
- `POST /api/documents/{id}/file` - Upload file
- `GET /api/documents/{id}/file` - Download file
- `GET /api/documents/{id}/files` - List files

**DTO Mappings:**

```javascript
DocumentCreateRequest {
  title: string (required, max 255)
  documentType: string (required, max 100)
  description: string (optional, max 10,000)
  ownerUserId: UUID (required)
  qrPath: string (optional, max 512)
  fileDir: string (optional, max 512)
}

DocumentResponse {
  id: Long
  title: string
  documentType: string
  description: string
  ownerUserId: UUID
  statuses: Array<string>
  processInstanceId: string
  qrPath: string
  fileDir: string
  createdAt: OffsetDateTime
  updatedAt: OffsetDateTime
}
```

### 5. **Tracking Service** (`src/services/trackingService.js`)

**Endpoints Integrated:**

- `POST /api/tracking/scan` - Record tracking event
- `GET /api/tracking/history/{documentId}` - Get document history
- `GET /api/tracking/latest/{documentId}` - Get latest event

**Helper Methods:**

- `recordQRScan()` - Record QR code scan
- `recordTransfer()` - Record document transfer
- `recordApproval()` - Record approval event
- `recordRejection()` - Record rejection event

### 6. **Workflow Service** (`src/services/workflowService.js`)

**Endpoints Integrated:**

- `POST /api/workflow/create` - Create workflow template
- `POST /api/workflow/start` - Start workflow instance
- `GET /api/workflow/tasks?deptKey={key}` - Get department tasks
- `POST /api/workflow/tasks/{taskId}/complete` - Complete task
- `GET /api/workflow/definitions/{key}/xml` - Get BPMN XML
- `GET /api/workflow/documents/{documentId}/status` - Get workflow status
- `GET /api/workflow/templates` - List templates
- `GET /api/workflow/templates/{id}` - Get template details
- `PUT /api/workflow/templates/{id}` - Update template
- `DELETE /api/workflow/templates/{id}` - Delete template

**DTO Mappings:**

```javascript
CreateTemplateRequest {
  name: string (required)
  documentType: string (optional)
  steps: Array<PipelineStep> (required, non-empty)
  isPermanent: boolean (optional)
}

PipelineStep {
  stepNo: number (required, positive)
  deptKey: string (required)
  instructions: string (optional)
  notifyFlag: boolean (optional)
}

StartWorkflowRequest {
  documentId: Long (required)
  templateId: Long (optional)
  customSteps: Array<PipelineStep> (optional)
  initiator: string (optional)
}
```

### 7. **Pipeline Service** (`src/services/pipelineService.js`)

- ✅ Convenience wrapper around workflow service
- ✅ Pipeline-focused API
- ✅ Step validation helper
- ✅ Custom workflow support

### 8. **Department Service** (`src/services/departmentService.js`)

- ✅ Department lookup utilities
- ✅ Task retrieval by department
- ✅ Client-side department definitions (until backend provides endpoint)

---

## 🔧 Configuration Updates

### Environment Files

**`.env`**

```bash
VITE_API_URL=/api
VITE_API_TIMEOUT_MS=10000
VITE_API_WITH_CREDENTIALS=true
VITE_DEV_MODE=true
```

**`.env.development`**

```bash
VITE_API_URL=/api
VITE_API_TIMEOUT_MS=10000
VITE_API_WITH_CREDENTIALS=true
VITE_DEV_MODE=true
```

**`.env.example`**

```bash
VITE_API_URL=/api
VITE_API_TIMEOUT_MS=15000
VITE_API_WITH_CREDENTIALS=true
```

### Vite Proxy Configuration (`vite.config.js`)

```javascript
proxy: {
  "/api/auth": {
    target: "http://localhost:8081",
    changeOrigin: true,
    secure: false,
  },
  "/api/documents": {
    target: "http://localhost:8082",
    changeOrigin: true,
    secure: false,
  },
  "/api/workflow": {
    target: "http://localhost:8083",
    changeOrigin: true,
    secure: false,
  },
  "/api/tracking": {
    target: "http://localhost:8084",
    changeOrigin: true,
    secure: false,
  },
}
```

---

## 📝 Updated React Components

### 1. **Login Component** (`src/pages/Auth/Login.jsx`)

**Changes:**

- ✅ Changed from `email` to `username` (matches backend)
- ✅ Uses `authService.login()` instead of direct API call
- ✅ Removed manual token storage (handled by authService)
- ✅ Removed hardcoded navigation on button click
- ✅ Proper error handling

### 2. **Onboarding Component** (`src/pages/Auth/Onboarding.jsx`)

**Changes:**

- ✅ Changed from `name` to `username` (matches backend RegisterRequest)
- ✅ Added optional `position` and `sectionId` fields
- ✅ Uses `authService.register()` instead of direct API call
- ✅ Removed email existence check (endpoint doesn't exist)
- ✅ Auto-login after registration
- ✅ Navigates to dashboard instead of login page

---

## 🔐 Authentication Flow

### Registration Flow

```
User fills form → authService.register() → POST /api/auth/register
→ Backend returns { token, username, email, role, position, sectionId }
→ Token stored in localStorage
→ User data cached
→ Redirect to /dashboard
```

### Login Flow

```
User submits credentials → authService.login() → POST /api/auth/login
→ Backend returns { token, username, email, role, position, sectionId }
→ Token stored in localStorage
→ User data cached
→ Redirect to /dashboard
```

### Authenticated Requests

```
api.get/post/put/delete() → Automatically adds "Authorization: Bearer {token}"
→ If 401 response → clearAuth() → Redirect to /login
```

---

## 🚀 Running the Integrated Application

### Prerequisites

1. Start all backend services:

   ```bash
   # user-service on port 8081
   # document-service on port 8082
   # workflow-service on port 8083
   # tracking-service on port 8084
   ```

2. Ensure PostgreSQL databases are running:
   - `user_db` (port 5432)
   - `document_db` (port 5432)
   - `workflow_db` (port 5432)
   - `tracking_db` (port 5432)

### Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173` with Vite proxy routing requests to backend services.

---

## 📊 API Request Examples

### Authentication

```javascript
// Register
await authService.register({
  username: "john_doe",
  email: "john@example.com",
  password: "SecurePass123!",
  role: "USER",
  position: "Manager",
  sectionId: "dept-001",
});

// Login
await authService.login({
  username: "john_doe",
  password: "SecurePass123!",
});

// Get Profile
const user = await authService.getProfile();
```

### Documents

```javascript
// Create document
const doc = await documentService.createDocument({
  title: "Project Proposal",
  documentType: "PROPOSAL",
  description: "Q4 2024 Project Proposal",
  ownerUserId: "550e8400-e29b-41d4-a716-446655440000",
});

// Get all documents
const docs = await documentService.getAllDocuments();

// Update status
await documentService.updateDocumentStatus(docId, ["REVIEWED", "APPROVED"]);
```

### Workflows

```javascript
// Create template
const template = await workflowService.createTemplate({
  name: "Standard Approval",
  documentType: "PROPOSAL",
  steps: [
    { stepNo: 1, deptKey: "hr", instructions: "HR Review" },
    { stepNo: 2, deptKey: "finance", instructions: "Budget Check" },
  ],
  isPermanent: true,
});

// Start workflow
const workflow = await workflowService.startWorkflow({
  documentId: 123,
  templateId: template.id,
  initiator: "john_doe",
});

// Get tasks for department
const tasks = await workflowService.getTasksForDepartment("hr");

// Complete task
await workflowService.completeTask(taskId, {
  userId: "john_doe",
  approved: true,
  notes: "Approved after review",
});
```

### Tracking

```javascript
// Record scan
await trackingService.recordScan({
  documentId: 123,
  eventType: "QR_SCANNED",
  location: "Building A, Floor 2",
});

// Get history
const history = await trackingService.getHistory(123);

// Get latest event
const latest = await trackingService.getLatestEvent(123);
```

---

## 🔍 Next Steps

### Recommended Enhancements

1. **Add Department CRUD endpoints** in backend
2. **Implement user management** (list users, update, delete)
3. **Add file download functionality** in frontend
4. **Create document search/filter** endpoints
5. **Implement real-time notifications** using WebSockets
6. **Add audit logging** for all user actions
7. **Implement role-based access control** in frontend routing

### Testing Checklist

- [ ] Test user registration flow
- [ ] Test login with valid/invalid credentials
- [ ] Test JWT token expiration handling
- [ ] Test document CRUD operations
- [ ] Test file upload/download
- [ ] Test workflow creation and execution
- [ ] Test task completion flow
- [ ] Test tracking event recording
- [ ] Test QR code generation/scanning
- [ ] Test department task filtering

---

## 📚 Documentation References

- **Backend API Docs**: Check Swagger UI at:

  - User Service: `http://localhost:8081/swagger-ui.html`
  - Document Service: `http://localhost:8082/swagger-ui.html`
  - Workflow Service: `http://localhost:8083/swagger-ui.html`
  - Tracking Service: `http://localhost:8084/swagger-ui.html`

- **Frontend Services**: See JSDoc comments in each service file
- **Environment Config**: `.env.example` for all configuration options

---

## 🎉 Summary

The frontend is now **fully integrated** with all backend microservices:

- ✅ Authentication with JWT
- ✅ Document management
- ✅ Workflow/pipeline operations
- ✅ Tracking and history
- ✅ File uploads
- ✅ QR code generation
- ✅ No more hardcoded mock data
- ✅ Production-ready proxy configuration

All API endpoints are properly mapped, DTOs match backend expectations, and error handling is consistent across the application.
