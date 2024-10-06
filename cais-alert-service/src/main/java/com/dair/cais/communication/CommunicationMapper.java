package com.dair.cais.communication;

import org.springframework.stereotype.Component;

@Component
public class CommunicationMapper {

    public Communication toModel(CommunicationEntity entity) {
        if (entity == null) {
            return null;
        }

        Communication communication = new Communication();
        communication.setId(entity.getId());
        communication.setType(entity.getType());
        communication.setAlertId(entity.getAlertId());
        communication.setMessage(entity.getMessage());
        communication.setHasAttachment(entity.getHasAttachment());
        communication.setAttachmentId(entity.getAttachmentId());
        communication.setUserId(entity.getUserId());
        communication.setCreateDate(entity.getCreateDate());


        return communication;
    }

    public CommunicationEntity toEntity(Communication model) {
        if (model == null) {
            return null;
        }

        CommunicationEntity entity = new CommunicationEntity();
        mapModelToEntity(model, entity);
        return entity;
    }

    private void mapModelToEntity(Communication model, CommunicationEntity entity) {
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setAlertId(model.getAlertId());
        entity.setMessage(model.getMessage());
        entity.setHasAttachment(model.getHasAttachment());
        entity.setAttachmentId(model.getAttachmentId());
        entity.setUserId(model.getUserId());
        // Set createDate to current system date when mapping
        entity.setCreateDate(model.getCreateDate());
    }

    public CommunicationEntity toEntity(String alertId, Communication model) {
        CommunicationEntity entity = new CommunicationEntity();
        entity.setAlertId(alertId);
        mapModelToEntity(model, entity);
        return entity;
    }
}