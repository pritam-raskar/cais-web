package com.dair.cais.connection;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConnectionDetails {
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
    private String additionalParams;
}