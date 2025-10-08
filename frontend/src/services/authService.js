import { api, setAuthToken, clearAuth } from "../lib/api.js";

/**
 * Authentication Service
 * Handles login, registration, profile management, and JWT token operations
 * Matches backend user-service endpoints (port 8081)
 */
export const authService = {
  /**
   * Register a new user
   * POST /api/auth/register
   * @param {Object} userData - Registration data
   * @param {string} userData.username - Username
   * @param {string} userData.password - Password
   * @param {string} userData.email - Email address
   * @param {string} [userData.role] - User role (optional)
   * @param {string} [userData.position] - User position (optional)
   * @param {string} [userData.sectionId] - Section/Department ID (optional)
   * @returns {Promise<Object>} AuthResponse with token and user details
   */
  async register(userData) {
    try {
      const response = await api.post("/auth/register", userData, {
        skipAuth: true,
      });

      // Store token and user data
      if (response.token) {
        setAuthToken(response.token);
        this.setCurrentUser({
          username: response.username,
          email: response.email,
          role: response.role,
          position: response.position,
          sectionId: response.sectionId,
        });
      }

      return response;
    } catch (error) {
      console.error("Registration error:", error);
      throw error;
    }
  },

  /**
   * Login user
   * POST /api/auth/login
   * @param {Object} credentials
   * @param {string} credentials.username - Username
   * @param {string} credentials.password - Password
   * @returns {Promise<Object>} AuthResponse with token and user details
   */
  async login(credentials) {
    try {
      const response = await api.post("/auth/login", credentials, {
        skipAuth: true,
      });

      // Store token and user data
      if (response.token) {
        setAuthToken(response.token);
        this.setCurrentUser({
          username: response.username,
          email: response.email,
          role: response.role,
          position: response.position,
          sectionId: response.sectionId,
        });
      }

      return response;
    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  },

  /**
   * Logout user
   * Clears local token and user data
   */
  logout() {
    clearAuth();
  },

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
  },

  /**
   * Check if user is authenticated
   * @returns {boolean} True if user has a valid token
   */
  isAuthenticated() {
    return !!localStorage.getItem("authToken");
  },

  /**
   * Get current user from localStorage
   * @returns {Object|null} User object or null
   */
  getCurrentUser() {
    const userStr = localStorage.getItem("user");
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        return null;
      }
    }
    return null;
  },

  /**
   * Set current user in localStorage
   * @param {Object} user - User object
   */
  setCurrentUser(user) {
    if (user) {
      localStorage.setItem("user", JSON.stringify(user));
    } else {
      localStorage.removeItem("user");
    }
  },

  /**
   * Get authentication token
   * @returns {string|null} JWT token or null
   */
  getToken() {
    return localStorage.getItem("authToken");
  },
};
