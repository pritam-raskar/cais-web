package com.dair.cais.cachemanagement;

import lombok.Data;

import java.util.Map;

@Data
public class CacheStatistics {
    private int totalCaches;
    private Map<String, Long> cacheSizes;
    private Map<String, String> lastUpdatedTimes;
}
