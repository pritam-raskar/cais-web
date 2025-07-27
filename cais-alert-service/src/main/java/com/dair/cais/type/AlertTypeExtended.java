package com.dair.cais.type;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertTypeExtended extends CaisBaseModel {
    private String alertTypeId;
    private String typeName;
    private String typeSlug;
    private String description;
    private boolean isActive;
    private List<String> extraField;
    private String field_schema;
    private String mandatory_fields;
    private Date createdAt;
    private Date updatedAt;
    private Integer workflowId;
}
