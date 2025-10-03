import React from "react";
import { useNavigate } from "react-router-dom";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";

// Components
import PageShell from "./components/PageShell.jsx";

// Auth Pages
import Login from "./pages/Auth/Login.jsx";
import ForgotPassword from "./Pages/Auth/ForgotPassword.jsx";
import Onboarding from "./Pages/Auth/Onboarding.jsx";

// Dashboard
import Dashboard from "./Pages/Dashboard/Dashboard.jsx";
import Learnmore from "./pages/Learnmore/Learnmore.jsx"; // Fixed casing for Learnmore import

// Documents
import DocumentList from "./Pages/Documents/DocumentList.jsx";
import DocumentDetails from "./Pages/Documents/DocumentDetails.jsx";
import NewDocument from "./pages/Documents/NewDocument.jsx"; // Corrected path to Pages/Documents
import EditDocument from "./Pages/Documents/EditDocument.jsx";

// Pipelines
import PipelineList from "./Pages/Pipelines/PipelineList.jsx";
import PipelineBuilder from "./Pages/Pipelines/PipelineBuilder.jsx";

// Handover
import HandoverQueue from "./Pages/Handover/HandoverQueue.jsx";
import QRVerification from "./Pages/Handover/QRVerification.jsx";
import HandoverHistory from "./Pages/Handover/HandoverHistory.jsx";

// Departments
import DepartmentManager from "./Pages/Departments/DepartmentManager.jsx";

// Notifications
import Notifications from "./pages/Notifications/Notifications.jsx";

// Audit
import AuditLog from "./Pages/Audit/AuditLog.jsx";

// Profile
import UserProfile from "./pages/Profile/UserProfile.jsx";
// Corrected: Added the missing import for AdminUserManagement
import AdminUserManagement from "./Pages/Admin/AdminUserManagement.jsx";

// Settings
import SystemSettings from "./Pages/Settings/SystemSettings.jsx";

// Help
import HelpCenter from "./Pages/Help/HelpCenter.jsx";

// Bulk
import BulkOperations from "./Pages/Bulk/BulkOperations.jsx";

// Mobile
import MobileLite from "./Pages/MobileLite/MobileLite.jsx";

//learnmore

// Simple home landing component for the root index
const HomeLanding = () => {
  const navigate = useNavigate();
  return (
    <div className="hero-section">
      <div className="content">
        <h1>Effortlessly Track Your Documents</h1>
        <p>
          Streamline your workflow with our intuitive and powerful document
          management solution. Keep track of approvals, versions, and deadlines
          with ease.
        </p>
        <button
          className="learn-more-btn"
          onClick={() => navigate("/learnmore")}
        >
          Explore Features
        </button>
        <div className="cool-logos">
          <p>Integrates With:</p>
          <div className="logo-images">
            <img src="/d1.jpg" alt="Slack" />
            <img src="/google-drive-logo.png" alt="Google Drive" />
            <img src="/microsoft-365-logo.png" alt="Microsoft 365" />
          </div>
        </div>
      </div>
    </div>
  );
};

// Helper to get user role (replace with real auth logic as needed)
const getUserRole = () => {
  // Example: role stored in localStorage as 'admin' or 'user'
  return localStorage.getItem("role") || "user";
};

function App() {
  return (
    <Router>
      <Routes>
        {/* Login and Auth pages - these will not be inside PageShell */}
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/onboarding" element={<Onboarding />} />

        {/* Landing page only on initial load */}
        <Route path="/" element={<PageShell />} />
        {/* All other routes are now top-level and only show when navigated to */}
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="documents" element={<DocumentList />} />
        <Route path="documents/:id" element={<DocumentDetails />} />
        <Route path="new-document" element={<NewDocument />} />
        <Route path="edit-document/:id" element={<EditDocument />} />
        <Route path="pipelines" element={<PipelineList />} />
        <Route path="pipelines/builder" element={<PipelineBuilder />} />
        <Route path="handover/queue" element={<HandoverQueue />} />
        <Route path="handover/verify" element={<QRVerification />} />
        <Route path="handover/history" element={<HandoverHistory />} />
        <Route path="departments" element={<DepartmentManager />} />
        <Route path="notifications" element={<Notifications />} />
        <Route path="audit" element={<AuditLog />} />
        <Route
          path="profile"
          element={
            getUserRole() === "admin" ? (
              <AdminUserManagement />
            ) : (
              <UserProfile />
            )
          }
        />
        <Route path="userprofile" element={<UserProfile />} />
        <Route path="adminusermanagement" element={<AdminUserManagement />} />
        <Route path="settings" element={<SystemSettings />} />
        <Route path="help" element={<HelpCenter />} />
        <Route path="bulk" element={<BulkOperations />} />
        <Route path="mobile" element={<MobileLite />} />
        <Route path="learnmore" element={<Learnmore />} />

        {/* Fallback for any unmatched routes */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
