import React from "react";
import { NavLink } from "react-router-dom"; // assuming you use react-router for navigation
import "../styles/Sidebar.css";

const Sidebar = () => {
  return (
    <aside className="sidebar">
      <h2 className="sidebar-title">DocTrack</h2>

      <nav className="sidebar-nav">
        <ul>
          <li>
            <NavLink to="/dashboard" activeClassName="active-link">
              Dashboard
            </NavLink>
          </li>

          <li>
            <NavLink to="/documents" activeClassName="active-link">
              Documents
            </NavLink>
          </li>

          <li>
            <NavLink to="/pipelines" activeClassName="active-link">
              Pipelines
            </NavLink>
          </li>

          <li>
            <NavLink to="/handover" activeClassName="active-link">
              Handover Queue
            </NavLink>
          </li>

          <li>
            <NavLink to="/departments" activeClassName="active-link">
              Departments
            </NavLink>
          </li>

          <li>
            <NavLink to="/notifications" activeClassName="active-link">
              Notifications
            </NavLink>
          </li>

          <li>
            <NavLink to="/audit" activeClassName="active-link">
              Audit Logs
            </NavLink>
          </li>

          <li>
            <NavLink to="/profile" activeClassName="active-link">
              Profile
            </NavLink>
          </li>

          <li>
            <NavLink to="/settings" activeClassName="active-link">
              Settings
            </NavLink>
          </li>

          <li>
            <NavLink to="/help" activeClassName="active-link">
              Help Center
            </NavLink>
          </li>
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;
