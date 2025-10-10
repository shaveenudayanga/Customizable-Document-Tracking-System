import React from "react";
import { Navigate } from "react-router-dom";
import { authService } from "../services/authService";

/**
 * ProtectedRoute Component
 * Wraps routes that require authentication and/or specific roles
 */
const ProtectedRoute = ({ children, requiredRole = null }) => {
  const isAuthenticated = authService.isAuthenticated();
  const currentUser = authService.getCurrentUser();

  // Check if user is authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Check role if required (case-insensitive comparison)
  if (
    requiredRole &&
    currentUser?.role?.toLowerCase() !== requiredRole.toLowerCase()
  ) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default ProtectedRoute;
