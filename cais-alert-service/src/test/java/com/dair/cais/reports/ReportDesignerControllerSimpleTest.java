package com.dair.cais.reports;

import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.mapper.ReportSummaryMapper;
import com.dair.cais.reports.repository.ReportsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ReportDesignerController using actual endpoints
 * Following established patterns from CaseControllerSimpleTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Report Designer Controller Simple Unit Tests")
class ReportDesignerControllerSimpleTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportDesignerService reportService;

    @Mock
    private ReportMetadataService metadataService;

    @Mock
    private ReportsRepository reportRepository;

    @Mock
    private ReportSummaryMapper reportSummaryMapper;

    @InjectMocks
    private ReportDesignerController reportDesignerController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(reportDesignerController).build();
    }

    @Test
    @DisplayName("GET /reports - Should return all reports successfully")
    void getAllReports_ValidRequest_ReturnsReports() throws Exception {
        // Given
        ReportDto mockReport1 = ReportsTestDataFactory.createTestReportDto();
        ReportDto mockReport2 = ReportsTestDataFactory.createTestReportDto();
        mockReport2.setReportId(1002);
        List<ReportDto> mockReports = Arrays.asList(mockReport1, mockReport2);

        when(reportService.getAllReports()).thenReturn(mockReports);

        // When & Then
        mockMvc.perform(get("/reports"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].reportId", is(mockReport1.getReportId())))
                .andExpect(jsonPath("$[1].reportId", is(mockReport2.getReportId())));

        verify(reportService).getAllReports();
    }

    @Test
    @DisplayName("GET /reports - Should return empty list when no reports exist")
    void getAllReports_NoReports_ReturnsEmptyList() throws Exception {
        // Given
        when(reportService.getAllReports()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/reports"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(reportService).getAllReports();
    }

    @Test
    @DisplayName("GET /reports/{id} - Should return report by ID successfully")
    void getReport_ValidId_ReturnsReport() throws Exception {
        // Given
        Integer reportId = 1001;
        ReportDto mockReport = ReportsTestDataFactory.createTestReportDto();
        mockReport.setReportId(reportId);
        when(reportService.getReport(reportId)).thenReturn(mockReport);

        // When & Then
        mockMvc.perform(get("/reports/{id}", reportId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId", is(reportId)))
                .andExpect(jsonPath("$.reportName", is(mockReport.getReportName())));

        verify(reportService).getReport(reportId);
    }

    @Test
    @DisplayName("GET /reports/{id} - Should return 404 when report not found")
    void getReport_ReportNotFound_Returns404() throws Exception {
        // Given
        Integer reportId = 999;
        when(reportService.getReport(reportId))
                .thenThrow(new RuntimeException("Report not found"));

        // When & Then
        mockMvc.perform(get("/reports/{id}", reportId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(reportService).getReport(reportId);
    }

    @Test
    @DisplayName("POST /reports - Should create report successfully")
    void createReport_ValidData_CreatesReport() throws Exception {
        // Given
        ReportCreateDto inputReport = ReportsTestDataFactory.createTestReportCreateDto();
        ReportDto savedReport = ReportsTestDataFactory.createTestReportDto();
        savedReport.setReportId(1001);
        when(reportService.createReport(any(ReportCreateDto.class))).thenReturn(savedReport);

        // When & Then
        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputReport)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId", is(savedReport.getReportId())))
                .andExpect(jsonPath("$.reportName", is(savedReport.getReportName())));

        verify(reportService).createReport(any(ReportCreateDto.class));
    }

    @Test
    @DisplayName("POST /reports - Should return 409 when report identifier already exists")
    void createReport_DuplicateIdentifier_Returns409() throws Exception {
        // Given
        ReportCreateDto inputReport = ReportsTestDataFactory.createTestReportCreateDto();
        when(reportService.createReport(any(ReportCreateDto.class)))
                .thenThrow(new RuntimeException("Report identifier already exists"));

        // When & Then
        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputReport)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(reportService).createReport(any(ReportCreateDto.class));
    }

    @Test
    @DisplayName("PUT /reports/{id} - Should update report successfully")
    void updateReport_ValidData_UpdatesReport() throws Exception {
        // Given
        Integer reportId = 1001;
        ReportUpdateDto updateReport = ReportsTestDataFactory.createTestReportUpdateDto();
        ReportDto updatedReport = ReportsTestDataFactory.createTestReportDto();
        updatedReport.setReportId(reportId);
        when(reportService.updateReport(eq(reportId), any(ReportUpdateDto.class))).thenReturn(updatedReport);

        // When & Then
        mockMvc.perform(put("/reports/{id}", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReport)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId", is(reportId)))
                .andExpect(jsonPath("$.reportName", is(updatedReport.getReportName())));

        verify(reportService).updateReport(eq(reportId), any(ReportUpdateDto.class));
    }

    @Test
    @DisplayName("DELETE /reports/{id} - Should delete report successfully")
    void deleteReport_ValidId_DeletesReport() throws Exception {
        // Given
        Integer reportId = 1001;
        doNothing().when(reportService).deleteReport(reportId);

        // When & Then
        mockMvc.perform(delete("/reports/{id}", reportId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(reportService).deleteReport(reportId);
    }

    @Test
    @DisplayName("GET /reports/search - Should search reports successfully")
    void searchReports_ValidCriteria_ReturnsReports() throws Exception {
        // Given
        String searchTerm = "test";
        String status = "PUBLISHED";
        List<ReportDto> mockReports = Arrays.asList(
            ReportsTestDataFactory.createTestReportDto()
        );
        Page<ReportDto> mockPage = new PageImpl<>(mockReports, PageRequest.of(0, 10), 1);

        when(reportService.searchReports(eq(searchTerm), eq(status), isNull(), any()))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/reports/search")
                        .param("searchTerm", searchTerm)
                        .param("status", status)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(reportService).searchReports(eq(searchTerm), eq(status), isNull(), any());
    }

    @Test
    @DisplayName("POST /reports/{id}/publish - Should publish report successfully")
    void publishReport_ValidId_PublishesReport() throws Exception {
        // Given
        Integer reportId = 1001;
        ReportDto publishedReport = ReportsTestDataFactory.createTestReportDto();
        publishedReport.setReportId(reportId);
        publishedReport.setStatus("PUBLISHED");
        when(reportService.publishReport(reportId)).thenReturn(publishedReport);

        // When & Then
        mockMvc.perform(post("/reports/{id}/publish", reportId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId", is(reportId)))
                .andExpect(jsonPath("$.status", is("PUBLISHED")));

        verify(reportService).publishReport(reportId);
    }

    @Test
    @DisplayName("POST /reports/{id}/execute - Should execute report successfully")
    void executeReport_ValidId_ExecutesReport() throws Exception {
        // Given
        Integer reportId = 1001;
        ReportExecutionResultDto executionResult = ReportsTestDataFactory.createTestReportExecutionResultDto();
        when(reportService.executeReport(eq(reportId), any())).thenReturn(executionResult);

        // When & Then
        mockMvc.perform(post("/reports/{id}/execute", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reportId", is(executionResult.getReportId())));

        verify(reportService).executeReport(eq(reportId), any());
    }

    @Test
    @DisplayName("GET /reports/summary - Should return reports summary successfully")
    void getReportsSummary_ValidRequest_ReturnsSummary() throws Exception {
        // Given - Mock the repository methods called by the endpoint
        when(reportRepository.getReportStatistics()).thenReturn(Map.of(
                "totalReports", 0L,
                "publishedReports", 0L,
                "draftReports", 0L,
                "archivedReports", 0L
        ));
        when(reportRepository.findAllReportsBasicInfo()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/reports/summary"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(reportRepository).getReportStatistics();
        verify(reportRepository).findAllReportsBasicInfo();
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void createReport_ServiceException_Returns500() throws Exception {
        // Given
        ReportCreateDto inputReport = ReportsTestDataFactory.createTestReportCreateDto();
        when(reportService.createReport(any(ReportCreateDto.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        // When & Then
        mockMvc.perform(post("/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputReport)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(reportService).createReport(any(ReportCreateDto.class));
    }
}