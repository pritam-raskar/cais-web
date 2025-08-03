package com.dair.cais.cases;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.cases.dto.BulkStepChangeRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Factory class for creating test data objects
 */
public class TestDataFactory {

    public static Case createTestCase() {
        Case testCase = new Case();
        testCase.setTitle("Test Case for Integration Testing");
        testCase.setDescription("This is a test case created for integration testing purposes");
        testCase.setStatus("New");
        testCase.setPriority("High");
        testCase.setCaseType("account-review");
        testCase.setOrgUnitId("ORG001");
        testCase.setOrgFamily("Test Family");
        testCase.setOwnerId("TEST_USER");
        testCase.setOwnerName("Test User");
        testCase.setCreatedBy("TEST_SYSTEM");
        testCase.setIsActive(true);
        return testCase;
    }

    public static Case createTestCaseWithMinimalData() {
        Case testCase = new Case();
        testCase.setTitle("Minimal Test Case");
        testCase.setDescription("Minimal test case for testing");
        testCase.setStatus("New");
        testCase.setPriority("Medium");
        testCase.setCaseType("account-review");
        testCase.setCreatedBy("TEST_SYSTEM");
        testCase.setIsActive(true);
        return testCase;
    }

    public static Case createTestCaseForUpdate() {
        Case testCase = new Case();
        testCase.setTitle("Updated Test Case Title");
        testCase.setDescription("This case has been updated for testing");
        testCase.setStatus("In Progress");
        testCase.setPriority("Low");
        testCase.setOwnerId("UPDATED_USER");
        testCase.setOwnerName("Updated User");
        return testCase;
    }

    public static BulkStepChangeRequest createBulkStepChangeRequest(List<Long> caseIds, Long stepId) {
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(caseIds);
        request.setStepId(stepId);
        request.setReason("Bulk step change for testing");
        request.setSkipValidation(false);
        return request;
    }

    public static BulkStepChangeRequest createBulkStepChangeRequestWithValidation(List<Long> caseIds, Long stepId) {
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(caseIds);
        request.setStepId(stepId);
        request.setReason("Bulk step change with validation for testing");
        request.setSkipValidation(false);
        return request;
    }

    public static AuditLogRequest createAuditLogRequest() {
        AuditLogRequest auditRequest = new AuditLogRequest();
        auditRequest.setUserId(123L);
        auditRequest.setUserRole("TEST_ANALYST");
        auditRequest.setActionId(1);
        auditRequest.setDescription("Test audit log entry for case workflow testing");
        auditRequest.setCategory("TEST_CATEGORY");
        auditRequest.setAffectedItemType("Case");
        return auditRequest;
    }

    public static AuditLogRequest createAuditLogRequestForBulk() {
        AuditLogRequest auditRequest = new AuditLogRequest();
        auditRequest.setUserId(123L);
        auditRequest.setUserRole("TEST_ANALYST");
        auditRequest.setActionId(2);
        auditRequest.setDescription("Bulk operation test audit log");
        auditRequest.setCategory("BULK_OPERATION");
        auditRequest.setAffectedItemType("Case");
        auditRequest.setAffectedItemId("BULK_TEST");
        return auditRequest;
    }

    // Test step IDs commonly used in tests
    public static final Long DEFAULT_STEP_ID = 70L; // Ready step
    public static final Long ALTERNATE_STEP_ID = 71L; // Under Review step
    public static final Long REJECTED_STEP_ID = 6L; // Rejected step
    public static final Long DEFAULT_WORKFLOW_ID = 104L;

    // Test case types
    public static final String ACCOUNT_REVIEW_CASE_TYPE = "account-review";
    public static final String CUSTOMER_SUPPORT_CASE_TYPE = "Customer Support";
    public static final String AML_CASE_TYPE = "AML";
}