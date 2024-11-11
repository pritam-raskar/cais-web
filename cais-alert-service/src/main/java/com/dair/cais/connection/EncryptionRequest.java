package com.dair.cais.connection;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class EncryptionRequest {
    @NotBlank(message = "Data to encrypt is required")
    private String data;
}
