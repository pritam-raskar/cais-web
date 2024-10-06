package com.dair.cais.permission;

import java.util.List;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Permission extends CaisBaseModel {

    private List<String> alertTypes;
    private List<String> roles;
    private List<String> businessUnits;
    private boolean assignedOnly;
}
