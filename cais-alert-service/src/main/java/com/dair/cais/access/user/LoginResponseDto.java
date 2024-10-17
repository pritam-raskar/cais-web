package com.dair.cais.access.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class LoginResponseDto {
    private Integer userId;
    private String token; // If you're using token-based authentication
    private ObjectNode permissions;
}