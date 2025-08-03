package com.dair.cais.cases.workflow.service;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrailService;
import com.dair.cais.cases.Case;
import com.dair.cais.cases.dto.BulkStepChangeRequest;
import com.dair.cais.cases.dto.BulkStepChangeResponse;
import com.dair.cais.cases.entity.CaseEntity;
import com.dair.cais.cases.entity.CaseTypeEntity;
import com.dair.cais.cases.mapper.CaseMapper;
import com.dair.cais.cases.repository.CaseRepository;
import com.dair.cais.cases.repository.CaseTypeRepository;
import com.dair.cais.steps.Step;
import com.dair.cais.steps.StepRepository;
import com.dair.cais.workflow.dto.StepTransitionDTO;
import com.dair.cais.workflow.entity.WorkflowEntity;
import com.dair.cais.workflow.entity.WorkflowStepEntity;
import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
import com.dair.cais.workflow.exception.WorkflowValidationException;
import com.dair.cais.workflow.repository.WorkflowRepository;
import com.dair.cais.workflow.repository.WorkflowStepRepository;
import com.dair.cais.workflow.repository.WorkflowTransitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CaseWorkflowService
 * Tests service layer logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Case Workflow Service Tests")
class CaseWorkflowServiceTest {

    @Mock
    private CaseRepository caseRepository;
    
    @Mock
    private CaseTypeRepository caseTypeRepository;
    
    @Mock
    private CaseMapper caseMapper;
    
    @Mock
    private WorkflowRepository workflowRepository;
    
    @Mock
    private WorkflowStepRepository workflowStepRepository;
    
    @Mock
    private WorkflowTransitionRepository workflowTransitionRepository;
    
    @Mock
    private StepRepository stepRepository;
    
    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private CaseWorkflowService caseWorkflowService;

    private CaseEntity testCaseEntity;
    private Case testCase;
    private Step testStep;
    private WorkflowEntity testWorkflow;
    private WorkflowStepEntity testWorkflowStep;
    private WorkflowTransitionEntity testTransition;
    private CaseTypeEntity testCaseType;

    @BeforeEach
    void setUp() {
        // Setup test entities
        testCaseEntity = new CaseEntity();
        testCaseEntity.setCaseId(1L);
        testCaseEntity.setTitle("Test Case");
        testCaseEntity.setCaseType("account-review");
        testCaseEntity.setWorkflowId(104L);
        testCaseEntity.setCurrentStepId(70L);
        testCaseEntity.setCurrentStepName("Ready");

        testCase = new Case();
        testCase.setCaseId(1L);
        testCase.setTitle("Test Case");
        testCase.setCaseType("account-review");
        testCase.setWorkflowId(104L);
        testCase.setCurrentStepId(70L);
        testCase.setCurrentStepName("Ready");

        testStep = new Step();
        testStep.setStepId(70L);
        testStep.setStepName("Ready");
        testStep.setStepStatusId(1);

        testWorkflow = new WorkflowEntity();
        testWorkflow.setWorkflowId(104L);
        testWorkflow.setWorkflowName("Account Review Workflow");

        testWorkflowStep = new WorkflowStepEntity();
        testWorkflowStep.setWorkflowStepId(1L);
        testWorkflowStep.setLabel("Ready");
        testWorkflowStep.setIsDefault(true);
        testWorkflowStep.setStep(testStep); // Set the step relationship

        testTransition = new WorkflowTransitionEntity();
        testTransition.setTransitionId(1L);
        testTransition.setTargetStep(testWorkflowStep); // Set the target step relationship

        testCaseType = new CaseTypeEntity();
        testCaseType.setTypeId(1L);
        testCaseType.setName("account-review");
        testCaseType.setWorkflowId(104L);
    }

    @Test
    @DisplayName("Should get available steps for case with current step")
    void getAvailableSteps_WithCurrentStep_ReturnsAvailableSteps() {
        // Given
        Long caseId = 1L;
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(104L, 70L))
                .thenReturn(Arrays.asList(testTransition));
        when(stepRepository.findAllById(anyList())).thenReturn(Arrays.asList(testStep));

