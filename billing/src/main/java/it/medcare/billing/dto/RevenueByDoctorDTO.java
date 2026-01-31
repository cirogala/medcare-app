package it.medcare.billing.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RevenueByDoctorDTO {

    private Long doctorId;
    private String doctorName;
    private BigDecimal totalAmount;
}
