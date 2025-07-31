package com.dair.cais.attachment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

import static com.dair.cais.common.config.CaisAlertConstants.MONGO_COLLECTION_ALERT_ATTACHMENTS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = MONGO_COLLECTION_ALERT_ATTACHMENTS)
public class ExtendedAttachment  {

    @MongoId(FieldType.OBJECT_ID)
    private String id;

    @Field(name = "fileData")
    private byte[] fileData; // Binary data of the file

    @Field(name = "alertId")
    @NotBlank(message = "Alert ID cannot be blank")
    private String alertId;

    @Field(name = "fileName")
    @NotBlank(message = "File name cannot be blank")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    private String fileName;

    @Field(name = "fileType")
    @NotBlank(message = "File type cannot be blank")
    private String fileType;

    @Field(name = "fileSize")
    private long fileSize;

    @Field(name = "createdDate")
    private Date createdDate;

    @Field(name = "updatedDate")
    private Date updatedDate;

    @Field(name = "createdBy")
    private String createdBy;

    @Field(name = "updatedBy")
    private String updatedBy;

    @Field(name = "comment")
    private String comment;
}