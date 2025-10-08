package com.docutrace.workflow_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PipelineTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    private String documentType;
    
    @NotNull
    @Column(columnDefinition = "jsonb", nullable = false)
    private String stepsJson;
    
    @NotNull
    @Column(name = "is_permanent", nullable = false)
    private Boolean isPermanent = false;
    
    @NotNull
    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false)
    private String createdBy;
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
