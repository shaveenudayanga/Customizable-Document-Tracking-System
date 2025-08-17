import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/AdminUserManagement.css";
// import { api } from "../../lib/api"; // Assume API for user management

const AdminUserManagement = () => {
  const navigate = useNavigate();
  const [currentUserRole, setCurrentUserRole] = useState("ADMIN"); // Mock current user's role (simulate context/auth)
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [editingUser, setEditingUser] = useState(null); // null for new, user object for edit

  // Form states for add/edit user
  const [formName, setFormName] = useState("");
  const [formEmail, setFormEmail] = useState("");
  const [formPassword, setFormPassword] = useState("");
  const [formRole, setFormRole] = useState("USER");
  const [formIsActive, setFormIsActive] = useState(true);

  // Mock user data - replace with actual API fetch
  const mockUsers = [
    {
      id: "u1",
      name: "Alice Admin",
      email: "alice.a@example.com",
      role: "ADMIN",
      isActive: true,
      createdAt: "2023-01-01",
    },
    {
      id: "u2",
      name: "Bob User",
      email: "bob.u@example.com",
      role: "USER",
      isActive: true,
      createdAt: "2023-02-10",
    },
    {
      id: "u3",
      name: "Charlie Handover",
      email: "charlie.h@example.com",
      role: "HANDOVER_AGENT",
      isActive: true,
      createdAt: "2023-03-15",
    },
    {
      id: "u4",
      name: "Diana Inactive",
      email: "diana.i@example.com",
      role: "USER",
      isActive: false,
      createdAt: "2023-04-20",
    },
  ];

  useEffect(() => {
    if (currentUserRole !== "ADMIN") {
      setLoading(false);
      setError(
        "Access Denied: You do not have administrative privileges to view this page."
      );
      return;
    }

    // Simulate fetching user list
    setLoading(true);
    setError("");
    setTimeout(() => {
      // In a real app: try { const response = await api.get('/admin/users'); setUsers(response.data); }
      setUsers(mockUsers);
      setLoading(false);
    }, 700);
  }, [currentUserRole]);

  const filteredUsers = users.filter(
    (user) =>
      user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.role.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleAddUserClick = () => {
    setEditingUser(null); // Ensure no user is being edited
    setFormName("");
    setFormEmail("");
    setFormPassword(""); // Password should usually not be pre-filled for security
    setFormRole("USER");
    setFormIsActive(true);
    setShowAddUserForm(true);
  };

  const handleEditUserClick = (userToEdit) => {
    setEditingUser(userToEdit);
    setFormName(userToEdit.name);
    setFormEmail(userToEdit.email);
    setFormPassword(""); // Always leave password field empty when editing for security
    setFormRole(userToEdit.role);
    setFormIsActive(userToEdit.isActive);
    setShowAddUserForm(true);
  };

  const handleDeleteUser = async (userId) => {
    if (
      !window.confirm("Are you sure you want to deactivate/delete this user?")
    ) {
      return;
    }
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      // In a real app: await api.delete(`/admin/users/${userId}`); or api.put(`/admin/users/${userId}/deactivate`);
      await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate API call
      setUsers((prevUsers) =>
        prevUsers.map((user) =>
          user.id === userId ? { ...user, isActive: !user.isActive } : user
        )
      );
      setSuccess(`User ${userId} status toggled successfully!`);
    } catch (err) {
      setError(err?.message || "Failed to toggle user status.");
    } finally {
      setLoading(false);
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    const userData = {
      name: formName,
      email: formEmail,
      role: formRole,
      isActive: formIsActive,
    };

    if (!formName || !formEmail || !formRole) {
      setError("Please fill in all required fields.");
      setLoading(false);
      return;
    }

    if (!editingUser && !formPassword) {
      // Password required for new user
      setError("Password is required for new user accounts.");
      setLoading(false);
      return;
    }
    if (!editingUser) {
      // Add password for new user
      userData.password = formPassword;
    }

    try {
      if (editingUser) {
        // In a real app: await api.put(`/admin/users/${editingUser.id}`, userData);
        await new Promise((resolve) => setTimeout(resolve, 1000));
        setUsers((prevUsers) =>
          prevUsers.map((user) =>
            user.id === editingUser.id ? { ...user, ...userData } : user
          )
        );
        setSuccess("User updated successfully!");
      } else {
        // In a real app: const response = await api.post('/admin/users', userData);
        await new Promise((resolve) => setTimeout(resolve, 1000));
        const newUserId = `u${Date.now()}`; // Mock new ID
        setUsers((prevUsers) => [
          ...prevUsers,
          { ...userData, id: newUserId, createdAt: new Date().toISOString() },
        ]);
        setSuccess("User created successfully!");
      }
      setShowAddUserForm(false);
    } catch (err) {
      setError(
        err?.message || `Failed to ${editingUser ? "update" : "create"} user.`
      );
    } finally {
      setLoading(false);
    }
  };

  if (loading && currentUserRole === "ADMIN") {
    return (
      <div className="admin-wrapper">
        <div className="loading-message">Loading users...</div>
      </div>
    );
  }

  if (error && currentUserRole !== "ADMIN") {
    return (
      <div className="admin-wrapper">
        <div className="error-message access-denied">{error}</div>
      </div>
    );
  }

  if (currentUserRole !== "ADMIN") {
    return (
      <div className="admin-wrapper">
        <div className="error-message access-denied">
          You do not have administrative privileges to view this page.
        </div>
      </div>
    );
  }

  return (
    <div className="admin-wrapper">
      <div className="admin-container">
        <div className="admin-header">
          <h1 className="admin-title">User Management</h1>
          <button onClick={() => navigate(-1)} className="back-button">
            ← Back
          </button>
        </div>
        <p className="admin-subtitle">Manage all users and their roles</p>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <div className="controls">
          <input
            type="text"
            placeholder="Search users..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <button onClick={handleAddUserClick} className="add-user-btn">
            + Add New User
          </button>
        </div>

        {showAddUserForm && (
          <div className="user-form-modal">
            <div className="user-form-content">
              <h2>{editingUser ? "Edit User" : "Add New User"}</h2>
              <form onSubmit={handleFormSubmit}>
                <div className="form-group">
                  <label htmlFor="form-name">Name</label>
                  <input
                    type="text"
                    id="form-name"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="form-email">Email</label>
                  <input
                    type="email"
                    id="form-email"
                    value={formEmail}
                    onChange={(e) => setFormEmail(e.target.value)}
                    required
                  />
                </div>
                {!editingUser && ( // Password field only for new users
                  <div className="form-group">
                    <label htmlFor="form-password">Password</label>
                    <input
                      type="password"
                      id="form-password"
                      value={formPassword}
                      onChange={(e) => setFormPassword(e.target.value)}
                      required={!editingUser}
                    />
                  </div>
                )}
                <div className="form-group">
                  <label htmlFor="form-role">Role</label>
                  <select
                    id="form-role"
                    value={formRole}
                    onChange={(e) => setFormRole(e.target.value)}
                    required
                  >
                    <option value="USER">USER</option>
                    <option value="HANDOVER_AGENT">HANDOVER_AGENT</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </div>
                <div className="form-group checkbox-group">
                  <input
                    type="checkbox"
                    id="form-is-active"
                    checked={formIsActive}
                    onChange={(e) => setFormIsActive(e.target.checked)}
                  />
                  <label htmlFor="form-is-active">Active</label>
                </div>
                <div className="form-actions">
                  <button
                    type="submit"
                    className="save-button"
                    disabled={loading}
                  >
                    {loading
                      ? "Saving..."
                      : editingUser
                      ? "Update User"
                      : "Create User"}
                  </button>
                  <button
                    type="button"
                    className="cancel-button"
                    onClick={() => setShowAddUserForm(false)}
                    disabled={loading}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {!showAddUserForm && (
          <div className="user-list-table">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.length > 0 ? (
                  filteredUsers.map((user) => (
                    <tr key={user.id}>
                      <td>{user.name}</td>
                      <td>{user.email}</td>
                      <td>
                        <span
                          className={`user-role-badge ${user.role.toLowerCase()}`}
                        >
                          {user.role}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`user-status-badge ${
                            user.isActive ? "active" : "inactive"
                          }`}
                        >
                          {user.isActive ? "Active" : "Inactive"}
                        </span>
                      </td>
                      <td className="action-buttons">
                        <button
                          className="edit-user-btn"
                          onClick={() => handleEditUserClick(user)}
                        >
                          Edit
                        </button>
                        <button
                          className={`toggle-status-btn ${
                            user.isActive ? "deactivate" : "activate"
                          }`}
                          onClick={() => handleDeleteUser(user.id)}
                        >
                          {user.isActive ? "Deactivate" : "Activate"}
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="no-users-found">
                      No users found matching your criteria.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminUserManagement;
