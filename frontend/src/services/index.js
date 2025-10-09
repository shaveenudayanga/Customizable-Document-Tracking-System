/**
 * Service Index
 * Centralized export of all service modules for cleaner imports
 *
 * Usage:
 * import { authService, documentService, workflowService } from '@/services';
 */

export { authService } from "./authService.js";
export { userService } from "./userService.js";
export { documentService } from "./documentService.js";
export { trackingService } from "./trackingService.js";
export { workflowService } from "./workflowService.js";
export { pipelineService } from "./pipelineService.js";
export { departmentService } from "./departmentService.js";
export { notificationService } from "./notificationService.js";

// Legacy service exports (if any still exist)
// These can be gradually deprecated
export { default as mockApiService } from "./mockApiService.js";
