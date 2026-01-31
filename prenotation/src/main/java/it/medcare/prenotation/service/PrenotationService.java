package it.medcare.prenotation.service;

import java.util.List;

import it.medcare.prenotation.dto.CreatePrenotationRequest;
import it.medcare.prenotation.dto.PrenotationDTO;
import it.medcare.prenotation.entity.Prenotation;

public interface PrenotationService {
	
	Prenotation createPrenotation(CreatePrenotationRequest req, Long userId);
	List<PrenotationDTO> getPrenotazioniUtente(Long utenteId);
	List<PrenotationDTO> getPrenotazioniMedico(Long doctorId);
	List<PrenotationDTO> getPrenotazioniAdmin(java.time.LocalDate from, java.time.LocalDate to);
	PrenotationDTO getPrenotazioneById(Long prenotationId);
	void deletePrenotazione(Long prenotationId, Long userId);
	void markCompleted(Long prenotationId, Long doctorId);
}
