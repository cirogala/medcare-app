package it.medcare.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import it.medcare.billing.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentDTO {

    private Long paymentId;
    private Long prenotationId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
