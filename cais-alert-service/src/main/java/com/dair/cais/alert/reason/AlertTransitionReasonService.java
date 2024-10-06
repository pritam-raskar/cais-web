package com.dair.cais.alert.reason;

import com.dair.cais.note.NoteExtended;
import com.dair.cais.note.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AlertTransitionReasonService {

    private final MongoTemplate mongoTemplate;
    private final NoteService noteService;


    @Autowired
    public AlertTransitionReasonService(MongoTemplate mongoTemplate, NoteService noteService) {

        this.mongoTemplate = mongoTemplate;
        this.noteService = noteService;
    }

    public AlertTransitionReason saveAlertTransitionReason(AlertTransitionReason alertTransitionReason) {
        // Set the current date time
        alertTransitionReason.setCreatedDate(LocalDateTime.now());

        // Save the transition reason
        AlertTransitionReason savedReason = mongoTemplate.save(alertTransitionReason, "AlertTransitionReason");

        // Call the note API if a note is provided
        if (alertTransitionReason.getNote() != null && !alertTransitionReason.getNote().isEmpty()) {
            String entity = "Alert - Transition";
            String entityValue = String.join(", ", alertTransitionReason.getCheckedReasons());

            NoteExtended createdNote = noteService.addNote(
                    alertTransitionReason.getNote(),
                    alertTransitionReason.getAlertId(),
                    alertTransitionReason.getUserName(),
                    entity,
                    entityValue
            );


        }

        return savedReason;
    }
}