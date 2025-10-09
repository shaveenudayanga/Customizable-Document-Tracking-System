import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/Login.css";
import { authService } from "../../services/authService";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGoogle, faFacebookF } from "@fortawesome/free-brands-svg-icons";
import loginImage from "../../assets/login.jpg";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await authService.login({ username, password });
      if (response && response.token) {
        // authService already stores the token and user data

        // Check user role and redirect accordingly
        const userRole = response.role?.toLowerCase();

        if (userRole === "admin") {
          // Redirect admin users to Admin User Management
          navigate("/adminusermanagement");
        } else {
          // Redirect regular users to dashboard
          navigate("/dashboard");
        }
      }
    } catch (err) {
      setError(err?.message || "Login failed. Please check your credentials.");
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    navigate("/");
  };

  return (
    <div className="login-wrapper">
      <div className="login-card">
        {/* Left Side Form */}
        <div className="login-form-section">
          <span className="close-btn" onClick={handleClose}>
            ×
          </span>
          <h2 className="login-title">Log In</h2>
          <p className="login-subtitle">
            Welcome back! Please enter your details
          </p>

          {error && <div className="error-message">{error}</div>}

          <form className="login-form" onSubmit={handleSubmit}>
            <label>Username</label>
            <input
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />

            <label>Password</label>
            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            <a className="forgot-password" href="/forgot-password">
              Forgot password?
            </a>

            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? "Logging in..." : "Log In"}
            </button>
          </form>

          <div className="divider">
            <span>Or Continue With</span>
          </div>

          <div className="social-login">
            <button className="google-btn">
              <FontAwesomeIcon icon={faGoogle} /> <br />
              Google
            </button>
            <button className="facebook-btn">
              <FontAwesomeIcon icon={faFacebookF} />
              <br />
              Facebook
            </button>
          </div>

          <p className="signup-link">
            Don’t have an account? <a href="/onboarding">Sign up</a>
          </p>
        </div>

        {/* Right Side Image */}
        <div className="login-image-section">
          <div className="gradient-overlay"></div>
          <img src={loginImage} alt="Login Visual" />
        </div>
      </div>
    </div>
  );
};

export default Login;
