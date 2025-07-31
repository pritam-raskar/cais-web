package com.dair.cais.attachment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttachmentEntityExtended implements Serializable {

    private static final long serialVersionUID = 1L;

    @MongoId(FieldType.OBJECT_ID)
    private String id;
    
    private byte[] fileData; // Binary data of the file
    private String alertId;
    private String fileName;
    private String fileType;
    private long fileSize;
    private Date createdDate;
    private Date updatedDate;
    private String createdBy;
    private String updatedBy;
    private String comment;
}