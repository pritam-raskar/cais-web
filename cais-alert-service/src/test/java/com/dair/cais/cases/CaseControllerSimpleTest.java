package com.dair.cais.cases;

import com.dair.cais.cases.service.CaseService;
import com.dair.cais.cases.workflow.service.CaseWorkflowService;
import com.dair.cais.cases.controller.CaseController;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple unit tests for CaseController to debug endpoint issues
 * Using MockMvc standalone setup to avoid database dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Case Controller Simple Tests - Debug")
class CaseControllerSimpleTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CaseService caseService;

    @Mock
    private CaseWorkflowService caseWorkflowService;

    @InjectMocks
    private CaseController caseController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(caseController).build();
    }

    @Test
    @DisplayName("GET /cases - Should return all cases successfully")
    void getAllCases_ValidRequest_ReturnsCases() throws Exception {
        // Given
        Case mockCase1 = TestDataFactory.createTestCase();
        mockCase1.setCaseId(1001L);
        Case mockCase2 = TestDataFactory.createTestCase();
        mockCase2.setCaseId(1002L);
        
        when(caseService.getAllCases()).thenReturn(Arrays.asList(mockCase1, mockCase2));

        // When & Then
        mockMvc.perform(get("/cases"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].caseId", is(mockCase1.getCaseId().intValue())))
                .andExpect(jsonPath("$[1].caseId", is(mockCase2.getCaseId().intValue())));

        verify(caseService).getAllCases();
    }

    @Test
    @DisplayName("GET /cases - Should return empty list when no cases exist")
    void getAllCases_NoCases_ReturnsEmptyList() throws Exception {
        // Given
        when(caseService.getAllCases()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/cases"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(caseService).getAllCases();
    }

    @Test
    @DisplayName("GET /cases/{id} - Should return case by ID successfully")
    void getCaseById_ValidId_ReturnsCase() throws Exception {
        // Given
        Long caseId = 1001L;
        Case mockCase = TestDataFactory.createTestCase();
        mockCase.setCaseId(caseId);
        when(caseService.getCase(caseId)).thenReturn(mockCase);

        // When & Then
        mockMvc.perform(get("/cases/{id}", caseId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.caseId", is(caseId.intValue())))
                .andExpect(jsonPath("$.title", is(mockCase.getTitle())));

        verify(caseService).getCase(caseId);
    }

    @Test
    @DisplayName("POST /cases - Should create case successfully")
    void createCase_ValidData_CreatesCase() throws Exception {
        // Given
        Case inputCase = TestDataFactory.createTestCaseWithMinimalData();
        Case savedCase = TestDataFactory.createTestCase();
        savedCase.setCaseId(1001L);
        when(caseService.createCase(any(Case.class))).thenReturn(savedCase);

        // When & Then
        mockMvc.perform(post("/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCase)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.caseId", is(savedCase.getCaseId().intValue())))
                .andExpect(jsonPath("$.title", is(savedCase.getTitle())));

        verify(caseService).createCase(any(Case.class));
    }

    @Test
    @DisplayName("PUT /cases/{id} - Should update case successfully")
    void updateCase_ValidData_UpdatesCase() throws Exception {
        // Given
        Long caseId = 1001L;
        Case updateCase = TestDataFactory.createTestCaseForUpdate();
        Case updatedCase = TestDataFactory.createTestCaseForUpdate();
        updatedCase.setCaseId(caseId);
        when(caseService.updateCase(eq(caseId), any(Case.class))).thenReturn(updatedCase);

        // When & Then
        mockMvc.perform(put("/cases/{id}", caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCase)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.caseId", is(caseId.intValue())))
                .andExpect(jsonPath("$.title", is(updatedCase.getTitle())));

        verify(caseService).updateCase(eq(caseId), any(Case.class));
    }

    @Test
    @DisplayName("DELETE /cases/{id} - Should delete case successfully")
    void deleteCase_ValidId_DeletesCase() throws Exception {
        // Given
        Long caseId = 1001L;
        doNothing().when(caseService).deleteCase(caseId);

        // When & Then
        mockMvc.perform(delete("/cases/{id}", caseId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(caseService).deleteCase(caseId);
    }

    @Test
    @DisplayName("GET /cases/{id} - Should return 404 when case not found")
    void getCaseById_CaseNotFound_Returns404() throws Exception {
        // Given
        Long caseId = 999L;
        when(caseService.getCase(caseId)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/cases/{id}", caseId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(caseService).getCase(caseId);
    }

    @Test
    @DisplayName("POST /cases - Should handle service exception")
    void createCase_ServiceException_ReturnsError() throws Exception {
        // Given
        Case inputCase = TestDataFactory.createTestCase();
        when(caseService.createCase(any(Case.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCase)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(caseService).createCase(any(Case.class));
    }
}