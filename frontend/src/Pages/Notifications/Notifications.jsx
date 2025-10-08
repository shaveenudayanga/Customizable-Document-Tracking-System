import React, { useState, useEffect } from "react";
import {
  Bell,
  Clock,
  CheckCircle,
  XCircle,
  FileText,
  ArrowRight,
  AlertCircle,
} from "lucide-react";
import "../../styles/Notifications.css";
// Ensure your main CSS file (AdminUserManagement.css) is imported by the parent layout

// Helper function to map notification type to icon and color
const getNotificationMetadata = (type) => {
  switch (type) {
    case "approval":
      return { icon: FileText, color: "var(--color-primary)" }; // Soft purple for action items
    case "update":
      return { icon: CheckCircle, color: "#28a745" }; // Green for success
    case "alert":
      return { icon: XCircle, color: "#dc3545" }; // Red for errors/alerts
    case "success":
      return { icon: CheckCircle, color: "var(--color-primary)" };
    default:
      return { icon: Bell, color: "var(--color-text-light)" };
  }
};

// --- Sub-Component: Individual Notification Item ---
const NotificationItem = ({ notification, markAsRead }) => {
  const { icon: Icon, color } = getNotificationMetadata(notification.type);

  return (
    <div
      className={`notification-item ${notification.read ? "read" : "unread"}`}
      onClick={() => !notification.read && markAsRead(notification.id)}
      style={{
        "--item-animation-delay": `${notification.id * 0.1}s`,
        "--icon-color": color,
      }}
    >
      <div
        className="notification-icon"
        style={{ color: color, borderColor: color }}
      >
        <Icon size={20} />
      </div>

      <div className="notification-content">
        <div className="notification-title">{notification.title}</div>
        <div className="notification-message">{notification.message}</div>
        <div className="notification-footer">
          <Clock size={12} />
          <span>{notification.time}</span>
        </div>
      </div>

      {/* Read/Unread Status Badge */}
      {!notification.read && (
        <div className="unread-dot" title="New notification"></div>
      )}

      <ArrowRight size={20} className="arrow-icon" />
    </div>
  );
};

// --- Main Component: Notification Panel ---
const NotificationPanel = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load notifications from API
  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      setError(null);
      // TODO: Replace with actual API call when backend endpoint is ready
      // const response = await fetch('/api/notifications', {
      //   headers: {
      //     'Authorization': `Bearer ${localStorage.getItem('token')}`
      //   }
      // });
      // const data = await response.json();
      // setNotifications(data);
      
      // For now, use empty array until API is implemented
      setNotifications([]);
    } catch (err) {
      console.error("Error loading notifications:", err);
      setError("Failed to load notifications. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
    // TODO: Update read status on backend
    // fetch(`/api/notifications/${id}/read`, { method: 'PUT', ... });
  };

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    // TODO: Update all as read on backend
    // fetch('/api/notifications/read-all', { method: 'PUT', ... });
  };

  if (loading) {
    return (
      <div className="notification-panel-card admin-details-card">
        <div className="panel-header">
          <h2 className="panel-title">
            <Bell size={24} style={{ marginRight: "10px" }} />
            Notifications
          </h2>
        </div>
        <div className="loading-spinner"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="notification-panel-card admin-details-card">
        <div className="panel-header">
          <h2 className="panel-title">
            <Bell size={24} style={{ marginRight: "10px" }} />
            Notifications
          </h2>
        </div>
        <div className="error-message">
          <AlertCircle size={20} />
          <p>{error}</p>
          <button onClick={loadNotifications} className="retry-btn">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="notification-panel-card admin-details-card">
      <div className="panel-header">
        <h2 className="panel-title">
          <Bell size={24} style={{ marginRight: "10px" }} />
          Notifications
          {unreadCount > 0 && (
            <span className="unread-badge">{unreadCount}</span>
          )}
        </h2>
        {unreadCount > 0 && (
          <button className="mark-all-btn" onClick={markAllAsRead}>
            Mark all as read
          </button>
        )}
      </div>

      <div className="notification-list">
        {notifications.length === 0 ? (
          <p className="empty-state">
            You're all caught up! No new notifications.
          </p>
        ) : (
          notifications.map((n) => (
            <NotificationItem
              key={n.id}
              notification={n}
              markAsRead={markAsRead}
            />
          ))
        )}
      </div>
    </div>
  );
};

export default NotificationPanel;
