package com.dair.cais.workflow.service;

import com.dair.cais.workflow.WorkflowTestDataFactory;
import com.dair.cais.workflow.entity.WorkflowEntity;
import com.dair.cais.workflow.exception.WorkflowAlreadyExistsException;
import com.dair.cais.workflow.exception.WorkflowUpdateException;
import com.dair.cais.workflow.mapper.WorkflowMapper;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.repository.WorkflowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * Unit tests for WorkflowService focusing on core functionality
 * Using actual method signatures from WorkflowService and WorkflowRepository
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Workflow Service Unit Tests")
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WorkflowService workflowService;

    @BeforeEach
    void setUp() throws Exception {
        // Mock ObjectMapper to handle JSON validation - using lenient to avoid unnecessary stubbing issues
        JsonNode mockJsonNode = mock(JsonNode.class);
        JsonNode mockStepsNode = mock(JsonNode.class);
        JsonNode mockTransitionsNode = mock(JsonNode.class);
        
        lenient().when(objectMapper.readTree(anyString())).thenReturn(mockJsonNode);
        lenient().when(mockJsonNode.has("steps")).thenReturn(true);
        lenient().when(mockJsonNode.has("transitions")).thenReturn(true);
        lenient().when(mockJsonNode.get("steps")).thenReturn(mockStepsNode);
        lenient().when(mockJsonNode.get("transitions")).thenReturn(mockTransitionsNode);
        lenient().when(mockStepsNode.isArray()).thenReturn(true);
        lenient().when(mockTransitionsNode.isArray()).thenReturn(true);
    }

    @Test
    @DisplayName("Should retrieve all workflows successfully")
    void getAllWorkflows_ValidRequest_ReturnsWorkflows() {
        // Given
        List<WorkflowEntity> mockEntities = Arrays.asList(
            WorkflowTestDataFactory.createTestWorkflowEntity(),
            WorkflowTestDataFactory.createTestWorkflowEntity()
        );
        List<Workflow> mockWorkflows = Arrays.asList(
            WorkflowTestDataFactory.createTestWorkflow(),
            WorkflowTestDataFactory.createTestWorkflow()
        );

        when(workflowRepository.findAll()).thenReturn(mockEntities);
        when(workflowMapper.toModelList(mockEntities)).thenReturn(mockWorkflows);

        // When
        List<Workflow> result = workflowService.getAllWorkflows();

        // Then
        assertThat(result).hasSize(2);
        verify(workflowRepository).findAll();
        verify(workflowMapper).toModelList(mockEntities);
    }

    @Test
    @DisplayName("Should retrieve workflow by ID successfully")
    void getWorkflowById_ValidId_ReturnsWorkflow() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        WorkflowEntity mockEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        Workflow mockWorkflow = WorkflowTestDataFactory.createTestWorkflow();

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(mockEntity));
        when(workflowMapper.toModel(mockEntity)).thenReturn(mockWorkflow);

        // When
        Workflow result = workflowService.getWorkflowById(workflowId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWorkflowId()).isEqualTo(workflowId);
        verify(workflowRepository).findById(workflowId);
        verify(workflowMapper).toModel(mockEntity);
    }

    @Test
    @DisplayName("Should return null when workflow not found")
    void getWorkflowById_WorkflowNotFound_ReturnsNull() {
        // Given
        Long workflowId = 999L;
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When
        Workflow result = workflowService.getWorkflowById(workflowId);

        // Then
        assertThat(result).isNull();
        verify(workflowRepository).findById(workflowId);
        verify(workflowMapper, never()).toModel(any());
    }

    @Test
    @DisplayName("Should create workflow successfully")
    void createWorkflow_ValidData_CreatesWorkflow() {
        // Given
        Workflow inputWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        inputWorkflow.setCreatedDate(null); // Simulate missing created date
        WorkflowEntity mockEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        WorkflowEntity savedEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        Workflow savedWorkflow = WorkflowTestDataFactory.createTestWorkflow();

        when(workflowMapper.toEntity(inputWorkflow)).thenReturn(mockEntity);
        when(workflowRepository.save(mockEntity)).thenReturn(savedEntity);
        when(workflowMapper.toModel(savedEntity)).thenReturn(savedWorkflow);

        // When
        Workflow result = workflowService.createWorkflow(inputWorkflow);

        // Then
        assertThat(result).isNotNull();
        assertThat(inputWorkflow.getCreatedDate()).isNotNull();
        assertThat(inputWorkflow.getUpdatedDate()).isNotNull();
        verify(workflowMapper).toEntity(inputWorkflow);
        verify(workflowRepository).save(mockEntity);
        verify(workflowMapper).toModel(savedEntity);
    }

    @Test
    @DisplayName("Should throw exception when creating workflow with null data")
    void createWorkflow_NullWorkflow_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> workflowService.createWorkflow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow cannot be null");

        verify(workflowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when creating workflow with null name")
    void createWorkflow_NullWorkflowName_ThrowsException() {
        // Given
        Workflow workflow = WorkflowTestDataFactory.createTestWorkflow();
        workflow.setWorkflowName(null);

        // When & Then
        assertThatThrownBy(() -> workflowService.createWorkflow(workflow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow name cannot be null or empty");

        verify(workflowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve workflow details successfully")
    void getWorkflowDetails_ValidId_ReturnsDetails() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        WorkflowEntity mockEntity = WorkflowTestDataFactory.createTestWorkflowEntity();

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(mockEntity));

        // When
        List<WorkflowDetailDTO> result = workflowService.getWorkflowDetails(workflowId);

        // Then
        assertThat(result).hasSize(1);
        WorkflowDetailDTO detail = result.get(0);
        assertThat(detail.getWorkflowId()).isEqualTo(workflowId);
        assertThat(detail.getWorkflowName()).isEqualTo(mockEntity.getWorkflowName());
        verify(workflowRepository).findById(workflowId);
    }

    @Test
    @DisplayName("Should throw exception when getting details for non-existent workflow")
    void getWorkflowDetails_WorkflowNotFound_ThrowsException() {
        // Given
        Long workflowId = 999L;
        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workflowService.getWorkflowDetails(workflowId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workflow not found with id: " + workflowId);

        verify(workflowRepository).findById(workflowId);
    }

    @Test
    @DisplayName("Should update workflow successfully")
    void updateWorkflow_ValidData_UpdatesWorkflow() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        Workflow updateWorkflow = WorkflowTestDataFactory.createTestWorkflowForUpdate();
        WorkflowEntity existingEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        WorkflowEntity updatedEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        Workflow savedWorkflow = WorkflowTestDataFactory.createTestWorkflow();

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(existingEntity));
        when(workflowRepository.existsByWorkflowName(updateWorkflow.getWorkflowName())).thenReturn(false);
        when(workflowMapper.toEntity(updateWorkflow)).thenReturn(updatedEntity);
        when(workflowRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(workflowMapper.toModel(updatedEntity)).thenReturn(savedWorkflow);

        // When
        Workflow result = workflowService.updateWorkflow(workflowId, updateWorkflow);

        // Then
        assertThat(result).isNotNull();
        verify(workflowRepository).findById(workflowId);
        verify(workflowMapper).toEntity(updateWorkflow);
        verify(workflowRepository).save(updatedEntity);
        verify(workflowMapper).toModel(updatedEntity);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent workflow")
    void updateWorkflow_WorkflowNotFound_ThrowsException() {
        // Given
        Long workflowId = 999L;
        Workflow updateWorkflow = WorkflowTestDataFactory.createTestWorkflow();

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workflowService.updateWorkflow(workflowId, updateWorkflow))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workflow not found with id: " + workflowId);

        verify(workflowRepository).findById(workflowId);
        verify(workflowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when updating workflow with duplicate name")
    void updateWorkflow_DuplicateName_ThrowsException() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        Workflow updateWorkflow = WorkflowTestDataFactory.createTestWorkflow();
        updateWorkflow.setWorkflowName("Existing Workflow Name");
        WorkflowEntity existingEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        existingEntity.setWorkflowName("Different Name");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(existingEntity));
        when(workflowRepository.existsByWorkflowName(updateWorkflow.getWorkflowName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> workflowService.updateWorkflow(workflowId, updateWorkflow))
                .isInstanceOf(WorkflowAlreadyExistsException.class)
                .hasMessageContaining("Workflow with name 'Existing Workflow Name' already exists");

        verify(workflowRepository).findById(workflowId);
        verify(workflowRepository).existsByWorkflowName(updateWorkflow.getWorkflowName());
        verify(workflowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete workflow successfully")
    void deleteWorkflow_ValidId_DeletesWorkflow() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        when(workflowRepository.existsById(workflowId)).thenReturn(true);

        // When
        workflowService.deleteWorkflow(workflowId);

        // Then
        verify(workflowRepository).existsById(workflowId);
        verify(workflowRepository).deleteById(workflowId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent workflow")
    void deleteWorkflow_WorkflowNotFound_ThrowsException() {
        // Given
        Long workflowId = 999L;
        when(workflowRepository.existsById(workflowId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> workflowService.deleteWorkflow(workflowId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workflow not found with id: " + workflowId);

        verify(workflowRepository).existsById(workflowId);
        verify(workflowRepository, never()).deleteById(workflowId);
    }

    @Test
    @DisplayName("Should check workflow existence successfully")
    void workflowExists_ValidId_ReturnsTrue() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        when(workflowRepository.existsById(workflowId)).thenReturn(true);

        // When
        boolean result = workflowService.workflowExists(workflowId);

        // Then
        assertThat(result).isTrue();
        verify(workflowRepository).existsById(workflowId);
    }

    @Test
    @DisplayName("Should get workflow count successfully")
    void getWorkflowCount_ValidRequest_ReturnsCount() {
        // Given
        long expectedCount = 5L;
        when(workflowRepository.count()).thenReturn(expectedCount);

        // When
        long result = workflowService.getWorkflowCount();

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(workflowRepository).count();
    }

    @Test
    @DisplayName("Should get workflow UI config successfully")
    void getWorkflowUiConfig_ValidId_ReturnsUiConfig() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        WorkflowEntity mockEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        String expectedUiConfig = "{\"version\":\"1.0\",\"layout\":\"vertical\"}";
        mockEntity.setUiConfig(expectedUiConfig);

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(mockEntity));

        // When
        String result = workflowService.getWorkflowUiConfig(workflowId);

        // Then
        assertThat(result).isEqualTo(expectedUiConfig);
        verify(workflowRepository).findById(workflowId);
    }

    @Test
    @DisplayName("Should return empty JSON when UI config is null")
    void getWorkflowUiConfig_NullUiConfig_ReturnsEmptyJson() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        WorkflowEntity mockEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        mockEntity.setUiConfig(null);

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(mockEntity));

        // When
        String result = workflowService.getWorkflowUiConfig(workflowId);

        // Then
        assertThat(result).isEqualTo("{}");
        verify(workflowRepository).findById(workflowId);
    }

    @Test
    @DisplayName("Should update workflow UI config successfully")
    void updateWorkflowUiConfig_ValidData_UpdatesUiConfig() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        String newUiConfig = "{\"version\":\"2.0\",\"layout\":\"horizontal\"}";
        WorkflowEntity existingEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        WorkflowEntity updatedEntity = WorkflowTestDataFactory.createTestWorkflowEntity();
        updatedEntity.setUiConfig(newUiConfig);
        Workflow savedWorkflow = WorkflowTestDataFactory.createTestWorkflow();

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(existingEntity));
        when(workflowRepository.save(any(WorkflowEntity.class))).thenReturn(updatedEntity);
        when(workflowMapper.toModel(updatedEntity)).thenReturn(savedWorkflow);

        // When
        Workflow result = workflowService.updateWorkflowUiConfig(workflowId, newUiConfig);

        // Then
        assertThat(result).isNotNull();
        verify(workflowRepository).findById(workflowId);
        verify(workflowRepository).save(any(WorkflowEntity.class));
        verify(workflowMapper).toModel(updatedEntity);
    }

    @Test
    @DisplayName("Should throw exception when updating UI config for non-existent workflow")
    void updateWorkflowUiConfig_WorkflowNotFound_ThrowsException() {
        // Given
        Long workflowId = 999L;
        String newUiConfig = "{\"version\":\"2.0\"}";

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> workflowService.updateWorkflowUiConfig(workflowId, newUiConfig))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Workflow not found with id: " + workflowId);

        verify(workflowRepository).findById(workflowId);
        verify(workflowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository exception during UI config update")
    void updateWorkflowUiConfig_RepositoryException_ThrowsWorkflowUpdateException() {
        // Given
        Long workflowId = WorkflowTestDataFactory.DEFAULT_WORKFLOW_ID;
        String newUiConfig = "{\"version\":\"2.0\"}";
        WorkflowEntity existingEntity = WorkflowTestDataFactory.createTestWorkflowEntity();

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(existingEntity));
        when(workflowRepository.save(any(WorkflowEntity.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> workflowService.updateWorkflowUiConfig(workflowId, newUiConfig))
                .isInstanceOf(WorkflowUpdateException.class)
                .hasMessageContaining("Failed to update workflow UI configuration");

        verify(workflowRepository).findById(workflowId);
        verify(workflowRepository).save(any(WorkflowEntity.class));
    }

    @Test
    @DisplayName("Should validate workflow with created by field")
    void createWorkflow_NullCreatedBy_ThrowsException() {
        // Given
        Workflow workflow = WorkflowTestDataFactory.createTestWorkflow();
        workflow.setCreatedBy(null);

        // When & Then
        assertThatThrownBy(() -> workflowService.createWorkflow(workflow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Created by cannot be null or empty");

        verify(workflowRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate workflow with empty workflow name")
    void createWorkflow_EmptyWorkflowName_ThrowsException() {
        // Given
        Workflow workflow = WorkflowTestDataFactory.createTestWorkflow();
        workflow.setWorkflowName("   ");

        // When & Then
        assertThatThrownBy(() -> workflowService.createWorkflow(workflow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Workflow name cannot be null or empty");

        verify(workflowRepository, never()).save(any());
    }
}