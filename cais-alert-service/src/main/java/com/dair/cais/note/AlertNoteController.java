package com.dair.cais.note;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Dair
 * @since 2022
 */

@RestController
@RequestMapping("/alerts/{alertId}/notes")
@Tag(name = "alert notes")

public class AlertNoteController {

   @Autowired
   private NoteService noteService;




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