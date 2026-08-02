package com.nurseadda.project.repository;

import com.nurseadda.project.entity.StaffDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffDocumentRepository extends JpaRepository<StaffDocument, Long> {

    List<StaffDocument> findByStaffProfileId(Long staffProfileId);

    void deleteByStaffProfileIdAndDocumentType(Long staffProfileId, com.nurseadda.project.enums.StaffDocumentType documentType);
}
