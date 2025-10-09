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

  /**
   * Update user profile
   * @param {Object} profileData - Updated profile data
   * @returns {Promise<Object>}
   */
  async updateProfile(profileData) {
    try {
      const response = await api.put("/auth/profile", profileData);

      if (response) {
        authService.setCurrentUser({
          username: response.username,
          email: response.email,
          role: response.role,
          position: response.position,
          sectionId: response.sectionId,
        });
      }

      return response;
    } catch (error) {
      console.error("Error updating profile:", error);
      throw error;
    }
  },

  /**
   * Get all users (admin only)
   * @returns {Promise<Array>}
   */
  async getAllUsers() {
    try {
      return await api.get("/users");
    } catch (error) {
      console.error("Error fetching all users:", error);
      throw error;
    }
  },

  /**
   * Get user by ID
   * @param {number} userId
   * @returns {Promise<Object>}
   */
  async getUserById(userId) {
    try {
      return await api.get(`/users/${userId}`);
    } catch (error) {
      console.error(`Error fetching user ${userId}:`, error);
      throw error;
    }
  },
};
