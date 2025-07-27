package com.dair.cais.cases.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

/**
 * Data Transfer Object for Case Statistics.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseStatistics {
    private Long totalCases;
    private Long openCases;
    private Long closedCases;
    private Duration averageResolutionTime;
    private Map<String, Long> casesByStatus;
    private Map<String, Long> casesByPriority;
    private Map<String, Long> casesByType;
    private Map<String, Long> casesByOrgUnit;
    private Map<String, Long> casesByOwner;
    private Map<String, Double> averageResolutionTimeByType;
    private Map<String, Double> averageResolutionTimeByPriority;
    private Long casesCreatedToday;
    private Long casesCreatedThisWeek;
    private Long casesCreatedThisMonth;
    private Long casesClosedToday;
    private Long casesClosedThisWeek;
    private Long casesClosedThisMonth;
    private Double slaMeetPercentage;
}