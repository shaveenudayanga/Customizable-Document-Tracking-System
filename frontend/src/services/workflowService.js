import { workflowAPI } from "../lib/api.js";

/**
 * Workflow Service
 * Handles workflow/pipeline templates, workflow instances, and task management
 * Matches backend workflow-service endpoints (port 8083)
 */
export const workflowService = {
  /**
   * Create a workflow template
   * POST /api/workflow/create
   * @param {Object} templateData
   * @param {string} templateData.name - Template name (required)
   * @param {string} [templateData.documentType] - Document type this template applies to
   * @param {Array<Object>} templateData.steps - Pipeline steps (required, non-empty)
   * @param {number} templateData.steps[].stepNo - Step number (positive integer)
   * @param {string} templateData.steps[].deptKey - Department key (required)
   * @param {string} [templateData.steps[].instructions] - Step instructions
   * @param {boolean} [templateData.steps[].notifyFlag] - Notification flag
   * @param {boolean} [templateData.isPermanent] - Whether template is permanent
   * @returns {Promise<Object>} Created template response
   */
  async createTemplate(templateData) {
    try {
      return await workflowAPI.post("/workflow/create", templateData);
    } catch (error) {
      console.error("Error creating template:", error);
      throw error;
    }
  },

  /**
   * Start a workflow instance
   * POST /api/workflow/start
   * @param {Object} workflowData
   * @param {number} workflowData.documentId - Document ID (required)
   * @param {number} [workflowData.templateId] - Template ID to use
   * @param {Array<Object>} [workflowData.customSteps] - Custom steps (if not using template)
   * @param {string} [workflowData.initiator] - User initiating the workflow
   * @returns {Promise<Object>} Started workflow response with processInstanceId
   */
  async startWorkflow(workflowData) {
    try {
      return await workflowAPI.post("/workflow/start", workflowData);
    } catch (error) {
      console.error("Error starting workflow:", error);
      throw error;
    }
  },

  /**
   * Get tasks for a department
   * GET /api/workflow/tasks?deptKey={deptKey}
   * @param {string} deptKey - Department key
   * @returns {Promise<Array>} List of active tasks for the department
   */
  async getTasksForDepartment(deptKey) {
    try {
      return await workflowAPI.get(`/workflow/tasks?deptKey=${deptKey}`);
    } catch (error) {
      console.error("Error fetching tasks:", error);
      throw error;
    }
  },

  /**
   * Complete a task
   * POST /api/workflow/tasks/{taskId}/complete
   * @param {string} taskId - Camunda task ID
   * @param {Object} completionData
   * @param {string} completionData.userId - User completing the task (required)
   * @param {string} [completionData.notes] - Completion notes
   * @param {boolean} completionData.approved - Approval decision (required)
   * @returns {Promise<void>}
   */
  async completeTask(taskId, completionData) {
    try {
      await workflowAPI.post(
        `/workflow/tasks/${taskId}/complete`,
        completionData
      );
    } catch (error) {
      console.error("Error completing task:", error);
      throw error;
    }
  },

  /**
   * Get BPMN XML for a process definition
   * GET /api/workflow/definitions/{key}/xml
   * @param {string} key - Process definition key
   * @returns {Promise<string>} BPMN XML string
   */
  async getBpmnXml(key) {
    try {
      return await workflowAPI.get(`/workflow/definitions/${key}/xml`);
    } catch (error) {
      console.error("Error fetching BPMN XML:", error);
      throw error;
    }
  },

  /**
   * Get workflow status for a document
   * GET /api/workflow/documents/{documentId}/status
   * @param {number} documentId - Document ID
   * @returns {Promise<Object>} Workflow status with active activities and tasks
   */
  async getWorkflowStatus(documentId) {
    try {
      return await workflowAPI.get(`/workflow/documents/${documentId}/status`);
    } catch (error) {
      console.error("Error fetching workflow status:", error);
      throw error;
    }
  },

  /**
   * List workflow templates
   * GET /api/workflow/templates?permanent={permanent}
   * @param {boolean} [permanent] - Filter by permanent flag (optional)
   * @returns {Promise<Array>} List of templates
   */
  async listTemplates(permanent = null) {
    try {
      const query = permanent !== null ? `?permanent=${permanent}` : "";
      return await workflowAPI.get(`/workflow/templates${query}`);
    } catch (error) {
      console.error("Error listing templates:", error);
      throw error;
    }
  },

  /**
   * Get template details
   * GET /api/workflow/templates/{id}
   * @param {number} id - Template ID
   * @returns {Promise<Object>} Template details with steps
   */
  async getTemplate(id) {
    try {
      return await workflowAPI.get(`/workflow/templates/${id}`);
    } catch (error) {
      console.error("Error fetching template:", error);
      throw error;
    }
  },

  /**
   * Update a workflow template
   * PUT /api/workflow/templates/{id}
   * @param {number} id - Template ID
   * @param {Object} updateData - Template update data
   * @returns {Promise<Object>} Updated template
   */
  async updateTemplate(id, updateData) {
    try {
      return await workflowAPI.put(`/workflow/templates/${id}`, updateData);
    } catch (error) {
      console.error("Error updating template:", error);
      throw error;
    }
  },

  /**
   * Delete a workflow template
   * DELETE /api/workflow/templates/{id}
   * @param {number} id - Template ID
   * @returns {Promise<void>}
   */
  async deleteTemplate(id) {
    try {
      await workflowAPI.delete(`/workflow/templates/${id}`);
    } catch (error) {
      console.error("Error deleting template:", error);
      throw error;
    }
  },

  /**
   * Helper: Create a pipeline step object
   * @param {number} stepNo - Step number
   * @param {string} deptKey - Department key
   * @param {string} [instructions] - Instructions
   * @param {boolean} [notifyFlag] - Notification flag
   * @returns {Object} Pipeline step
   */
  createPipelineStep(stepNo, deptKey, instructions = "", notifyFlag = false) {
    return {
      stepNo,
      deptKey,
      instructions,
      notifyFlag,
    };
  },
};
