package it.medcare.prenotation.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.medcare.prenotation.constants.Constants;
import it.medcare.prenotation.client.NotificationClient;
import it.medcare.prenotation.client.ProfilingClient;
import it.medcare.prenotation.dto.CreatePrenotationRequest;
import it.medcare.prenotation.dto.NotificationPrenotationRequest;
import it.medcare.prenotation.dto.PrenotationDTO;
import it.medcare.prenotation.dto.UserProfileDTO;
import it.medcare.prenotation.entity.Prenotation;
import it.medcare.prenotation.enums.PrenotationStatus;
import it.medcare.prenotation.mapper.PrenotationMapper;
import it.medcare.prenotation.repository.PrenotationRepository;
import it.medcare.prenotation.repository.VisitRepository;
import it.medcare.prenotation.repository.VisitSlotRepository;
import it.medcare.prenotation.service.PrenotationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrenotationServiceImpl implements PrenotationService{

	private static final DateTimeFormatter SLOT_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final PrenotationRepository prenotationRepo;
	private final VisitSlotRepository slotRepository;
	private final PrenotationMapper prenotationMapper;
	private final ProfilingClient profilingClient;
	private final NotificationClient notificationClient;
	private final VisitRepository visitRepository;

	@Transactional
	public Prenotation createPrenotation(CreatePrenotationRequest req, Long userId) {

		var slot = slotRepository.findAvailableSlot(
			req.getVisitTypeId(),
			req.getDoctorId(),
			req.getDate().toString(),
			req.getSlotTime().format(SLOT_TIME_FORMAT)
		).orElseThrow(() -> new IllegalArgumentException(Constants.SLOT_NOT_AVAILABLE));

		// controllo se la data passata dall'utente è nel passato
		if (req.getDate().isBefore(LocalDate.now())) {

			throw new IllegalArgumentException(Constants.INVALID_DATE);
		}

		Prenotation p = new Prenotation();
		p.setUserId(userId);
		p.setDoctorId(req.getDoctorId());
		p.setVisitTypeId(req.getVisitTypeId());
		p.setSlotId(slot.getSlotId());
		p.setDate(req.getDate());
		p.setSlotTime(req.getSlotTime());
		p.setStatus(PrenotationStatus.ACTIVE);
		p.setCreatedAt(LocalDateTime.now());
		p.setFlDeleted(false);
		slot.setAvailable(false);

		Prenotation saved = prenotationRepo.save(p);
		notifyPrenotationCreated(saved);
		return saved;
	}

	public List<PrenotationDTO> getPrenotazioniUtente(Long utenteId) {

		return prenotationRepo.findByUserIdAndFlDeletedFalse(utenteId).stream().map(prenotationMapper::toDto).toList();
	}

	public List<PrenotationDTO> getPrenotazioniMedico(Long doctorId) {

		return prenotationRepo.findByDoctorIdAndFlDeletedFalse(doctorId).stream().map(prenotationMapper::toDto).toList();
	}

	public List<PrenotationDTO> getPrenotazioniAdmin(LocalDate from, LocalDate to) {
		return prenotationRepo.findByDateBetweenAndFlDeletedFalse(from, to).stream()
			.map(prenotationMapper::toDto)
			.toList();
	}

	public PrenotationDTO getPrenotazioneById(Long prenotationId) {
		
		Prenotation prenotation = prenotationRepo.findById(prenotationId)
			.orElseThrow(() -> new EntityNotFoundException(Constants.PRENOTATION_NF_OR_UNAUTH));
		
		return prenotationMapper.toDto(prenotation);
	}

	@Transactional
	public void markCompleted(Long prenotationId, Long doctorId) {

		Prenotation prenotation = prenotationRepo.findByPrenotationIdAndDoctorIdAndFlDeletedFalse(
				prenotationId, doctorId).orElseThrow(() -> new EntityNotFoundException(Constants.PRENOTATION_NF_OR_UNAUTH));

		prenotation.setStatus(PrenotationStatus.COMPLETED);
	}

	@Transactional
	public void deletePrenotazione(Long prenotationId, Long userId) {

		Prenotation prenotation = prenotationRepo.findByPrenotationIdAndUserIdAndFlDeletedFalse(
				prenotationId, userId).orElseThrow(() -> new EntityNotFoundException(Constants.PRENOTATION_NF_OR_UNAUTH));

		// faccio un check se è una prenotazione passata
		LocalDateTime prenotationDateTime =LocalDateTime.of(prenotation.getDate(), prenotation.getSlotTime());

		if (prenotationDateTime.isBefore(LocalDateTime.now())) {

			throw new IllegalStateException(Constants.PRENOTATION_IN_THE_PAST);
		}

		prenotation.setFlDeleted(true);
		prenotation.setDeletedAt(LocalDateTime.now());
		prenotation.setStatus(PrenotationStatus.CANCELLED);
	}

	private void notifyPrenotationCreated(Prenotation prenotation) {
		
		try {
			UserProfileDTO patient = profilingClient.fetchExternalUserById(prenotation.getUserId());
			var doctor = profilingClient.fetchDoctorById(prenotation.getDoctorId());
			String visitType = visitRepository.findById(prenotation.getVisitTypeId())
				.map(v -> v.getDescription())
				.orElse("Visita");

			NotificationPrenotationRequest request = new NotificationPrenotationRequest();
			request.setPrenotationId(prenotation.getPrenotationId());
			request.setVisitType(visitType);
			request.setDate(prenotation.getDate().toString());
			request.setSlotTime(prenotation.getSlotTime().format(SLOT_TIME_FORMAT));

			if (patient != null) {
				
				request.setPatientEmail(patient.getEmail());
				request.setPatientName((patient.getNome() + " " + patient.getCognome()).trim());
			}
			
			if (doctor != null) {
				
				request.setDoctorEmail(doctor.getEmail());
				request.setDoctorName((doctor.getNome() + " " + doctor.getCognome()).trim());
			}

			notificationClient.notifyPrenotationCreated(request);
		} catch (Exception ex) {
			// ignoro
		}
	}
}
