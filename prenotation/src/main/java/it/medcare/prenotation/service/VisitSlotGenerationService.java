package it.medcare.prenotation.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.medcare.prenotation.client.ProfilingClient;

import it.medcare.prenotation.dto.DoctorProfileDTO;
import it.medcare.prenotation.entity.Visit;
import it.medcare.prenotation.entity.VisitSlot;
import it.medcare.prenotation.repository.VisitRepository;
import it.medcare.prenotation.repository.VisitSlotRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisitSlotGenerationService {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);
    private static final int SLOT_MINUTES = 60;
    private static final int MONTHS_AHEAD = 6;
    private final VisitRepository visitRepository;
    private final VisitSlotRepository slotRepository;
    private final ProfilingClient profilingClient;

    @Scheduled(cron = "0 0 1 * * *")//all'1 di notte
    @Transactional
    public void generateSlotsJob() {
    	
        generateSlots(LocalDate.now(), LocalDate.now().plusMonths(MONTHS_AHEAD));
    }

    public void generateSlots(LocalDate startDate, LocalDate endDate) {
    	
        List<DoctorProfileDTO> doctors = profilingClient.fetchDoctors();
        
        if (!doctors.isEmpty()) {
        	
            for (DoctorProfileDTO doctor : doctors) {
                generateSlotsForDoctor(doctor, startDate, endDate);
            }
        }
    }

    public void generateSlotsForDoctor(Long doctorId, LocalDate startDate, LocalDate endDate) {
    	
        if (doctorId == null) {
        	
            return;
        }

        DoctorProfileDTO doctor = profilingClient.fetchDoctorById(doctorId);
        
        if (doctor == null) {
        	
            return;
        }

        generateSlotsForDoctor(doctor, startDate, endDate);
    }

    private void generateSlotsForDoctor(DoctorProfileDTO doctor, LocalDate startDate, LocalDate endDate) {
    	
        if (doctor == null || doctor.getUserId() == null) {
        	
            return;
        }

        String specialization = normalizeSpecialization(doctor.getTypeDoctor());
        
        if (specialization.isEmpty()) {
        	
            return;
        }

        List<Visit> visits = visitRepository.findAll()
            .stream()
            .filter(visit -> !Boolean.TRUE.equals(visit.getFlagDeleted()))
            .filter(visit -> specialization.equals(normalizeSpecialization(visit.getSpecialization())))
            .toList();

        if (visits.isEmpty()) {
        	
            return;
        }

        LocalDate date = startDate;
        
        while (!date.isAfter(endDate)) {
        	
            if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            	
                createSlotsForDate(visits, doctor.getUserId(), date);
            }
            date = date.plusDays(1);
        }
    }

    private void createSlotsForDate(List<Visit> visits, Long doctorId, LocalDate date) {
    	
        List<VisitSlot> toCreate = new ArrayList<>();

        for (Visit visit : visits) {
            LocalTime time = WORK_START;
            while (time.plusMinutes(SLOT_MINUTES).compareTo(WORK_END) <= 0) {
                if (slotRepository.existsSlot(
                        visit.getVisitId(),
                        doctorId,
                        date.toString(),
                        time.toString()) == 0) {
                    VisitSlot slot = new VisitSlot();
                    slot.setVisit(visit);
                    slot.setDoctorId(doctorId);
                    slot.setVisitDate(date);
                    slot.setStartTime(time);
                    slot.setEndTime(time.plusMinutes(SLOT_MINUTES));
                    slot.setAvailable(true);
                    toCreate.add(slot);
                }
                time = time.plusMinutes(SLOT_MINUTES);
            }
        }

        if (!toCreate.isEmpty()) {
        	
            slotRepository.saveAll(toCreate);
        }
    }

    private String normalizeSpecialization(String value) {
    	
        if (value == null) {
        	
            return "";
        }
        
        return value.trim().toLowerCase(Locale.ROOT);
    }

}
