package com.dair.cais.access.alertType;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class alertType {
    private Integer atyId;
    private String alertTypeId;
    private LocalDateTime createdAt;
    private String description;
    private String typeName;
    private String typeSlug;
    private LocalDateTime updatedAt;
}