package com.dair.cais.access.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class TokenValidationResponse {
    private Integer userId;  // Changed from String to Integer to match login response
    private String token;    // Added token field
    private ObjectNode permissions;
}