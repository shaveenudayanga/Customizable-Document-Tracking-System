import React, { useState } from "react";
import {
  Bell,
  Clock,
  CheckCircle,
  XCircle,
  FileText,
  ArrowRight,
} from "lucide-react";
import "../../styles/Notifications.css";
// Ensure your main CSS file (AdminUserManagement.css) is imported by the parent layout

// Mock Notification Data
const initialNotifications = [
  {
    id: 1,
    type: "approval",
    title: "Document A102 Requires Approval",
    message:
      "A critical contract document has been uploaded by Jane Doe and is awaiting your review.",
    time: "5 min ago",
    read: false,
  },
  {
    id: 2,
    type: "update",
    title: "System Update Completed",
    message:
      "The nightly database backup and security patch was completed successfully at 2:00 AM.",
    time: "3 hours ago",
    read: true,
  },
  {
    id: 3,
    type: "alert",
    title: "File T404 Access Denied",
    message:
      "Unauthorized access attempt detected on the confidential client file T404.",
    time: "1 day ago",
    read: false,
  },
  {
    id: 4,
    type: "success",
    title: "New User Registered",
    message:
      "A new administrative user, Michael Scott, has successfully registered and requires role assignment.",
    time: "2 days ago",
    read: true,
  },
  {
    id: 5,
    type: "alert",
    title: "New User Registered",
    message:
      "A new administrative user, Michael Scott, has successfully registered and requires role assignment.",
    time: "2 days ago",
    read: true,
  },
];

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
  const [notifications, setNotifications] = useState(initialNotifications);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
  };

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

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
