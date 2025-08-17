import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "../../styles/DocumentList.css";
// Assume 'api' is configured for your backend calls
// import { api } from "../../lib/api";

const DocumentList = () => {
  const [documents, setDocuments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("createdAt");
  const [filterStatus, setFilterStatus] = useState("all");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Mock API data - replace with actual API calls
  const mockDocuments = [
    {
      id: "doc1",
      title: "Project Proposal Q3",
      status: "Approved",
      owner: "Alice",
      pipeline: "Sales",
      currentStep: "Final Review",
      createdAt: "2024-07-01",
    },
    {
      id: "doc2",
      title: "Marketing Campaign Brief",
      status: "Pending",
      owner: "Bob",
      pipeline: "Marketing",
      currentStep: "Content Creation",
      createdAt: "2024-07-15",
    },
    {
      id: "doc3",
      title: "Legal Contract Draft V2",
      status: "Rejected",
      owner: "Charlie",
      pipeline: "Legal",
      currentStep: "Client Feedback",
      createdAt: "2024-07-10",
    },
    {
      id: "doc4",
      title: "HR Onboarding Checklist",
      status: "Approved",
      owner: "Diana",
      pipeline: "HR",
      currentStep: "Completed",
      createdAt: "2024-06-20",
    },
    {
      id: "doc5",
      title: "Budget Allocation FY25",
      status: "Pending",
      owner: "Eve",
      pipeline: "Finance",
      currentStep: "Manager Approval",
      createdAt: "2024-08-01",
    },
  ];

  useEffect(() => {
    const fetchDocuments = async () => {
      setLoading(true);
      setError(null);
      try {
        // In a real app, you'd use api.get here
        // const response = await api.get('/documents', { params: { searchTerm, sortBy, filterStatus } });
        // setDocuments(response.data);

        // Simulate API call delay and filtering/sorting
        setTimeout(() => {
          let filtered = mockDocuments.filter(
            (doc) =>
              doc.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
              doc.owner.toLowerCase().includes(searchTerm.toLowerCase()) ||
              doc.pipeline.toLowerCase().includes(searchTerm.toLowerCase())
          );

          if (filterStatus !== "all") {
            filtered = filtered.filter((doc) => doc.status === filterStatus);
          }

          filtered.sort((a, b) => {
            if (sortBy === "createdAt") {
              return new Date(b.createdAt) - new Date(a.createdAt);
            }
            // Add more sorting logic if needed
            return 0;
          });

          setDocuments(filtered);
          setLoading(false);
        }, 500); // Simulate network delay
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
                    <span className={`doc-status ${doc.status.toLowerCase()}`}>
                      {doc.status}
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
