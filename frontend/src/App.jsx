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
import ProtectedRoute from "./components/ProtectedRoute.jsx";

// Auth Pages
import Login from "./pages/Auth/Login.jsx";
import ForgotPassword from "./pages/Auth/ForgotPassword.jsx";
import Onboarding from "./pages/Auth/Onboarding.jsx";

// Dashboard
import Dashboard from "./pages/Dashboard/Dashboard.jsx";
import Learnmore from "./pages/Learnmore/Learnmore.jsx";

// Documents
import DocumentList from "./pages/Documents/DocumentList.jsx";
import DocumentDetails from "./pages/Documents/DocumentDetails.jsx";
import NewDocument from "./pages/Documents/NewDocument.jsx"; // Corrected path to pages/Documents
import EditDocument from "./pages/Documents/EditDocument.jsx";

// Pipelines
import PipelineList from "./pages/Pipelines/PipelineList.jsx";
import PipelineBuilder from "./pages/Pipelines/PipelineBuilder.jsx";

// Handover
import HandoverQueue from "./pages/Handover/HandoverQueue.jsx";
import QRVerification from "./pages/Handover/QRVerification.jsx";
import HandoverHistory from "./pages/Handover/HandoverHistory.jsx";

// Departments
import DepartmentManager from "./pages/Departments/DepartmentManager.jsx";

// Notifications
import Notifications from "./pages/Notifications/Notifications.jsx";

// Audit
import AuditLog from "./pages/Audit/AuditLog.jsx";

// Profile
import UserProfile from "./pages/Profile/UserProfile.jsx";
// Corrected: Added the missing import for AdminUserManagement
import AdminUserManagement from "./Pages/Admin/AdminUserManagement.jsx";

// Settings
import SystemSettings from "./pages/Settings/SystemSettings.jsx";

// Help
import HelpCenter from "./pages/Help/HelpCenter.jsx";

// Bulk
import BulkOperations from "./pages/Bulk/BulkOperations.jsx";

// Mobile
import MobileLite from "./pages/MobileLite/MobileLite.jsx";

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

// Helper to get user role from authService
const getUserRole = () => {
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  return user.role || "user";
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
        {/* Protected routes - require authentication */}
        <Route
          path="dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="documents"
          element={
            <ProtectedRoute>
              <DocumentList />
            </ProtectedRoute>
          }
        />
        <Route
          path="documents/:id"
          element={
            <ProtectedRoute>
              <DocumentDetails />
            </ProtectedRoute>
          }
        />
        <Route
          path="new-document"
          element={
            <ProtectedRoute>
              <NewDocument />
            </ProtectedRoute>
          }
        />
        <Route
          path="edit-document/:id"
          element={
            <ProtectedRoute>
              <EditDocument />
            </ProtectedRoute>
          }
        />
        <Route
          path="pipelines"
          element={
            <ProtectedRoute>
              <PipelineList />
            </ProtectedRoute>
          }
        />
        <Route
          path="pipelines/builder"
          element={
            <ProtectedRoute>
              <PipelineBuilder />
            </ProtectedRoute>
          }
        />
        <Route
          path="handover/queue"
          element={
            <ProtectedRoute>
              <HandoverQueue />
            </ProtectedRoute>
          }
        />
        <Route
          path="handover/verify"
          element={
            <ProtectedRoute>
              <QRVerification />
            </ProtectedRoute>
          }
        />
        <Route
          path="handover/history"
          element={
            <ProtectedRoute>
              <HandoverHistory />
            </ProtectedRoute>
          }
        />
        <Route
          path="departments"
          element={
            <ProtectedRoute>
              <DepartmentManager />
            </ProtectedRoute>
          }
        />
        <Route
          path="notifications"
          element={
            <ProtectedRoute>
              <Notifications />
            </ProtectedRoute>
          }
        />
        <Route
          path="audit"
          element={
            <ProtectedRoute>
              <AuditLog />
            </ProtectedRoute>
          }
        />
        <Route
          path="profile"
          element={
            <ProtectedRoute>
              {getUserRole() === "admin" ? (
                <AdminUserManagement />
              ) : (
                <UserProfile />
              )}
            </ProtectedRoute>
          }
        />
        <Route
          path="userprofile"
          element={
            <ProtectedRoute>
              <UserProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="adminusermanagement"
          element={
            <ProtectedRoute requiredRole="admin">
              <AdminUserManagement />
            </ProtectedRoute>
          }
        />
        <Route
          path="settings"
          element={
            <ProtectedRoute>
              <SystemSettings />
            </ProtectedRoute>
          }
        />
        <Route
          path="help"
          element={
            <ProtectedRoute>
              <HelpCenter />
            </ProtectedRoute>
          }
        />
        <Route
          path="bulk"
          element={
            <ProtectedRoute>
              <BulkOperations />
            </ProtectedRoute>
          }
        />
        <Route
          path="mobile"
          element={
            <ProtectedRoute>
              <MobileLite />
            </ProtectedRoute>
          }
        />
        <Route path="learnmore" element={<Learnmore />} />

        {/* Fallback for any unmatched routes */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
