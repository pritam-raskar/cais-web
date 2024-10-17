package com.dair.cais.fileattachement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmAttachmentRepository extends JpaRepository<CmAttachment, Long> {
    List<CmAttachment> findByAlertId(String alertId);
}