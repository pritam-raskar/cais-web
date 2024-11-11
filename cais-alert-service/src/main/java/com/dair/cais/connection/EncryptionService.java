package com.dair.cais.connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
        this.objectMapper = objectMapper;
    }

    public EncryptedData encryptObject(Object obj) {
        try {
            byte[] iv = generateIv();
            byte[] keyBytes = hexStringToByteArray(encryptionKey);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            String jsonString = objectMapper.writeValueAsString(obj);
            byte[] encrypted = cipher.doFinal(jsonString.getBytes());

            return new EncryptedData(
                    bytesToHex(iv),
                    bytesToHex(encrypted)
            );
        } catch (Exception e) {
            log.error("Error encrypting object: {}", e.getMessage());
            throw new EncryptionException("Error encrypting data", e);
        }
    }

    public <T> T decryptObject(String encryptedHex, String ivHex, Class<T> type) {
        try {
            byte[] keyBytes = hexStringToByteArray(encryptionKey);
            byte[] ivBytes = hexStringToByteArray(ivHex);
            byte[] encryptedBytes = hexStringToByteArray(encryptedHex);

            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            String decryptedString = new String(decrypted);

            // Remove extra quotes if present
            decryptedString = removeExtraQuotes(decryptedString);

            try {
                return objectMapper.readValue(decryptedString, type);
            } catch (JsonProcessingException e) {
                log.error("Error parsing decrypted JSON: {}", decryptedString);
                log.error("Error details: {}", e.getMessage());
                throw new DecryptionException("Error parsing decrypted data", e);
            }
        } catch (Exception e) {
            log.error("Error decrypting object: {}", e.getMessage());
            throw new DecryptionException("Error decrypting data", e);
        }
    }

    private String removeExtraQuotes(String input) {
        // Remove leading and trailing quotes if they exist
        if (input.startsWith("\"") && input.endsWith("\"")) {
            // Also unescape any internal quotes
            String unquoted = input.substring(1, input.length() - 1);
            return unquoted.replace("\\\"", "\"");
        }
        return input;
    }

    private byte[] generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static String bytesToHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    private static byte[] hexStringToByteArray(String s) {
        return HexFormat.of().parseHex(s);
    }
}


//package com.dair.cais.connection;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import jakarta.annotation.PostConstruct;
//import java.security.SecureRandom;
//import java.util.HexFormat;
//
//@Slf4j
//@Service
//public class EncryptionService {
//    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
//
//    @Value("${encryption.key}")
//    private String encryptionKey;
//
//    private final ObjectMapper objectMapper;
//
//    public EncryptionService(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    public EncryptedData encryptObject(Object obj) {
//        try {
//            byte[] iv = generateIv();
//            byte[] keyBytes = hexStringToByteArray(encryptionKey);
//
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
//
//            String jsonString = objectMapper.writeValueAsString(obj);
//            byte[] encrypted = cipher.doFinal(jsonString.getBytes());
//
//            // Convert to hex strings to match JavaScript implementation
//            return new EncryptedData(
//                    bytesToHex(iv),
//                    bytesToHex(encrypted)
//            );
//        } catch (Exception e) {
//            log.error("Error encrypting object", e);
//            throw new EncryptionException("Error encrypting data", e);
//        }
//    }
//
//    public <T> T decryptObject(String encryptedHex, String ivHex, Class<T> type) {
//        try {
//            byte[] keyBytes = hexStringToByteArray(encryptionKey);
//            byte[] ivBytes = hexStringToByteArray(ivHex);
//            byte[] encryptedBytes = hexStringToByteArray(encryptedHex);
//
//            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
//            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
//
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
//
//            byte[] decrypted = cipher.doFinal(encryptedBytes);
//            return objectMapper.readValue(new String(decrypted), type);
//        } catch (Exception e) {
//            log.error("Error decrypting object", e);
//            throw new DecryptionException("Error decrypting data", e);
//        }
//    }
//
//    private byte[] generateIv() {
//        byte[] iv = new byte[16];
//        new SecureRandom().nextBytes(iv);
//        return iv;
//    }
//
//    private static String bytesToHex(byte[] bytes) {
//        return HexFormat.of().formatHex(bytes);
//    }
//
//    private static byte[] hexStringToByteArray(String s) {
//        return HexFormat.of().parseHex(s);
//    }
//
//    @PostConstruct
//    private void validateConfiguration() {
//        try {
//            byte[] keyBytes = hexStringToByteArray(encryptionKey);
//            if (keyBytes.length != 32) {
//                throw new IllegalStateException("Encryption key must be exactly 32 bytes (256 bits)");
//            }
//            log.info("Encryption service initialized successfully");
//        } catch (Exception e) {
//            log.error("Invalid encryption configuration", e);
//            throw new IllegalStateException("Invalid encryption configuration", e);
//        }
//    }
//}

//package com.dair.cais.connection;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.security.SecureRandom;
//import java.util.Base64;
//
//@Slf4j
//@Service
//public class EncryptionService {
//    private static final String ALGORITHM = "AES/CBC/PKCS5PADDING";
//
//    @Value("${encryption.key}")
//    private String encryptionKey;
//
//    private final ObjectMapper objectMapper;
//
//    public EncryptionService(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    public EncryptedData encryptObject(Object obj) {
//        try {
//            byte[] iv = generateIv();
//            SecretKeySpec keySpec = new SecretKeySpec(hexStringToByteArray(encryptionKey), "AES");
//            IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
//
//            String jsonString = objectMapper.writeValueAsString(obj);
//            byte[] encrypted = cipher.doFinal(jsonString.getBytes());
//
//            return new EncryptedData(
//                    Base64.getEncoder().encodeToString(iv),
//                    Base64.getEncoder().encodeToString(encrypted)
//            );
//        } catch (Exception e) {
//            log.error("Error encrypting object", e);
//            throw new RuntimeException("Error encrypting data", e);
//        }
//    }
//
//    public <T> T decryptObject(String encryptedData, String iv, Class<T> type) {
//        try {
//            SecretKeySpec keySpec = new SecretKeySpec(hexStringToByteArray(encryptionKey), "AES");
//            IvParameterSpec ivSpec = new IvParameterSpec(Base64.getDecoder().decode(iv));
//
//            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
//
//            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
//            return objectMapper.readValue(new String(decrypted), type);
//        } catch (Exception e) {
//            log.error("Error decrypting object", e);
//            throw new RuntimeException("Error decrypting data", e);
//        }
//    }
//
//    private byte[] generateIv() {
//        byte[] iv = new byte[16];
//        new SecureRandom().nextBytes(iv);
//        return iv;
//    }
//
//    private byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i + 1), 16));
//        }
//        return data;
//    }
//}