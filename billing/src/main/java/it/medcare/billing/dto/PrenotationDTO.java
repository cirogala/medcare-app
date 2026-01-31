package it.medcare.billing.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Data;

@Data
public class PrenotationDTO {

    private Long prenotationId;
    private Long userId;
    private Long doctorId;
    private Long visitTypeId;
    private Long slotId;
    private LocalDate date;
    private LocalTime slotTime;
    private String status;
    private LocalDateTime createdAt;
    private Boolean flDeleted;
    private LocalDateTime deletedAt;
}
