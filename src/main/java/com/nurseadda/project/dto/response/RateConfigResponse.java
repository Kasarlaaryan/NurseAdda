package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateConfigResponse {
    private Long id;
    private String shiftType;
    private BigDecimal staffHourlyRate;
    private BigDecimal clientHourlyRate;
    private BigDecimal overtimeMultiplier;
}
