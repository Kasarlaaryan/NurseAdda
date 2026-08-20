package com.nurseadda.project.service;

public interface EmailService {

    void sendOtp(String to, String otp);

    void sendProfileVerifiedEmail(String to, String firstName);

    void sendProfileRejectedEmail(String to, String firstName);

    void sendAssignmentAcceptedEmail(String adminEmail, String staffName, String designation, String location);

    void sendStaffApprovedToClientEmail(String clientEmail, String clientName, String staffName, String designation, String location);
}
