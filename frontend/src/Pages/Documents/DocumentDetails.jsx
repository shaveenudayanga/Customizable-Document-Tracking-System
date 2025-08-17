import React, { useState, useEffect } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import "../../styles/DocumentDetails.css";
// import { api } from "../../lib/api";

const DocumentDetails = () => {
  const { id } = useParams(); // Get document ID from URL
  const navigate = useNavigate();
  const [document, setDocument] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Mock API data - replace with actual API calls
  const mockDocumentDetails = {
    doc1: {
      id: "doc1",
      title: "Project Proposal Q3",
      status: "Approved",
      owner: "Alice Johnson",
      pipeline: "Sales Pipeline",
      currentStep: "Final Review",
      department: "Sales",
      category: "Proposals",
      createdAt: "2024-07-01T10:30:00Z",
      updatedAt: "2024-07-25T15:00:00Z",
      description:
        "Comprehensive proposal for the Q3 project, outlining objectives, methodologies, and expected outcomes. Includes detailed budget breakdown and timeline.",
      attachedFiles: [
        {
          name: "Proposal_Q3_v1.pdf",
          url: "/mock-files/proposal_q3_v1.pdf",
          type: "pdf",
        },
        {
          name: "Budget_Spreadsheet.xlsx",
          url: "/mock-files/budget_spreadsheet.xlsx",
          type: "excel",
        },
      ],
      history: [
        {
          timestamp: "2024-07-01T10:30:00Z",
          action: "Created",
          by: "Alice Johnson",
        },
        {
          timestamp: "2024-07-05T09:00:00Z",
          action: "Submitted for Review",
          by: "Alice Johnson",
        },
        {
          timestamp: "2024-07-10T11:45:00Z",
          action: "Reviewed by Manager",
          by: "Bob Smith",
        },
        {
          timestamp: "2024-07-15T14:20:00Z",
          action: "Approved by Sales Director",
          by: "Charlie Green",
        },
      ],
      auditTrail: [
        {
          timestamp: "2024-07-01T10:30:00Z",
          event: "Document created by Alice Johnson.",
        },
        {
          timestamp: "2024-07-02T11:00:00Z",
          event: "Metadata updated: 'Description' added by Alice Johnson.",
        },
        {
          timestamp: "2024-07-05T09:00:00Z",
          event: "Moved to 'Submitted for Review' step by Alice Johnson.",
        },
        {
          timestamp: "2024-07-10T11:45:00Z",
          event: "Status changed to 'Under Review' by Bob Smith.",
        },
        {
          timestamp: "2024-07-15T14:20:00Z",
          event: "Status changed to 'Approved' by Charlie Green.",
        },
        {
          timestamp: "2024-07-20T10:00:00Z",
          event: "File 'Budget_Spreadsheet.xlsx' downloaded by Diana Prince.",
        },
      ],
    },
    // Add more mock document details here if needed for other IDs
  };

  useEffect(() => {
    const fetchDocument = async () => {
      setLoading(true);
      setError(null);
      try {
        // In a real app: const response = await api.get(`/documents/${id}`);
        // setDocument(response.data);

        // Simulate API call delay
        setTimeout(() => {
          const doc = mockDocumentDetails[id];
          if (doc) {
            setDocument(doc);
          } else {
            setError("Document not found.");
          }
          setLoading(false);
        }, 500);
      } catch (err) {
        setError("Failed to fetch document details.");
        setLoading(false);
        console.error("Error fetching document details:", err);
      }
    };

    if (id) {
      fetchDocument();
    } else {
      setError("No document ID provided.");
      setLoading(false);
    }
  }, [id]);

  const handleDownload = (fileUrl, fileName) => {
    // In a real app, you might trigger a backend download or open in new tab
    window.open(fileUrl, "_blank");
  };

  const handleGoBack = () => {
    navigate(-1); // Go back to the previous page
  };

  if (loading) {
    return (
      <div className="doc-details-wrapper">
        <div className="loading-message">Loading document details...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="doc-details-wrapper">
        <div className="error-message doc-error">{error}</div>
        <button onClick={handleGoBack} className="back-button">
          Back to List
        </button>
      </div>
    );
  }

  if (!document) {
    return (
      <div className="doc-details-wrapper">
        <div className="no-document-selected">No document selected.</div>
      </div>
    );
  }

  return (
    <div className="doc-details-wrapper">
      <div className="doc-details-container">
        <div className="doc-details-header">
          <button onClick={handleGoBack} className="back-button">
            ← Back
          </button>
          <h1 className="doc-details-title">{document.title}</h1>
          <Link to={`/edit-document/${document.id}`} className="edit-doc-btn">
            Edit Document
          </Link>
        </div>

        <p className="doc-details-subtitle">
          Detailed information about your document
        </p>

        <div className="doc-info-sections">
          {/* Metadata Section */}
          <div className="info-section metadata-section">
            <h2 className="section-title">Metadata</h2>
            <div className="metadata-grid">
              <div className="metadata-item">
                <strong>ID:</strong> <span>{document.id}</span>
              </div>
              <div className="metadata-item">
                <strong>Status:</strong>{" "}
                <span className={`doc-status ${document.status.toLowerCase()}`}>
                  {document.status}
                </span>
              </div>
              <div className="metadata-item">
                <strong>Owner:</strong> <span>{document.owner}</span>
              </div>
              <div className="metadata-item">
                <strong>Pipeline:</strong> <span>{document.pipeline}</span>
              </div>
              <div className="metadata-item">
                <strong>Current Step:</strong>{" "}
                <span>{document.currentStep}</span>
              </div>
              <div className="metadata-item">
                <strong>Department:</strong> <span>{document.department}</span>
              </div>
              <div className="metadata-item">
                <strong>Category:</strong> <span>{document.category}</span>
              </div>
              <div className="metadata-item">
                <strong>Created At:</strong>{" "}
                <span>{new Date(document.createdAt).toLocaleString()}</span>
              </div>
              <div className="metadata-item">
                <strong>Last Updated:</strong>{" "}
                <span>{new Date(document.updatedAt).toLocaleString()}</span>
              </div>
            </div>
            <div className="description-box">
              <strong>Description:</strong>
              <p>{document.description || "No description provided."}</p>
            </div>
          </div>

          {/* Attached Files Section */}
          <div className="info-section files-section">
            <h2 className="section-title">Attached Files</h2>
            {document.attachedFiles && document.attachedFiles.length > 0 ? (
              <ul className="file-list">
                {document.attachedFiles.map((file, index) => (
                  <li key={index} className="file-item">
                    <span>{file.name}</span>
                    <div className="file-actions">
                      <button
                        onClick={() => handleDownload(file.url, file.name)}
                        className="action-button view-button"
                      >
                        View
                      </button>
                      <button
                        onClick={() => handleDownload(file.url, file.name)}
                        className="action-button download-button"
                      >
                        Download
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="no-files">No files attached.</p>
            )}
          </div>

          {/* History Section */}
          <div className="info-section history-section">
            <h2 className="section-title">Document History</h2>
            {document.history && document.history.length > 0 ? (
              <ul className="history-list">
                {document.history.map((item, index) => (
                  <li key={index} className="history-item">
                    <span className="history-timestamp">
                      {new Date(item.timestamp).toLocaleString()}:
                    </span>
                    <span className="history-action">
                      {item.action} by {item.by}
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="no-history">No history available.</p>
            )}
          </div>

          {/* Audit Trail Section */}
          <div className="info-section audit-section">
            <h2 className="section-title">Audit Trail</h2>
            {document.auditTrail && document.auditTrail.length > 0 ? (
              <ul className="audit-list">
                {document.auditTrail.map((item, index) => (
                  <li key={index} className="audit-item">
                    <span className="audit-timestamp">
                      {new Date(item.timestamp).toLocaleString()}:
                    </span>
                    <span className="audit-event">{item.event}</span>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="no-audit">No audit trail available.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DocumentDetails;
