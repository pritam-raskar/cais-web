package com.dair.cais.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkflowDetail {
    private Long workflowId;
    private String workflowName;
}
