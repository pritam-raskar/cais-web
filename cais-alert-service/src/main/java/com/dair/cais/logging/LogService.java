package com.dair.cais.logging;

import com.dair.cais.logging.LogEntryDTO;
import com.dair.cais.logging.LogResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.logging.LogLevel;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.InetAddress;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LogService {

    @Value("${logging.file.name}")
    private String logFilePath;

    @Value("${spring.application.name:cais-alert-service}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    private final ConcurrentLinkedQueue<LogEntryDTO> inMemoryLogs = new ConcurrentLinkedQueue<>();
    private static final int MAX_IN_MEMORY_LOGS = 10000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Scheduled(fixedRate = 5000)
    public void updateInMemoryLogs() {
        try {
            Path path = Paths.get(logFilePath);
            if (!Files.exists(path)) {
                log.warn("Log file does not exist at path: {}", logFilePath);
                return;
            }

            List<String> newLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
                String line;
                StringBuilder multiLineLog = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (isNewLogEntry(line)) {
                        if (multiLineLog.length() > 0) {
                            newLines.add(multiLineLog.toString());
                            multiLineLog = new StringBuilder();
                        }
                        multiLineLog.append(line);
                    } else if (multiLineLog.length() > 0) {
                        multiLineLog.append("\n").append(line);
                    }
                }
                if (multiLineLog.length() > 0) {
                    newLines.add(multiLineLog.toString());
                }
            }

            inMemoryLogs.clear();
            for (String logEntry : newLines) {
                parseLine(logEntry).ifPresent(log -> {
                    if (inMemoryLogs.size() >= MAX_IN_MEMORY_LOGS) {
                        inMemoryLogs.poll();
                    }
                    inMemoryLogs.offer(log);
                });
            }

            //log.debug("Updated in-memory logs. Current size: {}", inMemoryLogs.size()); // Uncomment to see log update in console
        } catch (IOException e) {
            log.error("Error reading log file: {}", e.getMessage(), e);
        }
    }

    private boolean isNewLogEntry(String line) {
        return line.matches("^\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*");
    }

    public LogResponse getLogs(int page, int limit) {
        List<LogEntryDTO> allLogs = new ArrayList<>(inMemoryLogs);
        Collections.reverse(allLogs); // Show newest logs first

        int totalElements = allLogs.size();
        int totalPages = (int) Math.ceil((double) totalElements / limit);

        // Validate page number
        if (page >= totalPages) {
            page = Math.max(0, totalPages - 1);
        }

        int startIndex = page * limit;
        int endIndex = Math.min(startIndex + limit, totalElements);

        List<LogEntryDTO> pagedLogs = startIndex < totalElements ?
                allLogs.subList(startIndex, endIndex) :
                new ArrayList<>();

        log.debug("Returning {} logs for page {} (limit: {})",
                pagedLogs.size(), page, limit);

        return LogResponse.builder()
                .logs(pagedLogs)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .currentPage(page)
                .hasNext(endIndex < totalElements)
                .hasPrevious(page > 0)
                .build();
    }

    private Optional<LogEntryDTO> parseLine(String line) {
        try {
            // Split the log entry into lines
            String[] lines = line.split("\n");
            String mainLine = lines[0];

            // Parse the main log line
            String[] parts = mainLine.split("\\s+", 6);
            if (parts.length < 6) {
                return Optional.empty();
            }

            // Extract timestamp
            String timestamp = parts[0] + " " + parts[1];

            // Extract thread name (remove square brackets)
            String threadName = parts[2].replaceAll("[\\[\\]]", "");

            // Extract log level
            String logLevel = parts[3];

            // Extract logger name
            String logger = parts[4];

            // Extract message (may contain spaces)
            String message = parts.length > 5 ? parts[5] : "";

            // Combine any stack trace from additional lines
            String stackTrace = null;
            if (lines.length > 1) {
                stackTrace = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length));
            }

            return Optional.of(LogEntryDTO.builder()
                    .timestamp(LocalDateTime.parse(timestamp, DATE_FORMAT))
                    .threadName(threadName)
                    .logLevel(logLevel)
                    .logger(logger)
                    .message(message)
                    .stackTrace(stackTrace)
                    .serviceName(applicationName)
                    //.hostName(InetAddress.getLocalHost().getHostName())
                    //.environment(activeProfile)
                    .build());
        } catch (Exception e) {
            log.warn("Error parsing log line: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // Helper method to validate log levels
    public boolean isValidLogLevel(String level) {
        try {
            LogLevel.valueOf(level.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}