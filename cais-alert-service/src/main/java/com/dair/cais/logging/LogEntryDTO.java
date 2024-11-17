package com.dair.cais.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogEntryDTO {
    private LocalDateTime timestamp;
    private String threadName;
    private String logLevel;
    private String logger;
    private String message;
    private String stackTrace;
    private String serviceName;
//    private String hostName;
//    private String environment;
}
