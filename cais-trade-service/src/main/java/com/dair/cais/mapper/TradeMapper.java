package com.dair.cais.mapper;

import org.springframework.stereotype.Component;

import com.dair.cais.entity.TradeEntity;
import com.dair.cais.model.Trade;

@Component
public class TradeMapper {

    public Trade toModel(TradeEntity entity) {
        Trade trade = new Trade();
        trade.setId(entity.getTradeId());
        trade.setName(entity.getName());
        trade.setType(entity.getType());
        trade.setAccountNumber(entity.getAccountNumber());
        trade.setBaseCurrentAmount(entity.getBaseCurrentAmount());
        trade.setBaseCurrentCommision(entity.getBaseCurrentCommision());
        trade.setBaseCurrentNetAmount(entity.getBaseCurrentNetAmount());
        trade.setBaseCurrentCd(entity.getBaseCurrentCd());
        trade.setDirectionCd(entity.getDirectionCd());
        trade.setBaseCurrentOtherFees(entity.getBaseCurrentOtherFees());
        trade.setBaseCurrentTradePrice(entity.getBaseCurrentTradePrice());
        trade.setOrigCurrentAmount(entity.getOrigCurrentAmount());
        trade.setOrigCurrentCd(entity.getOrigCurrentCd());
        trade.setInstrumentQuantity(entity.getInstrumentQuantity());
        trade.setQuantity(entity.getQuantity());

        return trade;
    }

    public TradeEntity toEntity(String extractTradeId, Trade trade) {
        TradeEntity tradeEntity = new TradeEntity();
        tradeEntity.setTradeId(extractTradeId);
        tradeEntity.setName(trade.getName());
        tradeEntity.setType(trade.getType());
        tradeEntity.setAccountNumber(trade.getAccountNumber());
        tradeEntity.setBaseCurrentAmount(trade.getBaseCurrentAmount());
        tradeEntity.setBaseCurrentCommision(trade.getBaseCurrentCommision());
        tradeEntity.setBaseCurrentNetAmount(trade.getBaseCurrentNetAmount());
        tradeEntity.setBaseCurrentCd(trade.getBaseCurrentCd());
        tradeEntity.setDirectionCd(trade.getDirectionCd());
        tradeEntity.setBaseCurrentOtherFees(trade.getBaseCurrentOtherFees());
        tradeEntity.setBaseCurrentTradePrice(trade.getBaseCurrentTradePrice());
        tradeEntity.setOrigCurrentAmount(trade.getOrigCurrentAmount());
        tradeEntity.setOrigCurrentCd(trade.getOrigCurrentCd());
        tradeEntity.setInstrumentQuantity(trade.getInstrumentQuantity());
        tradeEntity.setQuantity(trade.getQuantity());

        return tradeEntity;
    }

}