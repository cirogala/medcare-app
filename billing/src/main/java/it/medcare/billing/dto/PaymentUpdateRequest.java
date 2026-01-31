package it.medcare.billing.dto;

import it.medcare.billing.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentUpdateRequest {

    private PaymentStatus status;
}
