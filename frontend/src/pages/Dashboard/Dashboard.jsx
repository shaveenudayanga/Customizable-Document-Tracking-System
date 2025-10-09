// src/Pages/Dashboard/Dashboard.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Home,
  FileText,
  BarChart,
  Settings,
  LogOut,
  Users,
  CheckCircle,
  Clock,
  FileArchive,
  Eye,
  Send,
  XCircle,
  ChevronRight,
  FileX,
  Upload,
  Check,
  X,
  User,
} from "lucide-react";
import "../../styles/Dashboard.css";
import { userService } from "../../services/userService.js";
import { documentService } from "../../services/documentService.js";
import { authService } from "../../services/authService.js";
import { workflowService } from "../../services/workflowService.js";
import { trackingService } from "../../services/trackingService.js";
import { notificationService } from "../../services/notificationService.js";

// =================================================================
// --- Reusable Sub-Components ---
// =================================================================

// 1. Sidebar Link Component
const SidebarLink = ({ Icon, title, active, onClick }) => (
  <li className={`sidebar-link ${active ? "active" : ""}`} onClick={onClick}>
    <Icon size={20} />
    <span>{title}</span>
  </li>
);

// 2. Metric Card Component
const MetricCard = ({ title, value, Icon, colorClass }) => (
  <div className={`metric-card ${colorClass}`}>
    <div className="card-icon">
      <Icon size={32} />
    </div>
    <div className="card-content">
      <p className="card-title">{title}</p>
      <h3 className="card-value">{value}</h3>
    </div>
  </div>
);

// 3. Activity Log Data and Helpers
const activityLog = [
  {
    id: 1,
    type: "upload",
    description: 'New contract "NDA-Q4" uploaded by Jane Doe.',
    time: "5 min ago",
    user: "Jane Doe",
  },
  {
    id: 2,
    type: "approval",
    description: 'Approved "Invoice #4021" submitted by Sadish.',
    time: "1 hour ago",
    user: "Admin",
  },
  {
    id: 3,
    type: "rejection",
    description: 'Rejected "Q3 Budget Plan" due to incorrect format.',
    time: "3 hours ago",
    user: "Manager Bob",
  },
  {
    id: 4,
    type: "user",
    description: 'New user "Michael Scott" registered via invitation.',
    time: "1 day ago",
    user: "System",
  },
  {
    id: 5,
    type: "upload",
    description: 'Draft policy "Security Policy v2" updated.',
    time: "1 day ago",
    user: "Alice J.",
  },
];

const getActivityMetadata = (type) => {
  switch (type) {
    case "upload":
      return { Icon: Upload, color: "var(--color-primary)" };
    case "approval":
      return { Icon: Check, color: "var(--color-success)" };
    case "rejection":
      return { Icon: X, color: "var(--color-danger)" };
    case "user":
      return { Icon: User, color: "var(--color-info)" };
    default:
      return { Icon: Clock, color: "var(--color-text-light)" };
  }
};

// 4. Activity Item Component
const ActivityItem = ({ activity }) => {
  const { Icon, color } = getActivityMetadata(activity.type);

  return (
    <div
      className="activity-item"
      style={{
        "--item-animation-delay": `${activity.id * 0.1}s`,
        "--icon-color": color,
      }}
    >
      <div className="activity-icon" style={{ color: color }}>
        <Icon size={20} />
      </div>

      <div className="activity-content">
        <div className="activity-description">{activity.description}</div>
        <div className="activity-footer">
          <Clock size={12} />
          <span>{activity.time}</span>
        </div>
      </div>
    </div>
  );
};

// 5. Recent Activity Panel (Updated to use real data)
const RecentActivity = ({ activities }) => {
  return (
    <div className="secondary-block recent-activity-panel">
      <h3 className="block-title">Recent System Activity</h3>

      <div className="activity-list">
        {activities && activities.length > 0 ? (
          activities.map((activity) => (
            <ActivityItem key={activity.id} activity={activity} />
          ))
        ) : (
          <div className="activity-item">
            <div className="activity-icon">
              <Clock size={20} />
            </div>
            <div className="activity-content">
              <div className="activity-description">No recent activity</div>
              <div className="activity-footer">
                <Clock size={12} />
                <span>System ready</span>
              </div>
            </div>
          </div>
        )}
      </div>

      <div className="view-all-button-container">
        <button
          className="btn-view-all"
          onClick={() => navigate("/notifications")}
        >
          View All Notifications
        </button>
      </div>
    </div>
  );
};

// =================================================================
// --- Main Dashboard Component ---
// =================================================================

