package it.medcare.prenotation.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Claims;
import it.medcare.prenotation.common.rest.Headers;
import it.medcare.prenotation.common.rest.MediaType;
import it.medcare.prenotation.config.MedcareSecurityConfig;
import it.medcare.prenotation.common.utils.JwtUtils;
import it.medcare.prenotation.dto.VisitSlotDTO;
import it.medcare.prenotation.entity.VisitSlot;
import it.medcare.prenotation.enums.PrenotationStatus;
import it.medcare.prenotation.repository.PrenotationRepository;
import it.medcare.prenotation.repository.VisitSlotRepository;
import it.medcare.prenotation.service.JwtService;
import it.medcare.prenotation.service.VisitSlotGenerationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/visits/slots")
@RequiredArgsConstructor
public class VisitSlotController {

    private final VisitSlotGenerationService slotGenerationService;
    private final VisitSlotRepository slotRepository;
    private final PrenotationRepository prenotationRepository;
    private final JwtService jwtService;

    @MedcareSecurityConfig(enableAnonymous = true)
    @PostMapping(value = "/generate", produces = MediaType.PRENOTATION_V1)
    public ResponseEntity<Void> generateSlots(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestParam(required = false) Long doctorId) {

        if (doctorId != null) {
        	
            slotGenerationService.generateSlotsForDoctor(doctorId, LocalDate.now(), LocalDate.now().plusMonths(6));
            
        } else { 
        	
            slotGenerationService.generateSlots(LocalDate.now(), LocalDate.now().plusMonths(6));
        }

        return ResponseEntity.accepted().build();
    }

    @GetMapping(value = "/doctor", produces = MediaType.PRENOTATION_V1)
    public ResponseEntity<List<VisitSlotDTO>> getDoctorSlots(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @RequestParam LocalDate date,
            HttpServletRequest httpRequest) {

        Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));
        Long doctorId = claims.get("userId", Long.class);

        List<VisitSlot> slots = slotRepository.findByDoctorIdAndVisitDate(doctorId, date);
        Set<String> bookedTimes = prenotationRepository.findByDoctorIdAndDateAndFlDeletedFalse(doctorId, date)
            .stream()
            .filter(p -> p.getStatus() != PrenotationStatus.CANCELLED)
            .map(p -> p.getSlotTime() == null ? null : p.getSlotTime().toString())
            .filter(value -> value != null)
            .collect(Collectors.toSet());

        List<VisitSlotDTO> response = slots.stream()
            .map(slot -> {
                boolean booked = bookedTimes.contains(slot.getStartTime().toString());
                boolean available = Boolean.TRUE.equals(slot.getAvailable()) && !booked;
                return new VisitSlotDTO(
                    slot.getSlotId(),
                    slot.getVisitDate(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    available,
                    booked
                );
            })
            .toList();

        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{slotId}/availability", produces = MediaType.PRENOTATION_V1)
    public ResponseEntity<Void> updateAvailability(
            @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
            @PathVariable Long slotId,
            @RequestParam boolean available,
            HttpServletRequest httpRequest) {

        Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));
        Long doctorId = claims.get("userId", Long.class);

        slotRepository.findBySlotIdAndDoctorId(slotId, doctorId)
            .ifPresent(slot -> {
                slot.setAvailable(available);
                slotRepository.save(slot);
            });

        return ResponseEntity.noContent().build();
    }
}
