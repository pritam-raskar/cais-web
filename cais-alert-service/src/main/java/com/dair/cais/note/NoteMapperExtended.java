package com.dair.cais.note;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
    public class NoteMapperExtended {




        public NoteExtended toModel(NoteEntityExtended entity) {
            NoteExtended extendedNote = new NoteExtended();
            extendedNote.setAlertId(entity.getAlertId());
            extendedNote.setNote(entity.getNote());
            extendedNote.setNoteSize(entity.getNoteSize());
            extendedNote.setCreatedDate(entity.getCreatedDate());
            extendedNote.setCreatedBy(entity.getCreatedBy());
            extendedNote.setEntity(entity.getEntity());
            extendedNote.setEntityValue(entity.getEntityValue());

            return extendedNote;
        }

    public NoteEntityExtended toEntity(String note, String alertId, String createdBy, String entity, String entityValue){
        NoteEntityExtended NoteEntityExtended = new NoteEntityExtended();
        NoteEntityExtended.setEntity(entity);
        NoteEntityExtended.setAlertId(alertId);
        NoteEntityExtended.setCreatedBy(createdBy);
        NoteEntityExtended.setEntityValue(entityValue);
        NoteEntityExtended.setNote(note);
        NoteEntityExtended.setCreatedDate(new Date());
        NoteEntityExtended.setNoteSize(note.length());
        return NoteEntityExtended;
    }



        private void NoteEntityExtended(NoteExtended note, NoteEntityExtended noteEntity) {

        noteEntity.setAlertId(note.getAlertId());
        noteEntity.setEntityValue(note.getEntityValue());
        noteEntity.setEntityValue(note.getEntityValue());
        noteEntity.setNoteSize(note.getNoteSize());
        noteEntity.setNote(note.getNote());
        noteEntity.setCreatedBy(note.getCreatedBy());
        noteEntity.setCreatedDate(note.getCreatedDate());

        }

    public List<NoteExtended> toModel(List<NoteEntityExtended> entities) {
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
