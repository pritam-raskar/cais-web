package com.dair.cais.access.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;

@Data
@Schema(description = "Request object for updating a user")
public class UserUpdateRequest {

    @Schema(description = "User's first name", example = "John")
    private String userFirstName;

    @Schema(description = "User's middle name", example = "Robert")
    private String userMiddleName;

    @Schema(description = "User's last name", example = "Doe")
    private String userLastName;

    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's active status")
    private Boolean userIsActive;

    @Schema(description = "New password (optional)")
    private String newPassword;
}