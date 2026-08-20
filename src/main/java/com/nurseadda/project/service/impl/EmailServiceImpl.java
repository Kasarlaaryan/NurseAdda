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
    public void sendAssignmentAcceptedEmail(String adminEmail, String staffName, String designation, String location) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - assignment accepted notification to admin {} : staff={}, designation={}, location={}",
                    adminEmail, staffName, designation, location);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(adminEmail)
                    .subject("NurseAdda - Staff Accepted Assignment")
                    .html("<p>Hi Admin,</p>"
                            + "<p><b>" + staffName + "</b> has accepted a staffing request.</p>"
                            + "<p><b>Details:</b></p>"
                            + "<ul>"
                            + "<li>Designation: " + designation + "</li>"
                            + "<li>Location: " + location + "</li>"
                            + "</ul>"
                            + "<p>Please review and approve the staff details to send them to the client.</p>")
                    .build();
            resend.emails().send(email);
            log.info("Assignment accepted email sent to admin {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send assignment accepted email to {} : {}", adminEmail, e.getMessage());
        }
    }

    @Override
    public void sendStaffApprovedToClientEmail(String clientEmail, String clientName, String staffName, String designation, String location) {
        if (resend == null) {
            log.info("No RESEND_API_KEY configured - staff approved notification to client {} : staff={}, designation={}, location={}",
                    clientEmail, staffName, designation, location);
            return;
        }

        try {
            CreateEmailOptions email = CreateEmailOptions.builder()
                    .from(from)
                    .to(clientEmail)
                    .subject("NurseAdda - Your Assigned Staff Details")
                    .html("<p>Hi <b>" + clientName + "</b>,</p>"
                            + "<p>Great news! We have assigned a staff member to your staffing request.</p>"
                            + "<p><b>Assigned Staff Details:</b></p>"
                            + "<ul>"
                            + "<li>Name: " + staffName + "</li>"
                            + "<li>Designation: " + designation + "</li>"
                            + "<li>Location: " + location + "</li>"
                            + "</ul>"
                            + "<p>You can view the full profile and documents in your dashboard.</p>")
                    .build();
            resend.emails().send(email);
            log.info("Staff approved email sent to client {}", clientEmail);
        } catch (Exception e) {
            log.error("Failed to send staff approved email to {} : {}", clientEmail, e.getMessage());
        }
    }
}
