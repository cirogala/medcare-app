package it.medcare.prenotation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class CreatePrenotationRequest {

    private Long visitTypeId;
    private Long doctorId;
    private LocalDate date;
    private LocalTime slotTime;

}
