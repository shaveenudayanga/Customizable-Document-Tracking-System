# Migration Guide: Frontend Backend Integration

## 🔄 Changes Overview

This guide helps you update your components to use the new backend-integrated services.

---

## Breaking Changes

### 1. Authentication

**OLD:**

```javascript
import { api } from "../lib/api";

const handleLogin = async () => {
  const data = await api.post("auth/login", { email, password });
  localStorage.setItem("accessToken", data.token);
};
```

**NEW:**

```javascript
import { authService } from "../services/authService";

const handleLogin = async () => {
  // authService handles token storage automatically
  await authService.login({ username, password });
  // Token is already stored, just navigate
  navigate("/dashboard");
};
```

### 2. User Data

**OLD:**

```javascript
const user = {
  id: "user1",
  name: "John",
  email: "john@example.com",
};
```

**NEW:**

```javascript
import { userService } from "../services/userService";

const user = await userService.getCurrentUser();
// Returns: { username, email, role, position, sectionId }
```

### 3. Document Operations

**OLD:**

```javascript
const docs = await api.get("/documents");
```

**NEW:**

```javascript
import { documentService } from "../services/documentService";

const docs = await documentService.getAllDocuments();
// Returns array of DocumentResponse with proper typing
```

---

## Field Name Changes

### Registration/Login Forms

| Old Field | New Field   | Type              | Notes                                    |
| --------- | ----------- | ----------------- | ---------------------------------------- |
| `name`    | `username`  | string            | Backend uses username for authentication |
| `email`   | `email`     | string            | Still required but not for login         |
| -         | `position`  | string (optional) | New field for user position              |
| -         | `sectionId` | string (optional) | New field for department/section         |

### Document Objects

| Old Field | New Field           | Type          | Notes                           |
| --------- | ------------------- | ------------- | ------------------------------- |
| `status`  | `statuses`          | Array<string> | Now an array of statuses        |
| `owner`   | `ownerUserId`       | UUID string   | Now a proper UUID               |
| `id`      | `id`                | Long (number) | Keep as number                  |
| -         | `processInstanceId` | string        | New field for workflow tracking |
| -         | `qrPath`            | string        | QR code file path               |
| -         | `fileDir`           | string        | Document file directory         |

---

## Component Update Patterns

### Pattern 1: Authentication Components

```javascript
// Before
import { api } from "../lib/api";

const Login = () => {
  const handleSubmit = async () => {
    const response = await api.post("/auth/login", { email, password });
    localStorage.setItem("token", response.token);
    localStorage.setItem("user", JSON.stringify(response.user));
  };
};

// After
import { authService } from "../services/authService";

const Login = () => {
  const handleSubmit = async () => {
    // authService handles everything
    await authService.login({ username, password });
  };
};
```

### Pattern 2: Data Fetching Components

```javascript
// Before
const [documents, setDocuments] = useState([]);

useEffect(() => {
  api.get("/documents").then(setDocuments).catch(console.error);
}, []);

// After
import { documentService } from "../services/documentService";

const [documents, setDocuments] = useState([]);

useEffect(() => {
  documentService.getAllDocuments().then(setDocuments).catch(console.error);
}, []);
```

### Pattern 3: Form Submission

```javascript
// Before
const handleCreate = async (formData) => {
  await api.post("/documents", {
    title: formData.title,
    status: formData.status,
    owner: currentUser.id,
  });
};

// After
import { documentService } from "../services/documentService";
import { userService } from "../services/userService";

const handleCreate = async (formData) => {
  const currentUser = userService.getCachedCurrentUser();

  await documentService.createDocument({
    title: formData.title,
    documentType: formData.type,
    description: formData.description,
    ownerUserId: currentUser.id, // Should be UUID string
  });
};
```

---

## Service Method Reference

### authService

```javascript
// Login
await authService.login({ username, password });

// Register
await authService.register({
  username,
  email,
  password,
  role, // optional
  position, // optional
  sectionId, // optional
});

// Get profile (from API)
const user = await authService.getProfile();

// Check if logged in
const isLoggedIn = authService.isAuthenticated();

// Get cached user (no API call)
const user = authService.getCurrentUser();

// Logout
authService.logout();
```

