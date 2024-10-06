package com.dair.cais.model;

import java.time.LocalDate;

import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Position extends CaisBaseModel {

    private String key;
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
}
