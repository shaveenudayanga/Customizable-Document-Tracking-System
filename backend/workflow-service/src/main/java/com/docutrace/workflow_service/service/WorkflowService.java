package com.docutrace.workflow_service.service;

import com.docutrace.workflow_service.dto.*;
import com.docutrace.workflow_service.entity.*;
import com.docutrace.workflow_service.event.WorkflowEvent;
import com.docutrace.workflow_service.event.WorkflowEventPublisher;
import com.docutrace.workflow_service.event.WorkflowEventType;
import com.docutrace.workflow_service.exception.BadRequestException;
import com.docutrace.workflow_service.exception.ResourceNotFoundException;
import com.docutrace.workflow_service.integration.DocumentServiceClient;
import com.docutrace.workflow_service.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);
    private static final List<String> DEFAULT_DOCUMENT_STATUSES = List.of("DEPARTMENT_PENDING", "APPROVAL_PENDING");
    
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final BpmnGeneratorService bpmnGenerator;
    private final PipelineTemplateRepository templateRepo;
    private final PipelineInstanceRepository instanceRepo;
    private final DepartmentRepository departmentRepository;
    private final WorkflowEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final DocumentServiceClient documentServiceClient;

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) throws Exception {
        List<PipelineStep> steps = request.steps();
        validateSteps(steps);

        boolean permanent = Boolean.TRUE.equals(request.isPermanent());
        if (permanent && !hasRole("ADMIN")) {
            throw new AccessDeniedException("Only administrators can create permanent templates");
        }

        PipelineTemplate template = new PipelineTemplate();
        template.setName(request.name());
        template.setDocumentType(request.documentType());
        template.setStepsJson(objectMapper.writeValueAsString(steps));
        template.setIsPermanent(permanent);
        template.setCreatedBy(currentUsername());
        templateRepo.save(template);
        return toTemplateResponse(template);
    }

    @Transactional(readOnly = true)
    public List<TemplateResponse> listTemplates(Boolean permanent) {
        List<PipelineTemplate> templates = permanent == null
            ? templateRepo.findAll()
            : templateRepo.findByIsPermanent(permanent);
        return templates.stream()
            .map(this::toTemplateResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(Long id) {
        PipelineTemplate template = templateRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pipeline template not found: " + id));
        return toTemplateResponse(template);
    }

    @Transactional
    public TemplateResponse updateTemplate(Long id, UpdateTemplateRequest request) throws Exception {
        PipelineTemplate template = templateRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pipeline template not found: " + id));

        boolean currentlyPermanent = Boolean.TRUE.equals(template.getIsPermanent());
        boolean requestedPermanent = request.isPermanent() != null ? request.isPermanent() : currentlyPermanent;
        if ((currentlyPermanent || requestedPermanent) && !hasRole("ADMIN")) {
            throw new AccessDeniedException("Only administrators can manage permanent templates");
        }

        validateSteps(request.steps());

        template.setName(request.name());
        template.setDocumentType(request.documentType());
        template.setStepsJson(objectMapper.writeValueAsString(request.steps()));
        template.setIsPermanent(requestedPermanent);
        template.setVersion(Optional.ofNullable(template.getVersion()).orElse(1) + 1);
        templateRepo.save(template);
        return toTemplateResponse(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        PipelineTemplate template = templateRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pipeline template not found: " + id));

        if (Boolean.TRUE.equals(template.getIsPermanent()) && !hasRole("ADMIN")) {
            throw new AccessDeniedException("Only administrators can delete permanent templates");
        }
        if (!hasAnyRole("ADMIN", "STAFF")) {
            throw new AccessDeniedException("Unauthorized to delete templates");
        }

        templateRepo.delete(template);
    }
    
    @Transactional
    public StartWorkflowResponse startWorkflow(StartWorkflowRequest request) throws Exception {
        List<PipelineStep> steps;
        Long templateId = null;
        
        if (request.templateId() != null) {
            PipelineTemplate template = templateRepo.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline template not found: " + request.templateId()));
            steps = objectMapper.readValue(template.getStepsJson(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, PipelineStep.class));
            templateId = template.getId();
        } else {
            if (request.customSteps() == null || request.customSteps().isEmpty()) {
                throw new BadRequestException("Either templateId must be provided or customSteps must contain at least one step");
            }
            steps = request.customSteps();
        }

        validateSteps(steps);

        String initiator = Optional.ofNullable(request.initiator()).orElse(currentUsername());
        String processKey = "doc_pipeline_" + request.documentId() + "_" + System.currentTimeMillis();
        
        // Generate and deploy BPMN
        BpmnModelInstance bpmnModel = bpmnGenerator.generatePipeline(processKey, steps);
        org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
            .addModelInstance(processKey + ".bpmn", bpmnModel)
            .name("Pipeline for Document " + request.documentId())
            .deploy();
        
        // Get the deployed process definition
        org.camunda.bpm.engine.repository.ProcessDefinition processDefinition = 
            repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        
        // Start process using the definition ID
        Map<String, Object> variables = new HashMap<>();
        variables.put("documentId", request.documentId());
        variables.put("initiator", initiator);
        
        org.camunda.bpm.engine.runtime.ProcessInstance processInstance = 
            runtimeService.startProcessInstanceById(processDefinition.getId(), variables);
        
        // Save instance
        PipelineInstance instance = new PipelineInstance();
        instance.setDocumentId(request.documentId());
        instance.setTemplateId(templateId);
        instance.setProcessDefinitionKey(processKey);
        instance.setProcessInstanceId(processInstance.getProcessInstanceId());
        instance.setPipelineJson(objectMapper.writeValueAsString(steps));
        instance.setCreatedBy(initiator);
        instance.setStatus("IN_PROGRESS");
        instanceRepo.save(instance);

        runtimeService.setVariable(processInstance.getProcessInstanceId(), "pipelineInstanceId", instance.getId());

        propagateProcessInstanceLink(request.documentId(), processInstance.getProcessInstanceId());

        Map<String, Object> eventPayload = new HashMap<>();
        if (templateId != null) {
            eventPayload.put("templateId", templateId);
        }
        eventPayload.put("initiator", initiator);
        eventPublisher.publish(WorkflowEvent.of(
            WorkflowEventType.WORKFLOW_STARTED,
            request.documentId(),
            instance.getId(),
            instance.getProcessInstanceId(),
            eventPayload
        ));
        
        return new StartWorkflowResponse(
            processInstance.getProcessInstanceId(),
            processKey,
            instance.getId()
        );
    }
    
    public List<TaskResponse> getTasksForDepartment(String deptKey) {
        if (!departmentRepository.existsByKey(deptKey)) {
            throw new ResourceNotFoundException("Department not found: " + deptKey);
        }
        Map<String, PipelineInstance> instanceCache = new HashMap<>();
        return taskService.createTaskQuery()
            .taskCandidateGroup(deptKey)
            .active()
            .list()
            .stream()
            .map(task -> {
                PipelineInstance instance = instanceCache.computeIfAbsent(
                    task.getProcessInstanceId(),
                    id -> instanceRepo.findByProcessInstanceId(id).orElse(null)
                );
                Long docId = instance != null
                    ? instance.getDocumentId()
                    : (Long) runtimeService.getVariable(task.getProcessInstanceId(), "documentId");
                String instructions = instance != null
                    ? resolveInstructions(instance.getPipelineJson(), task.getTaskDefinitionKey())
                    : null;
                return new TaskResponse(
                    task.getId(),
                    task.getName(),
                    task.getProcessInstanceId(),
                    docId,
                    deptKey,
                    instructions
                );
            })
            .toList();
    }
    
    @Transactional
    public void completeTask(String taskId, CompleteTaskRequest request) {
        org.camunda.bpm.engine.task.Task task = taskService.createTaskQuery()
            .taskId(taskId)
            .singleResult();

        if (task == null) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", request.approved());
        variables.put("notes", request.notes());
        variables.put("completedBy", request.userId());
        variables.put("completedAt", LocalDateTime.now());
        Optional.ofNullable(request.location()).ifPresent(location -> variables.put("location", location));
        Optional.ofNullable(request.latitude()).ifPresent(lat -> variables.put("latitude", lat));
        Optional.ofNullable(request.longitude()).ifPresent(lon -> variables.put("longitude", lon));

        taskService.complete(taskId, variables);

        PipelineInstance instance = instanceRepo.findByProcessInstanceId(task.getProcessInstanceId())
            .orElseThrow(() -> new ResourceNotFoundException("Pipeline instance not found for process " + task.getProcessInstanceId()));

        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("taskId", taskId);
        eventPayload.put("taskName", task.getName());
        eventPayload.put("approved", request.approved());
        Optional.ofNullable(request.notes()).ifPresent(notes -> eventPayload.put("notes", notes));
        eventPayload.put("completedBy", request.userId());
        Optional.ofNullable(request.location()).ifPresent(location -> eventPayload.put("location", location));
        Optional.ofNullable(request.latitude()).ifPresent(lat -> eventPayload.put("latitude", lat));
        Optional.ofNullable(request.longitude()).ifPresent(lon -> eventPayload.put("longitude", lon));

        List<String> documentStatuses = request.documentStatuses();
        if (documentStatuses != null && !documentStatuses.isEmpty()) {
            try {
                documentServiceClient.updateDocumentStatus(
                    instance.getDocumentId(),
                    documentStatuses,
                    instance.getProcessInstanceId()
                );
            } catch (Exception ex) {
                log.warn("Failed to propagate document status update for document {}", instance.getDocumentId(), ex);
            }
            eventPayload.put("documentStatuses", documentStatuses);
        }

        String initiator = null;
        try {
            initiator = (String) runtimeService.getVariable(task.getProcessInstanceId(), "initiator");
        } catch (Exception ex) {
            log.debug("Unable to resolve initiator for process {}", task.getProcessInstanceId(), ex);
        }
        Optional.ofNullable(initiator).ifPresent(user -> eventPayload.put("initiator", user));

        eventPublisher.publish(WorkflowEvent.of(
            WorkflowEventType.TASK_COMPLETED,
            instance.getDocumentId(),
            instance.getId(),
            instance.getProcessInstanceId(),
            eventPayload
        ));

        if (Boolean.FALSE.equals(request.approved())) {
            instance.setStatus("REJECTED");
            instance.setCompletedAt(LocalDateTime.now());
            instanceRepo.save(instance);
            if (runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).count() > 0) {
                runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "Rejected by " + request.userId());
            }

            eventPublisher.publish(WorkflowEvent.of(
                WorkflowEventType.WORKFLOW_REJECTED,
                instance.getDocumentId(),
                instance.getId(),
                instance.getProcessInstanceId(),
                Map.of(
                    "rejectedBy", request.userId(),
                    "initiator", initiator
                )
            ));
        } else {
            if (updatePipelineInstanceStatusIfFinished(instance)) {
                eventPublisher.publish(WorkflowEvent.of(
                    WorkflowEventType.WORKFLOW_COMPLETED,
                    instance.getDocumentId(),
                    instance.getId(),
                    instance.getProcessInstanceId(),
                    Map.of(
                        "completedBy", request.userId(),
                        "initiator", initiator
                    )
                ));
            }
        }
    }
    
    public String getBpmnXml(String processDefinitionKey) {
        org.camunda.bpm.engine.repository.ProcessDefinition def = 
            repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();

        if (def == null) {
            throw new ResourceNotFoundException("Process definition not found: " + processDefinitionKey);
        }
        
        BpmnModelInstance model = repositoryService.getBpmnModelInstance(def.getId());
        return org.camunda.bpm.model.bpmn.Bpmn.convertToString(model);
    }

    public WorkflowStatusResponse getWorkflowStatus(Long documentId) {
        PipelineInstance instance = instanceRepo.findByDocumentId(documentId)
            .orElseThrow(() -> new ResourceNotFoundException("Pipeline instance not found for document " + documentId));

        boolean processActive = runtimeService.createProcessInstanceQuery()
            .processInstanceId(instance.getProcessInstanceId())
            .count() > 0;

        List<String> activeActivityIds = processActive
            ? runtimeService.getActiveActivityIds(instance.getProcessInstanceId())
            : List.of();

        List<WorkflowStatusResponse.ActiveTask> activeTasks = taskService.createTaskQuery()
            .processInstanceId(instance.getProcessInstanceId())
            .active()
            .list()
            .stream()
            .map(task -> new WorkflowStatusResponse.ActiveTask(
                task.getId(),
                task.getName(),
                (String) runtimeService.getVariable(task.getProcessInstanceId(), "initiator"),
                task.getTaskDefinitionKey(),
                task.getAssignee()
            ))
            .toList();

        return new WorkflowStatusResponse(
            instance.getId(),
            instance.getDocumentId(),
            instance.getProcessInstanceId(),
            instance.getProcessDefinitionKey(),
            instance.getStatus(),
            instance.getCreatedAt(),
            instance.getCompletedAt(),
            activeActivityIds,
            activeTasks
        );
    }

    private TemplateResponse toTemplateResponse(PipelineTemplate template) {
        return new TemplateResponse(
            template.getId(),
            template.getName(),
            template.getDocumentType(),
            Boolean.TRUE.equals(template.getIsPermanent()),
            template.getVersion(),
            template.getCreatedBy(),
            template.getCreatedAt(),
            deserializeSteps(template.getStepsJson())
        );
    }

    private List<PipelineStep> deserializeSteps(String stepsJson) {
        if (stepsJson == null || stepsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                stepsJson,
                objectMapper.getTypeFactory().constructCollectionType(List.class, PipelineStep.class)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize pipeline steps", e);
        }
    }

    private String resolveInstructions(String pipelineJson, String taskDefinitionKey) {
        if (pipelineJson == null || pipelineJson.isBlank()) {
            return null;
        }
        Integer stepNumber = extractStepNumber(taskDefinitionKey);
        if (stepNumber == null) {
            return null;
        }
        return deserializeSteps(pipelineJson).stream()
            .filter(step -> step.stepNo().equals(stepNumber))
            .map(PipelineStep::instructions)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private Integer extractStepNumber(String taskDefinitionKey) {
        if (taskDefinitionKey == null) {
            return null;
        }
        int markerIndex = taskDefinitionKey.lastIndexOf("_step_");
        if (markerIndex < 0) {
            return null;
        }
        String suffix = taskDefinitionKey.substring(markerIndex + 6);
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext() != null
            ? SecurityContextHolder.getContext().getAuthentication()
            : null;
    }

    private String currentUsername() {
        Authentication authentication = currentAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "system";
        }
        return authentication.getName();
    }

    private boolean hasRole(String role) {
        return hasAnyRole(role);
    }

    private boolean hasAnyRole(String... roles) {
        Authentication authentication = currentAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        for (GrantedAuthority authority : authorities) {
            String granted = authority.getAuthority();
            if (granted == null) {
                continue;
            }
            for (String role : roles) {
                if (granted.equals("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateSteps(List<PipelineStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new BadRequestException("Workflow steps cannot be empty");
        }

        Set<Integer> encounteredSteps = new HashSet<>();
        for (PipelineStep step : steps) {
            if (!departmentRepository.existsByKey(step.deptKey())) {
                throw new BadRequestException("Unknown department key: " + step.deptKey());
            }
            if (!encounteredSteps.add(step.stepNo())) {
                throw new BadRequestException("Duplicate step number: " + step.stepNo());
            }
        }

        int expectedSize = steps.size();
        for (int i = 1; i <= expectedSize; i++) {
            if (!encounteredSteps.contains(i)) {
                throw new BadRequestException("Missing step number: " + i);
            }
        }
    }

    private boolean updatePipelineInstanceStatusIfFinished(PipelineInstance instance) {
        boolean processActive = runtimeService.createProcessInstanceQuery()
            .processInstanceId(instance.getProcessInstanceId())
            .count() > 0;

        if (!processActive) {
            if (!"COMPLETED".equalsIgnoreCase(instance.getStatus())) {
                instance.setStatus("COMPLETED");
                instance.setCompletedAt(LocalDateTime.now());
                instanceRepo.save(instance);
            }
            return true;
        } else if (!"IN_PROGRESS".equalsIgnoreCase(instance.getStatus())) {
            instance.setStatus("IN_PROGRESS");
            instanceRepo.save(instance);
        }
        return false;
    }

    private void propagateProcessInstanceLink(Long documentId, String processInstanceId) {
        List<String> statuses = DEFAULT_DOCUMENT_STATUSES;
        try {
            DocumentServiceClient.DocumentSnapshot snapshot = documentServiceClient.getDocument(documentId);
            if (snapshot != null && snapshot.statuses() != null && snapshot.statuses().size() == 2) {
                statuses = snapshot.statuses();
            }
        } catch (HttpClientErrorException.NotFound notFound) {
            log.info("Document {} not found when linking process. Using default statuses.", documentId);
        } catch (Exception ex) {
            log.warn("Failed to fetch document {} details before linking process", documentId, ex);
        }

        try {
            documentServiceClient.updateDocumentStatus(documentId, statuses, processInstanceId);
        } catch (Exception ex) {
            log.warn("Failed to update document {} with process instance {}", documentId, processInstanceId, ex);
        }
    }
}

