import { trackingAPI } from "../lib/api.js";

/**
 * Tracking Service
 * Handles document tracking events and history
 * Matches backend tracking-service endpoints (port 8084)
 */
export const trackingService = {
  /**
   * Record a scan/tracking event
   * POST /api/tracking/scan
   * @param {Object} eventData
   * @param {number} eventData.documentId - Document ID (required)
   * @param {string} eventData.eventType - Event type (e.g., "SCANNED", "TRANSFERRED", "APPROVED")
   * @param {string} [eventData.location] - Location where event occurred
   * @param {string} [eventData.userId] - User ID who performed the action
   * @param {string} [eventData.notes] - Additional notes
   * @returns {Promise<Object>} Recorded event response
   */
  async recordScan(eventData) {
    try {
      return await trackingAPI.post("/tracking/scan", eventData);
    } catch (error) {
      console.error("Error recording scan:", error);
      throw error;
    }
  },

  /**
   * Get tracking history for a document
   * GET /api/tracking/history/{documentId}
   * @param {number} documentId - Document ID
   * @returns {Promise<Object>} Document history with all tracking events
   */
  async getHistory(documentId) {
    try {
      return await trackingAPI.get(`/tracking/history/${documentId}`);
    } catch (error) {
      console.error("Error fetching history:", error);
      throw error;
    }
  },

  /**
   * Get latest tracking event for a document
   * GET /api/tracking/latest/{documentId}
   * @param {number} documentId - Document ID
   * @returns {Promise<Object>} Latest tracking event
   */
  async getLatestEvent(documentId) {
    try {
      return await trackingAPI.get(`/tracking/latest/${documentId}`);
    } catch (error) {
      console.error("Error fetching latest event:", error);
      throw error;
    }
  },

  /**
   * Record a QR code scan
   * Convenience method for recording QR scan events
   * @param {number} documentId - Document ID
   * @param {Object} [additionalData] - Additional tracking data
   * @returns {Promise<Object>}
   */
  async recordQRScan(documentId, additionalData = {}) {
    return await this.recordScan({
      documentId,
      eventType: "QR_SCANNED",
      ...additionalData,
    });
  },

  /**
   * Record a document transfer event
   * @param {number} documentId - Document ID
   * @param {string} fromUser - Source user
   * @param {string} toUser - Destination user
   * @param {string} [notes] - Transfer notes
   * @returns {Promise<Object>}
   */
  async recordTransfer(documentId, fromUser, toUser, notes = "") {
    return await this.recordScan({
      documentId,
      eventType: "TRANSFERRED",
      notes: `Transfer from ${fromUser} to ${toUser}. ${notes}`,
    });
  },

  /**
   * Record document approval event
   * @param {number} documentId - Document ID
   * @param {string} approver - User who approved
   * @param {string} [notes] - Approval notes
   * @returns {Promise<Object>}
   */
  async recordApproval(documentId, approver, notes = "") {
    return await this.recordScan({
      documentId,
      eventType: "APPROVED",
      userId: approver,
      notes,
    });
  },

  /**
   * Record document rejection event
   * @param {number} documentId - Document ID
   * @param {string} rejector - User who rejected
   * @param {string} [reason] - Rejection reason
   * @returns {Promise<Object>}
   */
  async recordRejection(documentId, rejector, reason = "") {
    return await this.recordScan({
      documentId,
      eventType: "REJECTED",
      userId: rejector,
      notes: reason,
    });
  },
};
