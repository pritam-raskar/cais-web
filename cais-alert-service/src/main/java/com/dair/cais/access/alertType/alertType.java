package com.dair.cais.access.alertType;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class alertType {
    private Integer atyId;
    private String alertTypeId;
    private Timestamp createdAt;
    private String description;
    private String typeName;
    private String typeSlug;
    private Timestamp updatedAt;
}