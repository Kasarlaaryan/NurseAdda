package com.nurseadda.project.repository;

import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.enums.StaffDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StaffDocumentRepository extends JpaRepository<StaffDocument, Long> {

    List<StaffDocument> findByStaffProfileId(Long staffProfileId);

    /**
     * Batch load documents for many staff profiles in a single IN query.
     */
    List<StaffDocument> findByStaffProfileIdIn(Collection<Long> staffProfileIds);

    void deleteByStaffProfileIdAndDocumentType(Long staffProfileId, StaffDocumentType documentType);
}
