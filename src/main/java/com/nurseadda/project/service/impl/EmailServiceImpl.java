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
}
