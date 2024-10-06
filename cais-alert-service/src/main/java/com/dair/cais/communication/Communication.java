package com.dair.cais.communication;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Communication {
    private Long id;
    private String type;
    private String alertId;
    private String message;
    private Boolean hasAttachment;
    private String attachmentId;
    private String userId;
    private LocalDateTime createDate;
}