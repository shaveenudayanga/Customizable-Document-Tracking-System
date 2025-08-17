import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/UserProfile.css";
// import { api } from "../../lib/api"; // Assume API for fetching/updating user data

const UserProfile = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isEditing, setIsEditing] = useState(false);

  // Form states for editing
  const [editName, setEditName] = useState("");
  const [editEmail, setEditEmail] = useState("");
  const [editNotifications, setEditNotifications] = useState({
    email: true,
    sms: false,
    app: true,
  });
  const [editPreferences, setEditPreferences] = useState({
    theme: "light",
    timezone: "UTC",
  });

  // Mock user data - replace with actual API fetch
  const mockUserData = {
    id: "user123",
    name: "John Doe",
    email: "john.doe@example.com",
    role: "ADMIN", // Or 'USER', 'HANDOVER_AGENT'
    lastLogin: "2024-08-17T22:30:00Z",
    preferences: {
      theme: "light",
      timezone: "America/New_York",
      language: "en-US",
    },
    notificationSettings: {
      email: true,
      sms: false,
      app: true,
    },
  };

  useEffect(() => {
    // Simulate fetching user data
    setLoading(true);
    setError("");
    setTimeout(() => {
      // In a real app: try { const response = await api.get('/profile'); setUser(response.data); ... }
      setUser(mockUserData);
      setEditName(mockUserData.name);
      setEditEmail(mockUserData.email);
      setEditNotifications(mockUserData.notificationSettings);
      setEditPreferences(mockUserData.preferences);
      setLoading(false);
    }, 700);
  }, []);

  const handleSave = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true); // For saving state

    try {
      // In a real app: await api.put('/profile', { name: editName, email: editEmail, notificationSettings: editNotifications, preferences: editPreferences });
      await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call

      const updatedUser = {
        ...user,
        name: editName,
        email: editEmail,
        notificationSettings: editNotifications,
        preferences: editPreferences,
        updatedAt: new Date().toISOString(),
      };
      setUser(updatedUser); // Update local state
      setSuccess("Profile updated successfully!");
      setIsEditing(false);
    } catch (err) {
      setError(err?.message || "Failed to update profile.");
      console.error("Profile update error:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEdit = () => {
    // Reset form states to current user data
    if (user) {
      setEditName(user.name);
      setEditEmail(user.email);
      setEditNotifications(user.notificationSettings);
      setEditPreferences(user.preferences);
    }
    setIsEditing(false);
    setError("");
    setSuccess("");
  };

  const handlePasswordReset = () => {
    // In a real app, this would trigger a password reset flow (e.g., modal, navigate to forgot-password)
    alert("Password reset functionality would be triggered here!");
    // For now, just show a message
    setSuccess("Password reset link sent to your email (mock action).");
  };

  if (loading && !user) {
    return (
      <div className="profile-wrapper">
        <div className="loading-message">Loading user profile...</div>
      </div>
    );
  }

  if (error && !user) {
    return (
      <div className="profile-wrapper">
        <div className="error-message user-error">{error}</div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="profile-wrapper">
        <div className="no-profile-found">No user profile found.</div>
      </div>
    );
  }

  return (
    <div className="profile-wrapper">
      <div className="profile-container">
        <div className="profile-header">
          <h1 className="profile-title">
            {isEditing ? "Edit Your Profile" : `${user.name}'s Profile`}
          </h1>
          <button onClick={() => navigate(-1)} className="back-button">
            ← Back
          </button>
        </div>
        <p className="profile-subtitle">
          Manage your personal details and settings
        </p>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSave} className="profile-form">
          <div className="form-section">
            <h2 className="section-heading">Personal Information</h2>
            <div className="form-group">
              <label htmlFor="name">Full Name</label>
              <input
                type="text"
                id="name"
                value={editName}
                onChange={(e) => setEditName(e.target.value)}
                readOnly={!isEditing}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input
                type="email"
                id="email"
                value={editEmail}
                onChange={(e) => setEditEmail(e.target.value)}
                readOnly={!isEditing}
                required
              />
            </div>
            <div className="form-group">
              <label>Role</label>
              <input
                type="text"
                value={user.role}
                readOnly
                className="read-only-input"
              />
            </div>
            <div className="form-group">
              <label>Last Login</label>
              <input
                type="text"
                value={new Date(user.lastLogin).toLocaleString()}
                readOnly
                className="read-only-input"
              />
            </div>
          </div>

          <div className="form-section">
            <h2 className="section-heading">Preferences</h2>
            <div className="form-group">
              <label htmlFor="theme">Theme</label>
              <select
                id="theme"
                value={editPreferences.theme}
                onChange={(e) =>
                  setEditPreferences({
                    ...editPreferences,
                    theme: e.target.value,
                  })
                }
                disabled={!isEditing}
              >
                <option value="light">Light</option>
                <option value="dark">Dark</option>
              </select>
            </div>
            <div className="form-group">
              <label htmlFor="timezone">Timezone</label>
              <input
                type="text"
                id="timezone"
                value={editPreferences.timezone}
                onChange={(e) =>
                  setEditPreferences({
                    ...editPreferences,
                    timezone: e.target.value,
                  })
                }
                readOnly={!isEditing}
                placeholder="e.g., America/New_York"
              />
            </div>
            <div className="form-group">
              <label>Language</label>
              <input
                type="text"
                value={editPreferences.language}
                readOnly={!isEditing}
              />
            </div>
          </div>

          <div className="form-section">
            <h2 className="section-heading">Notification Settings</h2>
            <div className="checkbox-group">
              <input
                type="checkbox"
                id="email-notifications"
                checked={editNotifications.email}
                onChange={(e) =>
                  setEditNotifications({
                    ...editNotifications,
                    email: e.target.checked,
                  })
                }
                disabled={!isEditing}
              />
              <label htmlFor="email-notifications">Email Notifications</label>
            </div>
            <div className="checkbox-group">
              <input
                type="checkbox"
                id="sms-notifications"
                checked={editNotifications.sms}
                onChange={(e) =>
                  setEditNotifications({
                    ...editNotifications,
                    sms: e.target.checked,
                  })
                }
                disabled={!isEditing}
              />
              <label htmlFor="sms-notifications">SMS Notifications</label>
            </div>
            <div className="checkbox-group">
              <input
                type="checkbox"
                id="app-notifications"
                checked={editNotifications.app}
                onChange={(e) =>
                  setEditNotifications({
                    ...editNotifications,
                    app: e.target.checked,
                  })
                }
                disabled={!isEditing}
              />
              <label htmlFor="app-notifications">In-App Notifications</label>
            </div>
          </div>

          <div className="form-actions">
            {isEditing ? (
              <>
                <button
                  type="submit"
                  className="save-button"
                  disabled={loading}
                >
                  {loading ? "Saving..." : "Save Changes"}
                </button>
                <button
                  type="button"
                  className="cancel-button"
                  onClick={handleCancelEdit}
                  disabled={loading}
                >
                  Cancel
                </button>
              </>
            ) : (
              <>
                <button
                  type="button"
                  className="edit-button"
                  onClick={() => setIsEditing(true)}
                >
                  Edit Profile
                </button>
                <button
                  type="button"
                  className="reset-password-button"
                  onClick={handlePasswordReset}
                >
                  Reset Password
                </button>
              </>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserProfile;
