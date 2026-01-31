package it.medcare.prenotation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitSlotDTO {
    private Long slotId;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private boolean booked;
}
