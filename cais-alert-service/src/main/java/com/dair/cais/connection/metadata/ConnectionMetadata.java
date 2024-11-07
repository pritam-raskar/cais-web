package com.dair.cais.connection.metadata;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// DTO
@Data
public class ConnectionMetadata {
    @NotBlank(message = "Connection type is required")
    @Size(max = 50, message = "Connection type cannot exceed 50 characters")
    private String connectionType;

    @Size(max = 4000, message = "JSON structure cannot exceed 4000 characters")
    private String jsonStructure;
}
