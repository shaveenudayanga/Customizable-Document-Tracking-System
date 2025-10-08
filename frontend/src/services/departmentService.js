import { workflowService } from "./workflowService.js";
import { api } from "../lib/api.js";

/**
 * Department Service
 * Handles department-related operations
 * Can fetch from backend API or use predefined list as fallback
 */
export const departmentService = {
  /**
   * Get all departments from backend API
   * Falls back to predefined list if API fails
   * @returns {Promise<Array>} List of departments
   */
  async getAllDepartments() {
    try {
      // Try to fetch from backend API
      const departments = await api.get("/departments");
      return departments;
    } catch (error) {
      console.warn("Failed to fetch departments from API, using fallback list:", error);
      // Fallback to predefined list
      return this.getDepartments();
    }
  },
  /**
   * Get tasks for a specific department
   * Uses workflow service under the hood
   * @param {string} deptKey - Department key (e.g., "hr", "finance", "legal")
   * @returns {Promise<Array>} List of tasks for the department
   */
  async getTasksForDepartment(deptKey) {
    return await workflowService.getTasksForDepartment(deptKey);
  },

  /**
   * Get predefined department list
   * This is a client-side list until backend provides an endpoint
   * @returns {Array<Object>} List of departments
   */
  getDepartments() {
    return [
      {
        key: "hr",
        name: "Human Resources",
        description: "Employee and personnel management",
      },
      {
        key: "finance",
        name: "Finance",
        description: "Financial operations and accounting",
      },
      {
        key: "legal",
        name: "Legal",
        description: "Legal review and compliance",
      },
      { key: "it", name: "IT", description: "Information Technology" },
      {
        key: "operations",
        name: "Operations",
        description: "Business operations",
      },
      {
        key: "admin",
        name: "Administration",
        description: "General administration",
      },
      {
        key: "procurement",
        name: "Procurement",
        description: "Purchasing and procurement",
      },
      {
        key: "management",
        name: "Management",
        description: "Executive management",
      },
    ];
  },

  /**
   * Get department by key
   * @param {string} key - Department key
   * @returns {Object|null} Department object or null if not found
   */
  getDepartmentByKey(key) {
    const departments = this.getDepartments();
    return departments.find((dept) => dept.key === key) || null;
  },

  /**
   * Get department name by key
   * @param {string} key - Department key
   * @returns {string} Department name or the key if not found
   */
  getDepartmentName(key) {
    const dept = this.getDepartmentByKey(key);
    return dept ? dept.name : key;
  },
};