const Dashboard = () => {
  const navigate = useNavigate();
  // State management
  const [role, setRole] = useState(""); // Will be set from authService
  const [userName, setUserName] = useState("Loading...");
  const [activeTab, setActiveTab] = useState("Home");
  const [userMetrics, setUserMetrics] = useState([]);
  const [adminMetrics, setAdminMetrics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [recentActivity, setRecentActivity] = useState([]);
  const [pendingTasks, setPendingTasks] = useState([]);
  const [notifications, setNotifications] = useState([]);

  // Fetch user data and metrics
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        // Get current user from authService
        const currentUser = authService.getCurrentUser();

        if (currentUser) {
          setUserName(currentUser.username || "User");
          setRole(currentUser.role?.toLowerCase() || "user");
        }

        // Fetch real data from backend services
        const [documents, workflows, notifications, users] = await Promise.all([
          documentService.getAllDocuments().catch(() => []),
          workflowService.getMyTasks().catch(() => []),
          notificationService.getUserNotifications().catch(() => []),
          userService.getAllUsers().catch(() => []),
        ]);

        // Set notifications
        setNotifications(notifications.slice(0, 5)); // Show latest 5

        if (currentUser.role?.toLowerCase() === "admin") {
          // Calculate admin metrics from real backend data
          const totalDocuments = documents.length;
          const pendingApprovals = documents.filter(
            (doc) => doc.status === "PENDING"
          ).length;
          const activeUsers = users.length;
          const overdueDocuments = documents.filter(
            (doc) =>
              doc.status === "REJECTED" ||
              (doc.dueDate && new Date(doc.dueDate) < new Date())
          ).length;

          setAdminMetrics([
            {
              title: "Total Documents",
              value: totalDocuments,
              Icon: FileText,
              colorClass: "primary",
            },
            {
              title: "Pending Approvals",
              value: pendingApprovals,
              Icon: Send,
              colorClass: "warning",
            },
            {
              title: "Active Users",
              value: activeUsers,
              Icon: Users,
              colorClass: "success",
            },
            {
              title: "Overdue Documents",
              value: overdueDocuments,
              Icon: FileX,
              colorClass: "danger",
            },
          ]);

          // Set pending tasks for admin
          setPendingTasks(
            documents.filter((doc) => doc.status === "PENDING").slice(0, 5)
          );
        } else {
          // Calculate user-specific metrics from real backend data
          const userDocs = documents.filter(
            (doc) => doc.createdBy === currentUser.username
          );
          const userTasks = workflows.filter(
            (task) => task.assignee === currentUser.username
          );
          const documentsForReview = userTasks.filter(
            (task) => task.status === "PENDING"
          ).length;
          const documentsInTransit = userDocs.filter(
            (doc) => doc.status !== "APPROVED" && doc.status !== "REJECTED"
          ).length;
          const completedDocuments = userDocs.filter(
            (doc) => doc.status === "APPROVED"
          ).length;

          setUserMetrics([
            {
              title: "Documents for Review",
              value: documentsForReview,
              Icon: Eye,
              colorClass: "primary",
            },
            {
              title: "Documents in Transit",
              value: documentsInTransit,
              Icon: Clock,
              colorClass: "warning",
            },
            {
              title: "Completed Documents",
              value: completedDocuments,
              Icon: CheckCircle,
              colorClass: "success",
            },
            {
              title: "Total Uploads",
              value: userDocs.length,
              Icon: FileArchive,
              colorClass: "info",
            },
          ]);

          // Set pending tasks for user
          setPendingTasks(
            userTasks.filter((task) => task.status === "PENDING").slice(0, 5)
          );
        }

        // Create recent activity from notifications and document events
        const activityItems = notifications.map((notif, index) => ({
          id: notif.id || index,
          type: notif.type?.toLowerCase() || "info",
          description: notif.message || notif.content,
          time: notif.createdAt
            ? new Date(notif.createdAt).toLocaleString()
            : `${index + 1} hours ago`,
          user: notif.sender || "System",
        }));

        setRecentActivity(activityItems);
      } catch (error) {
        console.error("Error fetching dashboard data:", error);
        // Fallback to default values
        setUserName("User");
        setRole("user");
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  // Determine the data set to use
  const metricsData =
    role?.toLowerCase() === "user" ? userMetrics : adminMetrics;

  // --- Dynamic Table Content ---

  const UserPendingTable = ({ tasks }) => (
    <div className="table-block">
      <h3 className="block-title">Pending Tasks (My Actions)</h3>
      <div className="custom-table">
        <div className="table-header">
          <span>ID</span>
          <span>Document Name</span>
          <span>Status</span>
          <span>Due Date</span>
          <span>Action</span>
        </div>
        {tasks && tasks.length > 0 ? (
          tasks.map((task) => (
            <div key={task.id} className="table-row">
              <span>{task.id || task.documentId}</span>
              <span>
                {task.documentName || task.title || "Untitled Document"}
              </span>
              <span
                className={`status status-${
                  task.status?.toLowerCase() || "pending"
                }`}
              >
                {task.status || "Pending"}
              </span>
              <span>
                {task.dueDate
                  ? new Date(task.dueDate).toLocaleDateString()
                  : "N/A"}
              </span>
              <button
                className="btn-table primary"
                onClick={() =>
                  navigate(`/documents/${task.documentId || task.id}`)
                }
              >
                Review
              </button>
            </div>
          ))
        ) : (
          <div className="table-row">
            <span
              colSpan="5"
              style={{ textAlign: "center", gridColumn: "1 / -1" }}
            >
              No pending tasks
            </span>
          </div>
        )}
      </div>
    </div>
  );

  const AdminPendingTable = ({ documents }) => (
    <div className="table-block">
      <h3 className="block-title">Documents Requiring Approval</h3>
      <div className="custom-table">
        <div className="table-header">
          <span>ID</span>
          <span>User</span>
          <span>Document Title</span>
          <span>Submission Date</span>
          <span>Action</span>
        </div>
        {documents && documents.length > 0 ? (
          documents.map((doc) => (
            <div key={doc.id} className="table-row">
              <span>{doc.id}</span>
              <span>{doc.createdBy || doc.owner || "Unknown"}</span>
              <span>{doc.title || doc.name || "Untitled Document"}</span>
              <span>
                {doc.createdAt
                  ? new Date(doc.createdAt).toLocaleDateString()
                  : "N/A"}
              </span>
              <div className="action-buttons">
                <button
                  className="btn-table success"
                  onClick={async () => {
                    try {
                      await documentService.updateDocumentStatus(
                        doc.id,
                        "APPROVED"
                      );
                      // Refresh dashboard data
                      window.location.reload();
                    } catch (error) {
                      console.error("Error approving document:", error);
                      alert("Failed to approve document");
                    }
                  }}
                >
                  Approve
                </button>
                <button
                  className="btn-table danger"
                  onClick={async () => {
                    try {
                      await documentService.updateDocumentStatus(
                        doc.id,
                        "REJECTED"
                      );
                      // Refresh dashboard data
                      window.location.reload();
                    } catch (error) {
                      console.error("Error rejecting document:", error);
                      alert("Failed to reject document");
                    }
                  }}
                >
                  Reject
                </button>
              </div>
            </div>
          ))
        ) : (
          <div className="table-row">
            <span
              colSpan="5"
              style={{ textAlign: "center", gridColumn: "1 / -1" }}
            >
              No documents pending approval
            </span>
          </div>
        )}
      </div>
    </div>
  );

  // --- Render ---

  if (loading) {
    return (
      <div className="dashboard-wrapper">
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "100vh",
            fontSize: "1.2rem",
            color: "var(--color-text-light)",
          }}
        >
          Loading dashboard data...
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-wrapper">
      {/* Sidebar (Left) */}
      <aside className="sidebar">
        <h2 className="logo-title">DocuTrace</h2>
        <nav className="sidebar-nav">
          <ul>
            <SidebarLink
              Icon={Home}
              title="Dashboard"
              active={activeTab === "Home"}
              onClick={() => setActiveTab("Home")}
            />
            <SidebarLink
              Icon={FileText}
              title="All Documents"
              active={activeTab === "Docs"}
              onClick={() => {
                setActiveTab("Docs");
                navigate("/documents");
              }}
            />
            <SidebarLink
              Icon={BarChart}
              title="Define Pipelines"
              active={activeTab === "pipelines/builder"}
              onClick={() => {
                setActiveTab("pipelines/builder");
                navigate("/pipelines/builder");
              }}
            />
            {role?.toLowerCase() === "admin" && (
              <SidebarLink
                Icon={Users}
                title="User Management"
                active={activeTab === "Admin"}
                onClick={() => {
                  setActiveTab("Admin");
                  navigate("/userprofile");
                }}
              />
            )}
            <SidebarLink
              Icon={Settings}
              title="Settings"
              active={activeTab === "Settings"}
              onClick={() => setActiveTab("Settings")}
            />
          </ul>
        </nav>
        <div className="sidebar-footer">
          <SidebarLink
            Icon={LogOut}
            title="Logout"
            active={false}
            onClick={() => {
              alert("Logging out...");
              navigate("/");
            }}
          />
        </div>
      </aside>

      {/* Main Content (Right) */}
      <main className="dashboard-main">
        <header className="dashboard-header">
          <h1 className="header-greeting">
            Hello, <span className="user-name-highlight">{userName}</span>!
          </h1>
          <div className="header-info">
            <span className={`role-badge role-${role}`}>
              {role.toUpperCase() === "ADMIN" ? "Admin" : "Staff"}
            </span>
            <ChevronRight size={18} className="chevron" />
          </div>
        </header>

        {/* Metrics Cards */}
        <section className="metrics-grid">
          {metricsData.map((metric) => (
            <MetricCard key={metric.title} {...metric} />
          ))}
        </section>

        {/* Main Content Blocks (Tables and Activity Log) */}
        <section className="dashboard-content-blocks">
          <div className="primary-block">
            {role?.toLowerCase() === "admin" ? (
              <AdminPendingTable documents={pendingTasks} />
            ) : (
              <UserPendingTable tasks={pendingTasks} />
            )}
          </div>

          <RecentActivity activities={recentActivity} />
        </section>
      </main>
    </div>
  );
};

export default Dashboard;
