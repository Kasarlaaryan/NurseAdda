package com.nurseadda.project.entity;

import com.nurseadda.project.enums.StaffDocumentType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "staff_documents")
public class StaffDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "staff_profile_id", nullable = false)
    private StaffProfile staffProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StaffDocumentType documentType;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    private boolean verified = false;

    private LocalDate expiryDate;

    private boolean replacementRequested = false;

    private String requestReason;
}
