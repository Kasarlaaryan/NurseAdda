package com.nurseadda.project.service;

public interface EmailService {

    void sendOtp(String to, String otp);

    void sendProfileVerifiedEmail(String to, String firstName);

    void sendProfileRejectedEmail(String to, String firstName);

    void sendDocumentReplacementRequestEmail(String to, String firstName, String documentType, String reason);

    void sendDocumentSubmittedEmail(String to, String staffName, String documentType);
}
