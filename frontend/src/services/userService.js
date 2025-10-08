import { api } from "../lib/api.js";

// User Service - Replace hard-coded user data with API calls
export const userService = {
  // Get current user profile
  async getCurrentUser() {
    try {
      return await api.get("/users/me");
    } catch (error) {
      console.error("Error fetching current user:", error);
      return this.getMockCurrentUser();
    }
  },

  // Get all users (admin only)
  async getAllUsers() {
    try {
      return await api.get("/users");
    } catch (error) {
      console.error("Error fetching users:", error);
      return this.getMockUsers();
    }
  },

  // Update user profile
  async updateUser(id, userData) {
    try {
      return await api.put(`/users/${id}`, userData);
    } catch (error) {
      console.error("Error updating user:", error);
      throw error;
    }
  },

  // Mock data fallbacks
  getMockCurrentUser() {
    return {
      id: "user1",
      name: "Sadish",
      email: "sadish@doctutrace.com",
      role: "admin",
      department: "IT",
      avatar: "/path/to/avatar.jpg",
    };
  },

  getMockUsers() {
    return [
      {
        id: "user1",
        name: "Alice Johnson",
        email: "alice@company.com",
        role: "user",
        department: "Sales",
        status: "active",
      },
      {
        id: "user2",
        name: "Bob Smith",
        email: "bob@company.com",
        role: "user",
        department: "Marketing",
        status: "active",
      },
    ];
  },
};
