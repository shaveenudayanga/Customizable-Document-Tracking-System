import { api } from "../lib/api.js";
import { authService } from "./authService.js";

/**
 * User Service
 * Handles user-related operations beyond authentication
 * Uses authService for authentication operations
 */
export const userService = {
  /**
   * Get current user profile
   * Uses authService to fetch from backend
   * @returns {Promise<Object>} User profile
   */
  async getCurrentUser() {
    try {
      return await authService.getProfile();
    } catch (error) {
      console.error("Error fetching current user:", error);
      // Return cached user if API fails
      return authService.getCurrentUser();
    }
  },

  /**
   * Get cached current user without API call
   * @returns {Object|null} Cached user object
   */
  getCachedCurrentUser() {
    return authService.getCurrentUser();
  },

  /**
   * Check if user is authenticated
   * @returns {boolean}
   */
  isAuthenticated() {
    return authService.isAuthenticated();
  },

  /**
   * Login user
   * @param {Object} credentials
   * @param {string} credentials.username
   * @param {string} credentials.password
   * @returns {Promise<Object>}
   */
  async login(credentials) {
    return await authService.login(credentials);
  },

  /**
   * Register new user
   * @param {Object} userData - Registration data
   * @returns {Promise<Object>}
   */
  async register(userData) {
    return await authService.register(userData);
  },

  /**
   * Logout current user
   */
  logout() {
    authService.logout();
  },

  // Additional user management methods can be added here
  // when backend provides endpoints for user CRUD operations
};
