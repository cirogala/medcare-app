package it.medcare.prenotation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import it.medcare.prenotation.entity.VisitSlot;

public interface VisitSlotRepository extends JpaRepository<VisitSlot, Long> {
	
    List<VisitSlot> findByVisitVisitIdAndVisitDateAndAvailableTrue(
        Long visitId, LocalDate visitDate
    );

    List<VisitSlot> findByDoctorIdAndVisitVisitIdAndVisitDateAndAvailableTrue(
        Long doctorId, Long visitId, LocalDate visitDate
    );

    List<VisitSlot> findByDoctorIdAndVisitDate(
        Long doctorId, LocalDate visitDate
    );

    java.util.Optional<VisitSlot> findBySlotIdAndDoctorId(Long slotId, Long doctorId);

    @Query(
        value = "select distinct doctor_id from visit_slot " +
                "where visit_id = :visitId " +
                "and visit_date = CONVERT(date, :visitDate) " +
                "and start_time = CONVERT(time, :startTime) " +
                "and available = 1",
        nativeQuery = true)
    
    List<Long> findAvailableDoctorIds(
        @Param("visitId") Long visitId,
        @Param("visitDate") String visitDate,
        @Param("startTime") String startTime);

    @Query(
        value = "select case when count(1) > 0 then 1 else 0 end from visit_slot " +
                "where visit_id = :visitId " +
                "and doctor_id = :doctorId " +
                "and visit_date = CONVERT(date, :visitDate) " +
                "and start_time = CONVERT(time, :startTime)",
        nativeQuery = true)
    
    int existsSlot(Long visitId, Long doctorId, String visitDate, String startTime);


    @Query(
        value = "select top 1 * from visit_slot " +
                "where visit_id = :visitId " +
                "and doctor_id = :doctorId " +
                "and visit_date = CONVERT(date, :visitDate) " +
                "and start_time = CONVERT(time, :startTime) " +
                "and available = 1",
        nativeQuery = true)
    
    Optional<VisitSlot> findAvailableSlot(Long visitId, Long doctorId, String visitDate, String startTime);

}
