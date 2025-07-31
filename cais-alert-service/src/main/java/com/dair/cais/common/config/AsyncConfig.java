package com.dair.cais.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for notification processing
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {
    
    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Notification-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("Notification task rejected, executing synchronously");
            r.run();
        });
        executor.initialize();
        
        // Wrap with SecurityContext propagation
        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
