import { userAPI, notificationAPI } from "../lib/api.js";

/**
 * Notification Service
 * Handles user notifications, system events, and document events
 */
export const notificationService = {
  /**
   * Get user notifications from user service
   * GET /api/notifications
   * @returns {Promise<Array>} Array of user notifications
   */
  async getUserNotifications() {
    try {
      return await userAPI.get("/notifications");
    } catch (error) {
      console.error("Error fetching user notifications:", error);
      throw error;
    }
  },

  /**
   * Create a new notification
   * POST /api/notifications
   * @param {Object} notificationData
   * @param {string} notificationData.username - Target username
   * @param {string} notificationData.title - Notification title
   * @param {string} notificationData.message - Notification message
   * @param {string} notificationData.type - Notification type (INFO, WARNING, ERROR, SUCCESS)
   * @returns {Promise<Object>} Created notification
   */
  async createNotification(notificationData) {
    try {
      return await userAPI.post("/notifications", notificationData);
    } catch (error) {
      console.error("Error creating notification:", error);
      throw error;
    }
  },

  /**
   * Mark notification as read
   * PUT /api/notifications/{id}/read
   * @param {number} notificationId - Notification ID
   * @returns {Promise<void>}
   */
  async markAsRead(notificationId) {
    try {
      return await userAPI.put(`/notifications/${notificationId}/read`);
    } catch (error) {
      console.error("Error marking notification as read:", error);
      throw error;
    }
  },

  /**
   * Get document events from notification service
   * GET /api/document-events
   * @returns {Promise<Array>} Array of document events
   */
  async getDocumentEvents() {
    try {
      return await notificationAPI.get("/document-events");
    } catch (error) {
      console.error("Error fetching document events:", error);
      throw error;
    }
  },

  /**
   * Create a document event
   * POST /api/document-events
   * @param {Object} eventData
   * @param {string} eventData.eventId - Unique event ID
   * @param {string} eventData.eventType - Type of event
   * @param {Object} eventData.data - Event data
   * @returns {Promise<Object>} Created event
   */
  async createDocumentEvent(eventData) {
    try {
      return await notificationAPI.post("/document-events", eventData);
    } catch (error) {
      console.error("Error creating document event:", error);
      throw error;
    }
  },

  /**
   * Get unread notification count
   * @returns {Promise<number>} Number of unread notifications
   */
  async getUnreadCount() {
    try {
      const notifications = await this.getUserNotifications();
      return notifications.filter((n) => !n.read).length;
    } catch (error) {
      console.error("Error getting unread count:", error);
      return 0;
    }
  },

  /**
   * Subscribe to real-time notifications (placeholder for WebSocket implementation)
   * @param {Function} onNotification - Callback for new notifications
   */
  subscribeToNotifications(onNotification) {
    // TODO: Implement WebSocket connection for real-time notifications
    console.log("Real-time notifications subscription (to be implemented)");

    // For now, we can poll for notifications periodically
    return setInterval(async () => {
      try {
        const notifications = await this.getUserNotifications();
        const unread = notifications.filter((n) => !n.read);
        if (unread.length > 0) {
          onNotification(unread);
        }
      } catch (error) {
        console.error("Error polling notifications:", error);
      }
    }, 30000); // Poll every 30 seconds
  },

  /**
   * Unsubscribe from notifications
   * @param {number} subscriptionId - ID returned from subscribeToNotifications
   */
  unsubscribeFromNotifications(subscriptionId) {
    if (subscriptionId) {
      clearInterval(subscriptionId);
    }
  },
};

export default notificationService;
