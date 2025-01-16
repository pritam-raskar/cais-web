package com.dair.cais.connection.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionTestResult {
    private final boolean success;
    private final ConnectionErrorType errorType;
    private final String errorMessage;
    private final String technicalDetails;
    private final Long connectionId;
    private final String connectionName;

    public static ConnectionTestResult success(Long connectionId, String connectionName) {
        return new ConnectionTestResult(true, null, null, null, connectionId, connectionName);
    }

    public static ConnectionTestResult failure(ConnectionErrorType errorType, String errorMessage,
                                               String technicalDetails, Long connectionId, String connectionName) {
        return new ConnectionTestResult(false, errorType, errorMessage, technicalDetails, connectionId, connectionName);
    }
}