import React from "react";
import Sidebar from "./Sidebar";
import "../styles/PageShell.css";
import { Link, Outlet, useNavigate } from "react-router-dom";
import logoo2 from "../assets/logoo2.png";

const PageShell = () => {
  const navigate = useNavigate();
  const handleSubmit = (e) => {
    e.preventDefault();
    navigate("/dashboard"); // Go to dashboard on New Document click
  };
  return (
    <div className="home-container">
      <header className="header">
        <div className="logo">
          <img src={logoo2} alt="Document Tracking System Logo" />
        </div>
        <nav className="nav">
          <Link to="/documents" className="nav-link">
            Documents
          </Link>
          <Link to="/dashboard" className="nav-link">
            Reports
          </Link>
          <Link to="/profile" className="nav-link">
            Users
          </Link>
          <Link to="/settings" className="nav-link">
            Settings
          </Link>
        </nav>
        <div className="header-actions">
          <button className="get-started-btn" onClick={handleSubmit}>
            New Document
          </button>
          <div className="icon-container">
            <i className="search-icon">🔍</i>
          </div>
          <Link to="/login" className="icon-container">
            <i className="user-icon">👤</i>
          </Link>
        </div>
      </header>

      {/* Layout content area for nested routes */}
      <main>
        <Outlet />
      </main>
    </div>
  );
};

export default PageShell;