### documentService

```javascript
// List all documents
const docs = await documentService.getAllDocuments();

// Get by ID
const doc = await documentService.getDocumentById(123);

// Create
const newDoc = await documentService.createDocument({
  title: "My Document",
  documentType: "PROPOSAL",
  description: "Description here",
  ownerUserId: "uuid-here",
});

// Create with file
const docWithFile = await documentService.createDocumentWithFile(
  metadata,
  fileObject
);

// Update status
await documentService.updateDocumentStatus(docId, ["REVIEWED", "APPROVED"]);

// Get QR code
const qrUrl = documentService.getDownloadUrl(docId);
// or
const qrBase64 = await documentService.getDocumentQRCode(docId, true);
```

### workflowService

```javascript
// Create template
const template = await workflowService.createTemplate({
  name: "Approval Pipeline",
  documentType: "PROPOSAL",
  steps: [
    { stepNo: 1, deptKey: "hr", instructions: "HR Review" },
    { stepNo: 2, deptKey: "finance", instructions: "Budget Check" },
  ],
  isPermanent: true,
});

// Start workflow
await workflowService.startWorkflow({
  documentId: 123,
  templateId: template.id,
});

// Get department tasks
const tasks = await workflowService.getTasksForDepartment("hr");

// Complete task
await workflowService.completeTask(taskId, {
  userId: "john_doe",
  approved: true,
  notes: "Looks good",
});
```

### trackingService

```javascript
// Record scan
await trackingService.recordScan({
  documentId: 123,
  eventType: "QR_SCANNED",
  location: "Building A",
});

// Get history
const history = await trackingService.getHistory(123);

// Get latest event
const latest = await trackingService.getLatestEvent(123);

// Convenience methods
await trackingService.recordQRScan(docId, { location: "Office" });
await trackingService.recordApproval(docId, userId, "Approved");
```

---

## Common Pitfalls

### ❌ Don't: Manually manage tokens

```javascript
// Bad
localStorage.setItem("token", response.token);
```

### ✅ Do: Use authService

```javascript
// Good
await authService.login(credentials);
// Token is automatically stored
```

---

### ❌ Don't: Use old field names

```javascript
// Bad
const doc = {
  owner: user.name,
  status: "Pending",
};
```

### ✅ Do: Use correct field names

```javascript
// Good
const doc = {
  ownerUserId: user.id, // UUID
  statuses: ["PENDING"], // Array
};
```

---

### ❌ Don't: Import api directly in components

```javascript
// Bad
import { api } from "../lib/api";
const docs = await api.get("/documents");
```

### ✅ Do: Use service methods

```javascript
// Good
import { documentService } from "../services/documentService";
const docs = await documentService.getAllDocuments();
```

---

## Testing Your Changes

1. **Test Authentication:**

   ```bash
   # Start backend services
   # Open http://localhost:5173/login
   # Try logging in with a test user
   ```

2. **Check Network Tab:**

   - Requests should go to correct ports (8081-8084)
   - Headers should include `Authorization: Bearer {token}`
   - Response formats should match DTOs

3. **Verify Token Handling:**
   - Login → Check localStorage for "authToken"
   - Make authenticated request → Token should be in headers
   - Logout → Token should be removed

---

## Need Help?

- Check `BACKEND_INTEGRATION.md` for detailed API documentation
- Review service files for JSDoc comments
- Check Swagger UI at `http://localhost:808X/swagger-ui.html`
- Look at updated `Login.jsx` and `Onboarding.jsx` for examples

---

## Rollback Plan

If you need to temporarily revert:

1. Backend services not running → Components will show errors
2. To add mock fallbacks, wrap service calls in try-catch:

```javascript
try {
  return await documentService.getAllDocuments();
} catch (error) {
  console.error("API failed, using fallback");
  return MOCK_DATA; // Your mock data
}
```

**Note:** All mock data has been removed. You'll need to add it back if reverting.
