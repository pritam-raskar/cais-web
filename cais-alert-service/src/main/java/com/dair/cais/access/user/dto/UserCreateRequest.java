package com.dair.cais.access.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "Request object for creating a new user")
public class UserCreateRequest {

    @NotBlank(message = "Login name is required")
    @Size(min = 3, max = 50, message = "Login name must be between 3 and 50 characters")
    @Schema(description = "User login name", example = "john.doe")
    private String userLoginName;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "User password", example = "password123")
    private String password;

    @NotBlank(message = "First name is required")
    @Schema(description = "User's first name", example = "John")
    private String userFirstName;

    @Schema(description = "User's middle name", example = "Robert")
    private String userMiddleName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "User's last name", example = "Doe")
    private String userLastName;

    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
}