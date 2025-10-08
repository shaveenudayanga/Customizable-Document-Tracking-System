import React, { useState, useRef, useCallback } from "react";
import { v4 as uuidv4 } from "uuid";
import "../../styles/PipelineBuilder.css";

// --- Static Data for Configuration ---
const DEFAULT_STATUS_OPTIONS = [
  {
    label: "Awaiting Action",
    keyword: "AWAITING_ACTION",
    description: "Document has arrived in the department queue.",
  },
  {
    label: "In Review",
    keyword: "IN_REVIEW",
    description: "Document is actively being processed or checked.",
  },
  {
    label: "Approved/Rejected",
    keyword: "DECISION_MADE",
    description: "Final departmental decision recorded.",
  },
  {
    label: "Action Complete",
    keyword: "ACTION_COMPLETE",
    description: "Departmental task complete; document ready to move.",
  },
];

// --- Node Component for the Canvas ---
const DepartmentNode = ({ config, onClick, isSelected, index }) => {
  return (
    <div
      className={`pipeline-node department ${isSelected ? "selected" : ""}`}
      onClick={() => onClick(config)}
    >
      <div className="node-header">
        <span className="node-index">Step {index + 1}</span>
        <span className="node-title">
          {config.name || `Department ${index + 1}`}
        </span>
      </div>
      <div className="node-info">
        <span className="info-item">
          {config.notifyUser ? "🔔 Notifications ON" : "🔕 Notifications OFF"}
        </span>
      </div>
      {/* Pseudo-element for connection line is handled in CSS */}
    </div>
  );
};

