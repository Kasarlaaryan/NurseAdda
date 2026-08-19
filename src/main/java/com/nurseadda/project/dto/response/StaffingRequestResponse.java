package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffingRequestResponse {

    private Long id;
    private String clientName;
    private String designation;
    private String location;
    private String shift;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfStaff;
    private String requiredSkills;
    private String status;
    private LocalDateTime deadline;
    private boolean overdue;
    private long hoursRemaining;
    private long minutesRemaining;
    private BigDecimal estimatedTotal;
    private BigDecimal advanceAmount;
    private boolean advancePaid;
    private LocalDateTime createdAt;

    /**
     * Calculate remaining time from deadline. Call after setting deadline.
     */
    public void calculateRemainingTime() {
        if (deadline == null) {
            this.overdue = false;
            this.hoursRemaining = 0;
            this.minutesRemaining = 0;
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline)) {
            this.overdue = true;
            this.hoursRemaining = 0;
            this.minutesRemaining = 0;
        } else {
            this.overdue = false;
            Duration remaining = Duration.between(now, deadline);
            this.hoursRemaining = remaining.toHours();
            this.minutesRemaining = remaining.toMinutesPart();
        }
    }
}
