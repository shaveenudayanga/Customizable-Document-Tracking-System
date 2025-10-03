// src/Pages/Dashboard/NewDocument.jsx
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/NewDocument.css";
// import { api } from "../../lib/api";

// --- Utility Functions ---

// 1. Unique Document ID Generator (Simulating UUID or similar)
const generateUniqueId = () => {
  // Generates a short, readable unique code (e.g., DOC-ABC-12345)
  const prefix = "DOC-";
  const randomChars = Math.random().toString(36).substring(2, 5).toUpperCase();
  const randomNumbers = Math.floor(10000 + Math.random() * 90000);
  return `${prefix}${randomChars}-${randomNumbers}`;
};

// 2. Mock QR Code Component (Simulating a real library like qrcode.react)
const QRCodeMock = ({ value }) => {
  // In a real application, this would render the QR code SVG/Canvas.
  // Here, we display the data it would encode.
  if (!value) return null;
  return (
    <div className="qr-mock-container">
      <div className="qr-mock-box">
        {/*  - Placeholder for real QR code */}
        <span style={{ fontSize: "12px", color: "#673ab7" }}>
          QR Code Data:
        </span>
        <span style={{ fontSize: "10px", wordBreak: "break-all" }}>
          {value}
        </span>
      </div>
      <p className="qr-caption">Scan to view document details (Simulated)</p>
    </div>
  );
};

// --- Main Component ---

const NewDocument = () => {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [pipeline, setPipeline] = useState("");
  const [department, setDepartment] = useState("");
  const [category, setCategory] = useState("");
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // New State for Generated Data
  const [documentCode, setDocumentCode] = useState("");
  const [qrCodeData, setQrCodeData] = useState("");

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setFile(e.target.files[0]);
    } else {
      setFile(null);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setDocumentCode("");
    setQrCodeData("");

    if (!title || !pipeline || !department || !category || !file) {
      setError("Please fill in all required fields and select a file.");
      return;
    }

    setLoading(true);
    try {
      // 1. Generate Unique Code
      const uniqueId = generateUniqueId();
      setDocumentCode(uniqueId);

      // In a real scenario, the back-end would return the permanent URL.
      // We simulate the permanent URL that the QR code will encode.
      const permanentDocUrl = `${window.location.origin}/view-document/${uniqueId}`;
      setQrCodeData(permanentDocUrl);

      // --- API Upload Simulation ---
      // In a real app, send data + uniqueId to the API

      await new Promise((resolve) => setTimeout(resolve, 1500));

      console.log("Document creation simulated:", {
        uniqueId,
        permanentDocUrl,
      });

      setSuccess(`Document created! Code: ${uniqueId}. Redirecting...`);

      setTimeout(() => navigate("/documents"), 2500); // Wait longer to show the generated code
    } catch (err) {
      setError(err?.message || "Failed to upload document.");
      console.error("Upload error:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoBack = () => {
    navigate(-1); // Go back to the previous page
  };

  return (
    <div className="new-doc-wrapper">
      <div className="new-doc-container">
        <div className="new-doc-header">
          <button onClick={handleGoBack} className="back-button">
            ← Back
          </button>
          <h1 className="new-doc-title">Upload New Document</h1>
        </div>
        <p className="new-doc-subtitle">
          Fill in the details to create a new document record
        </p>

        {error && <div className="message error-message">{error}</div>}
        {success && <div className="message success-message">{success}</div>}
        {documentCode && (
          <div className="message generated-code-message">
            ✅ Document Code Generated: <strong>{documentCode}</strong>
          </div>
        )}

        <form className="new-doc-form" onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="title">
              Document Title <span className="required">*</span>
            </label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Q4 Marketing Report"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="description">Description</label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Provide a brief description of the document"
              rows="4"
            ></textarea>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="pipeline">
                Pipeline <span className="required">*</span>
              </label>
              <select
                id="pipeline"
                value={pipeline}
                onChange={(e) => setPipeline(e.target.value)}
                required
              >
                <option value="">Select Pipeline</option>
                <option value="Sales">Sales Pipeline</option>
                <option value="Marketing">Marketing Workflow</option>
                <option value="Legal">Legal Review</option>
                <option value="HR">HR Onboarding</option>
                <option value="Finance">Finance Approval</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="department">
                Department <span className="required">*</span>
              </label>
              <select
                id="department"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                required
              >
                <option value="">Select Department</option>
                <option value="Sales">Sales</option>
                <option value="Marketing">Marketing</option>
                <option value="Legal">Legal</option>
                <option value="HR">Human Resources</option>
                <option value="Finance">Finance</option>
                <option value="Operations">Operations</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="category">
              Category <span className="required">*</span>
            </label>
            <select
              id="category"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              required
            >
              <option value="">Select Category</option>
              <option value="Reports">Reports</option>
              <option value="Contracts">Contracts</option>
              <option value="Policies">Policies</option>
              <option value="Proposals">Proposals</option>
              <option value="Training">Training Materials</option>
            </select>
          </div>

          <div className="form-row file-and-qr-row">
            <div className="form-group file-upload-group">
              <label htmlFor="file-upload" className="file-upload-label">
                Upload File (PDF/Doc) <span className="required">*</span>
              </label>
              <input
                type="file"
                id="file-upload"
                onChange={handleFileChange}
                required
                className="file-input-hidden"
                accept=".pdf,.doc,.docx,.xls,.xlsx" // Accept common document types
              />
              <div className="file-input-display">
                <span className="file-name">
                  {file ? file.name : "Select a file to upload"}
                </span>
                <span className="browse-button">Browse</span>
              </div>
              {file && (
                <span className="file-size">
                  ({(file.size / 1024 / 1024).toFixed(2)} MB)
                </span>
              )}
            </div>

            {qrCodeData && (
              <div className="form-group qr-code-preview-group">
                <label>Generated QR Code</label>
                <QRCodeMock value={qrCodeData} />
              </div>
            )}
          </div>

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? "Uploading..." : "Create Document"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default NewDocument;
