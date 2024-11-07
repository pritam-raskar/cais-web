package com.dair.cais.access.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    private final JwtConfig jwtConfig;
    private final Key key;

    public JwtService(JwtConfig jwtConfig, Key key) {
        this.jwtConfig = jwtConfig;
        this.key = key;
    }

    public String generateToken(String userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public TokenValidationResult validateAndExtractClaims(String token) {
        try {
            Claims claims = validateToken(token);
            return new TokenValidationResult(
                    true,
                    claims.get("userId", String.class),
                    claims.get("username", String.class)
            );
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return new TokenValidationResult(false, null, null);
        }
    }

    @Data
    @AllArgsConstructor
    public static class TokenValidationResult {
        private boolean valid;
        private String userId;
        private String username;
    }
}