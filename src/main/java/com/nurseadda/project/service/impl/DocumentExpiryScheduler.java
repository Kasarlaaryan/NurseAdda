package com.nurseadda.project.service.impl;

import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Automatically flags verified staff documents whose expiry date has passed so
 * that a replacement must be submitted. Only already-verified documents are
 * flagged (documents pending review are left for the administrator to decide).
 * Runs daily at 02:00.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentExpiryScheduler {

    private static final String AUTO_EXPIRY_REASON = "Document has expired";

    private final StaffDocumentRepository staffDocumentRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 2 * * *")
    public void flagExpiredDocuments() {
        List<StaffDocument> expiredDocuments = staffDocumentRepository.findByExpiryDateBefore(LocalDate.now());

        int flagged = 0;
        for (StaffDocument document : expiredDocuments) {
            if (!document.isVerified() || document.isReplacementRequested()) {
                continue;
            }
            document.setReplacementRequested(true);
            document.setRequestReason(AUTO_EXPIRY_REASON);
            staffDocumentRepository.save(document);
            emailService.sendDocumentReplacementRequestEmail(
                    document.getStaffProfile().getUser().getEmail(),
                    document.getStaffProfile().getUser().getFirstName(),
                    document.getDocumentType().name(),
                    AUTO_EXPIRY_REASON
            );
            flagged++;
        }

        if (flagged > 0) {
            log.info("Auto-flagged {} expired staff documents for replacement", flagged);
        }
    }
}
