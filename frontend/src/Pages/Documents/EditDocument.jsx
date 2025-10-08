import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "../../styles/EditDocument.css";
import { documentService } from "../../services/documentService.js";

const EditDocument = () => {
  const { id } = useParams(); // Get document ID from URL
  const navigate = useNavigate();
  const [document, setDocument] = useState(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [pipeline, setPipeline] = useState("");
  const [department, setDepartment] = useState("");
  const [category, setCategory] = useState("");
  const [owner, setOwner] = useState(""); // Assuming owner can be reassigned
  const [status, setStatus] = useState(""); // Assuming status can be changed directly or via pipeline
  const [loading, setLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    const fetchDocument = async () => {
      setLoading(true);
      setError("");
      try {
        // Use documentService instead of hard-coded data
        const docData = await documentService.getDocumentById(id);

        if (docData) {
          setDocument(docData);
          setTitle(docData.title);
          setDescription(docData.description || "");
          setPipeline(docData.pipeline);
          setDepartment(docData.department);
          setCategory(docData.category);
          setOwner(docData.owner);
          setStatus(docData.status);
        } else {
          setError("Document not found for editing.");
        }
      } catch (err) {
        setError("Failed to fetch document for editing.");
        console.error("Error fetching document:", err);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchDocument();
    } else {
      setError("No document ID provided for editing.");
      setLoading(false);
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!title || !pipeline || !department || !category || !owner || !status) {
      setError("Please fill in all required fields.");
      return;
    }

    setIsSaving(true);
    try {
      const updatedData = {
        title,
        description,
        pipeline,
        department,
        category,
        owner,
        status,
        updatedAt: new Date().toISOString(), // Simulate update timestamp
      };

      // Use documentService to update the document
      await documentService.updateDocument(id, updatedData);
      setSuccess("Document updated successfully!");

      setTimeout(() => navigate(`/documents/${id}`), 1500); // Navigate back to details page
    } catch (err) {
      setError(err?.message || "Failed to update document.");
      console.error("Update error:", err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleGoBack = () => {
    navigate(-1); // Go back to the previous page
  };

  if (loading) {
    return (
      <div className="edit-doc-wrapper">
        <div className="loading-message">Loading document for editing...</div>
      </div>
    );
  }

  if (error && !document) {
    // Only show error if no document could be loaded at all
    return (
      <div className="edit-doc-wrapper">
        <div className="error-message doc-error">{error}</div>
        <button onClick={handleGoBack} className="back-button">
          Back to List
        </button>
      </div>
    );
  }

  if (!document) {
    // Fallback if document is null after loading but no error explicitly set
    return (
      <div className="edit-doc-wrapper">
        <div className="no-document-found">No document found for editing.</div>
      </div>
    );
  }

  return (
    <div className="edit-doc-wrapper">
      <div className="edit-doc-container">
        <div className="edit-doc-header">
          <button onClick={handleGoBack} className="back-button">
            ← Back
          </button>
          <h1 className="edit-doc-title">Edit Document: {document.title}</h1>
        </div>
        <p className="edit-doc-subtitle">
          Update the information for this document
        </p>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form className="edit-doc-form" onSubmit={handleSubmit}>
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

          <div className="form-row">
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

            <div className="form-group">
              <label htmlFor="owner">
                Owner <span className="required">*</span>
              </label>
              <input
                type="text"
                id="owner"
                value={owner}
                onChange={(e) => setOwner(e.target.value)}
                placeholder="e.g., John Doe"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="status">
              Status <span className="required">*</span>
            </label>
            <select
              id="status"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              required
            >
              <option value="">Select Status</option>
              <option value="Approved">Approved</option>
              <option value="Pending">Pending</option>
              <option value="Rejected">Rejected</option>
              <option value="Draft">Draft</option>
              <option value="In Review">In Review</option>
            </select>
          </div>

          <button type="submit" className="submit-btn" disabled={isSaving}>
            {isSaving ? "Saving Changes..." : "Save Changes"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default EditDocument;
