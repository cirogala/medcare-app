package it.medcare.prenotation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import io.jsonwebtoken.Claims;
import it.medcare.prenotation.common.rest.Headers;
import it.medcare.prenotation.common.rest.MediaType;
import it.medcare.prenotation.common.utils.JwtUtils;
import it.medcare.prenotation.config.MedcareSecurityConfig;
import it.medcare.prenotation.dto.CreatePrenotationRequest;
import it.medcare.prenotation.dto.PrenotationDTO;
import it.medcare.prenotation.entity.Prenotation;
import it.medcare.prenotation.entity.Visit;
import it.medcare.prenotation.entity.VisitSlot;
import it.medcare.prenotation.enums.RoleType;
import it.medcare.prenotation.repository.VisitRepository;
import it.medcare.prenotation.repository.VisitSlotRepository;
import it.medcare.prenotation.service.JwtService;
import it.medcare.prenotation.service.PrenotationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {

	private final VisitRepository visitRepository;
	private final VisitSlotRepository slotRepository;
	private final JwtService jwtService;
	private final PrenotationService prenotationService;

	// accessibile a tutti gli utenti autenticati
	@MedcareSecurityConfig(enableAnonymous = true)
	@GetMapping(value = "/all",  produces = MediaType.PRENOTATION_V1)
	public List<Visit> getAllVisits(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId) {

		return visitRepository.findAll().stream().filter(v -> !Boolean.TRUE.equals(v.getFlagDeleted())).toList();
	}

	@MedcareSecurityConfig(enableAnonymous = true)
	@GetMapping(value = "/available", produces = MediaType.PRENOTATION_V1)
	public List<VisitSlot> getAvailableSlots(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@RequestParam Long doctorId,
			@RequestParam Long visitId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

		return slotRepository.findByDoctorIdAndVisitVisitIdAndVisitDateAndAvailableTrue(doctorId, visitId, date);
	}

	@MedcareSecurityConfig(enableAnonymous = true)
	@GetMapping(value = "/available-doctors", produces = MediaType.PRENOTATION_V1)
	public List<Long> getAvailableDoctors(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@RequestParam Long visitId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam String slotTime) {

		return slotRepository.findAvailableDoctorIds(visitId, date.toString(), slotTime);
	}

	@MedcareSecurityConfig(enableAnonymous = true)
	@PostMapping(value = "/prenotations", produces = MediaType.PRENOTATION_V1)
	public Prenotation createPrenotation(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@RequestBody CreatePrenotationRequest request,
			HttpServletRequest httpRequest) {

		Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));

		Long userId = claims.get("userId", Long.class);

		return prenotationService.createPrenotation(request, userId);
	}

	@GetMapping(value = "/my", produces = MediaType.PRENOTATION_V1)
	public ResponseEntity<List<PrenotationDTO>> getMyPrenotazioni(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			HttpServletRequest httpRequest) {

		Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));
		Long userId = claims.get("userId", Long.class);

		return ResponseEntity.ok(prenotationService.getPrenotazioniUtente(userId));
	}

	@GetMapping(value = "/doctor/my", produces = MediaType.PRENOTATION_V1)
	public ResponseEntity<List<PrenotationDTO>> getMyPrenotazioniDoctor(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			HttpServletRequest httpRequest) {

		Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));
		Long doctorId = claims.get("userId", Long.class);

		return ResponseEntity.ok(prenotationService.getPrenotazioniMedico(doctorId));
	}

	@MedcareSecurityConfig(allowedRoles = {RoleType.ADMIN})
	@GetMapping(value = "/admin/prenotations", produces = MediaType.PRENOTATION_V1)
	public ResponseEntity<List<PrenotationDTO>> getAllPrenotazioniAdmin(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		return ResponseEntity.ok(prenotationService.getPrenotazioniAdmin(from, to));
	}

	@MedcareSecurityConfig(allowedRoles = {RoleType.ADMIN})
	@GetMapping(value = "/admin/prenotations/{id}", produces = MediaType.PRENOTATION_V1)
	public ResponseEntity<PrenotationDTO> getPrenotazioneAdmin(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@PathVariable Long id) {

		return ResponseEntity.ok(prenotationService.getPrenotazioneById(id));
	}

	@DeleteMapping(value = "/delete/{id}", produces = MediaType.PRENOTATION_V1)
	public ResponseEntity<Void> deletePrenotazione(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@PathVariable Long id,
			HttpServletRequest httpRequest) {

		Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));
		Long userId = claims.get("userId", Long.class);

		prenotationService.deletePrenotazione(id, userId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{id}/complete", produces = MediaType.PRENOTATION_V1)
	public ResponseEntity<Void> markCompleted(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
			@RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
			@RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@PathVariable Long id,
			HttpServletRequest httpRequest) {

		Claims claims = jwtService.parseToken(JwtUtils.extractToken(httpRequest));
		Long doctorId = claims.get("userId", Long.class);

		prenotationService.markCompleted(id, doctorId);
		return ResponseEntity.noContent().build();
	}
}
