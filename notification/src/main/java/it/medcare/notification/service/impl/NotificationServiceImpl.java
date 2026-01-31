package it.medcare.notification.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import it.medcare.notification.dto.NotificationResponse;
import it.medcare.notification.dto.PasswordResetRequest;
import it.medcare.notification.dto.PrenotationCreatedRequest;
import it.medcare.notification.dto.ReportUploadedRequest;
import it.medcare.notification.dto.UserCreatedRequest;
import it.medcare.notification.entity.NotificationLog;
import it.medcare.notification.enums.NotificationEventType;
import it.medcare.notification.enums.NotificationStatus;
import it.medcare.notification.repository.NotificationLogRepository;
import it.medcare.notification.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository repository;
    private final JavaMailSender mailSender;

    @Value("${notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${notification.mail.from:no-reply@medcare.local}")
    private String mailFrom;

    public NotificationServiceImpl(NotificationLogRepository repository, JavaMailSender mailSender) {
        this.repository = repository;
        this.mailSender = mailSender;
    }

    @Override
    public NotificationResponse notifyUserCreated(UserCreatedRequest request) {
    	
        String fullName = joinName(request.getNome(), request.getCognome());
        String subject = "Benvenuto in MedCare";
        String body = String.format(
        		
            "Ciao %s,\n\n" +
            "il tuo account è stato creato con successo.\n" +
            "Username: %s\n" +
            "Password: %s\n" +
            "Ruolo: %s\n\n" +
            "Accedi al portale per iniziare.\n\n" +
            "MedCare",
            fullName, safe(request.getUsername()), safe(request.getPassword()), safe(request.getRole())
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", request.getUserId());
        payload.put("username", request.getUsername());
        payload.put("role", request.getRole());
        payload.put("isInternal", request.getIsInternal());
        payload.put("isMed", request.getIsMed());

        return sendAndLog(NotificationEventType.USER_CREATED, request.getEmail(), subject, body, payload);
    }

    @Override
    public List<NotificationResponse> notifyPrenotationCreated(PrenotationCreatedRequest request) {
    	
        List<NotificationResponse> responses = new ArrayList<>();

        String subject = "Nuova prenotazione";
        String dateTime = String.format("%s %s", safe(request.getDate()), safe(request.getSlotTime()));

        String patientBody = String.format(
        		
            "Ciao %s,\n\n" +
            "la tua prenotazione è stata registrata.\n" +
            "Visita: %s\n" +
            "Medico: %s\n" +
            "Data e ora: %s\n\n" +
            "MedCare",
            safe(request.getPatientName()),
            safe(request.getVisitType()),
            safe(request.getDoctorName()),
            dateTime
        );

        String doctorBody = String.format(
        		
            "Ciao %s,\n\n" +
            "hai una nuova prenotazione.\n" +
            "Paziente: %s\n" +
            "Visita: %s\n" +
            "Data e ora: %s\n\n" +
            "MedCare",
            safe(request.getDoctorName()),
            safe(request.getPatientName()),
            safe(request.getVisitType()),
            dateTime
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("prenotationId", request.getPrenotationId());
        payload.put("visitType", request.getVisitType());
        payload.put("date", request.getDate());
        payload.put("slotTime", request.getSlotTime());

        responses.add(sendAndLog(NotificationEventType.PRENOTATION_CREATED, request.getPatientEmail(), subject, patientBody, payload));
        responses.add(sendAndLog(NotificationEventType.PRENOTATION_CREATED, request.getDoctorEmail(), subject, doctorBody, payload));

        return responses;
    }

    @Override
    public NotificationResponse notifyReportUploaded(ReportUploadedRequest request) {
    	
        String subject = "Referto disponibile";
        String dateTime = String.format("%s %s", safe(request.getDate()), safe(request.getSlotTime()));
        String body = String.format(
        		
            "Ciao %s,\n\n" +
            "il tuo referto è stato caricato.\n" +
            "Visita: %s\n" +
            "Medico: %s\n" +
            "Data e ora: %s\n\n" +
            "Puoi scaricare il referto dal portale.\n\n" +
            "MedCare",
            safe(request.getPatientName()),
            safe(request.getVisitType()),
            safe(request.getDoctorName()),
            dateTime
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("reportId", request.getReportId());
        payload.put("prenotationId", request.getPrenotationId());
        payload.put("visitType", request.getVisitType());
        payload.put("date", request.getDate());
        payload.put("slotTime", request.getSlotTime());

        return sendAndLog(NotificationEventType.REPORT_UPLOADED, request.getPatientEmail(), subject, body, payload);
    }

    @Override
    public NotificationResponse notifyPasswordReset(PasswordResetRequest request) {
    	
        String fullName = joinName(request.getNome(), request.getCognome());
        String subject = "Recupero credenziali MedCare";
        String body = String.format(
        		
            "Ciao %s,\n\n" +
            "abbiamo generato una nuova password per il tuo account.\n" +
            "Username: %s\n" +
            "Password: %s\n\n" +
            "Accedi al portale e modifica la password appena possibile.\n\n" +
            "MedCare",
            fullName, safe(request.getUsername()), safe(request.getPassword())
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", request.getUsername());

        return sendAndLog(NotificationEventType.PASSWORD_RESET, request.getEmail(), subject, body, payload);
    }

    private NotificationResponse sendAndLog(NotificationEventType eventType, String recipient, String subject, String body, Map<String, Object> payload) {
    	
        NotificationLog log = new NotificationLog();
        log.setEventType(eventType);
        log.setRecipientEmail(recipient);
        log.setSubject(subject);
        log.setBody(body);
        log.setCreatedAt(LocalDateTime.now());
        log.setPayload(payload);

        
        if (recipient == null || recipient.isBlank()) {
            log.setStatus(NotificationStatus.FAILED);
            log.setError("Mancano i destinatari");
            repository.save(log);
            
            return new NotificationResponse(log.getId(), log.getStatus().name(), "Destinatario mancante");
        }

        if (!mailEnabled) {
        	
            log.setStatus(NotificationStatus.SKIPPED);
            repository.save(log);
            
            return new NotificationResponse(log.getId(), log.getStatus().name(), "Invio email disabilitato");
        }

        try {
        	
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.setStatus(NotificationStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            repository.save(log);
            
            return new NotificationResponse(log.getId(), log.getStatus().name(), "Email inviata");
            
        } catch (Exception ex) {
        	
            log.setStatus(NotificationStatus.FAILED);
            log.setError(ex.getMessage());
            repository.save(log);
            
            return new NotificationResponse(log.getId(), log.getStatus().name(), "Errore invio email");
        }
    }

    private String joinName(String nome, String cognome) {
    	
        String name = safe(nome) + " " + safe(cognome);
        
        return name.trim();
    }

    private String safe(String value) {
    	
        return value == null ? "" : value;
    }
}
