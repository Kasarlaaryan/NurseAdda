package com.nurseadda.project.service.impl;

import com.nurseadda.project.entity.StaffDocument;
import com.nurseadda.project.entity.StaffProfile;
import com.nurseadda.project.entity.User;
import com.nurseadda.project.enums.StaffDocumentType;
import com.nurseadda.project.repository.StaffDocumentRepository;
import com.nurseadda.project.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentExpirySchedulerTest {

    @Mock
    private StaffDocumentRepository staffDocumentRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DocumentExpiryScheduler scheduler;

    private StaffDocument expiredDocument(boolean verified, boolean replacementRequested) {
        User user = new User();
        user.setEmail("rohan@test.com");
        user.setFirstName("Rohan");

        StaffProfile profile = new StaffProfile();
        profile.setUser(user);

        StaffDocument doc = new StaffDocument();
        doc.setId(100L);
        doc.setStaffProfile(profile);
        doc.setDocumentType(StaffDocumentType.STATE_BOARD_CERTIFICATE);
        doc.setExpiryDate(LocalDate.now().minusDays(1));
        doc.setVerified(verified);
        doc.setReplacementRequested(replacementRequested);
        return doc;
    }

    @Test
    @DisplayName("flagExpiredDocuments: flags verified expired documents and emails staff")
    void flagExpiredDocuments_flagsVerifiedExpiredAndEmails() {
        StaffDocument doc = expiredDocument(true, false);
        when(staffDocumentRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(doc));

        scheduler.flagExpiredDocuments();

        assertThat(doc.isReplacementRequested()).isTrue();
        assertThat(doc.getRequestReason()).isEqualTo("Document has expired");
        verify(staffDocumentRepository).save(doc);
        verify(emailService).sendDocumentReplacementRequestEmail(
                "rohan@test.com", "Rohan", "STATE_BOARD_CERTIFICATE", "Document has expired");
    }

    @Test
    @DisplayName("flagExpiredDocuments: skips documents pending review (not yet verified)")
    void flagExpiredDocuments_skipsUnverified() {
        StaffDocument doc = expiredDocument(false, false);
        when(staffDocumentRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(doc));

        scheduler.flagExpiredDocuments();

        assertThat(doc.isReplacementRequested()).isFalse();
        assertThat(doc.getRequestReason()).isNull();
        verify(staffDocumentRepository, never()).save(any());
        verify(emailService, never()).sendDocumentReplacementRequestEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("flagExpiredDocuments: skips documents already flagged for replacement")
    void flagExpiredDocuments_skipsAlreadyRequested() {
        StaffDocument doc = expiredDocument(true, true);
        when(staffDocumentRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(doc));

        scheduler.flagExpiredDocuments();

        assertThat(doc.getRequestReason()).isNull();
        verify(staffDocumentRepository, never()).save(any());
        verify(emailService, never()).sendDocumentReplacementRequestEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("flagExpiredDocuments: no expired documents means no work")
    void flagExpiredDocuments_noExpired_doesNothing() {
        when(staffDocumentRepository.findByExpiryDateBefore(any(LocalDate.class)))
                .thenReturn(List.of());

        scheduler.flagExpiredDocuments();

        verify(staffDocumentRepository, never()).save(any());
        verify(emailService, never()).sendDocumentReplacementRequestEmail(anyString(), anyString(), anyString(), anyString());
    }
}
