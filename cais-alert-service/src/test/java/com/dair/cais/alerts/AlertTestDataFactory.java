package com.dair.cais.alerts;

import com.dair.cais.alert.Alert;
import com.dair.cais.alert.AlertEntity;
import com.dair.cais.alert.dto.BulkStepChangeRequest;
import com.dair.cais.alert.filter.AlertFilterRequest;
import com.dair.cais.alert.filter.FilterCriteria;
import com.dair.cais.audit.AuditLogRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Factory class for creating test data objects for Alert module testing
 * Following the established pattern from case module testing
 */
public class AlertTestDataFactory {

    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Alert-specific test constants
    public static final String DEFAULT_ALERT_TYPE = "account-review";
    public static final String DEFAULT_ALERT_TYPE_ID = "AccRev";
    public static final String DEFAULT_WORKFLOW_ID = "104";
    public static final String DEFAULT_STEP_ID = "70"; // Ready step
    public static final String ALTERNATE_STEP_ID = "71"; // Under Review step
    public static final String REJECTED_STEP_ID = "6"; // Rejected step
    public static final String DEFAULT_ORG_UNIT_ID = "ORG001";
    public static final String DEFAULT_ORG_FAMILY = "Test Alert Family";
    public static final String DEFAULT_OWNER_ID = "TEST_ALERT_USER";
    public static final String DEFAULT_CREATED_BY = "TEST_ALERT_SYSTEM";

    /**
     * Create a standard test alert with all required fields
     */
    public static Alert createTestAlert() {
        Alert alert = new Alert();
        
        // Core identification
        alert.setAlertId("TEST_ALERT_" + generateUniqueId());
        alert.setAlertTypeId(DEFAULT_ALERT_TYPE_ID);
        alert.setAlertTypeName("Account Review Alert");
        
        // Dates and timestamps
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DATE_FORMATTER);
        alert.setCreateDate(formattedDateTime);
        alert.setLastUpdateDate(formattedDateTime);
        alert.setBusinessDate(formattedDateTime);
        alert.setCreatedAt(now);
        alert.setUpdatedAt(now);
        
        // Business data
        alert.setTotalScore(85.5);
        alert.setCreatedBy(DEFAULT_CREATED_BY);
        alert.setFocalEntity("Customer123");
        alert.setFocus("Transaction Monitoring");
        alert.setAlertRegion("US-EAST");
        alert.setAlertGroupId("GRP001");
        
        // Status and flags
        alert.setIsConsolidated(false);
        alert.setIsActive(true);
        alert.setHasMultipleScenario(false);
        alert.setIsDeleted(false);
        alert.setIsOrgUnitUpdated(false);
        alert.setIsRelatedAlert(false);
        alert.setIsCaseCreated(false);
        
        // Organization data
        alert.setOrgUnitId(DEFAULT_ORG_UNIT_ID);
        alert.setOrgUnitKey("ORG_KEY_001");
        alert.setOrgFamily(DEFAULT_ORG_FAMILY);
        alert.setOrgKey("ORG_KEY_001");
        
        // Ownership
        alert.setOwnerId(DEFAULT_OWNER_ID);
        alert.setOwnerName("Test Alert Owner");
        alert.setStatus("New");
        
        // Workflow data
        alert.setAlertStepId(DEFAULT_STEP_ID);
        alert.setAlertStepName("Ready");
        
        // Customer/Account data
        alert.setAccountId("ACC123456");
        alert.setAccountName("Test Account Name");
        alert.setCustomerId("CUST789012");
        alert.setCustomerName("Test Customer Name");
        alert.setTransactionId("TXN345678");
        
        // Additional fields
        alert.setPriority("High");
        alert.setTag("TEST_TAG");
        alert.setBranchId("BRANCH001");
        alert.setBranchName("Test Branch");
        alert.setRepresentativeId("REP001");
        alert.setRepresentativeName("Test Representative");
        alert.setBuildingBlock("Alert Processing");
        alert.setScenarioModelIds("SM001,SM002");
        alert.setScenarioModelNames("Model A,Model B");
        
        // Custom fields
        Map<String, Object> customFields = new HashMap<>();
        customFields.put("testField1", "testValue1");
        customFields.put("testField2", 100);
        alert.setCustomFields(customFields);
        
        // Workflow info
        Map<String, Object> workflowInfo = new HashMap<>();
        workflowInfo.put("workflowId", DEFAULT_WORKFLOW_ID);
        workflowInfo.put("currentStepId", DEFAULT_STEP_ID);
        alert.setWorkflowInfo(workflowInfo);
        
