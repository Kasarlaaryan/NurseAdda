package com.nurseadda.project.repository;

import com.nurseadda.project.entity.StaffDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffDocumentRepository extends JpaRepository<StaffDocument, Long> {

    List<StaffDocument> findByStaffProfileId(Long staffProfileId);

    List<StaffDocument> findByStaffProfileUserId(Long userId);
}
