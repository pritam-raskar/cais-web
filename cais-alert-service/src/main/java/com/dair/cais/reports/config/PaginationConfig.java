package com.dair.cais.reports.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

@Configuration
@ConfigurationProperties(prefix = "reports.execution")
@Data
@Validated
public class PaginationConfig {
    @Min(1)
    @Max(1000)
    private int pageSize = 50;
}