import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/NewDocument.css";
// import { api } from "../../lib/api";

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

    if (!title || !pipeline || !department || !category || !file) {
      setError("Please fill in all required fields and select a file.");
      return;
    }

    setLoading(true);
    try {
      // In a real app, you would create a FormData object for file uploads
      // const formData = new FormData();
      // formData.append("title", title);
      // formData.append("description", description);
      // formData.append("pipeline", pipeline);
      // formData.append("department", department);
      // formData.append("category", category);
      // formData.append("file", file);

      // const response = await api.post("/documents/upload", formData, {
      //   headers: {
      //     "Content-Type": "multipart/form-data",
      //   },
      // });
      // console.log("Document uploaded:", response.data);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000));
      console.log({
        title,
        description,
        pipeline,
        department,
        category,
        file: file.name,
      });
      setSuccess("Document uploaded successfully!");

      // Optionally navigate to the document details or list page
      // navigate(`/documents/${response.data.id}`);
      // For mock, navigate back to list after success
      setTimeout(() => navigate("/documents"), 1500);
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

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

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

          <div className="form-group file-upload-group">
            <label htmlFor="file-upload" className="file-upload-label">
              Upload File <span className="required">*</span>
            </label>
            <input
              type="file"
              id="file-upload"
              onChange={handleFileChange}
              required
              className="file-input-hidden"
            />
            <div className="file-input-display">
              <span className="file-name">
                {file ? file.name : "No file selected"}
              </span>
              <span className="browse-button">Browse</span>
            </div>
            {file && (
              <span className="file-size">
                ({(file.size / 1024 / 1024).toFixed(2)} MB)
              </span>
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
