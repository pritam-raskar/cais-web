package com.dair.cais.access.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret:dGhpc2lzYXZlcnlsb25nc2VjcmV0a2V5Zm9yand0Z2VuZXJhdGlvbg==}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @Bean
    public Key key() {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(decodedKey, "HmacSHA256");
    }

    public String getSecret() {
        return secret;
    }

    public Long getExpiration() {
        return expiration;
    }
}