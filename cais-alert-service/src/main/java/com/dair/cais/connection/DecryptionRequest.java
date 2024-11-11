package com.dair.cais.connection;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class DecryptionRequest {
    @NotBlank(message = "Encrypted data is required")
    private String encryptedData;

    @NotBlank(message = "IV is required")
    @Pattern(regexp = "^[A-Za-z0-9+/]*={0,2}$", message = "Invalid IV format")
    private String iv;
}
