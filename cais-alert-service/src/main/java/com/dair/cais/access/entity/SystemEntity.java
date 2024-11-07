package com.dair.cais.access.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SystemEntity {
    private Integer entityId;
    private String entityType;
    private String entityName;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}