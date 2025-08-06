package com.dair.cais.alerts.service;

import com.dair.cais.alert.Alert;
import com.dair.cais.alert.AlertRepository;
import com.dair.cais.alert.AlertService;
import com.dair.cais.alert.AlertEntity;
import com.dair.cais.alert.AlertMapper;
import com.dair.cais.alert.rdbms.RdbmsAlertMapper;
import com.dair.cais.alert.rdbms.RdbmsAlertRepository;
import com.dair.cais.alert.filter.MongoQueryBuilder;
import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.dair.cais.access.user.UserRepository;
import com.dair.cais.audit.AuditTrailService;
import com.dair.cais.organization.OrganizationFamilyRepository;
import com.dair.cais.sla.StepSlaService;
import com.dair.cais.steps.StepRepository;
import com.dair.cais.steps.StepStatusRepository;
import com.dair.cais.workflow.entity.WorkflowStepEntity;
import com.dair.cais.workflow.repository.WorkflowStepRepository;
import com.dair.cais.workflow.repository.WorkflowTransitionRepository;
import com.dair.cais.workflow.engine.WorkflowRuleEngine;
import com.dair.cais.steps.permission.StepPermissionService;
import com.dair.cais.alerts.AlertTestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.FindAndModifyOptions;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Fixed unit tests for AlertService with complete @Mock dependency setup
 * All AlertService dependencies properly mocked to prevent NullPointer exceptions
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Alert Service Simple Unit Tests - Fixed")
class AlertServiceSimpleTest {

    // Core Alert dependencies
    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertMapper alertMapper;

    @Mock
    private RdbmsAlertMapper rdbmsAlertMapper;

    @Mock
    private RdbmsAlertRepository rdbmsAlertRepository;

    // MongoDB dependencies
    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private MongoQueryBuilder mongoQueryBuilder;

    // Step and workflow dependencies
    @Mock
    private StepRepository stepsRepository;

    @Mock
    private StepStatusRepository stepStatusRepository;

    @Mock
    private WorkflowStepRepository workflowStepRepository;

    @Mock
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Mock
    private WorkflowRuleEngine workflowRuleEngine;

    @Mock
    private StepPermissionService stepPermissionService;

    // User and permission dependencies
    @Mock
    private UserPermissionService userPermissionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationFamilyRepository orgFamilyRepository;

    // Service dependencies
    @Mock
    private AuditTrailService auditTrailService;

    @Mock
    private StepSlaService stepSlaService;

    // Utility dependencies
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AlertService alertService;

    @Test
    @DisplayName("Should retrieve alert by ID successfully")
    void getAlertOnId_ValidId_ReturnsAlert() {
        // Given
        String alertId = "TEST_ALERT_123";
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();
        Alert mockAlert = AlertTestDataFactory.createTestAlert();

        when(alertRepository.getAlertOnId(alertId)).thenReturn(mockEntity);
        when(alertMapper.toModel(mockEntity)).thenReturn(mockAlert);

        // When
        Alert result = alertService.getAlertOnId(alertId);

        // Then
        assertThat(result).isNotNull();
        verify(alertRepository).getAlertOnId(alertId);
        verify(alertMapper).toModel(mockEntity);
    }

    @Test
    @DisplayName("Should handle null result when alert not found")
    void getAlertOnId_AlertNotFound_ReturnsNull() {
        // Given
        String alertId = "NON_EXISTENT_ALERT";
        when(alertRepository.getAlertOnId(alertId)).thenReturn(null);

        // When
        Alert result = alertService.getAlertOnId(alertId);

        // Then
        assertThat(result).isNull();
        verify(alertRepository).getAlertOnId(alertId);
        verify(alertMapper, never()).toModel(any());
    }

    @Test
    @DisplayName("Should create alert successfully")
    void createAlert_ValidData_CreatesAlert() {
        // Given
        Alert inputAlert = AlertTestDataFactory.createTestAlert();
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();
        AlertEntity savedEntity = AlertTestDataFactory.createTestAlertEntity();
        Alert savedAlert = AlertTestDataFactory.createTestAlert();

        // Mock MongoDB existence check
        when(mongoTemplate.exists(any(Query.class), anyString())).thenReturn(false);
        when(alertMapper.toEntity(inputAlert)).thenReturn(mockEntity);
        when(alertRepository.createUpsertAlert(mockEntity)).thenReturn(savedEntity);
        when(alertMapper.toModel(savedEntity)).thenReturn(savedAlert);

        // When
        Alert result = alertService.createAlert(inputAlert);

        // Then
        assertThat(result).isNotNull();
        verify(mongoTemplate).exists(any(Query.class), anyString());
        verify(alertMapper).toEntity(inputAlert);
        verify(alertRepository).createUpsertAlert(mockEntity);
        verify(alertMapper).toModel(savedEntity);
    }

