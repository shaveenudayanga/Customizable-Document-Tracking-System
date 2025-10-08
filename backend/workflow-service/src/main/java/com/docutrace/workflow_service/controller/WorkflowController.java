package com.docutrace.workflow_service.controller;

import com.docutrace.workflow_service.dto.*;
import com.docutrace.workflow_service.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Tag(name = "Workflow Management", description = "APIs for managing document approval workflows")
public class WorkflowController {
    
    private final WorkflowService workflowService;
    

    @Operation(
        summary = "Create a workflow template",
        description = "Creates a new pipeline template with validated department steps.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Template created"),
            @ApiResponse(responseCode = "400", description = "Invalid template definition")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping("/create")
    public ResponseEntity<TemplateResponse> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) throws Exception {
        TemplateResponse saved = workflowService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(
        summary = "Start a workflow",
        description = "Starts a new workflow instance using a template or custom steps.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Workflow started"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Template not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    @PostMapping("/start")
    public ResponseEntity<StartWorkflowResponse> startWorkflow(
            @Valid @RequestBody StartWorkflowRequest request) throws Exception {
        return ResponseEntity.ok(workflowService.startWorkflow(request));
    }
    
    @Operation(
        summary = "List tasks by department",
        description = "Returns active tasks assigned to the specified department.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @Parameter(description = "Department key", example = "hr")
            @RequestParam String deptKey) {
        return ResponseEntity.ok(workflowService.getTasksForDepartment(deptKey));
    }
    
    @Operation(
        summary = "Complete a task",
        description = "Completes an active task providing approval outcome and notes.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Task completed"),
            @ApiResponse(responseCode = "404", description = "Task not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @Parameter(description = "Camunda task identifier")
            @PathVariable String taskId,
            @Valid @RequestBody CompleteTaskRequest request) {
        workflowService.completeTask(taskId, request);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(
        summary = "Fetch BPMN XML",
        description = "Returns the BPMN XML document for the given process definition key.",
        responses = {
            @ApiResponse(responseCode = "200", description = "XML retrieved"),
            @ApiResponse(responseCode = "404", description = "Definition not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    @GetMapping("/definitions/{key}/xml")
    public ResponseEntity<String> getBpmnXml(@PathVariable String key) {
        return ResponseEntity.ok(workflowService.getBpmnXml(key));
    }

    @Operation(
        summary = "Get workflow status",
        description = "Retrieves the current status, active activities, and tasks for a document.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Status retrieved"),
            @ApiResponse(responseCode = "404", description = "Workflow instance not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','USER')")
    @GetMapping("/documents/{documentId}/status")
    public ResponseEntity<WorkflowStatusResponse> getWorkflowStatus(
            @Parameter(description = "Document identifier", example = "123")
            @PathVariable Long documentId) {
        return ResponseEntity.ok(workflowService.getWorkflowStatus(documentId));
    }

    @Operation(
        summary = "List workflow templates",
        description = "Returns templates optionally filtered by permanence.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Templates retrieved")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/templates")
    public ResponseEntity<List<TemplateResponse>> listTemplates(
            @Parameter(description = "Filter by permanent flag")
            @RequestParam(required = false) Boolean permanent) {
        return ResponseEntity.ok(workflowService.listTemplates(permanent));
    }

    @Operation(
        summary = "Get template details",
        description = "Fetches a single pipeline template with steps.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Template retrieved"),
            @ApiResponse(responseCode = "404", description = "Template not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/templates/{id}")
    public ResponseEntity<TemplateResponse> getTemplate(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getTemplate(id));
    }

    @Operation(
        summary = "Update a workflow template",
        description = "Updates template metadata and steps, incrementing its version.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Template updated"),
            @ApiResponse(responseCode = "400", description = "Invalid template"),
            @ApiResponse(responseCode = "404", description = "Template not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/templates/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTemplateRequest request) throws Exception {
        return ResponseEntity.ok(workflowService.updateTemplate(id, request));
    }

    @Operation(
        summary = "Delete a workflow template",
        description = "Removes a pipeline template. Permanent templates require admin access.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Template deleted"),
            @ApiResponse(responseCode = "404", description = "Template not found")
        }
    )
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        workflowService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
