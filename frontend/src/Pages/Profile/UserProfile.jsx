import React, { useState } from "react";
import {
  UserPlus,
  UserX,
  Send,
  Trash2,
  Search,
  Mail,
  Loader,
  CheckCircle,
} from "lucide-react";
import "../../styles/UserProfile.css";

// --- Mock Data ---
const initialUsers = [
  {
    id: 101,
    name: "Alice Johnson",
    email: "alice.j@corp.com",
    role: "Manager",
    status: "Active",
    dateJoined: "2023-01-15",
  },
  {
    id: 102,
    name: "Bob Smith",
    email: "bob.s@corp.com",
    role: "Auditor",
    status: "Active",
    dateJoined: "2023-03-22",
  },
  {
    id: 103,
    name: "Charlie Brown",
    email: "charlie.b@corp.com",
    role: "Viewer",
    status: "Active",
    dateJoined: "2023-05-10",
  },
];

const initialInvites = [
  {
    id: 201,
    email: "pending@invite.com",
    status: "Pending",
    role: "Viewer",
    dateSent: "2024-05-01",
  },
  {
    id: 202,
    email: "another@test.net",
    status: "Pending",
    role: "Auditor",
    dateSent: "2024-05-05",
  },
];

// --- Sub-Component: Invitation Form ---
const InviteUserForm = ({ addInvite }) => {
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("Viewer");
  const [isSending, setIsSending] = useState(false);
  const [sentSuccess, setSentSuccess] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!email) return;

    setIsSending(true);
    setSentSuccess(false);

    // Simulate API call delay
    setTimeout(() => {
      const newInvite = {
        id: Date.now(),
        email: email,
        status: "Pending",
        role: role,
        dateSent: new Date().toISOString().slice(0, 10),
      };
      addInvite(newInvite);

      setIsSending(false);
      setSentSuccess(true);
      setEmail("");
      setRole("Viewer");

      // Clear success message after a short time
      setTimeout(() => setSentSuccess(false), 3000);
    }, 1500);
  };

  return (
    <div className="invite-form-card">
      <h3 className="card-title">
        <UserPlus size={20} /> Invite New User
      </h3>
      <form onSubmit={handleSubmit} className="invite-form">
        <div className="form-row-grid">
          <div className="form-field">
            <label htmlFor="email">Email Address</label>
            <div className="input-with-icon">
              <Mail size={16} />
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="user@domain.com"
                required
              />
            </div>
          </div>
          <div className="form-field">
            <label htmlFor="role">Assign Role</label>
            <select
              id="role"
              value={role}
              onChange={(e) => setRole(e.target.value)}
            >
              <option value="Viewer">Viewer</option>
              <option value="Auditor">Auditor</option>
              <option value="Manager">Manager</option>
            </select>
          </div>
        </div>

        <button
          type="submit"
          className={`invite-button ${isSending ? "sending" : ""} ${
            sentSuccess ? "success" : ""
          }`}
          disabled={isSending || sentSuccess}
        >
          {isSending ? (
            <>
              <Loader size={18} className="spinner" /> Sending...
            </>
          ) : sentSuccess ? (
            <>
              <CheckCircle size={18} /> Invitation Sent
            </>
          ) : (
            <>
              <Send size={18} /> Send Invite Link
            </>
          )}
        </button>
      </form>
    </div>
  );
};

// --- Sub-Component: User Table Row ---
const UserRow = ({ user, onDelete }) => {
  const isInvite = user.status === "Pending";
  const rowClass = isInvite ? "invite-row" : "user-row";
  const statusClass =
    user.status === "Active" ? "status-active" : "status-pending";

  return (
    <tr className={rowClass}>
      <td>{user.name || "-"}</td>
      <td>{user.email}</td>
      <td>{user.role}</td>
      <td>
        <span className={`user-status-badge ${statusClass}`}>
          {user.status}
        </span>
      </td>
      <td>{user.dateJoined || user.dateSent}</td>
      <td className="action-cell">
        <button
          className="delete-button"
          onClick={() => onDelete(user.id, isInvite)}
          title={`Delete ${isInvite ? "Invitation" : "User"}`}
        >
          <Trash2 size={16} />
        </button>
      </td>
    </tr>
  );
};

// --- Main Component: User Management Panel ---
const UserManagement = () => {
  const [activeUsers, setActiveUsers] = useState(initialUsers);
  const [pendingInvites, setPendingInvites] = useState(initialInvites);
  const [searchTerm, setSearchTerm] = useState("");

  const addInvite = (newInvite) => {
    setPendingInvites((prev) => [...prev, newInvite]);
  };

  const handleDelete = (id, isInvite) => {
    if (isInvite) {
      setPendingInvites((prev) => prev.filter((invite) => invite.id !== id));
    } else {
      setActiveUsers((prev) => prev.filter((user) => user.id !== id));
    }
  };

  const allUsers = [...activeUsers, ...pendingInvites];

  const filteredUsers = allUsers.filter(
    (user) =>
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.role.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="user-management-panel admin-details-card">
      {/* 1. Invitation Form */}
      <InviteUserForm addInvite={addInvite} />

      <div className="management-table-section">
        <div className="table-header-controls">
          <h2 className="section-title">
            <UserX size={24} style={{ marginRight: "10px" }} />
            System Users & Invites
          </h2>
          <div className="search-box">
            <Search size={18} />
            <input
              type="text"
              placeholder="Search users by name, email, or role..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>

        {/* 2. User/Invite Table */}
        <div className="table-responsive">
          <table className="user-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>{pendingInvites.length > 0 ? "Date" : "Joined"}</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="6" className="empty-state-cell">
                    No users or invites match your search criteria.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <UserRow key={user.id} user={user} onDelete={handleDelete} />
                ))
              )}
            </tbody>
          </table>
        </div>

        {filteredUsers.length > 0 && (
          <div className="table-summary">
            Showing {filteredUsers.length} of {allUsers.length} total users and
            invites.
          </div>
        )}
      </div>
    </div>
  );
};

export default UserManagement;
