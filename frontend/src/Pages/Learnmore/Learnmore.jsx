import React, { useEffect } from "react";
import { Link } from "react-router-dom";
import "../../styles/Learnmore.css";

const LearnMorePage = () => {
  // CRITICAL: This useEffect adds the class to the body to enable scrolling.
  useEffect(() => {
    document.body.classList.add("learnmore-active");
    return () => {
      document.body.classList.remove("learnmore-active");
    };
  }, []);

  return (
    // The learn-more-wrapper ensures the content can scroll vertically
    <div className="learn-more-wrapper">
      {/* Reused Header Component */}
      <header className="landing-header">
        <div className="landing-logo">
          {/* Link to the main landing page */}
          <Link to="/" className="logo-link">
            DocuTrace
          </Link>
        </div>
      </header>
      {/* Main Content Area */}
      <div className="content-container">
        {/* Title and Subtitle */}
        <h1>Learn More About DocuTrace</h1>
        <p className="subtitle">
          A precise and simple solution for modern document tracking.
        </p>
        {/* Feature Grid: Arranged horizontally on large screens */}
        <div className="info-section-grid">
          <section className="info-section">
            <h2> Version Control & History</h2>
            <p>
              Every edit is automatically tracked and logged. You can review the
              complete history of any document, revert to a previous version
              instantly, and see exactly who made which change and when.
            </p>
          </section>

          <section className="info-section">
            <h2> Approval Workflows</h2>
            <p>
              Define custom, automated approval paths for critical documents.
              Set deadlines, notify stakeholders, and eliminate manual
              follow-up. Approvals are logged with an immutable audit trail.
            </p>
          </section>

          <section className="info-section">
            <h2> Simple Security & Compliance</h2>
            <p>
              Your data is protected with end-to-end encryption. Granular access
              controls ensure that only authorized users can view, edit, or
              approve documents. Compliance reporting is built-in.
            </p>
          </section>
        </div>{" "}
        {/* End info-section-grid */}
        {/* Call to Action Footer */}
        <div className="cta-footer-block">
          <h3>Ready to start tracking with precision?</h3>
          <Link to="/onboarding" className="cta-btn primary-cta-btn">
            Start Your Free Trial Today 🚀
          </Link>
        </div>
      </div>{" "}
      {/* End content-container */}
      {/* NEW: Animated Footer Component */}
      <footer className="docutrace-footer">
        <div className="footer-content-wrapper">
          <div className="footer-logo">
            <Link to="/" className="logo-link">
              DocuTrace
            </Link>
            <p>Precision Document Management</p>
          </div>
          <div className="footer-links">
            <h4>Product</h4>
            <Link to="/features">Features</Link>
            <Link to="/pricing">Pricing</Link>
            <Link to="/integrations">Integrations</Link>
          </div>
          <div className="footer-links">
            <h4>Company</h4>
            <Link to="/about">About Us</Link>
            <Link to="/careers">Careers</Link>
            <Link to="/contact">Contact</Link>
          </div>
          <div className="footer-social">
            <h4>Connect</h4>
            <div className="social-icons">
              {/* Placeholder Icons - Replace with actual icons (e.g., FontAwesome) */}
              <a href="https://twitter.com" aria-label="Twitter">
                🐦
              </a>
              <a href="https://linkedin.com" aria-label="LinkedIn">
                🔗
              </a>
              <a href="https://github.com" aria-label="GitHub">
                💻
              </a>
            </div>
          </div>
        </div>
        <div className="footer-bottom">
          <p>
            &copy; {new Date().getFullYear()} DocuTrace. All rights reserved.
          </p>
          <div className="legal-links">
            <Link to="/privacy">Privacy Policy</Link>
            <span>|</span>
            <Link to="/terms">Terms of Service</Link>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default LearnMorePage;