        // When
        List<Step> result = caseWorkflowService.getAvailableSteps(caseId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStepId()).isEqualTo(70L);
        verify(caseRepository).findById(caseId);
        verify(workflowTransitionRepository).findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(104L, 70L);
    }

    @Test
    @DisplayName("Should throw exception when case not found")
    void getAvailableSteps_CaseNotFound_ThrowsException() {
        // Given
        Long caseId = 999L;
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(caseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case not found with ID: 999");
    }

    @Test
    @DisplayName("Should throw exception when workflow not assigned")
    void getAvailableSteps_NoWorkflowAssigned_ThrowsException() {
        // Given
        Long caseId = 1L;
        testCaseEntity.setWorkflowId(null);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(caseId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No workflow assigned to this case");
    }

    @Test
    @DisplayName("Should throw exception when current step not set")
    void getAvailableSteps_NoCurrentStep_ThrowsException() {
        // Given
        Long caseId = 1L;
        testCaseEntity.setCurrentStepId(null);
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(caseId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Case does not have a current step");
    }

    @Test
    @DisplayName("Should get available steps via case type")
    void getAvailableStepsViaCaseType_ValidCaseType_ReturnsSteps() {
        // Given
        Long caseId = 1L;
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(caseTypeRepository.findByName("account-review")).thenReturn(Optional.of(testCaseType));
        when(workflowStepRepository.findByWorkflowWorkflowId(104L)).thenReturn(Arrays.asList(testWorkflowStep));
        when(stepRepository.findAllById(anyList())).thenReturn(Arrays.asList(testStep));

        // When
        List<Step> result = caseWorkflowService.getAvailableStepsViaCaseType(caseId);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(caseTypeRepository).findByName("account-review");
        verify(workflowStepRepository).findByWorkflowWorkflowId(104L);
    }

    @Test
    @DisplayName("Should throw exception when case type not found")
    void getAvailableStepsViaCaseType_CaseTypeNotFound_ThrowsException() {
        // Given
        Long caseId = 1L;
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(caseTypeRepository.findByName("account-review")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableStepsViaCaseType(caseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case type not found with name: account-review");
    }

    @Test
    @DisplayName("Should assign workflow to case")
    void assignWorkflow_ValidWorkflow_AssignsSuccessfully() {
        // Given
        Long caseId = 1L;
        Long workflowId = 104L;
        String userId = "testUser";
        
        testCaseEntity.setWorkflowId(null); // No existing workflow
        testCaseEntity.setCurrentStepId(null);
        
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(testWorkflow));
        when(workflowStepRepository.findByWorkflowWorkflowId(workflowId)).thenReturn(Arrays.asList(testWorkflowStep));
        when(stepRepository.findById(anyLong())).thenReturn(Optional.of(testStep));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(testCaseEntity);
        when(caseMapper.toModel(any(CaseEntity.class))).thenReturn(testCase);

        // When
        Case result = caseWorkflowService.assignWorkflow(caseId, workflowId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWorkflowId()).isEqualTo(workflowId);
        verify(caseRepository).save(any(CaseEntity.class));
        verify(workflowRepository).findById(workflowId);
    }

    @Test
    @DisplayName("Should throw exception when workflow not found")
    void assignWorkflow_WorkflowNotFound_ThrowsException() {
        // Given
        Long caseId = 1L;
        Long workflowId = 999L;
        String userId = "testUser";
        
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.assignWorkflow(caseId, workflowId, userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workflow not found with ID: 999");
    }

    @Test
    @DisplayName("Should transition case to new step")
    void transitionCase_ValidTransition_TransitionsSuccessfully() {
        // Given
        Long caseId = 1L;
        Long stepId = 71L;
        String reason = "Test transition";
        String userId = "testUser";
        
        Step targetStep = new Step();
        targetStep.setStepId(71L);
        targetStep.setStepName("Under Review");
        
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(104L, 70L))
                .thenReturn(Arrays.asList(testTransition));
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(targetStep));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(testCaseEntity);
        when(caseMapper.toModel(any(CaseEntity.class))).thenReturn(testCase);

        // When
        Case result = caseWorkflowService.transitionCase(caseId, stepId, reason, userId);

        // Then
        assertThat(result).isNotNull();
        verify(caseRepository).save(any(CaseEntity.class));
        verify(stepRepository).findById(stepId);
    }

    @Test
    @DisplayName("Should perform bulk step change")
    void changeStepBulk_ValidRequest_ProcessesAllCases() {
        // Given
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(Arrays.asList(1L, 2L));
        request.setStepId(71L);
        request.setReason("Bulk test");
        
        when(stepRepository.existsById(71L)).thenReturn(true);
        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCaseEntity));
        when(caseRepository.findById(2L)).thenReturn(Optional.of(testCaseEntity));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(testTransition));
        when(stepRepository.findById(anyLong())).thenReturn(Optional.of(testStep));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(testCaseEntity);
        when(caseMapper.toModel(any(CaseEntity.class))).thenReturn(testCase);

        // When
        BulkStepChangeResponse result = caseWorkflowService.changeStepBulk(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle bulk step change with validation errors")
    void changeStepBulk_InvalidStep_ReturnsValidationErrors() {
        // Given
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(Arrays.asList(1L));
        request.setStepId(999L);
        request.setReason("Test");
        
        when(stepRepository.existsById(999L)).thenReturn(false);

        // When
        BulkStepChangeResponse result = caseWorkflowService.changeStepBulk(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRequested()).isEqualTo(1);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getValidationErrors()).hasSize(1);
        assertThat(result.getValidationErrors().get(0)).contains("Step ID 999 does not exist");
    }

    @Test
    @DisplayName("Should change step with audit logging")
    void changeStepWithAudit_ValidRequest_LogsAudit() {
        // Given
        Long caseId = 1L;
        Long stepId = 71L;
        AuditLogRequest auditRequest = new AuditLogRequest();
        auditRequest.setUserId(123L);
        auditRequest.setUserRole("ANALYST");
        auditRequest.setActionId(1);
        auditRequest.setDescription("Test step change");
        auditRequest.setCategory("STEP_CHANGE");
        
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(testTransition));
        when(stepRepository.findById(stepId)).thenReturn(Optional.of(testStep));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(testCaseEntity);
        when(caseMapper.toModel(any(CaseEntity.class))).thenReturn(testCase);

        // When
        Case result = caseWorkflowService.changeStepWithAudit(caseId, stepId, auditRequest);

        // Then
        assertThat(result).isNotNull();
        verify(auditTrailService).logAction(
                eq(123L), eq("ANALYST"), eq(1), eq("Test step change"), eq("STEP_CHANGE"),
                eq("Case"), eq("1"), anyString(), eq("71")
        );
    }

    @Test
    @DisplayName("Should get step transitions for case")
    void getCaseStepTransitions_ValidCase_ReturnsTransitions() {
        // Given
        Long caseId = 1L;
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(104L, 70L))
                .thenReturn(Arrays.asList(testTransition));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndTargetStepWorkflowStepId(104L, 70L))
                .thenReturn(Arrays.asList(testTransition));

        // When
        StepTransitionDTO result = caseWorkflowService.getCaseStepTransitions(caseId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNextSteps()).isNotNull();
        assertThat(result.getBackSteps()).isNotNull();
    }

    @Test
    @DisplayName("Should validate invalid transition")
    void transitionCase_InvalidTransition_ThrowsException() {
        // Given
        Long caseId = 1L;
        Long stepId = 999L; // Invalid step
        String reason = "Test transition";
        String userId = "testUser";
        
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));
        when(workflowTransitionRepository.findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(104L, 70L))
                .thenReturn(Arrays.asList()); // No valid transitions

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.transitionCase(caseId, stepId, reason, userId))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("Invalid transition between steps in workflow");
    }
}