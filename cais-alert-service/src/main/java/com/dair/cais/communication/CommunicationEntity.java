package com.dair.cais.communication;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cm_alert_communication", schema = "info_alert")
public class CommunicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "communication_seq")
    @SequenceGenerator(name = "communication_seq", sequenceName = "cm_alert_communication_id_seq", allocationSize = 1, schema = "info_alert")
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "alert_id")
    private String alertId;

    @Column(name = "message")
    private String message;

    @Column(name = "has_attachment")
    private Boolean hasAttachment;

    @Column(name = "attachment_id")
    private String attachmentId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "create_date")
    private LocalDateTime createDate;
}