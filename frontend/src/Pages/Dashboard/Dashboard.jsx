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

// 5. Recent Activity Panel (New Secondary Block)
const RecentActivity = () => {
  return (
    <div className="secondary-block recent-activity-panel">
      <h3 className="block-title">Recent System Activity</h3>

      <div className="activity-list">
        {activityLog.map((activity) => (
          <ActivityItem key={activity.id} activity={activity} />
        ))}
      </div>

      <div className="view-all-button-container">
        <button className="btn-view-all">View Full Log</button>
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
  const [role, setRole] = useState("admin"); // Default to admin for demonstration
  const [userName, setUserName] = useState("Loading...");
  const [activeTab, setActiveTab] = useState("Home");
  const [userMetrics, setUserMetrics] = useState([]);
  const [adminMetrics, setAdminMetrics] = useState([]);
  const [loading, setLoading] = useState(true);

  // Fetch user data and metrics
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const currentUser = await userService.getCurrentUser();
        setUserName(currentUser.name);
        setRole(currentUser.role);

        // Set user role in localStorage for consistency
        localStorage.setItem("role", currentUser.role);

        // Fetch documents for metrics calculation
        const documents = await documentService.getAllDocuments();

        if (currentUser.role === "user") {
          // Calculate user-specific metrics
          const userDocs = documents.filter(
            (doc) => doc.owner === currentUser.name
          );
          setUserMetrics([
            {
              title: "Documents for Review",
              value: userDocs.filter((doc) => doc.status === "Pending").length,
              Icon: Eye,
              colorClass: "primary",
            },
            {
              title: "Documents in Transit",
              value: userDocs.filter((doc) => doc.currentStep !== "Completed")
                .length,
              Icon: Clock,
              colorClass: "warning",
            },
            {
              title: "Completed Documents",
              value: userDocs.filter((doc) => doc.status === "Approved").length,
              Icon: CheckCircle,
              colorClass: "success",
            },
            {
              title: "Recent Uploads",
              value: userDocs.length,
              Icon: FileArchive,
              colorClass: "info",
            },
          ]);
        } else {
          // Calculate admin metrics
          setAdminMetrics([
            {
              title: "Total Documents",
              value: documents.length,
              Icon: FileText,
              colorClass: "primary",
            },
            {
              title: "Pending Approvals",
              value: documents.filter((doc) => doc.status === "Pending").length,
              Icon: Send,
              colorClass: "warning",
            },
            {
              title: "Active Users",
              value: 25, // This would come from userService.getAllUsers().length
              Icon: Users,
              colorClass: "success",
            },
            {
              title: "Overdue Documents",
              value: documents.filter((doc) => doc.status === "Rejected")
                .length,
              Icon: FileX,
              colorClass: "danger",
            },
          ]);
        }
      } catch (error) {
        console.error("Error fetching user data:", error);
        // Fallback to default values
        setUserName("User");
      } finally {
        setLoading(false);
      }
    };

    fetchUserData();
  }, []);

  // Determine the data set to use
  const metricsData = role === "user" ? userMetrics : adminMetrics;

  // --- Dynamic Table Content ---

  const UserPendingTable = () => (
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
        {/* Simulated Data */}
        <div className="table-row">
          <span>DOC-001</span>
          <span>Q2 Financial Report</span>
          <span className="status status-pending">In Transit</span>
          <span>2025-10-05</span>
          <button className="btn-table primary">Review</button>
        </div>
        <div className="table-row">
          <span>DOC-002</span>
          <span>Vendor Contract B</span>
          <span className="status status-pending">Pending</span>
          <span>2025-10-07</span>
          <button className="btn-table primary">Sign</button>
        </div>
      </div>
    </div>
  );

  const AdminPendingTable = () => (
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
        {/* Simulated Data */}
        <div className="table-row">
          <span>APP-101</span>
          <span>John Doe</span>
          <span>Marketing Campaign Plan</span>
          <span>2025-10-01</span>
          <div className="action-buttons">
            <button className="btn-table success">Approve</button>
            <button className="btn-table danger">Reject</button>
          </div>
        </div>
        <div className="table-row">
          <span>APP-102</span>
          <span>Mary Jane</span>
          <span>Q3 Expense Report</span>
          <span>2025-10-02</span>
          <div className="action-buttons">
            <button className="btn-table success">Approve</button>
            <button className="btn-table danger">Reject</button>
          </div>
        </div>
      </div>
    </div>
  );

  // --- Render ---

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
            {role === "admin" && (
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
              {role.toUpperCase()}
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
            {role === "user" ? <UserPendingTable /> : <AdminPendingTable />}
          </div>

          <RecentActivity />
        </section>
      </main>
    </div>
  );
};

export default Dashboard;
