package com.nurseadda.project.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;

    @Builder.Default
    private List<String> errors = null;

    /** Number of login attempts remaining before the account is locked. */
    private Integer remainingAttempts;

    /** Seconds until a locked account is unlocked (only when locked). */
    private Long lockoutSeconds;
}
