package com.dair.cais.connection;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
class QueryResponse {
    private final boolean success;
    private final String message;
    private final List<Map<String, Object>> results;
}
