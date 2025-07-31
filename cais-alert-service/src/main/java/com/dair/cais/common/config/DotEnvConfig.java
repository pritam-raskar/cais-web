package com.dair.cais.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class to load environment variables from .env file
 */
@Configuration
@Slf4j
public class DotEnvConfig {

    @PostConstruct
    public void loadDotEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .filename(".env")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // Load .env variables into system properties so Spring can access them
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Only set if not already set (environment variables take precedence)
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    log.debug("Loaded environment variable from .env: {}", key);
                }
            });
            
            log.info("Successfully loaded .env file configuration");
        } catch (Exception e) {
            log.warn("Could not load .env file: {}. Using environment variables or defaults.", e.getMessage());
        }
    }
}