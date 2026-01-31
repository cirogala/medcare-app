package it.medcare.billing.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RevenueBySpecializationDTO {

    private String specialization;
    private BigDecimal totalAmount;
}
