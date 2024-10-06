package com.dair.cais.attachment;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attachment extends CaisBaseModel {

    private String value;
    private Date createdDate;
    private Date updatedDate;
}