    @Test
    @DisplayName("Should update alert total score successfully")
    void updateTotalScore_ValidData_UpdatesScore() {
        // Given
        String alertId = "TEST_ALERT_123";
        int newScore = 95;
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();
        AlertEntity updatedEntity = AlertTestDataFactory.createTestAlertEntity();
        updatedEntity.setTotalScore(newScore);
        Alert updatedAlert = AlertTestDataFactory.createTestAlert();
        updatedAlert.setTotalScore(newScore);

        // Mock all dependencies needed for updateTotalScore
        when(rdbmsAlertRepository.findByAlertId(alertId)).thenReturn(java.util.Optional.of(new com.dair.cais.alert.rdbms.RdbmsAlertEntity()));
        when(alertRepository.findByAlertId(alertId)).thenReturn(mockEntity);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(AlertEntity.class), anyString())).thenReturn(updatedEntity);
        when(alertMapper.toModel(updatedEntity)).thenReturn(updatedAlert);

        // When
        Alert result = alertService.updateTotalScore(alertId, newScore);

        // Then
        assertThat(result).isNotNull();
        verify(rdbmsAlertRepository).findByAlertId(alertId);
        verify(alertRepository).findByAlertId(alertId);
        verify(alertRepository).createUpsertAlert(any(AlertEntity.class));
        verify(alertMapper).toModel(updatedEntity);
    }

    @Test
    @DisplayName("Should update alert owner successfully")
    void updateOwnerId_ValidData_UpdatesOwner() {
        // Given
        String alertId = "TEST_ALERT_123";
        String newOwnerId = "NEW_OWNER";
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();
        AlertEntity updatedEntity = AlertTestDataFactory.createTestAlertEntity();
        updatedEntity.setOwnerId(newOwnerId);
        Alert updatedAlert = AlertTestDataFactory.createTestAlert();
        updatedAlert.setOwnerId(newOwnerId);

        // Mock all dependencies needed for updateOwnerId
        when(rdbmsAlertRepository.findByAlertId(alertId)).thenReturn(java.util.Optional.of(new com.dair.cais.alert.rdbms.RdbmsAlertEntity()));
        when(alertRepository.findByAlertId(alertId)).thenReturn(mockEntity);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(AlertEntity.class), anyString())).thenReturn(updatedEntity);
        when(alertMapper.toModel(updatedEntity)).thenReturn(updatedAlert);

        // When
        Alert result = alertService.updateOwnerId(alertId, newOwnerId);

        // Then
        assertThat(result).isNotNull();
        verify(rdbmsAlertRepository).findByAlertId(alertId);
        verify(alertRepository).findByAlertId(alertId);
        verify(alertRepository).createUpsertAlert(any(AlertEntity.class));
        verify(alertMapper).toModel(updatedEntity);
    }

    @Test
    @DisplayName("Should get alert by ID and type successfully")
    void getAlertById_ValidIdAndType_ReturnsAlert() {
        // Given
        String alertId = "TEST_ALERT_123";
        String alertType = "account-review";
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();
        Alert mockAlert = AlertTestDataFactory.createTestAlert();

        when(alertRepository.getAlertById(alertId, alertType)).thenReturn(mockEntity);
        when(alertMapper.toModel(mockEntity)).thenReturn(mockAlert);

        // When
        Alert result = alertService.getAlertById(alertId, alertType);

        // Then
        assertThat(result).isNotNull();
        verify(alertRepository).getAlertById(alertId, alertType);
        verify(alertMapper).toModel(mockEntity);
    }

    @Test
    @DisplayName("Should get all active alerts successfully")
    void getAllActiveAlerts_ValidRequest_ReturnsAlerts() {
        // Given
        AlertEntity mockEntity1 = AlertTestDataFactory.createTestAlertEntity();
        AlertEntity mockEntity2 = AlertTestDataFactory.createTestAlertEntity();
        Alert mockAlert1 = AlertTestDataFactory.createTestAlert();
        Alert mockAlert2 = AlertTestDataFactory.createTestAlert();

        when(alertRepository.findAllActiveAndNonDeletedAlerts())
                .thenReturn(java.util.Arrays.asList(mockEntity1, mockEntity2));
        when(alertMapper.toModel(mockEntity1)).thenReturn(mockAlert1);
        when(alertMapper.toModel(mockEntity2)).thenReturn(mockAlert2);

        // When
        java.util.List<Alert> result = alertService.getAllActiveAlerts();

        // Then
        assertThat(result).hasSize(2);
        verify(alertRepository).findAllActiveAndNonDeletedAlerts();
        verify(alertMapper, times(2)).toModel(any(AlertEntity.class));
    }

    @Test
    @DisplayName("Should delete alert successfully")
    void deleteAlertById_ValidId_DeletesAlert() {
        // Given
        String alertId = "TEST_ALERT_123";
        String alertType = "account-review";
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();

        when(alertRepository.deleteAlertById(alertId, alertType)).thenReturn(mockEntity);

        // When
        alertService.deleteAlertById(alertId, alertType);

        // Then
        verify(alertRepository).deleteAlertById(alertId, alertType);
    }

    @Test
    @DisplayName("Should handle repository exception during creation")
    void createAlert_RepositoryException_ThrowsException() {
        // Given
        Alert inputAlert = AlertTestDataFactory.createTestAlert();
        AlertEntity mockEntity = AlertTestDataFactory.createTestAlertEntity();

        when(alertMapper.toEntity(inputAlert)).thenReturn(mockEntity);
        when(alertRepository.createUpsertAlert(mockEntity))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> alertService.createAlert(inputAlert))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");

        verify(alertMapper).toEntity(inputAlert);
        verify(alertRepository).createUpsertAlert(mockEntity);
    }

    @Test
    @DisplayName("Should handle null entity during update")
    void updateTotalScore_NullEntity_HandlesGracefully() {
        // Given
        String alertId = "NON_EXISTENT_ALERT";
        int newScore = 95;

        when(alertRepository.findByAlertId(alertId)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> alertService.updateTotalScore(alertId, newScore))
                .isInstanceOf(RuntimeException.class);

        verify(alertRepository).findByAlertId(alertId);
        verify(alertRepository, never()).createUpsertAlert(any(AlertEntity.class));
    }
}