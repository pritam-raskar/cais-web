package com.dair.cais.attachment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedAttachment  {

    @Field(name = "fileData")
    private byte[] fileData; // Binary data of the file

    @Field(name = "alertId")
    private String alertId;

    @Field(name = "fileName")
    private String fileName;

    @Field(name = "fileType")
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