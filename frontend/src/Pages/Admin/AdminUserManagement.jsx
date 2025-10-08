import React, { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom"; // Ensure Link is imported
import "../../styles/AdminUserManagement.css";
import { userService } from "../../services/userService";
import { departmentService } from "../../services/departmentService";
// Added Camera to the imports
import {
  User,
  Settings,
  Lock,
  FileText,
  Bell,
  LogOut,
  Camera,
  Loader,
} from "lucide-react";

// --- Component 1: Admin Details Form ---
const AdminDetailsForm = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [role, setRole] = useState("");
  const [position, setPosition] = useState("");
  const [department, setDepartment] = useState("");
  const [sectionId, setSectionId] = useState("");
  const [profileImage, setProfileImage] = useState("");
  const [departments, setDepartments] = useState([]);
  const [loadingDepartments, setLoadingDepartments] = useState(false);

  const normalizeDepartmentList = (data) => {
    if (!data) return [];
    if (Array.isArray(data)) return data;

    if (data && typeof data === "object") {
      if (Array.isArray(data.items)) return data.items;
      if (Array.isArray(data.content)) return data.content;
      if (Array.isArray(data.data)) return data.data;

      const potentialValues = Object.values(data).filter(
        (value) => Array.isArray(value)
      );
      for (const arr of potentialValues) {
        if (Array.isArray(arr)) return arr;
      }

      const objectValues = Object.values(data).filter(
        (value) => value && typeof value === "object"
      );
      if (objectValues.length) {
        return objectValues;
      }
    }

    console.warn("Unable to normalize departments payload", data);
    return [];
  };

  // Fetch user profile on component mount
  useEffect(() => {
    loadUserProfile();
    loadDepartments();
  }, []);

  const loadDepartments = async () => {
    try {
      setLoadingDepartments(true);
      const deptList = await departmentService.getAllDepartments();
      setDepartments(normalizeDepartmentList(deptList));
    } catch (err) {
      console.error("Error loading departments:", err);
      const fallbackDepts = departmentService.getDepartments();
      setDepartments(normalizeDepartmentList(fallbackDepts));
    } finally {
      setLoadingDepartments(false);
    }
  };

  const loadUserProfile = async () => {
    try {
      setLoading(true);
      setError(null);
      const userData = await userService.getCurrentUser();
      
      if (userData) {
        setUsername(userData.username || "");
        setEmail(userData.email || "");
        setRole(userData.role || "USER");
        setPosition(userData.position || "");
        setSectionId(userData.sectionId || "");
        setDepartment(userData.sectionId || "");
        
        // Generate display name from username or email
        const displayName = userData.username 
          ? userData.username.split(/[._-]/).map(part => 
              part.charAt(0).toUpperCase() + part.slice(1)
            ).join(" ")
          : userData.email?.split("@")[0] || "User";
        setName(displayName);
        
        // Set default profile image based on email
        if (userData.email) {
          setProfileImage(generateInitialsAvatar(displayName, userData.email));
        }
      }
    } catch (err) {
      console.error("Error loading user profile:", err);
      setError("Failed to load user profile. Please try again.");
    } finally {
      setLoading(false);
    }
  };

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

  // Handle email change and auto-update profile photo
  const handleEmailChange = (e) => {
    const newEmail = e.target.value;
    setEmail(newEmail);

    if (newEmail && newEmail.includes("@") && newEmail.includes(".")) {
      setProfileImage(generateInitialsAvatar(name, newEmail));
    }
  };

  const handleImageChange = (event) => {
    if (event.target.files && event.target.files[0]) {
      setProfileImage(URL.createObjectURL(event.target.files[0]));
    }
  };

  const handleSaveChanges = async () => {
    try {
      setSaving(true);
      setError(null);
      
      // TODO: When backend supports profile update endpoint, call:
      // await userService.updateProfile({
      //   position,
      //   sectionId: department,
      // });
      
      const selectedDept = departments.find(
        (d) => (d.key || d.id) === department
      );
      const deptName = selectedDept ? selectedDept.name : department;

      alert(
        `Profile saved successfully!\n\nName: ${name}\nEmail: ${email}\nRole: ${role}\nPosition: ${position}\nDepartment: ${deptName}`
      );
    } catch (err) {
      console.error("Error saving profile:", err);
      setError("Failed to save profile. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="admin-details-card">
        <div style={{ textAlign: "center", padding: "3rem" }}>
          <Loader size={48} className="spinner" />
          <p style={{ marginTop: "1rem", color: "#888" }}>Loading profile...</p>
        </div>
      </div>
    );
  }

  if (error && !email) {
    return (
      <div className="admin-details-card">
        <div style={{ textAlign: "center", padding: "3rem" }}>
          <p style={{ color: "#ef4444", marginBottom: "1rem" }}>{error}</p>
          <button onClick={loadUserProfile} className="save-button">
            Retry
          </button>
        </div>
      </div>
    );
  }

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
          <p>{position}</p>
          <p className="email-link">{email}</p>
        </div>
      </div>

      {error && (
        <div style={{ 
          padding: "1rem", 
          background: "#fee", 
          color: "#c00", 
          borderRadius: "8px",
          marginBottom: "1rem"
        }}>
          {error}
        </div>
      )}

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
          <label>Position</label>
          <input
            type="text"
            value={position}
            onChange={(e) => setPosition(e.target.value)}
            placeholder="Enter position/title"
          />
        </div>
        <div className="form-field">
          <label>Department</label>
          {(() => {
            const departmentOptions = normalizeDepartmentList(departments);
            const getDeptValue = (dept) =>
              dept?.key ?? dept?.id ?? dept?.code ?? dept?.value ?? dept?.name ?? "";
            const getDeptName = (dept) =>
              dept?.name ?? dept?.displayName ?? dept?.title ?? dept?.key ?? dept?.id ?? "Department";

            return (
              <select
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                disabled={loadingDepartments || !departmentOptions.length}
                className="department-select"
              >
                <option value="">
                  {loadingDepartments
                    ? "Loading departments..."
                    : departmentOptions.length
                    ? "Select department"
                    : "No departments available"}
                </option>
                {departmentOptions.map((dept, index) => {
                  const value = getDeptValue(dept) || `dept-${index}`;
                  return (
                    <option key={value} value={value}>
                      {getDeptName(dept)}
                    </option>
                  );
                })}
              </select>
            );
          })()}
        </div>
        {/* Row 3 */}
        <div className="form-field">
          <label>Role</label>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            disabled
          >
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
            <option value="MANAGER">Manager</option>
          </select>
        </div>
        <div className="form-field">{/* Empty placeholder */}</div>
      </div>

      <div className="form-actions">
        <button 
          className="save-button" 
          onClick={handleSaveChanges}
          disabled={saving}
        >
          {saving ? (
            <>
              <Loader size={18} className="spinner" /> Saving...
            </>
          ) : (
            "Save Changes"
          )}
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
