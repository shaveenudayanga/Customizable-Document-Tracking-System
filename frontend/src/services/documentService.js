import { documentAPI } from "../lib/api.js";

/**
 * Document Service
 * Handles document CRUD operations and file management
 * Matches backend document-service endpoints (port 8082)
 */
export const documentService = {
  /**
   * Get all documents
   * GET /api/documents
   * @returns {Promise<Array>} List of documents ordered by creation date (desc)
   */
  async getAllDocuments() {
    try {
      return await documentAPI.get("/documents");
    } catch (error) {
      console.error("Error fetching documents:", error);
      throw error;
    }
  },

  /**
   * Get document by ID
   * GET /api/documents/{documentId}
   * @param {number} id - Document ID (Long in backend)
   * @returns {Promise<Object>} Document details
   */
  async getDocumentById(id) {
    try {
      return await documentAPI.get(`/documents/${id}`);
    } catch (error) {
      console.error("Error fetching document:", error);
      throw error;
    }
  },

  /**
   * Create new document
   * POST /api/documents
   * @param {Object} documentData
   * @param {string} documentData.title - Document title (required, max 255 chars)
   * @param {string} documentData.documentType - Document type (required, max 100 chars)
   * @param {string} [documentData.description] - Description (optional, max 10,000 chars)
   * @param {string} documentData.ownerUserId - Owner user UUID (required)
   * @param {string} [documentData.qrPath] - QR code path (optional, max 512 chars)
   * @param {string} [documentData.fileDir] - File directory (optional, max 512 chars)
   * @returns {Promise<Object>} Created document
   */
  async createDocument(documentData) {
    try {
      return await documentAPI.post("/documents", documentData);
    } catch (error) {
      console.error("Error creating document:", error);
      throw error;
    }
  },

  /**
   * Create document with file upload
   * POST /api/documents (multipart/form-data)
   * @param {Object} metadata - Document metadata
   * @param {File} file - File to upload
   * @returns {Promise<Object>} Created document
   */
  async createDocumentWithFile(metadata, file) {
    try {
      const formData = new FormData();
      formData.append(
        "metadata",
        new Blob([JSON.stringify(metadata)], { type: "application/json" })
      );
      if (file) {
        formData.append("file", file);
      }
      return await documentAPI.upload("/documents", formData);
    } catch (error) {
      console.error("Error creating document with file:", error);
      throw error;
    }
  },

  /**
   * Update document status
   * POST /api/documents/{documentId}/status
   * @param {number} documentId - Document ID
   * @param {Array<string>} statuses - Array of status strings
   * @returns {Promise<Object>} Updated document
   */
  async updateDocumentStatus(documentId, statuses) {
    try {
      return await documentAPI.post(`/documents/${documentId}/status`, { statuses });
    } catch (error) {
      console.error("Error updating document status:", error);
      throw error;
    }
  },

  /**
   * Get document QR code
   * GET /api/documents/{documentId}/qrcode
   * @param {number} documentId - Document ID
   * @param {boolean} asBase64 - If true, returns base64 string, otherwise returns image URL
   * @returns {Promise<string|Object>} QR code data
   */
  async getDocumentQRCode(documentId, asBase64 = false) {
    try {
      if (asBase64) {
        return await documentAPI.get(`/documents/${documentId}/qrcode`, {
          headers: { Accept: "application/json" },
        });
      } else {
        // Return the URL for the image using document service
        return `http://localhost:8082/api/documents/${documentId}/qrcode`;
      }
    } catch (error) {
      console.error("Error fetching QR code:", error);
      throw error;
    }
  },

  /**
   * Upload file to document
   * POST /api/documents/{documentId}/file
   * @param {number} documentId - Document ID
   * @param {File} file - File to upload
   * @returns {Promise<Object>} Upload response
   */
  async uploadFile(documentId, file) {
    try {
      const formData = new FormData();
      formData.append("file", file);
      return await documentAPI.upload(`/documents/${documentId}/file`, formData);
    } catch (error) {
      console.error("Error uploading file:", error);
      throw error;
    }
  },

  /**
   * Download document file
   * GET /api/documents/{documentId}/file
   * @param {number} documentId - Document ID
   * @returns {string} Download URL
   */
  getDownloadUrl(documentId) {
    return `http://localhost:8082/api/documents/${documentId}/file`;
  },

  /**
   * List files for a document
   * GET /api/documents/{documentId}/files
   * @param {number} documentId - Document ID
   * @returns {Promise<Array>} List of files
   */
  async listFiles(documentId) {
    try {
      return await documentAPI.get(`/documents/${documentId}/files`);
    } catch (error) {
      console.error("Error listing files:", error);
      throw error;
    }
  },
};
