package com.dair.cais.note;

import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public Note toModel(NoteEntity entity) {
        Note note = new Note();
        note.setId(String.valueOf(entity.getId()));
        note.setName(entity.getName());
        note.setDescription(entity.getDescription());

        note.setCreatedDate(entity.getCreatedDate());
        note.setUpdatedDate(entity.getUpdatedDate());

        return note;
    }

    public NoteEntity toEntity(Note note) {
        NoteEntity noteEntity = new NoteEntity();
        noteEntity.setId(note.getId());
        mapNoteToEntity(note, noteEntity);

        return noteEntity;
    }

    public NoteEntity toEntity(String noteId, Note note) {
        NoteEntity noteEntity = new NoteEntity();
        noteEntity.setId(noteId);
        mapNoteToEntity(note, noteEntity);

        return noteEntity;

    }

    private void mapNoteToEntity(Note note, NoteEntity noteEntity) {
        noteEntity.setName(note.getName());
        noteEntity.setDescription(note.getDescription());

        noteEntity.setCreatedDate(note.getCreatedDate());
        noteEntity.setUpdatedDate(note.getUpdatedDate());
    }

}