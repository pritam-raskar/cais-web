package com.dair.cais.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class EncryptionService {
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

    @Value("${encryption.key}")
    private String encryptionKey;

    private final ObjectMapper objectMapper;

    public EncryptionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EncryptedData encryptObject(Object obj) {
        try {
            byte[] iv = generateIv();
            SecretKeySpec keySpec = new SecretKeySpec(hexStringToByteArray(encryptionKey), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            String jsonString = objectMapper.writeValueAsString(obj);
            byte[] encrypted = cipher.doFinal(jsonString.getBytes());

            return new EncryptedData(
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(encrypted)
            );
        } catch (Exception e) {
            log.error("Error encrypting object", e);
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public <T> T decryptObject(String encryptedData, String iv, Class<T> type) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(hexStringToByteArray(encryptionKey), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Base64.getDecoder().decode(iv));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return objectMapper.readValue(new String(decrypted), type);
        } catch (Exception e) {
            log.error("Error decrypting object", e);
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}