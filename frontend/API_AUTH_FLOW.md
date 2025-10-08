# Authentication & Authorization Flow

## How Authorization Headers are Passed

### Current Implementation

The application automatically adds the `Authorization` header to all API requests through the centralized `api.js` utility.

#### Flow:

1. **User Login** (`POST /api/auth/login`)
   - User submits credentials
   - Backend returns JWT token
   - Token is stored in `localStorage` as `authToken`
   - User data is stored in `localStorage` as `user`

2. **Subsequent API Calls** (e.g., `GET /api/auth/profile`)
   - `authService.getProfile()` calls `api.get("/auth/profile")`
   - `api.js` automatically retrieves token from `localStorage`
   - Adds header: `Authorization: Bearer ${token}`
   - Request is sent to backend with JWT token

3. **Backend Validation**
   - API Gateway or service receives request
   - Extracts JWT from Authorization header
   - Validates token signature and expiration
   - Allows or denies request based on validation

### Code Reference

#### `frontend/src/lib/api.js` (lines 36-50)
```javascript
async function request(method, path, body, options = {}) {
  const url = buildUrl(path);
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  // Add JWT token if available and not explicitly excluded
  const token = getAuthToken();
  if (token && !options.skipAuth) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method,
    headers,
    credentials: "include",
    body: body ? JSON.stringify(body) : undefined,
  });
  // ... rest of request handling
}
```

#### `frontend/src/services/authService.js` (lines 93-104)
```javascript
/**
 * Get current user profile
 * GET /api/auth/profile
 * Requires JWT token in Authorization header
 * @returns {Promise<Object>} User profile data
 */
async getProfile() {
  try {
    const response = await api.get("/auth/profile");
    // Update cached user data
    this.setCurrentUser(response);
    return response;
  } catch (error) {
    console.error("Error fetching profile:", error);
    throw error;
  }
}
```

### How AdminUserManagement Uses This

#### `frontend/src/Pages/Admin/AdminUserManagement.jsx` (lines 33-72)
```javascript
const loadUserProfile = async () => {
  try {
    setLoading(true);
    setError(null);
    // This call automatically includes Authorization header
    const userData = await userService.getCurrentUser();
    
    if (userData) {
      setUsername(userData.username || "");
      setEmail(userData.email || "");
      setRole(userData.role || "USER");
      setPosition(userData.position || "");
      setSectionId(userData.sectionId || "");
      // ... rest of profile loading
    }
  } catch (err) {
    console.error("Error loading user profile:", err);
    setError("Failed to load user profile. Please try again.");
  } finally {
    setLoading(false);
  }
};
```

### Request Example

When `AdminUserManagement` component loads, it makes this request:

```
GET /api/auth/profile HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Handling

- **Storage**: JWT token stored in `localStorage.authToken`
- **Retrieval**: `getAuthToken()` function in `api.js`
- **Injection**: Automatically added to all requests (except those with `skipAuth: true`)
- **Expiration**: 401 responses trigger automatic logout and redirect to login

### Public Endpoints (No Auth Required)

To skip Authorization header for public endpoints:

```javascript
const response = await api.post("/auth/login", credentials, {
  skipAuth: true  // Don't send Authorization header
});
```

This is used for:
- `POST /api/auth/login`
- `POST /api/auth/register`

### Security Features

1. **Automatic Token Injection**: No manual header setting needed
2. **401 Handling**: Auto-logout on unauthorized responses
3. **Token Refresh**: Can be implemented in api.js middleware
4. **CORS**: Credentials included for cross-origin requests
5. **XSS Protection**: Token in localStorage (consider httpOnly cookies for production)

### Testing Authorization

You can verify the Authorization header is being sent using browser DevTools:

1. Open Browser DevTools (F12)
2. Go to Network tab
3. Navigate to Admin User Management page
4. Look for request to `/api/auth/profile`
5. Check Request Headers - should see `Authorization: Bearer ...`

### Backend Expected Format

The backend expects:
```
Authorization: Bearer <JWT_TOKEN>
```

Where `<JWT_TOKEN>` is the token returned from login/register endpoints.
