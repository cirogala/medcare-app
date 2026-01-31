package it.medcare.prenotation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.medcare.prenotation.entity.Prenotation;

public interface PrenotationRepository extends JpaRepository<Prenotation, Long> {
	
    List<Prenotation> findByUserId(Long userId);
    List<Prenotation> findByUserIdAndFlDeletedFalse(Long userId);
    List<Prenotation> findByDoctorIdAndFlDeletedFalse(Long doctorId);
    List<Prenotation> findByDoctorIdAndDateAndFlDeletedFalse(Long doctorId, java.time.LocalDate date);
    List<Prenotation> findByDateBetweenAndFlDeletedFalse(java.time.LocalDate from, java.time.LocalDate to);
    Optional<Prenotation> findByPrenotationIdAndUserIdAndFlDeletedFalse(Long prenotationId, Long userId);
    Optional<Prenotation> findByPrenotationIdAndDoctorIdAndFlDeletedFalse(Long prenotationId, Long doctorId);
}
