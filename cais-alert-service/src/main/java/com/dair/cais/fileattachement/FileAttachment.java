package com.dair.cais.fileattachement;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileAttachment {
    private String alertId;
    private String fileName;
    private String uniqueFileName;
    private String fileType;
    private Long fileSize;
    private String createdBy;
    private LocalDateTime createdDate;
    private String comment;
}