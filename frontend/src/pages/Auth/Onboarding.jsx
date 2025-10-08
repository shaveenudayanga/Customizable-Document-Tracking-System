import { useState, useEffect, useRef } from "react";
import { useNavigate, Link } from "react-router-dom";
import "../../styles/Onboarding.css";
import { authService } from "../../services/authService";
import signInImage from "../../assets/sign_in.jpg";

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;
// Password regex: at least 8 chars, 1 uppercase, 1 lowercase, 1 number
const passwordStrengthRegex = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$/;

const Onboarding = () => {
  // Form values - matching backend RegisterRequest DTO
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState("USER");
  const [position, setPosition] = useState("");
  const [sectionId, setSectionId] = useState("");

  // UI states
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // Field validation states
  const [usernameValid, setUsernameValid] = useState(null);
  const [emailValid, setEmailValid] = useState(null);
  const [passwordStrength, setPasswordStrength] = useState(0); // 0-4 strength
  const [passwordsMatch, setPasswordsMatch] = useState(null);

  const navigate = useNavigate();

  // Validate username in real-time
  useEffect(() => {
    if (username === "") {
      setUsernameValid(null);
      return;
    }
    setUsernameValid(username.trim().length >= 3);
  }, [username]);

  // Validate email in real-time
  useEffect(() => {
    if (email === "") {
      setEmailValid(null);
      return;
    }
    setEmailValid(emailRegex.test(email.trim()));
  }, [email]);

  // Calculate password strength
  useEffect(() => {
    if (!password) {
      setPasswordStrength(0);
      return;
    }

    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[^A-Za-z0-9]/.test(password)) strength++;

    setPasswordStrength(strength);
  }, [password]);

  // Check if passwords match
  useEffect(() => {
    if (!confirmPassword) {
      setPasswordsMatch(null);
      return;
    }
    setPasswordsMatch(password === confirmPassword);
  }, [password, confirmPassword]);

  const getPasswordStrengthLabel = () => {
    if (!password) return "";
    const labels = [": Weak", ": Fair", ": Good", ": Strong"];
    return labels[passwordStrength - 1] || ": Too weak";
  };

  const validate = () => {
    const u = username.trim();
    const e = email.trim();
    if (u.length < 3) return "Username must be at least 3 characters.";
    if (!emailRegex.test(e)) return "Please enter a valid email.";
    if (password.length < 8) return "Password must be at least 8 characters.";
    if (password !== confirmPassword) return "Passwords do not match.";
    return "";
  };

  const extractError = (err) => {
    const d = err?.response?.data;
    const fieldErrors =
      d?.errors?.map?.(
        (e) => e.defaultMessage || e.message || `${e.field}: ${e.error}`
      ) ||
      d?.violations?.map?.((v) => `${v.fieldName || v.field}: ${v.message}`) ||
      null;
    return (
      (fieldErrors && fieldErrors.join(", ")) ||
      d?.message ||
      d?.error ||
      err?.message ||
      "Sign up failed"
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    const v = validate();
    if (v) {
      setError(v);
      return;
    }
    setLoading(true);
    try {
      // Match backend RegisterRequest DTO
      await authService.register({
        username: username.trim(),
        email: email.trim(),
        password,
        role: role || undefined,
        position: position || undefined,
        sectionId: sectionId || undefined,
      });
      // After successful registration, authService stores token automatically
      navigate("/dashboard", {
        replace: true,
      });
    } catch (err) {
      setError(extractError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    navigate("/");
  };

  const isDisabled =
    loading ||
    !usernameValid ||
    emailValid !== true ||
    passwordStrength < 2 ||
    !passwordsMatch;

  // Password criteria for ticking each rule
  const passwordCriteria = [
    { label: "At least 8 characters", ok: password.length >= 8 },
    { label: "One uppercase letter", ok: /[A-Z]/.test(password) },
    { label: "One number", ok: /[0-9]/.test(password) },
    { label: "One special character", ok: /[^A-Za-z0-9]/.test(password) },
  ];

  return (
    <div className="signup-wrapper">
      <div className="signup-card">
        {/* Right Side Image */}
        <div className="signup-image-section">
          <div className="gradient-overlay"></div>
          <img src={signInImage} alt="Signup Visual" />{" "}
          {/* FIXED: use imported image */}
        </div>

        {/* Left Side Form */}
        <div className="signup-form-section">
          <span className="close-btn" onClick={handleClose}>
            ×
          </span>
          <h2 className="signup-title">Create Account</h2>
          <p className="signup-subtitle">
            Join DocuTrace and start tracking your documents
          </p>

          {error && <div className="error-message">{error}</div>}

          <form className="signup-form" onSubmit={handleSubmit}>
            <label htmlFor="username">
              Username
              {usernameValid !== null && (
                <span
                  className={usernameValid ? "valid-text" : "invalid-text"}
                  style={{ color: usernameValid ? "#1c5d34" : "#cd6161" }}
                >
                  {usernameValid ? "✓" : "✗ Min 3 characters"}
                </span>
              )}
            </label>
            <input
              type="text"
              id="username"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              className={usernameValid === false ? "invalid-input" : ""}
              required
            />

            <label htmlFor="email">
              Email
              {emailValid !== null && (
                <span
                  className={emailValid ? "valid-text" : "invalid-text"}
                  style={{ color: emailValid ? "#1c5d34" : "#cd6161" }}
                >
                  {emailValid ? "✓" : "✗"}
                </span>
              )}
            </label>
            <input
              type="email"
              id="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              className={emailValid === false ? "invalid-input" : ""}
              required
            />

            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              placeholder="Create a password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            {password && (
              <div className="password-strength-meter">
                <div
                  className={`strength-bar strength-${passwordStrength}`}
                ></div>
              </div>
            )}
            {password && (
              <div className="password-hints">
                <p>Password should contain:</p>
                <ul className="password-hints-list">
                  {passwordCriteria.map((c) => (
                    <li
                      key={c.label}
                      className={c.ok ? "valid-hint" : ""}
                      style={{ color: c.ok ? "#1c5d34" : undefined }}
                    >
                      <span
                        className={`hint-check ${c.ok ? "ok" : ""}`}
                        style={{ color: c.ok ? "#1c5d34" : undefined }}
                      ></span>
                      {c.label}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <label htmlFor="confirmPassword">
              Confirm Password
              {passwordsMatch !== null && (
                <span
                  className={passwordsMatch ? "valid-text" : "invalid-text"}
                  style={{ color: passwordsMatch ? "#1c5d34" : "#cd6161" }}
                >
                  {passwordsMatch ? "✓" : "Passwords don't match"}
                </span>
              )}
            </label>
            <input
              type="password"
              id="confirmPassword"
              placeholder="Retype your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className={passwordsMatch === false ? "invalid-input" : ""}
              required
            />

            <button type="submit" className="signup-btn" disabled={isDisabled}>
              {loading ? "Creating..." : "Sign Up"}
            </button>
          </form>

          <div className="signup-footer">
            <span>Already have an account? </span>
            <Link to="/login">Log In</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Onboarding;
