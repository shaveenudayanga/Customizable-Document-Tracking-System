import { workflowService } from "./workflowService.js";

/**
 * Pipeline Service
 * Handles pipeline (workflow template) operations
 * This is a convenience wrapper around workflow service for pipeline-related operations
 */
export const pipelineService = {
  /**
   * Create a new pipeline template
   * @param {Object} pipelineData
   * @param {string} pipelineData.name - Pipeline name
   * @param {string} [pipelineData.documentType] - Document type
   * @param {Array<Object>} pipelineData.steps - Pipeline steps
   * @param {boolean} [pipelineData.isPermanent] - Whether pipeline is permanent
   * @returns {Promise<Object>} Created pipeline
   */
  async createPipeline(pipelineData) {
    return await workflowService.createTemplate(pipelineData);
  },

  /**
   * List all pipelines (workflow templates)
   * @param {boolean} [permanent] - Filter by permanent flag
   * @returns {Promise<Array>} List of pipelines
   */
  async listPipelines(permanent = null) {
    return await workflowService.listTemplates(permanent);
  },

  /**
   * Get pipeline by ID
   * @param {number} id - Pipeline ID
   * @returns {Promise<Object>} Pipeline details
   */
  async getPipeline(id) {
    return await workflowService.getTemplate(id);
  },

  /**
   * Update a pipeline
   * @param {number} id - Pipeline ID
   * @param {Object} updateData - Pipeline update data
   * @returns {Promise<Object>} Updated pipeline
   */
  async updatePipeline(id, updateData) {
    return await workflowService.updateTemplate(id, updateData);
  },

  /**
   * Delete a pipeline
   * @param {number} id - Pipeline ID
   * @returns {Promise<void>}
   */
  async deletePipeline(id) {
    return await workflowService.deleteTemplate(id);
  },

  /**
   * Start a workflow using a pipeline
   * @param {number} documentId - Document ID
   * @param {number} pipelineId - Pipeline (template) ID
   * @param {string} [initiator] - User initiating the workflow
   * @returns {Promise<Object>} Started workflow response
   */
  async startWorkflow(documentId, pipelineId, initiator = null) {
    return await workflowService.startWorkflow({
      documentId,
      templateId: pipelineId,
      initiator,
    });
  },

  /**
   * Start a custom workflow (without using a template)
   * @param {number} documentId - Document ID
   * @param {Array<Object>} steps - Custom pipeline steps
   * @param {string} [initiator] - User initiating the workflow
   * @returns {Promise<Object>} Started workflow response
   */
  async startCustomWorkflow(documentId, steps, initiator = null) {
    return await workflowService.startWorkflow({
      documentId,
      customSteps: steps,
      initiator,
    });
  },

  /**
   * Get workflow status for a document
   * @param {number} documentId - Document ID
   * @returns {Promise<Object>} Workflow status
   */
  async getWorkflowStatus(documentId) {
    return await workflowService.getWorkflowStatus(documentId);
  },

  /**
   * Helper: Create a pipeline step
   * @param {number} stepNo - Step number
   * @param {string} deptKey - Department key
   * @param {string} [instructions] - Step instructions
   * @param {boolean} [notifyFlag] - Notification flag
   * @returns {Object} Pipeline step object
   */
  createStep(stepNo, deptKey, instructions = "", notifyFlag = false) {
    return workflowService.createPipelineStep(
      stepNo,
      deptKey,
      instructions,
      notifyFlag
    );
  },

  /**
   * Validate pipeline steps
   * @param {Array<Object>} steps - Pipeline steps to validate
   * @returns {Object} { valid: boolean, errors: Array<string> }
   */
  validateSteps(steps) {
    const errors = [];

    if (!Array.isArray(steps) || steps.length === 0) {
      errors.push("Pipeline must have at least one step");
      return { valid: false, errors };
    }

    const stepNumbers = new Set();
    steps.forEach((step, index) => {
      if (!step.stepNo || step.stepNo <= 0) {
        errors.push(
          `Step ${index + 1}: Step number must be a positive integer`
        );
      }
      if (stepNumbers.has(step.stepNo)) {
        errors.push(`Step ${index + 1}: Duplicate step number ${step.stepNo}`);
      }
      stepNumbers.add(step.stepNo);

      if (!step.deptKey || step.deptKey.trim() === "") {
        errors.push(`Step ${index + 1}: Department key is required`);
      }
    });

    return {
      valid: errors.length === 0,
      errors,
    };
  },
};
