package com.dair.cais.branch;

import java.util.Date;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Branch extends CaisBaseModel {

    private Date createdDate;
    private Date updatedDate;
}
