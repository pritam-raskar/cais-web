package com.dair.cais.attachment;

import org.springframework.stereotype.Component;

@Component
public class AttachmentMapperExtended {

    public ExtendedAttachment toModel(AttachmentEntityExtended attachmentEntityextended) {
        ExtendedAttachment attachmentextended  = new ExtendedAttachment();
        attachmentextended.setId(attachmentEntityextended.getId());
        attachmentextended.setFileData(attachmentEntityextended.getFileData());
        attachmentextended.setAlertId(attachmentEntityextended.getAlertId());
        attachmentextended.setFileName(attachmentEntityextended.getFileName());
        attachmentextended.setFileSize(attachmentEntityextended.getFileSize());
        attachmentextended.setFileType(attachmentEntityextended.getFileType());
        attachmentextended.setCreatedDate(attachmentEntityextended.getCreatedDate());
        attachmentextended.setUpdatedDate(attachmentEntityextended.getUpdatedDate());
        attachmentextended.setCreatedBy(attachmentEntityextended.getCreatedBy());
        attachmentextended.setUpdatedBy(attachmentEntityextended.getUpdatedBy());
        attachmentextended.setComment(attachmentEntityextended.getComment());
        return attachmentextended;
    }

    private void mapAttachmentToEntity(ExtendedAttachment attachmentextended, AttachmentEntityExtended attachmententityextended) {
        attachmententityextended.setId(attachmentextended.getId());
        attachmententityextended.setAlertId(attachmentextended.getAlertId());
        attachmententityextended.setFileData(attachmentextended.getFileData());
        attachmententityextended.setFileName(attachmentextended.getFileName());
        attachmententityextended.setFileSize(attachmentextended.getFileSize());
        attachmententityextended.setFileType(attachmentextended.getFileType());
        attachmententityextended.setCreatedDate(attachmentextended.getCreatedDate());
        attachmententityextended.setUpdatedDate(attachmentextended.getUpdatedDate());
        attachmententityextended.setCreatedBy(attachmentextended.getCreatedBy());
        attachmententityextended.setUpdatedBy(attachmentextended.getUpdatedBy());
        attachmententityextended.setComment(attachmentextended.getComment());
    }

    public AttachmentEntityExtended toEntity(ExtendedAttachment attachmentextended) {
        AttachmentEntityExtended attachmententityextended = new AttachmentEntityExtended();
        mapAttachmentToEntity(attachmentextended, attachmententityextended);

        return attachmententityextended;
    }

    public AttachmentEntityExtended toEntity(String alertId,  ExtendedAttachment attachmentextended) {
        AttachmentEntityExtended attachmententityextended = new AttachmentEntityExtended();
        attachmententityextended.setAlertId(alertId);
        mapAttachmentToEntity(attachmentextended, attachmententityextended);

        return attachmententityextended;

    }

    public AttachmentEntityExtended toEntity(String alertId, String attachmentId, ExtendedAttachment attachmentextended) {
        AttachmentEntityExtended attachmententityextended = new AttachmentEntityExtended();
        mapAttachmentToEntity(attachmentextended, attachmententityextended);

        return attachmententityextended;

    }



}