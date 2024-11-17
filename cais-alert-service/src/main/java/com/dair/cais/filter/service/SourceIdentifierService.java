package com.dair.cais.filter.service;

import com.dair.cais.reports.ReportsEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SourceIdentifierService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieves the source identifier (report ID) for Analytics reports
     * @param sourceIdentifier The source identifier to look up
     * @return The report ID as a string, or null if not found
     */
    @Transactional(readOnly = true)
    public String getSourceIdentifierForAnalytics(String sourceIdentifier) {
        log.debug("Fetching source identifier for Analytics report with identifier: {}", sourceIdentifier);

        try {
            ReportsEntity report = entityManager.createQuery(
                            "SELECT r FROM ReportsEntity r WHERE r.reportId= :sourceIdentifier",
                            ReportsEntity.class)
                    .setParameter("sourceIdentifier", sourceIdentifier)
                    .getSingleResult();

            String reportId = String.valueOf(report.getReportId());
            log.debug("Found report ID {} for source identifier {}", reportId, sourceIdentifier);
            return reportId;

        } catch (NoResultException e) {
            log.warn("No report found for source identifier: {}", sourceIdentifier);
            return null;
        } catch (Exception e) {
            log.error("Error fetching report ID for source identifier {}: {}",
                    sourceIdentifier, e.getMessage());
            return null;
        }
    }
}