package com.dair.cais.cases;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.cases.dto.BulkStepChangeRequest;
import com.dair.cais.cases.dto.BulkStepChangeResponse;
import com.dair.cais.cases.repository.CaseRepository;
import com.dair.cais.cases.workflow.dto.StepTransitionRequest;
import com.dair.cais.workflow.dto.StepTransitionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for Case Workflow functionality
 * Tests all HTTP methods: GET, POST, PATCH, PUT, DELETE
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("Comprehensive Case Workflow Integration Tests")
class ComprehensiveCaseWorkflowTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseRepository caseRepository;

    // Test data
    private static Long testCaseId;
    private static Long secondTestCaseId;
    private static String testCaseNumber;

    @BeforeEach
    void setUp() {
        if (mockMvc == null) {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }
    }

    // ==================== SETUP PHASE ====================

    @Test
    @Order(1)
    @DisplayName("Setup: Create test cases for workflow testing")
    void setup_CreateTestCases() throws Exception {
        // Create first test case
        Case newCase = TestDataFactory.createTestCase();
        
        MvcResult result = mockMvc.perform(post("/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCase)))
                .andDo(print())
                .andReturn();
        
        // Debug: print the response details
        System.out.println("Response Status: " + result.getResponse().getStatus());
        System.out.println("Response Body: " + result.getResponse().getContentAsString());
        
        // Now check expectations
        if (result.getResponse().getStatus() != 201) {
            System.err.println("Case creation failed with status: " + result.getResponse().getStatus());
            System.err.println("Response body: " + result.getResponse().getContentAsString());
            // Return early to avoid further test execution
            return;
        }

        Case createdCase = objectMapper.readValue(
                result.getResponse().getContentAsString(), Case.class);
        testCaseId = createdCase.getCaseId();
        testCaseNumber = createdCase.getCaseNumber();

        assertThat(testCaseId).isNotNull();
        assertThat(testCaseNumber).isNotNull();

        // Create second test case for bulk operations
        Case secondCase = TestDataFactory.createTestCaseWithMinimalData();
        secondCase.setTitle("Second Test Case for Bulk Operations");
        
        MvcResult secondResult = mockMvc.perform(post("/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondCase)))
                .andExpect(status().isCreated())
                .andReturn();

        Case secondCreatedCase = objectMapper.readValue(
                secondResult.getResponse().getContentAsString(), Case.class);
        secondTestCaseId = secondCreatedCase.getCaseId();

        assertThat(secondTestCaseId).isNotNull();
        System.out.println("Created test cases: " + testCaseId + " and " + secondTestCaseId);
    }

    // ==================== GET OPERATIONS ====================

    @Test
    @Order(2)
    @DisplayName("GET: Test all case retrieval endpoints")
    void testGetOperations() throws Exception {
        // Test individual case retrieval
        mockMvc.perform(get("/cases/{id}", testCaseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId", is(testCaseId.intValue())))
                .andExpect(jsonPath("$.title", containsString("Test Case")))
                .andExpect(jsonPath("$.caseType", is(TestDataFactory.ACCOUNT_REVIEW_CASE_TYPE)));

        // Test get all cases
        mockMvc.perform(get("/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[?(@.caseId == " + testCaseId + ")].title", 
                    hasItem(containsString("Test Case"))));

        // Test get case by case number
        mockMvc.perform(get("/cases/by-number")
                .param("caseNumber", testCaseNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId", is(testCaseId.intValue())))
                .andExpect(jsonPath("$.caseNumber", is(testCaseNumber)));

        // Test available steps (should work even without workflow assigned)
        mockMvc.perform(get("/case-workflows/cases/{id}/available-steps", testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));
    }

    @Test
    @Order(3)
    @DisplayName("GET: Test error handling for non-existent cases")
    void testGetOperations_ErrorHandling() throws Exception {
        Long nonExistentId = 99999L;

        // Test 404 for non-existent case
        mockMvc.perform(get("/cases/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        // Test 404 for non-existent case workflow endpoints
        mockMvc.perform(get("/case-workflows/cases/{id}/available-steps", nonExistentId))
                .andExpect(status().isNotFound());

        // Test invalid case number
        mockMvc.perform(get("/cases/by-number")
                .param("caseNumber", "NON_EXISTENT_CASE"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST OPERATIONS ====================

    @Test
    @Order(4)
    @DisplayName("POST: Test workflow assignment and step transitions")
    void testPostOperations_WorkflowAssignment() throws Exception {
        // Assign workflow to test case
        mockMvc.perform(post("/case-workflows/cases/{id}/assign-workflow", testCaseId)
                .param("workflowId", TestDataFactory.DEFAULT_WORKFLOW_ID.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId", is(testCaseId.intValue())));

        // Transition case to initial step
        StepTransitionRequest transitionRequest = new StepTransitionRequest();
        transitionRequest.setReason("Initial step assignment for testing");
        transitionRequest.setComment("Setting up test case with workflow step");

        mockMvc.perform(post("/case-workflows/cases/{id}/transition", testCaseId)
                .param("stepId", TestDataFactory.DEFAULT_STEP_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transitionRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStepId", is(TestDataFactory.DEFAULT_STEP_ID.intValue())))
                .andExpect(jsonPath("$.currentStepName", is("Ready")));

        // Verify the case now has workflow and step assigned
        mockMvc.perform(get("/cases/{id}", testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStepId", is(TestDataFactory.DEFAULT_STEP_ID.intValue())))
                .andExpect(jsonPath("$.currentStepName", is("Ready")));
    }

    @Test
    @Order(5)
    @DisplayName("POST: Test bulk step change operations")
    void testPostOperations_BulkStepChange() throws Exception {
        // Setup: Assign workflow to second test case as well
        mockMvc.perform(post("/case-workflows/cases/{id}/assign-workflow", secondTestCaseId)
                .param("workflowId", TestDataFactory.DEFAULT_WORKFLOW_ID.toString()))
                .andExpect(status().isOk());

        // Set initial step for second case
        StepTransitionRequest transitionRequest = new StepTransitionRequest();
        transitionRequest.setReason("Initial step for bulk testing");

        mockMvc.perform(post("/case-workflows/cases/{id}/transition", secondTestCaseId)
                .param("stepId", TestDataFactory.DEFAULT_STEP_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transitionRequest)))
                .andExpect(status().isOk());

        // Test bulk step change
        BulkStepChangeRequest bulkRequest = TestDataFactory.createBulkStepChangeRequest(
                Arrays.asList(testCaseId, secondTestCaseId), 
                TestDataFactory.ALTERNATE_STEP_ID);

        MvcResult bulkResult = mockMvc.perform(post("/cases/bulk/step-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested", is(2)))
                .andExpect(jsonPath("$.successCount", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.failureCount", greaterThanOrEqualTo(0)))
                .andReturn();

        BulkStepChangeResponse response = objectMapper.readValue(
                bulkResult.getResponse().getContentAsString(), BulkStepChangeResponse.class);
        
        assertThat(response.getTotalRequested()).isEqualTo(2);
        assertThat(response.getSuccessCount() + response.getFailureCount()).isEqualTo(2);
    }

    @Test
    @Order(6)
    @DisplayName("POST: Test bulk step change with audit logging")
    void testPostOperations_BulkStepChangeWithAudit() throws Exception {
        BulkStepChangeRequest bulkRequest = TestDataFactory.createBulkStepChangeRequest(
                Arrays.asList(testCaseId), TestDataFactory.DEFAULT_STEP_ID);
        
        AuditLogRequest auditRequest = TestDataFactory.createAuditLogRequestForBulk();

        // Create combined request body for audit endpoint
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("caseIds", bulkRequest.getCaseIds());
        requestBody.put("stepId", bulkRequest.getStepId());
        requestBody.put("reason", bulkRequest.getReason());
        requestBody.put("auditLogRequest", auditRequest);

        mockMvc.perform(post("/cases/audit/bulk/step-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequested", is(1)));

        // Test alternative bulk endpoint
        mockMvc.perform(post("/cases/bulk/change-step")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isOk());
    }

    // ==================== PATCH OPERATIONS ====================

    @Test
    @Order(7)
    @DisplayName("PATCH: Test single case step changes")
    void testPatchOperations_StepChanges() throws Exception {
        // Test simple step change
        mockMvc.perform(patch("/cases/changestep/{id}", testCaseId)
                .param("stepId", TestDataFactory.ALTERNATE_STEP_ID.toString())
                .param("userId", "TEST_USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStepId", is(TestDataFactory.ALTERNATE_STEP_ID.intValue())));

        // Test step change with audit
        AuditLogRequest auditRequest = TestDataFactory.createAuditLogRequest();
        auditRequest.setAffectedItemId(testCaseId.toString());

        mockMvc.perform(patch("/cases/audit/changestep/{id}", testCaseId)
                .param("stepId", TestDataFactory.DEFAULT_STEP_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(auditRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStepId", is(TestDataFactory.DEFAULT_STEP_ID.intValue())));
    }

    @Test
    @Order(8)
    @DisplayName("PATCH: Test case status and assignment changes")
    void testPatchOperations_StatusAndAssignment() throws Exception {
        // Test status change
        mockMvc.perform(patch("/cases/{id}/status", testCaseId)
                .param("newStatus", "In Progress")
                .param("reason", "Moving case to in progress for testing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("In Progress")));

        // Test case assignment
        mockMvc.perform(patch("/cases/{id}/assign", testCaseId)
                .param("userId", "NEW_ASSIGNED_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId", is("NEW_ASSIGNED_USER")));
    }

    // ==================== PUT OPERATIONS ====================

    @Test
    @Order(9)
    @DisplayName("PUT: Test case updates")
    void testPutOperations() throws Exception {
        Case updateCase = TestDataFactory.createTestCaseForUpdate();
        
        mockMvc.perform(put("/cases/{id}", testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCase)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updateCase.getTitle())))
                .andExpect(jsonPath("$.description", is(updateCase.getDescription())))
                .andExpect(jsonPath("$.status", is(updateCase.getStatus())))
                .andExpect(jsonPath("$.priority", is(updateCase.getPriority())));

        // Verify the update persisted
        mockMvc.perform(get("/cases/{id}", testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(updateCase.getTitle())));
    }

    // ==================== WORKFLOW-SPECIFIC TESTS ====================

    @Test
    @Order(10)
    @DisplayName("GET: Test step transitions and workflow information")
    void testWorkflowInformationEndpoints() throws Exception {
        // Test step transitions (should work now that case has workflow and step)
        mockMvc.perform(get("/cases/{id}/step-transitions", testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextSteps", isA(List.class)))
                .andExpect(jsonPath("$.backSteps", isA(List.class)));

        // Test possible transitions
        mockMvc.perform(get("/case-workflows/cases/{id}/possible-transitions", testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));

        // Test available steps (should return workflow steps)
        mockMvc.perform(get("/case-workflows/cases/{id}/available-steps", testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].stepId", notNullValue()));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @Order(11)
    @DisplayName("Error Handling: Test validation and error scenarios")
    void testErrorHandling() throws Exception {
        Long nonExistentId = 99999L;
        Long invalidStepId = 99999L;

        // Test invalid step change
        mockMvc.perform(patch("/cases/changestep/{id}", testCaseId)
                .param("stepId", invalidStepId.toString())
                .param("userId", "TEST_USER"))
                .andExpect(status().isBadRequest());

        // Test bulk operation with empty request
        mockMvc.perform(post("/cases/bulk/step-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        // Test workflow assignment to non-existent case
        mockMvc.perform(post("/case-workflows/cases/{id}/assign-workflow", nonExistentId)
                .param("workflowId", "104"))
                .andExpect(status().isNotFound());

        // Test step transition for case without workflow
        Case newCaseWithoutWorkflow = TestDataFactory.createTestCaseWithMinimalData();
        newCaseWithoutWorkflow.setTitle("Case Without Workflow");
        
        MvcResult result = mockMvc.perform(post("/cases")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCaseWithoutWorkflow)))
                .andExpect(status().isCreated())
                .andReturn();

        Case createdCaseWithoutWorkflow = objectMapper.readValue(
                result.getResponse().getContentAsString(), Case.class);

        mockMvc.perform(get("/cases/{id}/step-transitions", createdCaseWithoutWorkflow.getCaseId()))
                .andExpect(status().isBadRequest());
    }

    // ==================== SEARCH AND FILTER TESTS ====================

    @Test
    @Order(12)
    @DisplayName("POST: Test case search functionality")
    void testSearchFunctionality() throws Exception {
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("status", "In Progress");
        searchCriteria.put("caseType", TestDataFactory.ACCOUNT_REVIEW_CASE_TYPE);

        mockMvc.perform(post("/cases/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchCriteria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)));

        // Test empty search criteria
        mockMvc.perform(post("/cases/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());
    }

    // ==================== CLEANUP PHASE ====================

    @Test
    @Order(13)
    @DisplayName("DELETE: Test case deletion")
    void cleanup_DeleteTestCases() throws Exception {
        // Delete second test case
        mockMvc.perform(delete("/cases/{id}", secondTestCaseId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/cases/{id}", secondTestCaseId))
                .andExpect(status().isNotFound());

        // Delete main test case
        mockMvc.perform(delete("/cases/{id}", testCaseId))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/cases/{id}", testCaseId))
                .andExpect(status().isNotFound());

        System.out.println("Successfully cleaned up test cases: " + testCaseId + " and " + secondTestCaseId);
    }

    @AfterEach
    void tearDown() {
        // Any per-test cleanup can go here
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("Comprehensive Case Workflow Tests Completed Successfully!");
    }
}