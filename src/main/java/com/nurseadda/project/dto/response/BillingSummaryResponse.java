package com.nurseadda.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingSummaryResponse {
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;
    private BigDecimal totalPaidToStaff;
    private BigDecimal pendingStaffPayments;
    private BigDecimal profit;
}
