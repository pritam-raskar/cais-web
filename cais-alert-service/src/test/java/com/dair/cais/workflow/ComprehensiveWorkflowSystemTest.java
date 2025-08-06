package com.dair.cais.workflow;

import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive end-to-end system tests for Workflow module
 * Tests the complete workflow lifecycle with real Spring context
 * Following established patterns from case and alert modules
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebMvc
@DisplayName("Comprehensive Workflow System Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComprehensiveWorkflowSystemTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WorkflowService workflowService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    // Test data holders for cross-test state management
    private static Long createdWorkflowId;
    private static String originalUiConfig;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @Order(1)
    @DisplayName("System Test: Create new workflow via API")
    void systemTest_CreateWorkflow_Success() throws Exception {
        // Given
        Workflow newWorkflow = WorkflowTestDataFactory.createTestWorkflowWithMinimalData();
        newWorkflow.setWorkflowName("System Test Workflow " + System.currentTimeMillis());

        // When & Then
        String response = mockMvc.perform(post("/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newWorkflow)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowName", is(newWorkflow.getWorkflowName())))
                .andExpect(jsonPath("$.createdBy", is(newWorkflow.getCreatedBy())))
                .andExpect(jsonPath("$.workflowId", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Parse response to get the created workflow ID for subsequent tests
        Workflow createdWorkflow = objectMapper.readValue(response, Workflow.class);
        createdWorkflowId = createdWorkflow.getWorkflowId();
        originalUiConfig = createdWorkflow.getUiConfig();

        assertThat(createdWorkflowId).isNotNull();
        assertThat(createdWorkflow.getWorkflowName()).isEqualTo(newWorkflow.getWorkflowName());
    }

    @Test
    @Order(2)
    @DisplayName("System Test: Retrieve workflow by ID")
    void systemTest_GetWorkflowById_Success() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}", createdWorkflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(createdWorkflowId.intValue())))
                .andExpect(jsonPath("$.workflowName", containsString("System Test Workflow")))
                .andExpect(jsonPath("$.createdBy", is(WorkflowTestDataFactory.DEFAULT_CREATED_BY)));
    }

    @Test
    @Order(3)
    @DisplayName("System Test: Retrieve workflow details")
    void systemTest_GetWorkflowDetails_Success() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}/details", createdWorkflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].workflowId", is(createdWorkflowId.intValue())))
                .andExpect(jsonPath("$[0].workflowName", containsString("System Test Workflow")));
    }

    @Test
    @Order(4)
    @DisplayName("System Test: Check workflow existence")
    void systemTest_WorkflowExists_ReturnsTrue() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // When & Then
        mockMvc.perform(get("/workflows/exists/{workflowId}", createdWorkflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));
    }

    @Test
    @Order(5)
    @DisplayName("System Test: Get workflow count")
    void systemTest_GetWorkflowCount_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workflows/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", greaterThan(0)));
    }

    @Test
    @Order(6)
    @DisplayName("System Test: Retrieve all workflows")
    void systemTest_GetAllWorkflows_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/workflows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.workflowId == " + createdWorkflowId + ")].workflowName", 
                        hasItem(containsString("System Test Workflow"))));
    }

    @Test
    @Order(7)
    @DisplayName("System Test: Get workflow UI configuration")
    void systemTest_GetWorkflowUiConfig_Success() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}/ui-config", createdWorkflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(originalUiConfig != null ? originalUiConfig : "{}"));
    }

    @Test
    @Order(8)
    @DisplayName("System Test: Update workflow UI configuration")
    void systemTest_UpdateWorkflowUiConfig_Success() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // Given
        String newUiConfig = "{\"steps\":[{\"id\":1,\"name\":\"Start\"}],\"transitions\":[]}";
        var uiConfigRequest = new Object() {
            public final String uiConfig = newUiConfig;
        };

        // When & Then
        mockMvc.perform(put("/workflows/{workflowId}/ui-config", createdWorkflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uiConfigRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(createdWorkflowId.intValue())));

        // Verify the UI configuration was updated
        mockMvc.perform(get("/workflows/{workflowId}/ui-config", createdWorkflowId))
                .andExpect(status().isOk())
                .andExpect(content().string(newUiConfig));
    }

    @Test
    @Order(9)
    @DisplayName("System Test: Update workflow details")
    void systemTest_UpdateWorkflow_Success() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // Given
        Workflow updateWorkflow = WorkflowTestDataFactory.createTestWorkflowForUpdate();
        updateWorkflow.setWorkflowName("Updated System Test Workflow " + System.currentTimeMillis());
        updateWorkflow.setDescription("Updated description for system test");

        // When & Then
        mockMvc.perform(put("/workflows/{workflowId}", createdWorkflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWorkflow)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(createdWorkflowId.intValue())))
                .andExpect(jsonPath("$.workflowName", is(updateWorkflow.getWorkflowName())))
                .andExpect(jsonPath("$.description", is(updateWorkflow.getDescription())));

        // Verify the workflow was updated
        mockMvc.perform(get("/workflows/{workflowId}", createdWorkflowId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowName", is(updateWorkflow.getWorkflowName())))
                .andExpect(jsonPath("$.description", is(updateWorkflow.getDescription())));
    }

    @Test
    @Order(10)
    @DisplayName("System Test: Service layer integration")
    void systemTest_ServiceLayerIntegration_Success() {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // Test service layer methods directly
        Workflow workflow = workflowService.getWorkflowById(createdWorkflowId);
        assertThat(workflow).isNotNull();
        assertThat(workflow.getWorkflowId()).isEqualTo(createdWorkflowId);
        assertThat(workflow.getWorkflowName()).containsIgnoringCase("Updated System Test Workflow");

        // Test workflow existence
        boolean exists = workflowService.workflowExists(createdWorkflowId);
        assertThat(exists).isTrue();

        // Test workflow count
        long count = workflowService.getWorkflowCount();
        assertThat(count).isGreaterThan(0);

        // Test workflow details
        List<WorkflowDetailDTO> details = workflowService.getWorkflowDetails(createdWorkflowId);
        assertThat(details).isNotEmpty();
        assertThat(details.get(0).getWorkflowId()).isEqualTo(createdWorkflowId);
    }

    @Test
    @Order(11)
    @DisplayName("System Test: Error handling for non-existent workflow")
    void systemTest_ErrorHandling_NonExistentWorkflow() throws Exception {
        // Given
        Long nonExistentId = 999999L;

        // When & Then - Test various endpoints with non-existent ID
        mockMvc.perform(get("/workflows/{workflowId}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/workflows/exists/{workflowId}", nonExistentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        // Service layer should return null for non-existent workflow
        Workflow nonExistentWorkflow = workflowService.getWorkflowById(nonExistentId);
        assertThat(nonExistentWorkflow).isNull();

        boolean exists = workflowService.workflowExists(nonExistentId);
        assertThat(exists).isFalse();
    }

    @Test
    @Order(12)
    @DisplayName("System Test: Delete workflow (cleanup)")
    @Transactional
    void systemTest_DeleteWorkflow_Success() throws Exception {
        // Ensure previous test created a workflow
        assertThat(createdWorkflowId).isNotNull();

        // When & Then
        mockMvc.perform(delete("/workflows/{workflowId}", createdWorkflowId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify the workflow was deleted
        mockMvc.perform(get("/workflows/{workflowId}", createdWorkflowId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/workflows/exists/{workflowId}", createdWorkflowId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @Order(13)
    @DisplayName("System Test: Workflow lifecycle validation")
    void systemTest_CompleteWorkflowLifecycle_Success() throws Exception {
        // Create a new workflow for lifecycle testing
        Workflow lifecycleWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        lifecycleWorkflow.setWorkflowName("Lifecycle Test Workflow " + System.currentTimeMillis());

        // 1. Create workflow
        String createResponse = mockMvc.perform(post("/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lifecycleWorkflow)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Workflow createdLifecycleWorkflow = objectMapper.readValue(createResponse, Workflow.class);
        Long lifecycleWorkflowId = createdLifecycleWorkflow.getWorkflowId();

        try {
            // 2. Read workflow
            mockMvc.perform(get("/workflows/{workflowId}", lifecycleWorkflowId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workflowId", is(lifecycleWorkflowId.intValue())));

            // 3. Update workflow
            lifecycleWorkflow.setDescription("Updated lifecycle description");
            mockMvc.perform(put("/workflows/{workflowId}", lifecycleWorkflowId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(lifecycleWorkflow)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description", is("Updated lifecycle description")));

            // 4. Update UI config
            String testUiConfig = "{\"steps\":[],\"transitions\":[],\"test\":true}";
            var uiConfigRequest = new Object() {
                public final String uiConfig = testUiConfig;
            };

            mockMvc.perform(put("/workflows/{workflowId}/ui-config", lifecycleWorkflowId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(uiConfigRequest)))
                    .andExpect(status().isOk());

            // 5. Verify all changes
            mockMvc.perform(get("/workflows/{workflowId}", lifecycleWorkflowId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description", is("Updated lifecycle description")));

            mockMvc.perform(get("/workflows/{workflowId}/ui-config", lifecycleWorkflowId))
                    .andExpect(status().isOk())
                    .andExpect(content().string(testUiConfig));

        } finally {
            // 6. Cleanup - Delete workflow
            mockMvc.perform(delete("/workflows/{workflowId}", lifecycleWorkflowId))
                    .andExpect(status().isNoContent());
        }
    }

    @AfterAll
    static void cleanup() {
        // Reset static test data holders
        createdWorkflowId = null;
        originalUiConfig = null;
    }
}