        return alert;
    }

    /**
     * Create a minimal test alert with only required fields
     */
    public static Alert createTestAlertWithMinimalData() {
        Alert alert = new Alert();
        
        alert.setAlertId("MINIMAL_ALERT_" + generateUniqueId());
        alert.setAlertTypeId(DEFAULT_ALERT_TYPE_ID);
        alert.setCreatedBy(DEFAULT_CREATED_BY);
        alert.setTotalScore(50.0);
        alert.setStatus("New");
        alert.setAlertStepId(DEFAULT_STEP_ID);
        alert.setIsActive(true);
        alert.setIsDeleted(false);
        
        LocalDateTime now = LocalDateTime.now();
        String formattedDateTime = now.format(DATE_FORMATTER);
        alert.setCreateDate(formattedDateTime);
        alert.setLastUpdateDate(formattedDateTime);
        alert.setCreatedAt(now);
        alert.setUpdatedAt(now);
        
        return alert;
    }

    /**
     * Create a test alert for update operations
     */
    public static Alert createTestAlertForUpdate() {
        Alert alert = createTestAlert();
        alert.setAlertId("UPDATE_ALERT_" + generateUniqueId());
        alert.setStatus("In Progress");
        alert.setAlertStepId(ALTERNATE_STEP_ID);
        alert.setAlertStepName("Under Review");
        alert.setTotalScore(95.0);
        alert.setPriority("Critical");
        alert.setOwnerId("UPDATED_OWNER");
        alert.setOwnerName("Updated Owner Name");
        
        LocalDateTime now = LocalDateTime.now();
        alert.setLastUpdateDate(now.format(DATE_FORMATTER));
        alert.setUpdatedAt(now);
        
        return alert;
    }

    /**
     * Create a test alert with workflow integration
     */
    public static Alert createTestAlertWithWorkflow() {
        Alert alert = createTestAlert();
        alert.setAlertId("WORKFLOW_ALERT_" + generateUniqueId());
        
        // Set workflow-specific data
        Map<String, Object> workflowInfo = new HashMap<>();
        workflowInfo.put("workflowId", DEFAULT_WORKFLOW_ID);
        workflowInfo.put("currentStepId", DEFAULT_STEP_ID);
        workflowInfo.put("assignedUserId", DEFAULT_OWNER_ID);
        workflowInfo.put("stepAssignedAt", LocalDateTime.now().toString());
        alert.setWorkflowInfo(workflowInfo);
        
        return alert;
    }

    /**
     * Create a test alert with attachments simulation
     */
    public static Alert createTestAlertWithAttachments() {
        Alert alert = createTestAlert();
        alert.setAlertId("ATTACHMENT_ALERT_" + generateUniqueId());
        
        // Simulate attachment details in custom fields
        Map<String, Object> customFields = new HashMap<>();
        customFields.put("hasAttachments", true);
        customFields.put("attachmentCount", 3);
        customFields.put("attachmentTypes", Arrays.asList("PDF", "Excel", "Image"));
        alert.setCustomFields(customFields);
        
        return alert;
    }

    /**
     * Create a test alert with notes simulation
     */
    public static Alert createTestAlertWithNotes() {
        Alert alert = createTestAlert();
        alert.setAlertId("NOTES_ALERT_" + generateUniqueId());
        
        // Simulate notes in reason details
        Map<String, Object> reasonDetails = new HashMap<>();
        reasonDetails.put("investigationNotes", "This alert requires further investigation");
        reasonDetails.put("analystComments", "Suspicious transaction pattern detected");
        reasonDetails.put("noteCount", 2);
        alert.setReasonDetails(reasonDetails);
        
        return alert;
    }

    /**
     * Create bulk step change request for alerts
     */
    public static BulkStepChangeRequest createBulkAlertStepChangeRequest(List<String> alertIds, Long stepId) {
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setAlertIds(alertIds);
        request.setStepId(stepId);
        request.setReason("Bulk step change for alert testing");
        request.setSkipValidation(false);
        return request;
    }

    /**
     * Create bulk step change request with validation skip
     */
    public static BulkStepChangeRequest createBulkAlertStepChangeRequestNoValidation(List<String> alertIds, Long stepId) {
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setAlertIds(alertIds);
        request.setStepId(stepId);
        request.setReason("Bulk step change with validation skipped");
        request.setSkipValidation(true);
        return request;
    }

    /**
     * Create alert filter request for testing search functionality
     */
    public static AlertFilterRequest createAlertFilterRequest() {
        AlertFilterRequest filterRequest = new AlertFilterRequest();
        
        FilterCriteria criteria = new FilterCriteria();
        // Add filter criteria based on available FilterCriteria fields
        filterRequest.setFilterCriteria(criteria);
        filterRequest.setAuditLogRequest(createAuditLogRequest());
        
        return filterRequest;
    }

    /**
     * Create audit log request for alert operations
     */
    public static AuditLogRequest createAuditLogRequest() {
        AuditLogRequest auditRequest = new AuditLogRequest();
        auditRequest.setUserId(123L);
        auditRequest.setUserRole("TEST_ALERT_ANALYST");
        auditRequest.setActionId(1);
        auditRequest.setDescription("Test audit log entry for alert operations");
        auditRequest.setCategory("ALERT_OPERATION");
        auditRequest.setAffectedItemType("Alert");
        return auditRequest;
    }

    /**
     * Create audit log request for bulk alert operations
     */
    public static AuditLogRequest createAuditLogRequestForBulkAlerts() {
        AuditLogRequest auditRequest = new AuditLogRequest();
        auditRequest.setUserId(123L);
        auditRequest.setUserRole("TEST_ALERT_ANALYST");
        auditRequest.setActionId(2);
        auditRequest.setDescription("Bulk alert operation test audit log");
        auditRequest.setCategory("BULK_ALERT_OPERATION");
        auditRequest.setAffectedItemType("Alert");
        auditRequest.setAffectedItemId("BULK_ALERT_TEST");
        return auditRequest;
    }

    /**
     * Create alert entity for MongoDB operations
     */
    public static AlertEntity createTestAlertEntity() {
        AlertEntity entity = new AlertEntity();
        Alert alert = createTestAlert();
        
        // Copy fields from Alert to AlertEntity
        entity.setId(alert.getId());
        entity.setAlertId(alert.getAlertId());
        entity.setCreateDate(alert.getCreateDate());
        entity.setLastUpdateDate(alert.getLastUpdateDate());
        entity.setTotalScore(alert.getTotalScore());
        entity.setCreatedBy(alert.getCreatedBy());
        entity.setBusinessDate(alert.getBusinessDate());
        entity.setFocalEntity(alert.getFocalEntity());
        entity.setFocus(alert.getFocus());
        entity.setAlertTypeId(alert.getAlertTypeId());
        entity.setAlertRegion(alert.getAlertRegion());
        entity.setAlertGroupId(alert.getAlertGroupId());
        entity.setIsConsolidated(alert.getIsConsolidated());
        entity.setIsActive(alert.getIsActive());
        entity.setHasMultipleScenario(alert.getHasMultipleScenario());
        entity.setIsDeleted(alert.getIsDeleted());
        entity.setOrgUnitId(alert.getOrgUnitId());
        entity.setOrgUnitKey(alert.getOrgUnitKey());
        entity.setOrgFamily(alert.getOrgFamily());
        entity.setOwnerId(alert.getOwnerId());
        entity.setOwnerName(alert.getOwnerName());
        entity.setStatus(alert.getStatus());
        entity.setAlertStepId(alert.getAlertStepId());
        entity.setAlertStepName(alert.getAlertStepName());
        entity.setIsCaseCreated(alert.getIsCaseCreated());
        entity.setDetails(alert.getDetails());
        entity.setCustomFields(alert.getCustomFields());
        entity.setWorkflowInfo(alert.getWorkflowInfo());
        entity.setCreatedAt(alert.getCreatedAt());
        entity.setUpdatedAt(alert.getUpdatedAt());
        
        return entity;
    }

    /**
     * Create a list of test alerts for bulk operations
     */
    public static List<Alert> createTestAlertList(int count) {
        List<Alert> alerts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Alert alert = createTestAlert();
            alert.setAlertId("BULK_ALERT_" + i + "_" + generateUniqueId());
            alerts.add(alert);
        }
        return alerts;
    }

    /**
     * Create a list of alert IDs for bulk operations
     */
    public static List<String> createAlertIdList(int count) {
        List<String> alertIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            alertIds.add("TEST_ALERT_ID_" + i + "_" + generateUniqueId());
        }
        return alertIds;
    }

    /**
     * Generate a unique ID for test data
     */
    private static String generateUniqueId() {
        return String.valueOf(System.currentTimeMillis() + RANDOM.nextInt(1000));
    }

    /**
     * Create future date string
     */
    public static String futureDate() {
        return LocalDateTime.now().plusDays(1).format(DATE_FORMATTER);
    }

    /**
     * Create past date string
     */
    public static String pastDate() {
        return LocalDateTime.now().minusDays(1).format(DATE_FORMATTER);
    }
}