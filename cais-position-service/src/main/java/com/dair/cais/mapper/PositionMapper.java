package com.dair.cais.mapper;

import org.springframework.stereotype.Component;

import com.dair.cais.entity.PositionEntity;
import com.dair.cais.model.Position;

@Component
public class PositionMapper {

    public Position toModel(PositionEntity entity) {
        Position position = new Position();
        position.setId(entity.getId());
        position.setName(entity.getName());
        position.setType(entity.getType());
        position.setAccountNumber(entity.getAccountNumber());
        position.setLongShortCd(entity.getLongShortCd());
        position.setPositionDateTime(entity.getPositionDateTime());
        position.setProductKey(entity.getProductKey());
        position.setBaseCurrCd(entity.getBaseCurrCd());
        position.setBaseCurrPrice(entity.getBaseCurrPrice());
        position.setBaseCurrValue(entity.getBaseCurrValue());
        position.setQuantity(entity.getQuantity());
        position.setProduct(entity.getProduct());
        position.setProductName(entity.getProductName());
        position.setSectorCd(entity.getSectorCd());
        position.setSymbol(entity.getSymbol());

        return position;
    }

    public PositionEntity toEntity(String extractPositionId, Position position) {
        PositionEntity positionEntity = new PositionEntity();
        positionEntity.setId(extractPositionId);
        positionEntity.setName(position.getName());
        positionEntity.setType(position.getType());
        positionEntity.setAccountNumber(position.getAccountNumber());
        positionEntity.setLongShortCd(position.getLongShortCd());
        positionEntity.setPositionDateTime(position.getPositionDateTime());
        positionEntity.setProductKey(position.getProductKey());
        positionEntity.setBaseCurrCd(position.getBaseCurrCd());
        positionEntity.setBaseCurrPrice(position.getBaseCurrPrice());
        positionEntity.setBaseCurrValue(position.getBaseCurrValue());
        positionEntity.setQuantity(position.getQuantity());
        positionEntity.setProduct(position.getProduct());
        positionEntity.setProductName(position.getProductName());
        positionEntity.setSectorCd(position.getSectorCd());
        positionEntity.setSymbol(position.getSymbol());

        return positionEntity;
    }

}