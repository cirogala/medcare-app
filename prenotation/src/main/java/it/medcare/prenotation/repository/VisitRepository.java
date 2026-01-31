package it.medcare.prenotation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.medcare.prenotation.entity.Visit;

public interface VisitRepository extends JpaRepository<Visit, Long> {
	
	
}



