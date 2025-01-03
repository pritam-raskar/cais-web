package com.dair.cais.workflow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransitionReasonDTO {
    private Long id;

    @NotBlank(message = "Reason details cannot be empty")
    @Size(max = 4000, message = "Reason details cannot exceed 4000 characters")
    private String reasonDetails;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedDate;

    private String createdBy;
    private String updatedBy;
}