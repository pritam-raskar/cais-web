package com.dair.cais.cases.note.repository;

import com.dair.cais.cases.note.entity.CaseNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Case Notes.
 */
@Repository
public interface CaseNoteRepository extends JpaRepository<CaseNoteEntity, Long> {

    /**
     * Find all notes for a case.
     *
     * @param caseId the case ID
     * @return list of case notes
     */
    List<CaseNoteEntity> findByCaseId(Long caseId);

    /**
     * Delete all notes for a case.
     *
     * @param caseId the case ID
     */
    void deleteByCaseId(Long caseId);
}