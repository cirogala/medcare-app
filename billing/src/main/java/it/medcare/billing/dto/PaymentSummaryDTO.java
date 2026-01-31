package it.medcare.billing.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentSummaryDTO {

    private int paidCount;
    private int pendingCount;
    private int refundedCount;
    private BigDecimal totalPaidAmount;
    private BigDecimal totalPendingAmount;
}
