package com.dair.cais.logging;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LogResponse {
    private List<LogEntryDTO> logs;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private boolean hasNext;
    private boolean hasPrevious;
}
