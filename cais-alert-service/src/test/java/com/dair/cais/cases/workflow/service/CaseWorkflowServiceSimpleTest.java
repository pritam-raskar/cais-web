package com.dair.cais.cases.workflow.service;

import com.dair.cais.cases.dto.BulkStepChangeRequest;
import com.dair.cais.cases.dto.BulkStepChangeResponse;
import com.dair.cais.cases.entity.CaseEntity;
import com.dair.cais.cases.repository.CaseRepository;
import com.dair.cais.steps.StepRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Simplified unit tests for CaseWorkflowService focusing on core functionality
 * These tests demonstrate the testing approach without complex entity relationships
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Case Workflow Service - Simple Tests")
class CaseWorkflowServiceSimpleTest {

    @Mock
    private CaseRepository caseRepository;
    
    @Mock
    private StepRepository stepRepository;

    @InjectMocks
    private CaseWorkflowService caseWorkflowService;

    private CaseEntity testCaseEntity;

    @BeforeEach
    void setUp() {
        testCaseEntity = new CaseEntity();
        testCaseEntity.setCaseId(1L);
        testCaseEntity.setTitle("Test Case");
        testCaseEntity.setCaseType("account-review");
        testCaseEntity.setWorkflowId(104L);
        testCaseEntity.setCurrentStepId(70L);
        testCaseEntity.setCurrentStepName("Ready");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when case not found")
    void getAvailableSteps_CaseNotFound_ThrowsEntityNotFoundException() {
        // Given
        Long nonExistentCaseId = 999L;
        when(caseRepository.findById(nonExistentCaseId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(nonExistentCaseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case not found with ID: 999");
        
        verify(caseRepository).findById(nonExistentCaseId);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no workflow assigned")
    void getAvailableSteps_NoWorkflowAssigned_ThrowsIllegalStateException() {
        // Given
        Long caseId = 1L;
        testCaseEntity.setWorkflowId(null); // No workflow assigned
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(caseId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No workflow assigned to this case");
        
        verify(caseRepository).findById(caseId);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no current step")
    void getAvailableSteps_NoCurrentStep_ThrowsIllegalStateException() {
        // Given
        Long caseId = 1L;
        testCaseEntity.setCurrentStepId(null); // No current step
        when(caseRepository.findById(caseId)).thenReturn(Optional.of(testCaseEntity));

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(caseId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Case does not have a current step");
        
        verify(caseRepository).findById(caseId);
    }

    @Test
    @DisplayName("Should handle bulk step change with invalid step ID")
    void changeStepBulk_InvalidStepId_ReturnsValidationErrors() {
        // Given
        Long invalidStepId = 999L;
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(Arrays.asList(1L, 2L));
        request.setStepId(invalidStepId);
        request.setReason("Test bulk change");
        
        when(stepRepository.existsById(invalidStepId)).thenReturn(false);

        // When
        BulkStepChangeResponse result = caseWorkflowService.changeStepBulk(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(2);
        assertThat(result.getValidationErrors()).hasSize(1);
        assertThat(result.getValidationErrors().get(0)).contains("Step ID 999 does not exist");
        
        verify(stepRepository).existsById(invalidStepId);
        // Should not attempt to process individual cases when step doesn't exist
        verify(caseRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should handle bulk step change with empty case list")
    void changeStepBulk_EmptyCaseList_ReturnsZeroResults() {
        // Given
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(Arrays.asList()); // Empty list
        request.setStepId(70L);
        request.setReason("Test bulk change");
        
        when(stepRepository.existsById(70L)).thenReturn(true);

        // When
        BulkStepChangeResponse result = caseWorkflowService.changeStepBulk(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRequested()).isEqualTo(0);
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(0);
        assertThat(result.getValidationErrors()).isEmpty();
        
        verify(stepRepository).existsById(70L);
    }

    @Test
    @DisplayName("Should handle case not found in bulk operation")
    void changeStepBulk_CaseNotFound_RecordsFailure() {
        // Given
        Long nonExistentCaseId = 999L;
        BulkStepChangeRequest request = new BulkStepChangeRequest();
        request.setCaseIds(Arrays.asList(nonExistentCaseId));
        request.setStepId(70L);
        request.setReason("Test bulk change");
        
        when(stepRepository.existsById(70L)).thenReturn(true);
        when(caseRepository.findById(nonExistentCaseId)).thenReturn(Optional.empty());

        // When
        BulkStepChangeResponse result = caseWorkflowService.changeStepBulk(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRequested()).isEqualTo(1);
        assertThat(result.getSuccessCount()).isEqualTo(0);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getFailedCases()).containsKey(nonExistentCaseId);
        assertThat(result.getFailedCases().get(nonExistentCaseId)).contains("Case not found");
        
        verify(stepRepository).existsById(70L);
        verify(caseRepository).findById(nonExistentCaseId);
    }

    @Test
    @DisplayName("Should validate constructor injection")
    void constructor_AllDependenciesInjected_ServiceCreated() {
        // When & Then
        assertThat(caseWorkflowService).isNotNull();
        
        // Verify that all required dependencies are injected
        // This is done by verifying that methods can be called without NullPointerException
        // for the basic validation cases
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void methods_WithNullInputs_HandleGracefully() {
        // Test null case ID - service currently throws EntityNotFoundException 
        // which is acceptable behavior for null input
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(null))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case not found with ID: null");
    }

    @Test
    @DisplayName("Should properly format error messages")
    void errorHandling_ProvidesDescriptiveMessages() {
        // Given
        Long caseId = 123L;
        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> caseWorkflowService.getAvailableSteps(caseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Case not found with ID: 123");
    }
}