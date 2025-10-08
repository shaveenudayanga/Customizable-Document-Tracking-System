import React from "react";
import { useNavigate, Link } from "react-router-dom"; // Ensure Link is imported
import "../../styles/AdminUserManagement.css";
// Added Camera to the imports
import {
  User,
  Settings,
  Lock,
  FileText,
  Bell,
  LogOut,
  Camera,
} from "lucide-react";

// --- Component 1: Admin Details Form (UNCHANGED from last modification) ---
const AdminDetailsForm = () => {
  const navigate = useNavigate();
  const [name, setName] = React.useState("Jane Doe");
  const [email, setEmail] = React.useState("jane.doe@company.com");
  const [profileImage, setProfileImage] = React.useState(
    "/path/to/admin-photo.jpg"
  );

  // Function to generate simple hash (browser compatible)
  const simpleHash = (str) => {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = (hash << 5) - hash + char;
      hash = hash & hash; // Convert to 32bit integer
    }
    return Math.abs(hash).toString(16);
  };

  // Function to generate Gravatar-style URL from email
  const generateGravatarUrl = (email) => {
    const hash = simpleHash(email.toLowerCase().trim());
    return `https://www.gravatar.com/avatar/${hash}?s=200&d=identicon&r=g`;
  };

  // Generate avatar from initials and email
  const generateInitialsAvatar = (name, email) => {
    const initials = name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .substring(0, 2);
    const colors = ["6366f1", "8b5cf6", "ec4899", "f59e0b", "10b981", "ef4444"];
    const colorIndex = Math.abs(simpleHash(email)) % colors.length;
    return `https://ui-avatars.com/api/?name=${initials}&background=${colors[colorIndex]}&color=fff&size=200`;
  };

  // Mock user database lookup (replace with real API call)
  const mockUserPhotos = {
    "jane.doe@company.com":
      "https://images.unsplash.com/photo-1494790108755-2616b612b993?w=200&h=200&fit=crop&crop=face",
    "john.smith@company.com":
      "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop&crop=face",
    "admin@doctutrace.com":
      "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=200&h=200&fit=crop&crop=face",
  };

  // Handle email change and auto-update profile photo
  const handleEmailChange = (e) => {
    const newEmail = e.target.value;
    setEmail(newEmail);

    // Auto-update profile photo if email is valid
    if (newEmail && newEmail.includes("@") && newEmail.includes(".")) {
      // Priority 1: Check mock user database (replace with real API call)
      if (mockUserPhotos[newEmail.toLowerCase()]) {
        setProfileImage(mockUserPhotos[newEmail.toLowerCase()]);
      }
      // Priority 2: Use initials-based avatar
      else {
        setProfileImage(generateInitialsAvatar(name, newEmail));
      }

      // Alternative: Use Gravatar
      // setProfileImage(generateGravatarUrl(newEmail));
    }
  };

  const handleImageChange = (event) => {
    if (event.target.files && event.target.files[0]) {
      setProfileImage(URL.createObjectURL(event.target.files[0]));
    }
  };

  const handleSaveChanges = () => {
    alert(`Saving changes for: ${name}. Email: ${email}. Photo updated.`);
  };

  return (
    <div className="admin-details-card">
      <div className="profile-header">
        <div className="profile-image-container">
          <img src={profileImage} alt={name} className="profile-image" />

          <input
            type="file"
            id="profile-photo-upload"
            accept="image/*"
            style={{ display: "none" }}
            onChange={handleImageChange}
          />
          <label htmlFor="profile-photo-upload" className="photo-upload-button">
            <Camera size={20} />
          </label>
        </div>

        <div className="profile-info">
          <h2>{name}</h2>
          <p>System Administrator</p>
          <p className="email-link">{email}</p>
        </div>
      </div>

      <div className="form-grid">
        {/* Row 1 */}
        <div className="form-field">
          <label>Name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label>Email Account</label>
          <input
            type="email"
            value={email}
            onChange={handleEmailChange}
            placeholder="Enter email address"
          />
        </div>
        {/* Row 2 */}
        <div className="form-field">
          <label>Mobile Number</label>
          <input
            type="text"
            defaultValue="Add Number"
            placeholder="Add Number"
          />
        </div>
        <div className="form-field">
          <label>Address</label>
          <input type="text" defaultValue="123 Main St, Anytown, USA" />
        </div>
        {/* Row 3 */}
        <div className="form-field">
          <label>Company Address</label>
          <input type="text" defaultValue="456 Corporate Blvd, Head Office" />
        </div>
        <div className="form-field">{/* Empty placeholder */}</div>
      </div>

      <div className="form-actions">
        <button className="save-button" onClick={handleSaveChanges}>
          Save Changes
        </button>
      </div>
    </div>
  );
};

// --- Component 2: Side Menu and Settings (UNCHANGED) ---
const SideMenu = ({ activeItem, setActiveItem }) => {
  const navigate = useNavigate();
  return (
    <div className="side-menu-card">
      <h3 className="menu-heading">Menu</h3>
      <nav className="menu-list">
        <div
          className={`menu-item ${activeItem === "profile" ? "active" : ""}`}
          onClick={() => setActiveItem("profile")}
        >
          <User size={20} /> My Profile
        </div>
        <div
          className={`menu-item ${activeItem === "users" ? "active" : ""}`}
          onClick={() => {
            setActiveItem("users");
            navigate("/userprofile");
          }}
        >
          <Lock size={20} /> User Management
        </div>
        <div
          className={`menu-item ${activeItem === "audit" ? "active" : ""}`}
          onClick={() => {
            setActiveItem("audit");
            navigate("/dashboard");
          }}
        >
          <FileText size={20} /> Dashboard
        </div>
        <div
          className={`menu-item ${activeItem === "notify" ? "active" : ""}`}
          onClick={() => {
            setActiveItem("notify");
            navigate("/notifications");
          }}
        >
          <Bell size={20} /> Notification
        </div>
      </nav>

      <h3 className="menu-heading settings-heading">
        <Settings size={18} /> Settings
      </h3>
      <div className="settings-controls">
        <div className="setting-row">
          <label>Theme</label>
          <select defaultValue="Dark">
            <option>Dark</option>
            <option>Light</option>
          </select>
        </div>
        <div className="setting-row">
          <label>Language</label>
          <select defaultValue="Eng">
            <option>English</option>
            <option>Sinhala</option>
          </select>
        </div>
      </div>

      <div className="logout-button" onClick={() => navigate("/pageshell")}>
        <LogOut size={20} /> Log Out
      </div>
    </div>
  );
};

// --- Main Layout Component (MODIFIED to include NEW Footer) ---
const AdminProfileLayout = () => {
  const [activeMenuItem, setActiveMenuItem] = React.useState("profile");

  return (
    <div className="admin-profile-container">
      <header className="doctutrace-header">DocuTrace</header>

      <div className="profile-content-area">
        <SideMenu
          activeItem={activeMenuItem}
          setActiveItem={setActiveMenuItem}
        />
        <AdminDetailsForm />
      </div>

      {/* --- NEW DETAILED FOOTER ELEMENT --- */}

      {/* ----------------------------------- */}
    </div>
  );
};

export default AdminProfileLayout;
