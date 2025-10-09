package com.docutrace.workflow_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(nullable = false)
    private Long documentId;
    private Long templateId;
    @NotBlank
    @Column(nullable = false)
    private String processDefinitionKey;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String processInstanceId;
    
    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String pipelineJson;
    
    @NotBlank
    @Column(nullable = false)
    private String status = "IN_PROGRESS";
    
    @NotBlank
    @Column(nullable = false)
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
