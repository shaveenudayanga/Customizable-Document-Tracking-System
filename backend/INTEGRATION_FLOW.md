# End-to-End Integration Flow

The backend now wires the document, workflow, tracking, and user notification services together. The following sequence shows the data propagation when a document is submitted and processed.

1. **Submit a document**
   ```bash
   curl -X POST http://localhost:8082/api/documents \
     -H "Content-Type: application/json" \
     -d '{
       "title": "Purchase Order",
       "documentType": "PROCUREMENT",
       "description": "Purchase request for laptops",
       "ownerUserId": "6df6c2e4-74c3-4a1e-9e0f-8d0b95e06d74"
     }'
   ```
   - `document-service` persists the record, generates a QR code, and publishes a `DocumentLifecycleEvent` containing the document data and QR payload.
   - `DocumentIntegrationCoordinator` reacts asynchronously, triggering:
     - `workflow-service` `/api/workflow/start` (auto-start) with the new document id.
     - `tracking-service` `/api/tracking/register` to register the QR metadata.
     - `tracking-service` `/api/tracking/scan` to log an initial "DOCUMENT_SUBMITTED" event.

2. **Workflow auto-start**
   - `workflow-service` deploys a BPMN pipeline and stores the instance.
   - It updates the originating document status via `document-service` `/api/documents/{id}/status`.
   - `WorkflowEventPublisher` emits `WORKFLOW_STARTED`, which subsequently yields:
     - A tracking entry with type `WORKFLOW_STARTED`.
     - A user notification for the initiator (`/api/notifications`).

3. **Task completion**
   ```bash
   curl -X POST http://localhost:8083/api/workflow/tasks/{taskId}/complete \
     -H "Content-Type: application/json" \
     -d '{
       "userId": "finance.manager",
       "approved": true,
       "notes": "Approved for issuance",
       "documentStatuses": ["FINANCE_REVIEWED", "APPROVED"],
       "location": "Finance Department",
       "latitude": 6.9271,
       "longitude": 79.8612
     }'
   ```
   - The workflow task is completed and the document status is synchronized with `document-service`.
   - A `TASK_COMPLETED` event is published; the listener logs a real-time tracking event and notifies the initiator.

4. **Workflow completion / rejection**
   - When the process finishes, `WORKFLOW_COMPLETED` (or `WORKFLOW_REJECTED`) is emitted.
   - The tracking service records the final state, and the user service stores a notification for the document owner.

5. **Retrieve document traceability**
   ```bash
   curl http://localhost:8084/api/tracking/history/{documentId}
   ```
   - Returns the composed history, including QR registration, workflow milestones, geo-coordinates, and notes.

6. **View notifications** *(authenticated request through API gateway or direct user-service call)*
   ```bash
   curl http://localhost:8081/api/notifications \
     -H "Authorization: Bearer <JWT>"
   ```
   - Provides unread/read notifications related to workflow progress.

These examples assume default localhost ports, but every service base URL is configurable through environment variables (see `application.yml` files).