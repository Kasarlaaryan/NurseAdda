package com.nurseadda.project.service.impl;

import com.nurseadda.project.service.EmailService;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final Resend resend;
    private final String from;

    public EmailServiceImpl(
            @Value("${app.email.resend-api-key}") String resendApiKey,
            @Value("${app.email.from}") String from
    ) {
        this.from = from;
        this.resend = (resendApiKey == null || resendApiKey.isBlank())
                ? null
                : new Resend(resendApiKey);
    }

    @Override
    public void sendOtp(String to, String otp) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - OTP for {} : {}", to, otp);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject("NurseAdda OTP Verification")
                    .html("<p>Your NurseAdda OTP is: <b>" + otp + "</b>. It expires in 10 minutes.</p>")
                    .build();
            resend.emails().send(email);
            log.info("OTP email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {} : {}", to, e.getMessage());
        }
    }

    @Override
    public void sendProfileVerifiedEmail(String to, String firstName) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - profile verified email to {} for {}", to, firstName);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject("NurseAdda - Profile Verified")
                    .html("<p>Hi <b>" + firstName + "</b>,</p>"
                            + "<p>Congratulations! Your NurseAdda profile has been <b>verified</b> "
                            + "by the administrator.</p>"
                            + "<p>You can now use the portal to find and accept assignments.</p>")
                    .build();
            resend.emails().send(email);
            log.info("Profile verified email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send profile verified email to {} : {}", to, e.getMessage());
        }
    }

    @Override
    public void sendDocumentSubmittedEmail(String to, String staffName, String documentType) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - document submitted email to {}", to);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject("NurseAdda - New Document Submitted for Review")
                    .html("<p>Hi,</p>"
                            + "<p>Staff member <b>" + staffName + "</b> has submitted a new "
                            + "<b>" + documentType.replace("_", " ") + "</b> for review.</p>"
                            + "<p>Please log in to the admin console to verify the document and "
                            + "re-verify the staff profile once all documents are approved.</p>")
                    .build();
            resend.emails().send(email);
            log.info("Document submitted email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send document submitted email to {} : {}", to, e.getMessage());
        }
    }

    @Override
    public void sendProfileRejectedEmail(String to, String firstName) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - profile rejected email to {} for {}", to, firstName);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject("NurseAdda - Profile Not Verified")
                    .html("<p>Hi <b>" + firstName + "</b>,</p>"
                            + "<p>We regret to inform you that your NurseAdda profile could not be "
                            + "<b>verified</b> by the administrator at this time.</p>"
                            + "<p>This may be due to missing or invalid documents. Please update your "
                            + "profile and documents and contact support for re-verification.</p>")
                    .build();
            resend.emails().send(email);
            log.info("Profile rejected email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send profile rejected email to {} : {}", to, e.getMessage());
        }
    }

    @Override
    public void sendDocumentReplacementRequestEmail(String to, String firstName, String documentType, String reason) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - document replacement request email to {} for {}", to, firstName);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject("NurseAdda - Document Update Required")
                    .html("<p>Hi <b>" + firstName + "</b>,</p>"
                            + "<p>Your <b>" + documentType.replace("_", " ") + "</b> needs to be updated "
                            + "before you can continue using the portal.</p>"
                            + (reason == null || reason.isBlank() ? ""
                                    : "<p>Reason: <b>" + reason + "</b></p>")
                            + "<p>Please upload a new document from your profile page as soon as possible.</p>")
                    .build();
            resend.emails().send(email);
            log.info("Document replacement request email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send document replacement request email to {} : {}", to, e.getMessage());
        }
    }
}
