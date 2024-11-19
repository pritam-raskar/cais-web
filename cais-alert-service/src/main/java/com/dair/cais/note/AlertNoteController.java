package com.dair.cais.note;

import com.dair.cais.audit.AuditLogRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Dair
 * @since 2022
 */

@RestController
@RequestMapping("/alerts/notes")
@Tag(name = "alert notes")

public class AlertNoteController {

   @Autowired
   private NoteService noteService;

   @Autowired
   private NoteServiceWithAudit noteServiceWithAudit;


   @PostMapping(value = "/addnote-audit/{alertId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @Operation(summary = "Add a note with audit logging")
   public ResponseEntity<NoteExtended> addNoteWithAudit(
           @RequestParam("note") String note,
           @PathVariable String alertId,
           @RequestParam("createdBy") String createdBy,
           @RequestParam("entity") String entity,
           @RequestParam("entityValue") String entityValue,
           @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {

      ObjectMapper objectMapper = new ObjectMapper();
      AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);

      // Validate audit request
      if (auditLogRequest.getUserId() == null) {
         throw new IllegalArgumentException("userId cannot be null");
      }

      NoteExtended createdNote = noteServiceWithAudit.addNoteWithAudit(
              note,
              alertId,
              createdBy,
              entity,
              entityValue,
              auditLogRequest
      );

      return ResponseEntity.ok().body(createdNote);
   }

   @PostMapping(value = "/addnote-multi-alerts-audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   @Operation(summary = "Add a note to multiple alerts with audit logging")
   public ResponseEntity<Map<String, NoteExtended>> addNoteToMultipleAlertsWithAudit(
           @RequestParam("note") String note,
           @RequestParam("alertIds") String alertIdsJson,
           @RequestParam("createdBy") String createdBy,
           @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {

      ObjectMapper objectMapper = new ObjectMapper();
      List<String> alertIds = objectMapper.readValue(alertIdsJson, new TypeReference<List<String>>() {});
      AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);

      // Validate audit request
      if (auditLogRequest.getUserId() == null) {
         throw new IllegalArgumentException("userId cannot be null");
      }

      // Note that we're not using entity and entityValue from parameters anymore
      Map<String, NoteExtended> results = noteServiceWithAudit.addNoteToMultipleAlertsWithAudit(
              note,
              alertIds,
              createdBy,
              "Alert",  // Hardcoded as "Alert"
              null,     // This will be set to alertId for each note
              auditLogRequest
      );

      return ResponseEntity.ok().body(results);
   }

   @GetMapping("/getNotes-audit")
   @Operation(summary = "Fetch all notes for an alert with audit logging")
   public ResponseEntity<List<NoteExtended>> fetchNotesWithAudit(
           @PathVariable String alertId,
           @RequestParam("auditLogRequest") String auditLogRequestJson) throws IOException {

      ObjectMapper objectMapper = new ObjectMapper();
      AuditLogRequest auditLogRequest = objectMapper.readValue(auditLogRequestJson, AuditLogRequest.class);

      List<NoteExtended> notes = noteServiceWithAudit.getNotesWithAudit(alertId, auditLogRequest);
      return ResponseEntity.ok().body(notes);
   }



   @PostMapping("/addnote")
   @Operation(summary = "Add a note")
   public ResponseEntity<NoteExtended> AddNote(
           @RequestParam("note") String note,
           @PathVariable String alertId,
           @RequestParam("createdBy") String createdBy,
           @RequestParam("entity") String entity,
           @RequestParam("entityValue") String entityValue) {

      NoteExtended createdNote = noteService.addNote(note, alertId, createdBy, entity, entityValue );
      return ResponseEntity.ok().body(createdNote);
   }

   @GetMapping("/getNotes")
   @Operation(summary = "Fetch all notes for an alert")
   public ResponseEntity<List<NoteExtended>> fetchNotes(
           @PathVariable String alertId) {
      List<NoteExtended> listOfNotes = noteService.fetchNotes(alertId);
      return  ResponseEntity.ok().body(listOfNotes);
   }


   @PostMapping
   @Operation(summary = "Create a note")
   public ResponseEntity<Note> createNote(@RequestBody Note note) {
      Note createdNote = noteService.createNote(note);
      return ResponseEntity.ok().body(createdNote);
   }

   @PatchMapping("{noteId}")
   @Operation(summary = "Update a note")
   public ResponseEntity<Note> patchNote(@PathVariable final String noteId, @RequestBody Note note) {
      Note updatedNote = noteService.patchNote(noteId, note);
      return ResponseEntity.ok().body(updatedNote);
   }

   @PostMapping("/bulk")
   @Operation(summary = "Create bulk notes")
   public ResponseEntity<List<Note>> createNotes(@RequestBody List<Note> notes) {
      List<Note> createdNotes = noteService.createNotes(notes);
      return ResponseEntity.ok().body(createdNotes);
   }

//   @GetMapping("{noteId}")
//   @Operation(summary = "Get a note by its id")
//   public ResponseEntity<Note> getNoteById(@PathVariable final String noteId) {
//      Note noteById = noteService.getNoteById(noteId);
//      return ResponseEntity.ok().body(noteById);
//   }

   @GetMapping("")
   @Operation(summary = "Get all notes; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllNotes(
         @RequestParam(required = false) String name,
         @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
         @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {
      return ResponseEntity.ok()
            .body(noteService.getAllNotes(name,
                  createdDateFrom, createdDateTo, limit, offset));
   }
}