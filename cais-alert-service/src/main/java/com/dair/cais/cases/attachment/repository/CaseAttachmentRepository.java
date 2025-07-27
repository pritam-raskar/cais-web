package com.dair.cais.cases.attachment.repository;

import com.dair.cais.cases.attachment.entity.CaseAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Case Attachments.
 */
@Repository
public interface CaseAttachmentRepository extends JpaRepository<CaseAttachmentEntity, Long> {

    /**
     * Find all attachments for a case.
     *
     * @param caseId the case ID
     * @return list of case attachments
     */
    List<CaseAttachmentEntity> findByCaseId(Long caseId);

    /**
     * Delete all attachments for a case.
     *
     * @param caseId the case ID
     */
    void deleteByCaseId(Long caseId);
}