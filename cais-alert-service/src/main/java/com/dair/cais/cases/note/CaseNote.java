package com.dair.cais.cases.note;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Case Notes.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseNote {
    private Long noteId;
    private Long caseId;
    private String content;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}