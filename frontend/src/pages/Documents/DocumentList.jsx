import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "../../styles/DocumentList.css";
import { documentService } from "../../services/documentService.js";

const DocumentList = () => {
  const [documents, setDocuments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [filterStatus, setFilterStatus] = useState("all");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDocuments = async () => {
      setLoading(true);
      setError(null);
      try {
        // Use documentService instead of hard-coded data
        let allDocuments = await documentService.getAllDocuments();

        // Apply client-side filtering and sorting
        let filtered = allDocuments.filter(
          (doc) =>
            (doc.title || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
            (doc.owner || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
            (doc.pipeline || '').toLowerCase().includes(searchTerm.toLowerCase())
        );

        if (filterStatus !== "all") {
          filtered = filtered.filter((doc) => doc.status === filterStatus);
        }

        filtered.sort((a, b) => {
          if (sortBy === "createdAt") {
            return new Date(b.createdAt) - new Date(a.createdAt);
          }
          return 0;
        });

        setDocuments(filtered);
        setLoading(false);
      } catch (err) {
        setError("Failed to fetch documents.");
        setLoading(false);
        console.error("Error fetching documents:", err);
      }
    };

    fetchDocuments();
  }, [searchTerm, sortBy, filterStatus]);

  return (
    <div className="doc-list-wrapper">
      <div className="doc-list-container">
        <h1 className="doc-list-title">Document Hub</h1>
        <p className="doc-list-subtitle">
          Browse and manage all your documents
        </p>

        <div className="controls">
          <input
            type="text"
            placeholder="Search documents..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="sort-select"
          >
            <option value="createdAt">Sort by Date</option>
            <option value="title">Sort by Title</option>
            {/* Add more sort options */}
          </select>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="filter-select"
          >
            <option value="all">All Statuses</option>
            <option value="Approved">Approved</option>
            <option value="Pending">Pending</option>
            <option value="Rejected">Rejected</option>
            {/* Add more filter options */}
          </select>
          <Link to="/new-document" className="new-doc-btn">
            + New Document
          </Link>
        </div>

        {loading && <div className="loading-message">Loading documents...</div>}
        {error && <div className="error-message">{error}</div>}

        {!loading && !error && (
          <div className="document-cards-grid">
            {documents.length > 0 ? (
              documents.map((doc) => (
                <Link
                  to={`/documents/${doc.id}`}
                  key={doc.id}
                  className="document-card"
                >
                  <div className="doc-card-header">
                    <h3 className="doc-card-title">{doc.title}</h3>
                    <span className={`doc-status ${(doc.status || 'unknown').toLowerCase()}`}>
                      {doc.status || 'Unknown'}
                    </span>
                  </div>
                  <p className="doc-card-info">Owner: {doc.owner}</p>
                  <p className="doc-card-info">Pipeline: {doc.pipeline}</p>
                  <p className="doc-card-info">Step: {doc.currentStep}</p>
                  <p className="doc-card-info">Created: {doc.createdAt}</p>
                </Link>
              ))
            ) : (
              <div className="no-documents-found">
                No documents found matching your criteria.
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default DocumentList;
