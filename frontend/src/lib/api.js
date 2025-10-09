// Multi-service API configuration for different backend services
const SERVICES = {
  USER: import.meta.env.VITE_USER_SERVICE_URL || "http://localhost:8081/api",
  DOCUMENT:
    import.meta.env.VITE_DOCUMENT_SERVICE_URL || "http://localhost:8082/api",
  WORKFLOW:
    import.meta.env.VITE_WORKFLOW_SERVICE_URL || "http://localhost:8083/api",
  TRACKING:
    import.meta.env.VITE_TRACKING_SERVICE_URL || "http://localhost:8084/api",
  NOTIFICATION:
    import.meta.env.VITE_NOTIFICATION_SERVICE_URL ||
    "http://localhost:8085/api",
};

const BASE = import.meta.env.VITE_API_URL || "/api";

const isAbsolute = (u) => /^https?:\/\//i.test(u);
const trimSlashEnd = (s) => s.replace(/\/+$/, "");
const trimSlashStart = (s) => s.replace(/^\/+/, "");

function buildUrl(path, service = null) {
  const cleanPath = trimSlashStart(path || "");

  // If service is specified, use the service-specific URL
  if (service && SERVICES[service]) {
    return `${trimSlashEnd(SERVICES[service])}/${cleanPath}`;
  }

  // Otherwise use the default base URL
  if (isAbsolute(BASE)) {
    return `${trimSlashEnd(BASE)}/${cleanPath}`;
  }
  // Relative base like "/api"
  const basePath = `/${trimSlashStart(BASE)}`;
  return `${trimSlashEnd(basePath)}/${cleanPath}`;
}

// Get JWT token from localStorage
function getAuthToken() {
  return localStorage.getItem("authToken");
}

// Set JWT token in localStorage
export function setAuthToken(token) {
  if (token) {
    localStorage.setItem("authToken", token);
  } else {
    localStorage.removeItem("authToken");
  }
}

// Clear authentication
export function clearAuth() {
  localStorage.removeItem("authToken");
  localStorage.removeItem("user");
}

async function request(method, path, body, options = {}) {
  const url = buildUrl(path, options.service);
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

  const text = await res.text();
  const data = text
    ? (() => {
        try {
          return JSON.parse(text);
        } catch {
          return { message: text };
        }
      })()
    : null;

  if (!res.ok) {
    // Handle 401 Unauthorized - token expired or invalid
    if (res.status === 401) {
      clearAuth();
      // Optionally redirect to login
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    const err = new Error(data?.message || data?.error || "Request failed");
    err.response = { status: res.status, data };
    throw err;
  }
  return data;
}

// Multipart form data request (for file uploads)
async function uploadRequest(method, path, formData, service = null) {
  const url = buildUrl(path, service);
  const headers = {};

  // Add JWT token if available
  const token = getAuthToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(url, {
    method,
    headers,
    credentials: "include",
    body: formData,
  });

  const text = await res.text();
  const data = text
    ? (() => {
        try {
          return JSON.parse(text);
        } catch {
          return { message: text };
        }
      })()
    : null;

  if (!res.ok) {
    if (res.status === 401) {
      clearAuth();
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    const err = new Error(data?.message || data?.error || "Upload failed");
    err.response = { status: res.status, data };
    throw err;
  }
  return data;
}

export const api = {
  get: (path, options) => request("GET", path, null, options),
  post: (path, body, options) => request("POST", path, body, options),
  put: (path, body, options) => request("PUT", path, body, options),
  patch: (path, body, options) => request("PATCH", path, body, options),
  delete: (path, options) => request("DELETE", path, null, options),
  upload: (path, formData, service) =>
    uploadRequest("POST", path, formData, service),
};

// Service-specific API instances
export const userAPI = {
  get: (path, options = {}) =>
    request("GET", path, null, { ...options, service: "USER" }),
  post: (path, body, options = {}) =>
    request("POST", path, body, { ...options, service: "USER" }),
  put: (path, body, options = {}) =>
    request("PUT", path, body, { ...options, service: "USER" }),
  delete: (path, options = {}) =>
    request("DELETE", path, null, { ...options, service: "USER" }),
};

export const documentAPI = {
  get: (path, options = {}) =>
    request("GET", path, null, { ...options, service: "DOCUMENT" }),
  post: (path, body, options = {}) =>
    request("POST", path, body, { ...options, service: "DOCUMENT" }),
  put: (path, body, options = {}) =>
    request("PUT", path, body, { ...options, service: "DOCUMENT" }),
  delete: (path, options = {}) =>
    request("DELETE", path, null, { ...options, service: "DOCUMENT" }),
  upload: (path, formData) => uploadRequest("POST", path, formData, "DOCUMENT"),
};

export const workflowAPI = {
  get: (path, options = {}) =>
    request("GET", path, null, { ...options, service: "WORKFLOW" }),
  post: (path, body, options = {}) =>
    request("POST", path, body, { ...options, service: "WORKFLOW" }),
  put: (path, body, options = {}) =>
    request("PUT", path, body, { ...options, service: "WORKFLOW" }),
  delete: (path, options = {}) =>
    request("DELETE", path, null, { ...options, service: "WORKFLOW" }),
};

export const trackingAPI = {
  get: (path, options = {}) =>
    request("GET", path, null, { ...options, service: "TRACKING" }),
  post: (path, body, options = {}) =>
    request("POST", path, body, { ...options, service: "TRACKING" }),
  put: (path, body, options = {}) =>
    request("PUT", path, body, { ...options, service: "TRACKING" }),
  delete: (path, options = {}) =>
    request("DELETE", path, null, { ...options, service: "TRACKING" }),
};

export const notificationAPI = {
  get: (path, options = {}) =>
    request("GET", path, null, { ...options, service: "NOTIFICATION" }),
  post: (path, body, options = {}) =>
    request("POST", path, body, { ...options, service: "NOTIFICATION" }),
  put: (path, body, options = {}) =>
    request("PUT", path, body, { ...options, service: "NOTIFICATION" }),
  delete: (path, options = {}) =>
    request("DELETE", path, null, { ...options, service: "NOTIFICATION" }),
};
