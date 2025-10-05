import React from "react";
import { Link, Outlet } from "react-router-dom";
import "../styles/PageShell.css";
// NOTE: Images are no longer used since the Integrations/Features section is removed for simplicity.
// Removed logoImage1 and logoImage2 imports

// Simple Icon Components for Features (simulating a font awesome-like experience)
// Keeping this component, but it's not used in the JSX below to keep it simple.

const PageShell = () => {
  // Removed scroll-based logic (useEffect and useRef) for a non-scrolling page

  return (
    // The wrapper is now responsible for containing a single viewport height
    <div className="landing-wrapper">
      <header className="landing-header">
        <div className="landing-logo">
          <Link to="/" className="logo-link">
            DocuTrace
          </Link>
        </div>
      </header>

      {/* Hero section now serves as the main page content, filling remaining space */}
      <div className="hero-section">
        <div className="gradient-overlay-hero"></div>
        {/* Simplified the content wrapper for central focus */}
        <div className="hero-content">
          <h1>Effortlessly Track Your Documents</h1>
          <p>
            Streamline your workflow with our intuitive and powerful document
            management solution. Keep track of approvals, versions, and
            deadlines with ease.
          </p>
          <div className="hero-actions">
            <Link to="/onboarding" className="cta-btn primary-cta-btn">
              Get Started
            </Link>
            <Link to="/learnmore" className="cta-btn secondary-cta-btn">
              Learn More
            </Link>
          </div>
        </div>
      </div>
      {/* Render child routes here, outside hero section */}
      <Outlet />

      {/* FOOTER SECTION - Removed for absolute simplicity, as it usually requires scrolling.
          If you must keep a footer, it should be made small and absolutely positioned at the bottom.
          For a truly non-scrolling, simple page, we omit it. */}
      {/* <footer className="footer">...</footer> */}
    </div>
  );
};

export default PageShell;
