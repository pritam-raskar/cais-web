package com.dair.cais.type;

import java.util.Date;
import java.util.Map;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertType extends CaisBaseModel {

    private String name;
    private String description;
    private String slug;
    private boolean active;
    private Map<String, Object> fields;

    private Date createdDate;
    private Date updatedDate;
}
