package it.medcare.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class OverviewDTO {

    private LocalDate from;
    private LocalDate to;
    private int totalVisits;
    private int paidCount;
    private int pendingCount;
    private int refundedCount;
    private BigDecimal totalExpectedAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal averageTicket;
}
