package com.dair.cais.type;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import lombok.Data;

@Data
public class AlertTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @MongoId(FieldType.OBJECT_ID)
    private String id;
    private String name;
    private String description;
    private String slug;
    private boolean active;
    private Map<String, Object> fields;

    private Date createdDate;
    private Date updatedDate;
}