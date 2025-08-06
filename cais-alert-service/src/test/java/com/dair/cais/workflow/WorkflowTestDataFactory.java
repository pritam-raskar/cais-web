package com.dair.cais.workflow;

import com.dair.cais.alerts.AlertTestDataFactory;
import com.dair.cais.workflow.model.*;
import com.dair.cais.workflow.entity.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Factory class for creating test data objects for Workflow module testing
 * Extends AlertTestDataFactory to maintain consistency with established patterns
 */
public class WorkflowTestDataFactory extends AlertTestDataFactory {

    private static final Random RANDOM = new Random();

    // Workflow-specific test constants
    public static final Long DEFAULT_WORKFLOW_ID = 104L;
    public static final String DEFAULT_WORKFLOW_NAME = "Test Account Review Workflow";
    public static final String DEFAULT_WORKFLOW_DESCRIPTION = "Test workflow for account review process";
    public static final String DEFAULT_CREATED_BY = "TEST_WORKFLOW_SYSTEM";
    public static final String DEFAULT_UPDATED_BY = "TEST_WORKFLOW_USER";

    /**
     * Create a standard test workflow with all required fields
     */
    public static Workflow createTestWorkflow() {
        Workflow workflow = new Workflow();
        
        workflow.setWorkflowId(DEFAULT_WORKFLOW_ID);
        workflow.setWorkflowName(DEFAULT_WORKFLOW_NAME);
        workflow.setDescription(DEFAULT_WORKFLOW_DESCRIPTION);
        workflow.setCreatedBy(DEFAULT_CREATED_BY);
        workflow.setUpdatedBy(DEFAULT_UPDATED_BY);
        
        LocalDateTime now = LocalDateTime.now();
        workflow.setCreatedDate(now);
        workflow.setUpdatedDate(now);
        
        // UI Configuration JSON - matches service validation requirements
        workflow.setUiConfig("{\"steps\":[],\"transitions\":[]}");
        
        return workflow;
    }

    /**
     * Create a test workflow entity for database operations
     */
    public static WorkflowEntity createTestWorkflowEntity() {
        WorkflowEntity entity = new WorkflowEntity();
        
        entity.setWorkflowId(DEFAULT_WORKFLOW_ID);
        entity.setWorkflowName(DEFAULT_WORKFLOW_NAME);
        entity.setDescription(DEFAULT_WORKFLOW_DESCRIPTION);
        entity.setCreatedBy(DEFAULT_CREATED_BY);
        entity.setUpdatedBy(DEFAULT_UPDATED_BY);
        
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedDate(now);
        entity.setUpdatedDate(now);
        
        entity.setUiConfig("{\"steps\":[],\"transitions\":[]}");
        
        return entity;
    }

    /**
     * Create a minimal test workflow
     */
    public static Workflow createTestWorkflowWithMinimalData() {
        Workflow workflow = new Workflow();
        
        workflow.setWorkflowId(DEFAULT_WORKFLOW_ID + RANDOM.nextInt(1000));
        workflow.setWorkflowName("Minimal Test Workflow");
        workflow.setCreatedBy(DEFAULT_CREATED_BY);
        
        LocalDateTime now = LocalDateTime.now();
        workflow.setCreatedDate(now);
        workflow.setUpdatedDate(now);
        
        return workflow;
    }

    /**
     * Create a test workflow for update operations
     */
    public static Workflow createTestWorkflowForUpdate() {
        Workflow workflow = createTestWorkflow();
        workflow.setWorkflowId(DEFAULT_WORKFLOW_ID + 1);
        workflow.setWorkflowName("Updated Test Workflow");
        workflow.setDescription("Updated workflow description");
        workflow.setUpdatedBy("UPDATED_USER");
        workflow.setUpdatedDate(LocalDateTime.now());
        
        return workflow;
    }

    /**
     * Create workflow detail DTO for workflow details operations
     */
    public static WorkflowDetailDTO createTestWorkflowDetailDTO() {
        WorkflowDetailDTO detail = new WorkflowDetailDTO();
        
        detail.setWorkflowId(DEFAULT_WORKFLOW_ID);
        detail.setWorkflowName(DEFAULT_WORKFLOW_NAME);
        detail.setDescription(DEFAULT_WORKFLOW_DESCRIPTION);
        detail.setCreatedBy(DEFAULT_CREATED_BY);
        detail.setUpdatedBy(DEFAULT_UPDATED_BY);
        
        LocalDateTime now = LocalDateTime.now();
        detail.setCreatedDate(now);
        detail.setUpdatedDate(now);
        
        return detail;
    }

    /**
     * Create a list of test workflows for bulk operations
     */
    public static List<Workflow> createTestWorkflowList(int count) {
        List<Workflow> workflows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Workflow workflow = createTestWorkflow();
            workflow.setWorkflowId(DEFAULT_WORKFLOW_ID + i);
            workflow.setWorkflowName("Test Workflow " + i);
            workflows.add(workflow);
        }
        return workflows;
    }

    /**
     * Create a list of test workflow entities for database operations
     */
    public static List<WorkflowEntity> createTestWorkflowEntityList(int count) {
        List<WorkflowEntity> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            WorkflowEntity entity = createTestWorkflowEntity();
            entity.setWorkflowId(DEFAULT_WORKFLOW_ID + i);
            entity.setWorkflowName("Test Workflow " + i);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * Generate a unique ID for test data
     */
    private static String generateUniqueId() {
        return String.valueOf(System.currentTimeMillis() + RANDOM.nextInt(1000));
    }

    /**
     * Create future date for testing
     */
    public static LocalDateTime futureDateTime() {
        return LocalDateTime.now().plusDays(1);
    }

    /**
     * Create past date for testing
     */
    public static LocalDateTime pastDateTime() {
        return LocalDateTime.now().minusDays(1);
    }
}