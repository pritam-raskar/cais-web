package com.dair.cais.report;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cais_reports")
public class ReportEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private String reportId;
    private String name;

    private Date createdDate;
    private Date updatedDate;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> customFields;
}