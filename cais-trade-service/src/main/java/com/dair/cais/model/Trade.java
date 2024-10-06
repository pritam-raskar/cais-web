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
public class Trade extends CaisBaseModel {

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
}
