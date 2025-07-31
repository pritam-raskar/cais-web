package com.dair.cais.note;

import com.dair.cais.note.exception.NoteException;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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






   @Transactional
   public NoteExtended addNote(String note, String alertId, String createdBy, String entity, String entityValue) {
      log.info("Adding note for alertId: {}, createdBy: {}", alertId, createdBy);
      
      validateNoteInput(note, alertId, createdBy, entity);
      
      try {
         NoteEntityExtended addnote = noteRepository.addNote(note, alertId, createdBy, entity, entityValue);
         log.info("Successfully added note for alertId: {}", alertId);
         return noteMapperExtended.toModel(addnote);
      } catch (Exception e) {
         log.error("Failed to add note for alertId: {}, error: {}", alertId, e.getMessage());
         throw new NoteException("Failed to add note: " + e.getMessage(), e);
      }
   }
   
   private void validateNoteInput(String note, String alertId, String createdBy, String entity) {
      if (!StringUtils.hasText(note)) {
         throw new NoteException("Note content cannot be empty");
      }
      
      if (note.length() > 1000) {
         throw new NoteException("Note content cannot exceed 1000 characters");
      }
      
      if (!StringUtils.hasText(alertId)) {
         throw new NoteException("Alert ID cannot be empty");
      }
      
      if (!StringUtils.hasText(createdBy)) {
         throw new NoteException("Created by cannot be empty");
      }
      
      if (!StringUtils.hasText(entity)) {
         throw new NoteException("Entity cannot be empty");
      }
   }


   public List<NoteExtended> fetchNotes(String alertId) {
      log.debug("Fetching notes for alertId: {}", alertId);
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
      if (errorMessage.length() > 0) {
         log.warn("Validation failed: {}", errorMessage.toString());
         throw new NoteException(errorMessage.toString());
      }
   }

}
