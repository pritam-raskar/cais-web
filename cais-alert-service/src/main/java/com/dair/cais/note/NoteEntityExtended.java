package com.dair.cais.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteEntityExtended implements Serializable {

    private static final long serialVersionUID = 1L;

    private String alertId;
    private String note;
    private int noteSize;  // Size calculated when note is submitted
    private String createdBy;
    private Date createdDate;
    private String entity;
    private String entityValue;
}