package com.dair.cais.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class NoteExtended  {

    private String alertId;
    private String note;
    private int noteSize;  // Size calculated when note is submitted
    private String createdBy;
    private Date createdDate;
    private String entity;
    private String entityValue;
}
