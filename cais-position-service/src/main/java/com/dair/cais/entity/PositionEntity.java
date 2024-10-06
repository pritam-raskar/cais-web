package com.dair.cais.entity;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cais_positions")
public class PositionEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private String name;
    private String type;

    private String tradeKey;
    private String accountNumber;
    private String longShortCd;
    private String positionDateTime;
    private String productKey;
    private String baseCurrCd;
    private String baseCurrPrice;
    private String baseCurrValue;
    private String quantity;
    private String product;
    private String productName;
    private String sectorCd;
    private String symbol;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> customFields;
}