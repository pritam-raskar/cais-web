package com.dair.cais.cases.note.service;

import com.dair.cais.cases.note.CaseNote;
import com.dair.cais.cases.note.entity.CaseNoteEntity;
import com.dair.cais.cases.note.mapper.CaseNoteMapper;
import com.dair.cais.cases.note.repository.CaseNoteRepository;
import com.dair.cais.cases.repository.CaseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing case notes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseNoteService {

    private final CaseNoteRepository caseNoteRepository;
    private final CaseNoteMapper caseNoteMapper;
    private final CaseRepository caseRepository;

    /**
     * Add a note to a case.
     *
     * @param caseId the case ID
     * @param note   the note to add
     * @return the added note
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional
    public CaseNote addNote(Long caseId, CaseNote note) {
        log.debug("Adding note to case ID: {}", caseId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        // Set case ID in note
        note.setCaseId(caseId);

        // Set timestamps if not already set
        LocalDateTime now = LocalDateTime.now();
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(now);
        }
        if (note.getUpdatedAt() == null) {
            note.setUpdatedAt(now);
        }

        // Save note
        CaseNoteEntity entity = caseNoteMapper.toEntity(note);
        CaseNoteEntity savedEntity = caseNoteRepository.save(entity);

        log.info("Added note with ID: {} to case ID: {}", savedEntity.getNoteId(), caseId);
        return caseNoteMapper.toModel(savedEntity);
    }

    /**
     * Get all notes for a case.
     *
     * @param caseId the case ID
     * @return list of notes
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional(readOnly = true)
    public List<CaseNote> getNotes(Long caseId) {
        log.debug("Getting notes for case ID: {}", caseId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        List<CaseNoteEntity> entities = caseNoteRepository.findByCaseId(caseId);
        return caseNoteMapper.toModelList(entities);
    }

    /**
     * Update a case note.
     *
     * @param noteId the note ID
     * @param note   the updated note
     * @return the updated note
     * @throws EntityNotFoundException if the note is not found
     */
    @Transactional
    public CaseNote updateNote(Long noteId, CaseNote note) {
        log.debug("Updating note with ID: {}", noteId);

        CaseNoteEntity existingEntity = caseNoteRepository.findById(noteId)
                .orElseThrow(() -> {
                    log.error("Note not found with ID: {}", noteId);
                    return new EntityNotFoundException("Note not found with ID: " + noteId);
                });

        // Update content
        existingEntity.setContent(note.getContent());
        existingEntity.setUpdatedAt(LocalDateTime.now());

        CaseNoteEntity updatedEntity = caseNoteRepository.save(existingEntity);

        log.info("Updated note with ID: {}", noteId);
        return caseNoteMapper.toModel(updatedEntity);
    }

    /**
     * Delete a case note.
     *
     * @param noteId the note ID
     * @throws EntityNotFoundException if the note is not found
     */
    @Transactional
    public void deleteNote(Long noteId) {
        log.debug("Deleting note with ID: {}", noteId);

        if (!caseNoteRepository.existsById(noteId)) {
            log.error("Note not found with ID: {}", noteId);
            throw new EntityNotFoundException("Note not found with ID: " + noteId);
        }

        caseNoteRepository.deleteById(noteId);
        log.info("Deleted note with ID: {}", noteId);
    }
}