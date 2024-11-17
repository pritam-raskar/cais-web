package com.dair.cais.cachemanagement;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CacheInfo {
    private String cacheName;
    private long size;
    private Map<String, Object> entries;
    private List<String> cacheKeys;  // Added this field
    private String lastUpdated;
}

