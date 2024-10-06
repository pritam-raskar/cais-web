package com.dair.cais.attachment;

import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public Attachment toModel(AttachmentEntity attachmentEntity) {
        Attachment attachment = new Attachment();
        attachment.setId(String.valueOf(attachmentEntity.getId()));
        attachment.setName(attachmentEntity.getName());
        attachment.setDescription(attachmentEntity.getDescription());
        attachment.setValue(attachmentEntity.getValue());

        attachment.setCreatedDate(attachmentEntity.getCreatedDate());
        attachment.setUpdatedDate(attachmentEntity.getUpdatedDate());

        return attachment;
    }

    public AttachmentEntity toEntity(Attachment attachment) {
        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setId(attachment.getId());
        mapAttachmentToEntity(attachment, attachmentEntity);

        return attachmentEntity;
    }

    public AttachmentEntity toEntity(String alertId, Attachment attachment) {
        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setAlertId(alertId);
        mapAttachmentToEntity(attachment, attachmentEntity);

        return attachmentEntity;

    }

    public AttachmentEntity toEntity(String alertId, String attachmentId, Attachment attachment) {
        AttachmentEntity attachmentEntity = new AttachmentEntity();
        attachmentEntity.setId(attachmentId);
        mapAttachmentToEntity(attachment, attachmentEntity);

        return attachmentEntity;

    }

    private void mapAttachmentToEntity(Attachment attachment, AttachmentEntity alertEntity) {
        alertEntity.setName(attachment.getName());
        alertEntity.setDescription(attachment.getDescription());
        alertEntity.setValue(attachment.getValue());

        alertEntity.setCreatedDate(attachment.getCreatedDate());
        alertEntity.setUpdatedDate(attachment.getUpdatedDate());
    }

}