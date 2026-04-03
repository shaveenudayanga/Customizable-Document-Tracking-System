import React, { useState } from "react";
import "../../styles/ForgotPassword.css";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();
    // Here you can handle the password reset logic
    alert(`Password reset link sent to ${email}`);
    setEmail("");
  };

  return (
    <div className="forgot-password-page">
      <div className="forgot-password-card">
        <h2>Forgot Password</h2>
        <p>Enter your email address to reset your password.</p>
        <form className="forgot-password-form" onSubmit={handleSubmit}>
          <label htmlFor="email">Email Address</label>
          <input
            type="email"
            id="email"
            name="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <button type="submit">Send Reset Link</button>
        </form>
        <div className="back-to-login">
          <a href="/login">Back to Login</a>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
