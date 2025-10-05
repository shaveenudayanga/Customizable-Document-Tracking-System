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

  return (
    <div className="pipeline-builder-page">
      <header className="builder-header">
        <h1 className="header-title">Sequential Flow Builder (Admin View)</h1>
        <div className="header-actions">
          <button className="btn btn-secondary">
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
