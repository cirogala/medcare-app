package it.medcare.billing.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class VisitDTO {

    private Long visitId;
    private String code;
    private String description;
    private Integer durationMin;
    private BigDecimal price;
    private String specialization;
    private Boolean flagDeleted;
}
