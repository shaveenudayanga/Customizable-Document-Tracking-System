# Authorization Header Flow in AdminUserManagement

## Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                   AdminUserManagement.jsx                        │
│                                                                  │
│  useEffect(() => {                                              │
│    loadUserProfile();  ────────────────────┐                   │
│  }, []);                                    │                   │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  const loadUserProfile = async () => {                          │
│    const userData = await userService.getCurrentUser();         │
│  };                                         │                   │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│               userService.js (Line 14-22)                       │
│                                                                  │
│  async getCurrentUser() {                                       │
│    return await authService.getProfile();  │                   │
│  }                                          │                   │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              authService.js (Line 93-104)                       │
│                                                                  │
│  async getProfile() {                                           │
│    const response = await api.get("/auth/profile");            │
│    return response;                        │                   │
│  }                                          │                   │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                   api.js (Line 36-50)                           │
│                                                                  │
│  async function request(method, path, body, options) {          │
│    const headers = { "Content-Type": "application/json" };     │
│                                                                  │
│    // AUTOMATIC AUTHORIZATION HEADER INJECTION                 │
│    const token = getAuthToken();  ────────┐                    │
│    if (token && !options.skipAuth) {      │                    │
│      headers["Authorization"] = `Bearer ${token}`;             │
│    }                                       │                    │
│                                            │                    │
│    return fetch(url, {                    │                    │
│      method: "GET",                       │                    │
│      headers,  ◄──────────────────────────┘                    │
│      body                                                       │
│    });                                                          │
│  }                                          │                   │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP REQUEST                                 │
│                                                                  │
│  GET /api/auth/profile HTTP/1.1                                │
│  Host: localhost:8080                                           │
│  Content-Type: application/json                                 │
│  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... │
│                                                                  │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway (Port 8080)                      │
│                                                                  │
│  Receives request with Authorization header                     │
│  Routes to user-service:8081/api/auth/profile                  │
│  Forwards Authorization header                                  │
│                                             │                   │
└─────────────────────────────────────────────┼───────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                User Service (Port 8081)                         │
│                                                                  │
│  @GetMapping("/api/auth/profile")                              │
│  public ResponseEntity<UserResponse> getProfile(                │
│    @RequestHeader("Authorization") String authHeader            │
│  ) {                                                            │
│    // Extract token from "Bearer <token>"                      │
│    String token = authHeader.substring(7);                     │
│    // Validate JWT                                             │
│    // Return user profile                                      │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

## Key Points

### 1. **No Manual Header Setting Required**
   - The `api.js` utility automatically adds the `Authorization` header
   - Developers don't need to manually set headers in component code
   - Token is retrieved from `localStorage.authToken`

### 2. **Token Storage** (Line 18-26 in api.js)
   ```javascript
   function getAuthToken() {
     return localStorage.getItem("authToken");
   }
   
   export function setAuthToken(token) {
     if (token) {
       localStorage.setItem("authToken", token);
     }
   }
   ```

### 3. **Authorization Header Format**
   ```
   Authorization: Bearer <JWT_TOKEN>
   ```
   - `Bearer` is the authentication scheme
   - Followed by a space
   - Then the actual JWT token

### 4. **When is the Header Added?**
   - **Always** added to all API requests
   - **Except** when `skipAuth: true` is passed in options
   - Used for public endpoints like login/register

### 5. **AdminUserManagement Specific Flow**
   ```javascript
   // In AdminUserManagement.jsx
   useEffect(() => {
     loadUserProfile();  // Called on component mount
   }, []);
   
   const loadUserProfile = async () => {
     // This internally calls:
     // authService.getProfile()
     //   → api.get("/auth/profile")
     //     → fetch with Authorization header
     const userData = await userService.getCurrentUser();
   };
   ```

## Example HTTP Request

When `AdminUserManagement` loads, this exact request is sent:

```http
GET /api/auth/profile HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTcyODQ5MzIwMH0.signature_here
```

## Verify in Browser

To see the Authorization header being sent:

1. Open Browser DevTools (F12)
2. Navigate to **Network** tab
3. Open Admin User Management page
4. Find the request to `/api/auth/profile`
5. Click on it
6. Go to **Headers** section
7. Look under **Request Headers**
8. You'll see: `Authorization: Bearer eyJhb...`

## Testing

Run the test script to verify:

```bash
./test-authorization-header.sh
```

This will test:
- ✅ Login and receive token
- ✅ Get profile WITH Authorization header (should succeed)
- ✅ Get profile WITHOUT Authorization header (should fail)
- ✅ Get profile with INVALID token (should fail)

## Security Considerations

1. **Token in localStorage**: Easy to implement but vulnerable to XSS
2. **Better approach**: Use httpOnly cookies (requires backend changes)
3. **Token expiration**: 401 responses trigger auto-logout
4. **HTTPS required**: Always use HTTPS in production
5. **Token refresh**: Consider implementing refresh token mechanism
