package com.dair.cais.note;

import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoteService {

   @Autowired
   private NoteMapper noteMapper;
   @Autowired
   private NoteRepository noteRepository;

   @Autowired
   private NoteMapperExtended noteMapperExtended;

   @Autowired
   private NoteExtended NoteExtended;




   public NoteExtended addNote(String note, String alertId, String createdBy, String entity, String entityValue) {
      NoteEntityExtended addnote = noteRepository.addNote(note, alertId, createdBy, entity, entityValue);
      return noteMapperExtended.toModel(addnote);
   }


   public List<NoteExtended> fetchNotes(String alertId) {
      return noteRepository.findByAlertId(alertId);
   }




   public Note createNote(Note note) {
      NoteEntity upsertedNote = noteRepository.createUpsertNote(noteMapper.toEntity(note));
      return noteMapper.toModel(upsertedNote);
   }

   public Note patchNote(String alertId, Note note) {
      NoteEntity upsertedNote = noteRepository.patchNote(noteMapper.toEntity(alertId, note));
      return noteMapper.toModel(upsertedNote);
   }

   public Note getNoteById(final String noteId) {
      NoteEntity noteById = noteRepository.getNoteById(noteId);
      if (noteById == null) {
         throw new CaisIllegalArgumentException();
      }
      return noteMapper.toModel(noteById);
   }

   public Map<String, Object> getAllNotes(String name, Date createdDateFrom, Date createdDateTo, @Valid int limit,
         @Valid int offset) {
      validateRequestParams(name, createdDateFrom, createdDateTo, offset,
            limit);

      try {

         List<NoteEntity> allNoteEntities = noteRepository.getAllNotes(name,
               createdDateFrom, createdDateTo, offset, limit);

         List<Note> allNotes = allNoteEntities.stream().map(a -> noteMapper.toModel(a))
               .collect(Collectors.toList());

         // Page<NoteEntity> workspaceEntityPages = routeToJpaMethod(name, offset,
         // limit, favourite, recent);
         // List<Note> alerts = workspaceEntityPages.getContent().stream().map(w ->
         // workspaceMapper.toModel(w))
         // .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("alerts", allNotes);
         response.put("count", allNotes.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving alerts");
      }
   }

   public List<Note> createNotes(List<Note> alerts) {
      List<Note> createdNotes = alerts.stream().map(a -> createNote(a)).collect(Collectors.toList());
      return createdNotes;
   }

   private void validateRequestParams(String name, Date createdDateFrom, Date createdDateTo, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty()) {
         if (name.length() > 20) {
            errorMessage.append("name cannot be longer than 20 characters;");
         }
      }

      if (createdDateFrom != null && createdDateTo != null) {
         if (createdDateFrom.after(createdDateTo)) {
            errorMessage.append("from date cannot be after to date;");
         }
      }

      if (limit < 0) {
         errorMessage.append("limit cannot be negative;");
      }
      if (offset < 0) {
         errorMessage.append("offset cannot be negative;");
      }
      if (errorMessage.isEmpty()) {
         return;
      }

      throw new CaisIllegalArgumentException(errorMessage.toString());
   }

}
