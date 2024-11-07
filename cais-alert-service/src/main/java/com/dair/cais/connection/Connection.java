package com.dair.cais.connection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Connection {
    private Long connectionId;

    @NotBlank(message = "Connection name is required")
    private String connectionName;

    @NotNull(message = "Connection type is required")
    private ConnectionType connectionType;

    private ConnectionDetails connectionDetails;
    private String iv;
    private String encryptedData;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Helper method to get connection type as string
    public String getConnectionTypeAsString() {
        return connectionType != null ? connectionType.getValue() : null;
    }
}