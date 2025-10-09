// src/Pages/Dashboard/NewDocument.jsx
import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/NewDocument.css";
import { documentService } from "../../services/documentService.js";
import { workflowService } from "../../services/workflowService.js";
import { authService } from "../../services/authService.js";

// --- Utility Functions ---

// Mock QR Code Component (Simulating a real library like qrcode.react)
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
      <p className="qr-caption">Scan to view document details</p>
    </div>
  );
};

// --- Main Component ---

const NewDocument = () => {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [pipeline, setPipeline] = useState("");
  const [category, setCategory] = useState("");
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [availablePipelines, setAvailablePipelines] = useState([]);
  const [loadingPipelines, setLoadingPipelines] = useState(true);

  // New State for Generated Data
  const [documentCode, setDocumentCode] = useState("");
  const [qrCodeData, setQrCodeData] = useState("");

  // Check authentication on component mount
  useEffect(() => {
    if (!authService.isAuthenticated()) {
      setError(
        "Please log in to upload documents. Redirecting to login page..."
      );
      setTimeout(() => navigate("/auth/login"), 3000);
      return;
    }
  }, [navigate]);

  // Load available pipelines on component mount
  useEffect(() => {
    const loadPipelines = async () => {
      try {
        // Check if user is authenticated before making API calls
        if (!authService.isAuthenticated()) {
          console.warn("User not authenticated, using default pipelines");
          setAvailablePipelines([
            { id: "general", name: "General Approval Workflow" },
            { id: "review", name: "Document Review Process" },
            { id: "financial", name: "Financial Approval" },
          ]);
          setLoadingPipelines(false);
          return;
        }

        setLoadingPipelines(true);

        // Add a timeout to prevent hanging API calls
        const timeoutPromise = new Promise((_, reject) =>
          setTimeout(() => reject(new Error("API timeout")), 5000)
        );

        const apiPromise = workflowService.listTemplates();

        const templates = await Promise.race([apiPromise, timeoutPromise]);
        setAvailablePipelines(templates || []);
      } catch (error) {
        console.error("Error loading pipelines:", error);
        // Fallback to default pipelines if API fails
        setAvailablePipelines([
          { id: "general", name: "General Approval Workflow" },
          { id: "review", name: "Document Review Process" },
          { id: "financial", name: "Financial Approval" },
        ]);
        // Don't set error state for pipeline loading failure
        console.warn("Using fallback pipelines due to API error");
      } finally {
        setLoadingPipelines(false);
      }
    };

    // Only load pipelines if user is authenticated
    if (authService.isAuthenticated()) {
      loadPipelines();
    } else {
      // Set default pipelines immediately if not authenticated
      setAvailablePipelines([
        { id: "general", name: "General Approval Workflow" },
        { id: "review", name: "Document Review Process" },
        { id: "financial", name: "Financial Approval" },
      ]);
      setLoadingPipelines(false);
    }
  }, []);

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

    if (!title || !pipeline || !category || !file) {
      setError("Please fill in all required fields and select a file.");
      return;
    }

    // Get current user for document ownership
    const currentUser = authService.getCurrentUser();
    if (!currentUser || !currentUser.userId) {
      setError("User not authenticated. Please log in again.");
      return;
    }

    setLoading(true);
    try {
      // Create document metadata
      const documentMetadata = {
        title: title,
        documentType: category,
        description: description,
        ownerUserId: currentUser.userId,
      };

      // Create document with file upload
      const createdDocument = await documentService.createDocumentWithFile(
        documentMetadata,
        file
      );

      // Start the selected workflow/pipeline
      if (pipeline) {
        try {
          await workflowService.startWorkflow({
            documentId: createdDocument.id,
            templateId: parseInt(pipeline),
            initiator: currentUser.username,
          });
        } catch (workflowError) {
          console.warn(
            "Document created but workflow failed to start:",
            workflowError
          );
          // Don't fail the entire operation if workflow fails
        }
      }

      // Store the document code (ID) for display
      setDocumentCode(`DOC-${createdDocument.id}`);

      // Set QR code data (document view URL)
      const documentViewUrl = `${window.location.origin}/documents/${createdDocument.id}`;
      setQrCodeData(documentViewUrl);

      setSuccess(
        `Document "${
          createdDocument.title
        }" created successfully! Document ID: ${createdDocument.id}${
          pipeline ? " - Workflow started!" : ""
        }`
      );

      // Redirect after showing success message
      setTimeout(() => navigate("/documents"), 3000);
    } catch (err) {
      setError(err?.message || "Failed to create document. Please try again.");
      console.error("Document creation error:", err);
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

          <div className="form-group">
            <label htmlFor="pipeline">
              Pipeline <span className="required">*</span>
            </label>
            <select
              id="pipeline"
              value={pipeline}
              onChange={(e) => setPipeline(e.target.value)}
              required
              disabled={loadingPipelines}
            >
              <option value="">
                {loadingPipelines ? "Loading pipelines..." : "Select Pipeline"}
              </option>
              {availablePipelines.map((pipelineTemplate) => (
                <option key={pipelineTemplate.id} value={pipelineTemplate.id}>
                  {pipelineTemplate.name}
                  {pipelineTemplate.documentType &&
                    ` (${pipelineTemplate.documentType})`}
                </option>
              ))}
            </select>
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
