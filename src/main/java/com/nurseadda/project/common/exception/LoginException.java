package com.nurseadda.project.common.exception;

import lombok.Getter;

@Getter
public class LoginException extends RuntimeException {

    private final Integer remainingAttempts;
    private final Long lockoutSeconds;

    public LoginException(String message, Integer remainingAttempts, Long lockoutSeconds) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.lockoutSeconds = lockoutSeconds;
    }
}
