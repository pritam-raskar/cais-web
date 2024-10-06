package com.dair.cais.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cais_trades")
public class TradeEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    private String tradeId;
    private String name;
    private String type;

    private String tradeKey;
    private String accountNumber;
    private int baseCurrentAmount;
    private int baseCurrentCommision;
    private int baseCurrentNetAmount;
    private String baseCurrentCd;
    private String directionCd;
    private String baseCurrentOtherFees;
    private int baseCurrentTradePrice;
    private int origCurrentAmount;
    private String origCurrentCd;
    private int origCurrentCommision;
    private String productKey;
    private int instrumentQuantity;
    private int quantity;

    private LocalDate settleDate;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> customFields;
}