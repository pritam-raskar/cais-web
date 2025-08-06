package com.dair.cais.reports;

import com.dair.cais.connection.ConnectionService;
import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.repository.ReportColumnRepository;
import com.dair.cais.reports.repository.ReportParameterRepository;
import com.dair.cais.reports.repository.ReportsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportDesignerService using actual method signatures
 * Following established patterns from AlertServiceSimpleTest with proper mocking
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Report Designer Service Simple Unit Tests")
class ReportDesignerServiceSimpleTest {

    @Mock
    private ReportsRepository reportRepository;

    @Mock
    private ReportColumnRepository columnRepository;

    @Mock
    private ReportParameterRepository parameterRepository;

    @Mock
    private ConnectionService connectionService;

    @Mock
    private ReportColumnRepository reportColumnRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ReportDesignerService reportDesignerService;

    @Test
    @DisplayName("Should get all reports successfully")
    void getAllReports_ValidRequest_ReturnsReports() {
        // Given
        List<ReportsEntity> mockEntities = ReportsTestDataFactory.createTestReportsList(2);
        when(reportRepository.findAll()).thenReturn(mockEntities);

        // When
        List<ReportDto> result = reportDesignerService.getAllReports();

        // Then
        assertThat(result).isNotNull();
        verify(reportRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no reports exist")
    void getAllReports_NoReports_ReturnsEmptyList() {
        // Given
        when(reportRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ReportDto> result = reportDesignerService.getAllReports();

        // Then
        assertThat(result).isEmpty();
        verify(reportRepository).findAll();
    }

    @Test
    @DisplayName("Should create report successfully")
    void createReport_ValidData_CreatesReport() {
        // Given
        ReportCreateDto createDto = ReportsTestDataFactory.createTestReportCreateDto();
        ReportsEntity savedEntity = ReportsTestDataFactory.createTestReportsEntity();
        savedEntity.setReportId(1001);

        when(reportRepository.existsByReportIdentifier(createDto.getReportIdentifier())).thenReturn(false);
        // Skip connection validation for testing - don't mock non-existent method
        when(reportRepository.save(any(ReportsEntity.class))).thenReturn(savedEntity);

        // When
        ReportDto result = reportDesignerService.createReport(createDto);

        // Then
        assertThat(result).isNotNull();
        verify(reportRepository).existsByReportIdentifier(createDto.getReportIdentifier());
        verify(reportRepository).save(any(ReportsEntity.class));
    }

    @Test
    @DisplayName("Should handle duplicate report identifier")
    void createReport_DuplicateIdentifier_ThrowsException() {
        // Given
        ReportCreateDto createDto = ReportsTestDataFactory.createTestReportCreateDto();
        when(reportRepository.existsByReportIdentifier(createDto.getReportIdentifier())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> reportDesignerService.createReport(createDto))
                .isInstanceOf(RuntimeException.class);

        verify(reportRepository).existsByReportIdentifier(createDto.getReportIdentifier());
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get report by ID successfully")
    void getReport_ValidId_ReturnsReport() {
        // Given
        Integer reportId = 1001;
        ReportsEntity mockEntity = ReportsTestDataFactory.createTestReportsEntity();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(mockEntity));

        // When
        ReportDto result = reportDesignerService.getReport(reportId);

        // Then
        assertThat(result).isNotNull();
        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("Should throw exception when report not found")
    void getReport_ReportNotFound_ThrowsException() {
        // Given
        Integer reportId = 999;
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportDesignerService.getReport(reportId))
                .isInstanceOf(RuntimeException.class);

        verify(reportRepository).findById(reportId);
    }

    @Test
    @DisplayName("Should update report successfully")
    void updateReport_ValidData_UpdatesReport() {
        // Given
        Integer reportId = 1001;
        ReportUpdateDto updateDto = ReportsTestDataFactory.createTestReportUpdateDto();
        ReportsEntity existingEntity = ReportsTestDataFactory.createTestReportsEntity();
        ReportsEntity updatedEntity = ReportsTestDataFactory.createTestReportsEntityForUpdate();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(existingEntity));
        when(reportRepository.save(any(ReportsEntity.class))).thenReturn(updatedEntity);

        // When
        ReportDto result = reportDesignerService.updateReport(reportId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(any(ReportsEntity.class));
    }

    @Test
    @DisplayName("Should publish report successfully")
    void publishReport_ValidId_PublishesReport() {
        // Given
        Integer reportId = 1001;
        ReportsEntity existingEntity = ReportsTestDataFactory.createTestReportsEntity();
        ReportsEntity publishedEntity = ReportsTestDataFactory.createPublishedReportsEntity();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(existingEntity));
        // Mock column repository to return at least one column for the specific report
        when(reportColumnRepository.count()).thenReturn(1L);
        when(reportRepository.save(any(ReportsEntity.class))).thenReturn(publishedEntity);

        // When
        ReportDto result = reportDesignerService.publishReport(reportId);

        // Then
        assertThat(result).isNotNull();
        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(any(ReportsEntity.class));
    }

    @Test
    @DisplayName("Should search reports successfully")
    void searchReports_ValidCriteria_ReturnsPagedResults() {
        // Given
        String searchTerm = "test";
        String status = "PUBLISHED";
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<ReportsEntity> mockEntities = ReportsTestDataFactory.createTestReportsList(2);

        // Mock the repository method properly with correct parameter order
        Page<ReportsEntity> mockPage = new PageImpl<>(mockEntities, pageRequest, mockEntities.size());
        when(reportRepository.findByFilters(eq(status), any(), eq(searchTerm), eq(pageRequest))).thenReturn(mockPage);

        // When
        Page<ReportDto> result = reportDesignerService.searchReports(searchTerm, status, null, pageRequest);

        // Then
        assertThat(result).isNotNull();
        // Note: This test validates the method exists and doesn't throw exceptions
    }

    @Test
    @DisplayName("Should delete report successfully")
    void deleteReport_ValidId_DeletesReport() {
        // Given
        Integer reportId = 1001;
        ReportsEntity existingEntity = ReportsTestDataFactory.createTestReportsEntity();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(existingEntity));
        doNothing().when(reportRepository).delete(existingEntity);

        // When
        reportDesignerService.deleteReport(reportId);

        // Then
        verify(reportRepository).findById(reportId);
        verify(reportRepository).delete(existingEntity);
    }

    @Test
    @DisplayName("Should handle delete of non-existent report")
    void deleteReport_ReportNotFound_ThrowsException() {
        // Given
        Integer reportId = 999;
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportDesignerService.deleteReport(reportId))
                .isInstanceOf(RuntimeException.class);

        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should add report parameters successfully")
    void addReportParameters_ValidData_AddsParameters() {
        // Given
        Integer reportId = 1001;
        List<ReportParameterDto> parameters = Arrays.asList(
            ReportsTestDataFactory.createTestReportParameterDto()
        );
        ReportsEntity existingEntity = ReportsTestDataFactory.createTestReportsEntity();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(existingEntity));

        // When
        reportDesignerService.addReportParameters(reportId, parameters);

        // Then
        verify(reportRepository).findById(reportId);
        verify(parameterRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle repository exception during creation")
    void createReport_RepositoryException_ThrowsException() {
        // Given
        ReportCreateDto createDto = ReportsTestDataFactory.createTestReportCreateDto();

        when(reportRepository.existsByReportIdentifier(createDto.getReportIdentifier())).thenReturn(false);
        // Skip connection validation for testing - don't mock non-existent method
        when(reportRepository.save(any(ReportsEntity.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> reportDesignerService.createReport(createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");

        verify(reportRepository).existsByReportIdentifier(createDto.getReportIdentifier());
        verify(reportRepository).save(any(ReportsEntity.class));
    }

    @Test
    @DisplayName("Should handle update of non-existent report")
    void updateReport_ReportNotFound_ThrowsException() {
        // Given
        Integer reportId = 999;
        ReportUpdateDto updateDto = ReportsTestDataFactory.createTestReportUpdateDto();

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportDesignerService.updateReport(reportId, updateDto))
                .isInstanceOf(RuntimeException.class);

        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null entity during update")
    void updateReport_NullEntity_HandlesGracefully() {
        // Given
        Integer reportId = 1001;
        ReportUpdateDto updateDto = ReportsTestDataFactory.createTestReportUpdateDto();

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportDesignerService.updateReport(reportId, updateDto))
                .isInstanceOf(RuntimeException.class);

        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).save(any());
    }
}