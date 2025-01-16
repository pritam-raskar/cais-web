package com.dair.cais.connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;

@Slf4j
@Service
public class EncryptionService {
    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

    @Value("${encryption.key}")
    private String encryptionKey;

    private final ObjectMapper objectMapper;

    public EncryptionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public EncryptedData encryptObject(Object obj) {
        try {
            log.debug("Starting encryption process for object type: {}", obj.getClass().getSimpleName());
            byte[] iv = generateIv();
            byte[] keyBytes = hexStringToByteArray(encryptionKey);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            String jsonString = objectMapper.writeValueAsString(obj);
            byte[] encrypted = cipher.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));

            String ivHex = bytesToHex(iv);
            String encryptedHex = bytesToHex(encrypted);

            log.debug("Successfully encrypted object of type: {}", obj.getClass().getSimpleName());
            return new EncryptedData(ivHex, encryptedHex);
        } catch (JsonProcessingException e) {
            log.error("Error serializing object to JSON: {}", e.getMessage(), e);
            throw new EncryptionException("Error serializing object", e);
        } catch (Exception e) {
            log.error("Error encrypting object of type {}: {}", obj.getClass().getSimpleName(), e.getMessage(), e);
            throw new EncryptionException("Error encrypting data", e);
        }
    }

    public String decryptToString(String encryptedHex, String ivHex) {
        try {
            log.debug("Starting decryption process");
            byte[] keyBytes = hexStringToByteArray(encryptionKey);
            byte[] ivBytes = hexStringToByteArray(ivHex);
            byte[] encryptedBytes = hexStringToByteArray(encryptedHex);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            String decryptedString = new String(decrypted, StandardCharsets.UTF_8);

            // Clean up the decrypted string
            decryptedString = cleanDecryptedString(decryptedString);

            log.debug("Successfully decrypted data to string");
            return decryptedString;
        } catch (Exception e) {
            log.error("Error during decryption process: {}", e.getMessage(), e);
            throw new DecryptionException("Error decrypting data", e);
        }
    }

    private String cleanDecryptedString(String input) {
        if (input == null) {
            return null;
        }

        // Remove surrounding quotes if present
        input = input.replaceAll("^\"(.*)\"$", "$1");

        // Unescape JSON if it's escaped
        if (input.contains("\\\"")) {
            try {
                // Use Jackson to properly unescape the JSON string
                return objectMapper.readValue('"' + input + '"', String.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to unescape JSON string, returning as is: {}", e.getMessage());
                return input;
            }
        }
        return input;
    }

    public <T> T decryptObject(String encryptedHex, String ivHex, Class<T> type) {
        try {
            log.debug("Starting decryption process for type: {}", type.getSimpleName());
            String decryptedString = decryptToString(encryptedHex, ivHex);

            try {
                // Try parsing the JSON directly
                T result = objectMapper.readValue(decryptedString, type);
                log.debug("Successfully decrypted and deserialized object of type: {}", type.getSimpleName());
                return result;
            } catch (JsonProcessingException firstAttemptError) {
                log.debug("First attempt at JSON parsing failed, trying alternative parsing");

                try {
                    // If direct parsing fails, try to parse it as a JsonNode first
                    JsonNode jsonNode = objectMapper.readTree(decryptedString);
                    T result = objectMapper.convertValue(jsonNode, type);
                    log.debug("Successfully decrypted and deserialized object using alternative parsing");
                    return result;
                } catch (Exception secondAttemptError) {
                    log.error("Error deserializing decrypted data to type {}: {}. Decrypted string: {}",
                            type.getSimpleName(), firstAttemptError.getMessage(),
                            maskSensitiveData(decryptedString));
                    throw new DecryptionException("Error deserializing decrypted data", firstAttemptError);
                }
            }
        } catch (DecryptionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during decryption for type {}: {}", type.getSimpleName(), e.getMessage(), e);
            throw new DecryptionException("Error during decryption process", e);
        }
    }

    private String maskSensitiveData(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"*****\"")
                .replaceAll("\"username\"\\s*:\\s*\"[^\"]*\"", "\"username\":\"*****\"");
    }

    private byte[] generateIv() {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

    private static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    private static byte[] hexStringToByteArray(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException("Hex string cannot be null or empty");
        }
        return HexFormat.of().parseHex(s);
    }

    @PostConstruct
    private void validateConfiguration() {
        try {
            byte[] keyBytes = hexStringToByteArray(encryptionKey);
            if (keyBytes.length != 32) {
                throw new IllegalStateException("Encryption key must be exactly 32 bytes (256 bits)");
            }
            log.info("Encryption service initialized successfully");
        } catch (Exception e) {
            log.error("Invalid encryption configuration", e);
            throw new IllegalStateException("Invalid encryption configuration", e);
        }
    }
}