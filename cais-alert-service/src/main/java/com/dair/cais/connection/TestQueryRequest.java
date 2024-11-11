package com.dair.cais.connection;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
class TestQueryRequest {
    @NotBlank(message = "Query is required")
    private String query;
}
