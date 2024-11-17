package com.dair.cais.cachemanagement;

import lombok.Data;

import java.util.List;

@Data
public class CacheSummary {
    private String cacheName;
    private long size;
    private List<String> cacheKeys;  // Added this field
    private String lastUpdated;
}