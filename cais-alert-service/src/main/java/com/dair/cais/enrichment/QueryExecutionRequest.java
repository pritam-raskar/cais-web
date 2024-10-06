package com.dair.cais.enrichment;

import java.util.List;


class QueryExecutionRequest {
    private String query;
    private List<Object> parameters;

    // Getters and setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public List<Object> getParameters() { return parameters; }
    public void setParameters(List<Object> parameters) { this.parameters = parameters; }
}
