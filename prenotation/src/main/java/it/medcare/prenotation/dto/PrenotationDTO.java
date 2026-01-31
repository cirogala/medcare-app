package it.medcare.prenotation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import it.medcare.prenotation.enums.PrenotationStatus;
import lombok.Data;

@Data
public class PrenotationDTO {

    private Long prenotationId;
    private Long userId;
    private Long doctorId;
    private Long visitTypeId;
    private LocalDate date;
    private LocalTime slotTime;
    private PrenotationStatus status;
}
