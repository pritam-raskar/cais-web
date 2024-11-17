package com.dair.cais.cachemanagement;

import lombok.Data;

import java.util.List;

@Data
public class CacheClearRequest {
    private List<String> cacheNames;  // If empty, clear all caches
    private List<String> keys;        // If provided, clear only these keys
}
