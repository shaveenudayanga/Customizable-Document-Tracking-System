import { api } from "../lib/api.js";

// Document Service - Replace hard-coded data with API calls
export const documentService = {
  // Get all documents
  async getAllDocuments(params = {}) {
    try {
      return await api.get("/documents", params);
    } catch (error) {
      console.error("Error fetching documents:", error);
      // Fallback to mock data during development
      return this.getMockDocuments();
    }
  },

  // Get document by ID
  async getDocumentById(id) {
    try {
      return await api.get(`/documents/${id}`);
    } catch (error) {
      console.error("Error fetching document:", error);
      // Fallback to mock data
      return this.getMockDocumentById(id);
    }
  },

  // Create new document
  async createDocument(documentData) {
    try {
      return await api.post("/documents", documentData);
    } catch (error) {
      console.error("Error creating document:", error);
      throw error;
    }
  },

  // Update document
  async updateDocument(id, documentData) {
    try {
      return await api.put(`/documents/${id}`, documentData);
    } catch (error) {
      console.error("Error updating document:", error);
      throw error;
    }
  },

  // Delete document
  async deleteDocument(id) {
    try {
      return await api.delete(`/documents/${id}`);
    } catch (error) {
      console.error("Error deleting document:", error);
      throw error;
    }
  },

  // Mock data fallbacks (keep temporarily during transition)
  getMockDocuments() {
    return [
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
      // ... other mock documents
    ];
  },

  getMockDocumentById(id) {
    const mockData = {
      doc1: {
        id: "doc1",
        title: "Project Proposal Q3",
        status: "Approved",
        owner: "Alice",
        pipeline: "Sales",
        currentStep: "Final Review",
        department: "Sales",
        category: "Proposals",
        description: "Comprehensive proposal for the Q3 project...",
        createdAt: "2024-07-01T10:30:00Z",
        updatedAt: "2024-07-25T15:00:00Z",
      },
      // ... other mock documents
    };
    return mockData[id] || null;
  },
};
