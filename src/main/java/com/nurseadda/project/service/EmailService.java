package com.nurseadda.project.service;

public interface EmailService {

    void sendOtp(String to, String otp);

    void sendProfileVerifiedEmail(String to, String firstName);

    void sendProfileRejectedEmail(String to, String firstName);
}