// --- Main Builder Component ---
const PipelineBuilder = () => {
  const [departmentInput, setDepartmentInput] = useState(
    "Finance, HR, Legal, Archive"
  );
  const [pipelineNodes, setPipelineNodes] = useState([]);
  const [selectedNode, setSelectedNode] = useState(null);
  const [savedPipelines, setSavedPipelines] = useState([
    {
      id: 1,
      name: "Standard Document Approval",
      description: "Finance → HR → Legal → Archive",
      nodes: [
        {
          id: "1",
          name: "Finance",
          type: "department",
          index: 0,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Review financial aspects of the document.",
        },
        {
          id: "2",
          name: "HR",
          type: "department",
          index: 1,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Review HR compliance and policies.",
        },
        {
          id: "3",
          name: "Legal",
          type: "department",
          index: 2,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Review legal requirements and compliance.",
        },
        {
          id: "4",
          name: "Archive",
          type: "department",
          index: 3,
          notifyUser: false,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Archive the document.",
        },
      ],
      createdAt: new Date("2024-01-15"),
      lastModified: new Date("2024-01-20"),
    },
    {
      id: 2,
      name: "Quick HR Processing",
      description: "HR → Legal",
      nodes: [
        {
          id: "5",
          name: "HR",
          type: "department",
          index: 0,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Initial HR review and processing.",
        },
        {
          id: "6",
          name: "Legal",
          type: "department",
          index: 1,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Final legal approval.",
        },
      ],
      createdAt: new Date("2024-02-01"),
      lastModified: new Date("2024-02-01"),
    },
    {
      id: 3,
      name: "Financial Audit Flow",
      description: "Finance → Audit → Legal → Archive",
      nodes: [
        {
          id: "7",
          name: "Finance",
          type: "department",
          index: 0,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Financial review and validation.",
        },
        {
          id: "8",
          name: "Audit",
          type: "department",
          index: 1,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Comprehensive audit review.",
        },
        {
          id: "9",
          name: "Legal",
          type: "department",
          index: 2,
          notifyUser: true,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Legal compliance check.",
        },
        {
          id: "10",
          name: "Archive",
          type: "department",
          index: 3,
          notifyUser: false,
          statusOptions: DEFAULT_STATUS_OPTIONS,
          instructions: "Archive audited document.",
        },
      ],
      createdAt: new Date("2024-01-10"),
      lastModified: new Date("2024-01-25"),
    },
  ]);
  const [currentPipelineId, setCurrentPipelineId] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(null);
  const canvasRef = useRef(null);

  const generateFlow = useCallback(() => {
    const names = departmentInput
      .split(/[\s,]+/) // Split by comma or whitespace
      .map((name) => name.trim())
      .filter((name) => name.length > 0);

    if (names.length === 0) return;

    const newNodes = names.map((name, index) => ({
      id: uuidv4(),
      name: name,
      type: "department",
      index: index,
      notifyUser: true, // Default to ON
      statusOptions: DEFAULT_STATUS_OPTIONS, // Use default statuses
      instructions: `Review the document and update its status.`,
    }));

    setPipelineNodes(newNodes);
    setSelectedNode(null); // Clear selection after generating
  }, [departmentInput]);

  const handleNodeClick = (config) => {
    setSelectedNode(config);
  };

  const handleConfigChange = (field, value) => {
    setSelectedNode((prev) => ({ ...prev, [field]: value }));
    // Update the main pipeline array
    setPipelineNodes((prevNodes) =>
      prevNodes.map((node) =>
        node.id === selectedNode.id ? { ...selectedNode, [field]: value } : node
      )
    );
  };

  const loadPipeline = (pipeline) => {
    setPipelineNodes([...pipeline.nodes]);
    setCurrentPipelineId(pipeline.id);
    setSelectedNode(null);
    // Update the department input to reflect the loaded pipeline
    const departmentNames = pipeline.nodes.map((node) => node.name).join(", ");
    setDepartmentInput(departmentNames);
  };

  const savePipeline = () => {
    if (pipelineNodes.length === 0) {
      alert("Please create a pipeline first before saving.");
      return;
    }

    const pipelineName = prompt("Enter a name for this pipeline:");
    if (!pipelineName) return;

    const newPipeline = {
      id: Date.now(),
      name: pipelineName,
      description: pipelineNodes.map((node) => node.name).join(" → "),
      nodes: [...pipelineNodes],
      createdAt: new Date(),
      lastModified: new Date(),
    };

    if (currentPipelineId) {
      // Update existing pipeline
      setSavedPipelines((prev) =>
        prev.map((pipeline) =>
          pipeline.id === currentPipelineId
            ? {
                ...newPipeline,
                id: currentPipelineId,
                createdAt: pipeline.createdAt,
              }
            : pipeline
        )
      );
    } else {
      // Add new pipeline
      setSavedPipelines((prev) => [...prev, newPipeline]);
      setCurrentPipelineId(newPipeline.id);
    }

    alert("Pipeline saved successfully!");
  };

  const deletePipeline = (pipelineId) => {
    setSavedPipelines((prev) =>
      prev.filter((pipeline) => pipeline.id !== pipelineId)
    );
    if (currentPipelineId === pipelineId) {
      setCurrentPipelineId(null);
      setPipelineNodes([]);
      setSelectedNode(null);
      setDepartmentInput("Finance, HR, Legal, Archive");
    }
    setShowDeleteConfirm(null);
  };

  const confirmDelete = (pipelineId) => {
    setShowDeleteConfirm(pipelineId);
  };

  return (
    <div className="pipeline-builder-page">
      <header className="builder-header">
        <h1 className="header-title">Sequential Flow Builder (Admin View)</h1>
        <div className="header-actions">
          <button className="btn btn-secondary" onClick={savePipeline}>
            <span className="icon">💾</span> Save Configuration
          </button>
          <button
            className="btn btn-primary"
            onClick={() => alert("Pipeline published successfully!")}
          >
            <span className="icon">🚀</span> Publish Pipeline
          </button>
        </div>
      </header>

      <div className="pipeline-container">
        {/* 1. Department List Input / Flow Generator */}
        <aside className="builder-generator">
          <h2> Define Departments</h2>
          <p className="description">
            Enter department names separated by commas or new lines. This
            defines the sequence of the document flow.
          </p>
          <div className="form-group">
            <textarea
              value={departmentInput}
              onChange={(e) => setDepartmentInput(e.target.value)}
              rows="6"
              placeholder="e.g., HR, Finance, Legal, Operations"
            />
          </div>
          <button
            className="btn btn-primary generate-btn"
            onClick={generateFlow}
          >
            <span className="icon">➕</span> Generate Flow
          </button>
          {pipelineNodes.length > 0 && (
            <div className="flow-summary">
              <p>Flow Steps Generated: {pipelineNodes.length}</p>
              <p className="summary-title">
                First Step: {pipelineNodes[0].name}
              </p>
            </div>
          )}

          {/* Saved Pipelines Section */}
          <div className="saved-pipelines-section">
            <h3>💾 Saved Pipelines</h3>
            <div className="saved-pipelines-list">
              {savedPipelines.length === 0 ? (
                <p className="no-pipelines">No saved pipelines yet.</p>
              ) : (
                savedPipelines.map((pipeline) => (
                  <div
                    key={pipeline.id}
                    className={`saved-pipeline-item ${
                      currentPipelineId === pipeline.id ? "active" : ""
                    }`}
                  >
                    <div
                      className="pipeline-info"
                      onClick={() => loadPipeline(pipeline)}
                    >
                      <div className="pipeline-name">{pipeline.name}</div>
                      <div className="pipeline-description">
                        {pipeline.description}
                      </div>
                      <div className="pipeline-meta">
                        Last modified:{" "}
                        {pipeline.lastModified.toLocaleDateString()}
                      </div>
                    </div>
                    <div className="pipeline-actions">
                      <button
                        className="btn-delete"
                        onClick={(e) => {
                          e.stopPropagation();
                          confirmDelete(pipeline.id);
                        }}
                        title="Delete Pipeline"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Delete Confirmation Modal */}
          {showDeleteConfirm && (
            <div className="delete-modal-overlay">
              <div className="delete-modal">
                <h3>Confirm Delete</h3>
                <p>
                  Are you sure you want to delete this pipeline? This action
                  cannot be undone.
                </p>
                <div className="modal-actions">
                  <button
                    className="btn btn-secondary"
                    onClick={() => setShowDeleteConfirm(null)}
                  >
                    Cancel
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => deletePipeline(showDeleteConfirm)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          )}
        </aside>

        {/* 2. Pipeline Canvas (Linear Flow) */}
        <main ref={canvasRef} className="builder-canvas linear-flow">
          <div className="start-end-marker start-marker">
            <span className="icon">📄</span> Document Submitted
          </div>

          {pipelineNodes.length === 0 ? (
            <p className="canvas-placeholder">
              Use the left panel to define departments and generate the
              sequential flow.
            </p>
          ) : (
            pipelineNodes.map((node, index) => (
              <DepartmentNode
                key={node.id}
                config={node}
                onClick={handleNodeClick}
                isSelected={selectedNode && selectedNode.id === node.id}
                index={index}
              />
            ))
          )}

          {pipelineNodes.length > 0 && (
            <div className="start-end-marker end-marker">
              <span className="icon">✅</span> Pipeline Complete
            </div>
          )}
        </main>

        {/* 3. Configuration Panel */}
        <aside className="builder-config">
          <h2>2. Configure Selected Step</h2>
          {selectedNode ? (
            <div className="config-form">
              <h3>
                {selectedNode.name || "New Department"} (Step{" "}
                {selectedNode.index + 1})
              </h3>

              <div className="form-group">
                <label htmlFor="stepName">Department Name (Editable)</label>
                <input
                  id="stepName"
                  type="text"
                  value={selectedNode.name}
                  onChange={(e) => handleConfigChange("name", e.target.value)}
                />
              </div>

              <div className="form-group">
                <label htmlFor="instructions">
                  Instructions for Department
                </label>
                <textarea
                  id="instructions"
                  value={selectedNode.instructions}
                  onChange={(e) =>
                    handleConfigChange("instructions", e.target.value)
                  }
                  rows="3"
                />
              </div>

              <div className="form-group toggle-group">
                <label>User Notification on Status Change</label>
                <div className="toggle-switch">
                  <input
                    type="checkbox"
                    id="notifyToggle"
                    checked={selectedNode.notifyUser}
                    onChange={(e) =>
                      handleConfigChange("notifyUser", e.target.checked)
                    }
                  />
                  <label
                    htmlFor="notifyToggle"
                    className="toggle-label"
                  ></label>
                  <span className="toggle-status">
                    {selectedNode.notifyUser ? "ON" : "OFF"}
                  </span>
                </div>
                <p className="help-text">
                  Controls whether the user who submitted the document receives
                  an email/in-app alert when this department updates the status.
                </p>
              </div>

              <div className="form-group status-group">
                <label>Department Status Options (Review & Edit)</label>
                {selectedNode.statusOptions.map((status, i) => (
                  <div key={i} className="status-item">
                    <span className="status-label">{status.label}</span>
                    <p className="status-desc">{status.description}</p>
                  </div>
                ))}
                <button className="btn btn-tertiary">
                  Edit Status Options
                </button>
              </div>
            </div>
          ) : (
            <p className="config-placeholder">
              <span className="icon">👈</span> Select a department step on the
              left or generate a flow to begin configuration.
            </p>
          )}
        </aside>
      </div>
    </div>
  );
};

export default PipelineBuilder;
