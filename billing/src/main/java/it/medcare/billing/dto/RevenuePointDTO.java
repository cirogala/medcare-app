package it.medcare.billing.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RevenuePointDTO {

    private String label;
    private BigDecimal totalAmount;
}
