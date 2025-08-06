package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.WorkflowTestDataFactory;
import com.dair.cais.workflow.exception.WorkflowAlreadyExistsException;
import com.dair.cais.workflow.exception.WorkflowUpdateException;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.service.WorkflowService;
import com.dair.cais.workflow.dto.WorkflowUiConfigDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for WorkflowController using MockMvc
 * Following established patterns from alert and case controller tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Workflow Controller Unit Tests")
class WorkflowControllerTest {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;

    @Mock
    private WorkflowService workflowService;

    @InjectMocks
    private WorkflowController workflowController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(workflowController).build();
    }

    @Test
    @DisplayName("GET /workflows - Should return all workflows successfully")
    void getAllWorkflows_ValidRequest_ReturnsWorkflows() throws Exception {
        // Given
        List<Workflow> mockWorkflows = Arrays.asList(
            WorkflowTestDataFactory.createTestWorkflow(),
            WorkflowTestDataFactory.createTestWorkflowForUpdate()
        );
        when(workflowService.getAllWorkflows()).thenReturn(mockWorkflows);

        // When & Then
        mockMvc.perform(get("/workflows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].workflowId", is(WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID.intValue())))
                .andExpect(jsonPath("$[0].workflowName", is(WorkflowTestDataFactory.DEFAULT_WORKFLOW_NAME)))
                .andExpect(jsonPath("$[1].workflowId", is((int)(WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID + 1))))
                .andExpect(jsonPath("$[1].workflowName", is("Updated Test Workflow")));

        verify(workflowService).getAllWorkflows();
    }

    @Test
    @DisplayName("GET /workflows - Should return empty list when no workflows exist")
    void getAllWorkflows_NoWorkflows_ReturnsEmptyList() throws Exception {
        // Given
        when(workflowService.getAllWorkflows()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/workflows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(workflowService).getAllWorkflows();
    }

    @Test
    @DisplayName("GET /workflows/{id} - Should return workflow by ID successfully")
    void getWorkflowById_ValidId_ReturnsWorkflow() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        Workflow mockWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        when(workflowService.getWorkflowById(workflowId)).thenReturn(mockWorkflow);

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}", workflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(workflowId.intValue())))
                .andExpect(jsonPath("$.workflowName", is(WorkflowTestDataFactory.DEFAULT_WORKFLOW_NAME)))
                .andExpect(jsonPath("$.description", is(WorkflowTestDataFactory.DEFAULT_WORKFLOW_DESCRIPTION)))
                .andExpect(jsonPath("$.createdBy", is(WorkflowTestDataFactory.DEFAULT_CREATED_BY)));

        verify(workflowService).getWorkflowById(workflowId);
    }

    @Test
    @DisplayName("GET /workflows/{id} - Should return 404 when workflow not found")
    void getWorkflowById_WorkflowNotFound_Returns404() throws Exception {
        // Given
        Long workflowId = 999L;
        when(workflowService.getWorkflowById(workflowId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}", workflowId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(workflowService).getWorkflowById(workflowId);
    }

    @Test
    @DisplayName("POST /workflows - Should create workflow successfully")
    void createWorkflow_ValidData_CreatesWorkflow() throws Exception {
        // Given
        Workflow inputWorkflow = WorkflowTestDataFactory.createTestWorkflowWithMinimalData();
        Workflow savedWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        when(workflowService.createWorkflow(any(Workflow.class))).thenReturn(savedWorkflow);

        // When & Then
        mockMvc.perform(post("/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputWorkflow)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(savedWorkflow.getWorkflowId().intValue())))
                .andExpect(jsonPath("$.workflowName", is(savedWorkflow.getWorkflowName())))
                .andExpect(jsonPath("$.createdBy", is(savedWorkflow.getCreatedBy())));

        verify(workflowService).createWorkflow(any(Workflow.class));
    }

    @Test
    @DisplayName("POST /workflows - Should return 409 when workflow already exists")
    void createWorkflow_WorkflowAlreadyExists_Returns409() throws Exception {
        // Given
        Workflow inputWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        when(workflowService.createWorkflow(any(Workflow.class)))
                .thenThrow(new WorkflowAlreadyExistsException("Workflow already exists"));

        // When & Then
        mockMvc.perform(post("/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputWorkflow)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Workflow already exists")));

        verify(workflowService).createWorkflow(any(Workflow.class));
    }

    @Test
    @DisplayName("GET /workflows/{id}/details - Should return workflow details successfully")
    void getWorkflowDetails_ValidId_ReturnsDetails() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        List<WorkflowDetailDTO> mockDetails = Arrays.asList(
            WorkflowTestDataFactory.createTestWorkflowDetailDTO()
        );
        when(workflowService.getWorkflowDetails(workflowId)).thenReturn(mockDetails);

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}/details", workflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].workflowId", is(workflowId.intValue())))
                .andExpect(jsonPath("$[0].workflowName", is(WorkflowTestDataFactory.DEFAULT_WORKFLOW_NAME)));

        verify(workflowService).getWorkflowDetails(workflowId);
    }

    @Test
    @DisplayName("PUT /workflows/{id} - Should update workflow successfully")
    void updateWorkflow_ValidData_UpdatesWorkflow() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        Workflow updateWorkflow = WorkflowTestDataFactory.createTestWorkflowForUpdate();
        Workflow updatedWorkflow = WorkflowTestDataFactory.createTestWorkflowForUpdate();
        when(workflowService.updateWorkflow(eq(workflowId), any(Workflow.class))).thenReturn(updatedWorkflow);

        // When & Then
        mockMvc.perform(put("/workflows/{workflowId}", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWorkflow)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(updatedWorkflow.getWorkflowId().intValue())))
                .andExpect(jsonPath("$.workflowName", is("Updated Test Workflow")));

        verify(workflowService).updateWorkflow(eq(workflowId), any(Workflow.class));
    }

    @Test
    @DisplayName("DELETE /workflows/{id} - Should delete workflow successfully")
    void deleteWorkflow_ValidId_DeletesWorkflow() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        doNothing().when(workflowService).deleteWorkflow(workflowId);

        // When & Then
        mockMvc.perform(delete("/workflows/{workflowId}", workflowId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(workflowService).deleteWorkflow(workflowId);
    }

    @Test
    @DisplayName("GET /workflows/count - Should return workflow count successfully")
    void getWorkflowCount_ValidRequest_ReturnsCount() throws Exception {
        // Given
        long expectedCount = 5L;
        when(workflowService.getWorkflowCount()).thenReturn(expectedCount);

        // When & Then
        mockMvc.perform(get("/workflows/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("5"));

        verify(workflowService).getWorkflowCount();
    }

    @Test
    @DisplayName("GET /workflows/exists/{id} - Should return true when workflow exists")
    void workflowExists_WorkflowExists_ReturnsTrue() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        when(workflowService.workflowExists(workflowId)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/workflows/exists/{workflowId}", workflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(workflowService).workflowExists(workflowId);
    }

    @Test
    @DisplayName("GET /workflows/{id}/ui-config - Should return UI config successfully")
    void getWorkflowUiConfig_ValidId_ReturnsUiConfig() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        String expectedUiConfig = "{\"steps\":[],\"transitions\":[]}";
        when(workflowService.getWorkflowUiConfig(workflowId)).thenReturn(expectedUiConfig);

        // When & Then
        mockMvc.perform(get("/workflows/{workflowId}/ui-config", workflowId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedUiConfig));

        verify(workflowService).getWorkflowUiConfig(workflowId);
    }

    @Test
    @DisplayName("PUT /workflows/{id}/ui-config - Should update UI config successfully")
    void updateWorkflowUiConfig_ValidData_UpdatesUiConfig() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        String newUiConfig = "{\"steps\":[{\"id\":1}],\"transitions\":[]}";
        WorkflowUiConfigDTO uiConfigDTO = new WorkflowUiConfigDTO();
        uiConfigDTO.setUiConfig(newUiConfig);
        
        Workflow updatedWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        when(workflowService.updateWorkflowUiConfig(workflowId, newUiConfig)).thenReturn(updatedWorkflow);

        // When & Then
        mockMvc.perform(put("/workflows/{workflowId}/ui-config", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uiConfigDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.workflowId", is(workflowId.intValue())));

        verify(workflowService).updateWorkflowUiConfig(workflowId, newUiConfig);
    }

    @Test
    @DisplayName("PUT /workflows/{id}/ui-config - Should return 400 when UI config is null")
    void updateWorkflowUiConfig_NullUiConfig_Returns400() throws Exception {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        WorkflowUiConfigDTO uiConfigDTO = new WorkflowUiConfigDTO();
        uiConfigDTO.setUiConfig(null);

        // When & Then
        mockMvc.perform(put("/workflows/{workflowId}/ui-config", workflowId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uiConfigDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(workflowService, never()).updateWorkflowUiConfig(anyLong(), anyString());
    }
}