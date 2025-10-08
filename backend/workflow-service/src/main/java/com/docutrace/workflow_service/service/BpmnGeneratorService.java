package com.docutrace.workflow_service.service;

import com.docutrace.workflow_service.dto.PipelineStep;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BpmnGeneratorService {
    
    public BpmnModelInstance generatePipeline(String processKey, List<PipelineStep> steps) {
        BpmnModelInstance model = Bpmn.createEmptyModel();
        Definitions definitions = model.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://localhost:8083/api/workflow");
        // add camunda namespace for extension attributes like historyTimeToLive
        definitions.getDomElement().registerNamespace("camunda", "http://camunda.org/schema/1.0/bpmn");
        model.setDefinitions(definitions);

        Process process = model.newInstance(Process.class);
        process.setId(processKey);
        process.setName("Document Pipeline: " + processKey);
        // mark process as executable so Camunda registers a ProcessDefinition on deployment
        process.setExecutable(true);
    // keep history available for a week; Camunda API handles the namespace wiring
    process.setCamundaHistoryTimeToLiveString("P7D");
        definitions.addChildElement(process);

        // Start event
        StartEvent start = model.newInstance(StartEvent.class);
        start.setId(processKey + "_start");
        start.setName("Document Submitted");
        process.addChildElement(start);
        
        FlowNode previous = start;
        
        // Generate user tasks for each step
        for (int i = 0; i < steps.size(); i++) {
            PipelineStep step = steps.get(i);
            
            UserTask task = model.newInstance(UserTask.class);
            task.setId(processKey + "_step_" + (i + 1));
            task.setName(step.deptKey() + " Review");
            task.setCamundaCandidateGroups(step.deptKey());
            process.addChildElement(task);
            
            // Connect previous node to this task
            SequenceFlow flow = model.newInstance(SequenceFlow.class);
            flow.setId(processKey + "_flow_" + (i + 1));
            process.addChildElement(flow);
            flow.setSource(previous);
            flow.setTarget(task);
            previous.getOutgoing().add(flow);
            task.getIncoming().add(flow);
            
            previous = task;
        }
        
        // End event
        EndEvent end = model.newInstance(EndEvent.class);
        end.setId(processKey + "_end");
        end.setName("Pipeline Complete");
        process.addChildElement(end);
        
        SequenceFlow lastFlow = model.newInstance(SequenceFlow.class);
        lastFlow.setId(processKey + "_flow_end");
        process.addChildElement(lastFlow);
        lastFlow.setSource(previous);
        lastFlow.setTarget(end);
        previous.getOutgoing().add(lastFlow);
        end.getIncoming().add(lastFlow);
        
        Bpmn.validateModel(model);
        return model;
    }
}
