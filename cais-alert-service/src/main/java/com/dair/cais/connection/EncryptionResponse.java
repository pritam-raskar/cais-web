package com.dair.cais.connection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class EncryptionResponse {
    private String iv;
    private String encryptedData;
}
