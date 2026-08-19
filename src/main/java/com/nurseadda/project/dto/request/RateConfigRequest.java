package com.nurseadda.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateConfigRequest {

    @NotBlank(message = "Shift type is required (8HR or 12HR)")
    private String shiftType;

    @NotNull(message = "Staff hourly rate is required")
    @Positive(message = "Staff hourly rate must be positive")
    private BigDecimal staffHourlyRate;

    @NotNull(message = "Client hourly rate is required")
    @Positive(message = "Client hourly rate must be positive")
    private BigDecimal clientHourlyRate;

    private BigDecimal overtimeMultiplier;
}
