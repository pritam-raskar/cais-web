package com.dair.cais.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.dair.cais.common.repository.CustomDatabaseRepository;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReportService {

   @Autowired
   private ReportMapper reportMapper;
   @Autowired
   private ReportRepository reportRepository;
   @Autowired
   private CustomDatabaseRepository customDatabaseRepository;

   public Report createReport(Report report) {
      String extractReportId = Objects.nonNull(report.getId()) ? report.getId() : UUID.randomUUID().toString();
      ReportEntity reportEntityToUpsert = reportMapper.toEntity(extractReportId, report);
      ReportEntity upsertedReportEntity = reportRepository.save(reportEntityToUpsert);
      return reportMapper.toModel(upsertedReportEntity);
   }

   public List<Report> createReports(List<Report> reports) {
      List<Report> createdReports = reports.stream().map(a -> createReport(a)).collect(Collectors.toList());
      return createdReports;
   }

   public Report getReportById(final String reportId) {
      Optional<ReportEntity> reportByIdOptional = reportRepository.findById(reportId);
      if (reportByIdOptional.isPresent()) {
         return reportMapper.toModel(reportByIdOptional.get());
      } else {
         throw new CaisNotFoundException();
      }
   }

   public Map<String, Object> getAllReports(String name, int offset, int limit) {
      String schema = "public";
      List<Report> allReports;
      try {
         if (name==null || !StringUtils.isEmpty(name)) {
            List<String> tableNames = customDatabaseRepository.findAllTableNames(schema);
            allReports = tableNames.stream().map(a -> reportMapper.toModel(a))
                  .collect(Collectors.toList());
         } else {
            Page<ReportEntity> allReportEntities = routeToJpaMethod(schema, name, offset, limit);

            allReports = allReportEntities.stream().map(a -> reportMapper.toModel(a))
                  .collect(Collectors.toList());
         }
         Map<String, Object> response = new HashMap<>();
         response.put("reports", allReports);
         response.put("count", allReports.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving reports");
      }
   }

   private Page<ReportEntity> routeToJpaMethod(String schema, String name, int offset, int limit) {
      int pageNumber = (int) (Math.floor(offset / limit) + (offset % limit));

      Pageable paging = PageRequest.of(pageNumber, limit);
      Page<ReportEntity> reportEntityPages;
      if (StringUtils.isNotEmpty(name)) {
         reportEntityPages = reportRepository.findByNameContainingIgnoreCase(name, paging);
      } else {
         reportEntityPages = reportRepository.findAll(paging);
      }
      return reportEntityPages;
   }

}
