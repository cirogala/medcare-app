package it.medcare.profiling.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.net.URI;

import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import it.medcare.profiling.constants.Constants;
import it.medcare.profiling.controller.dtos.UserProfileDTO;
import it.medcare.profiling.controller.dtos.UserUpdateRequest;
import it.medcare.profiling.common.rest.Headers;
import it.medcare.profiling.client.notification.NotificationClient;
import it.medcare.profiling.client.notification.UserCreatedNotificationRequest;
import it.medcare.profiling.entity.MdtRole;
import it.medcare.profiling.entity.User;
import it.medcare.profiling.enums.RoleType;
import it.medcare.profiling.repository.MdtRoleRepo;
import it.medcare.profiling.repository.UserRepo;
import it.medcare.profiling.service.CredentialService;
import it.medcare.profiling.service.UserService;
import it.medcare.profiling.controller.dtos.PasswordResetRequest;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
	
    private final UserRepo userRepo;
    private final MdtRoleRepo roleRepo;
    private final CredentialService credentialService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate = new RestTemplate();
    private final NotificationClient notificationClient;

    @Value("${prenotation.slots-url}")
    private String prenotationSlotsUrl;
    
    public UserServiceImpl(UserRepo userRepo, MdtRoleRepo roleRepo, CredentialService usernameService, PasswordEncoder passwordEncoder, NotificationClient notificationClient) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.credentialService = usernameService;
        this.passwordEncoder = passwordEncoder;
        this.notificationClient = notificationClient;
    }

    @Override
    @Transactional
    public User createInternalUser(User user) {

        // controllo se email esiste già

        if (userRepo.existsByEmail(user.getEmail())) {
        	
            throw new IllegalArgumentException(Constants.EMAIL_ESISTENTE);
        }
        
        MdtRole adminRole = roleRepo.findByRoleType(RoleType.ADMIN).orElseThrow(() 
        		-> new IllegalStateException(Constants.RUOLO_NON_TROVATO));
        
        // genero una username
        String username = credentialService.generateUsername(RoleType.ADMIN);
        user.setUsername(username);
        
        // genero una password sicura
        String rawPassword = credentialService.generateRawPassword();
        String passwordSecured = passwordEncoder.encode(rawPassword);
        user.setPassword(passwordSecured);
        
        user.setRole(adminRole);
        user.setIsInternal(true);
        user.setFlagDeleted((byte) 0);

        User saved = userRepo.save(user);
        notifyUserCreated(saved, rawPassword);
        return saved;
    }
    
    @Override
    @Transactional
    public User createExternalUser(User user) {
    	
    	// controllo se email esiste già

        if (userRepo.existsByEmail(user.getEmail())) {
        	
            throw new IllegalArgumentException(Constants.EMAIL_ESISTENTE);
        }
        
        MdtRole adminRole = roleRepo.findByRoleType(RoleType.PAZIENTE).orElseThrow(() 
        		-> new IllegalStateException(Constants.RUOLO_NON_TROVATO));

        // genero una username
        String username = credentialService.generateUsername(RoleType.PAZIENTE);
        user.setUsername(username);
        
        // genero una password sicura
        String rawPassword = credentialService.generateRawPassword();
        String passwordSecured = passwordEncoder.encode(rawPassword);
        user.setPassword(passwordSecured);
        
        user.setRole(adminRole);
        user.setIsInternal(false);
        user.setFlagDeleted((byte) 0);
        
        User saved = userRepo.save(user);
        notifyUserCreated(saved, rawPassword);
    	return saved;
    }

    @Override
    @Transactional
    public User createDoctorUser(User user) {
    	
    	// controllo se email esiste già

        if (userRepo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(Constants.EMAIL_ESISTENTE);
        }
        
        MdtRole adminRole = roleRepo.findByRoleType(RoleType.MEDICO).orElseThrow(() 
        		-> new IllegalStateException(Constants.RUOLO_NON_TROVATO));

        // genero una username
        String username = credentialService.generateUsername(RoleType.MEDICO);
        
        // genero una password sicura
        String rawPassword = credentialService.generateRawPassword();
        String passwordSecured = passwordEncoder.encode(rawPassword);
        user.setPassword(passwordSecured);
        
        user.setUsername(username);
        user.setRole(adminRole);
        user.setIsInternal(false);
        user.setFlagDeleted((byte) 0);
        
        User saved = userRepo.save(user);
        triggerSlotGeneration(saved.getUserId());
        notifyUserCreated(saved, rawPassword);
    	return saved;
    }

    private void triggerSlotGeneration(Long doctorId) {
    	
        if (doctorId == null || prenotationSlotsUrl == null || prenotationSlotsUrl.isBlank()) {
        	
            return;
        }

        try {
            String url = prenotationSlotsUrl + "?doctorId=" + doctorId;
            RequestEntity<Void> request = RequestEntity
                .post(URI.create(url))
                .header(Headers.ACCEPT, "application/medcare.v1+json")
                .header(Headers.KEY_LOGIC, "PRENOTATION")
                .header(Headers.TRANSACTION_ID, UUID.randomUUID().toString())
                .build();

            ResponseEntity<Void> response = restTemplate.exchange(request, Void.class);
            logger.info("Inzio la generazione degli slot per doctorId={} (status={})", doctorId, response.getStatusCode());
            
        } catch (Exception ex) {
        	
            logger.warn("Generazione per gli slot del doctorId={}", doctorId, ex);
        }
    }

    private void notifyUserCreated(User user, String rawPassword) {
        try {
            UserCreatedNotificationRequest request = new UserCreatedNotificationRequest();
            request.setUserId(user.getUserId());
            request.setUsername(user.getUsername());
            request.setEmail(user.getEmail());
            request.setNome(user.getNome());
            request.setCognome(user.getCognome());
            request.setIsInternal(user.getIsInternal());
            request.setIsMed(user.getIsMed());
            request.setPassword(rawPassword);
            if (user.getRole() != null) {
                request.setRole(user.getRole().getRoleDescription());
            }
            notificationClient.notifyUserCreated(request);
        } catch (Exception ex) {
            // ignoro
        }
    }
    
    public UserProfileDTO retrieveMyInfo(String username) {

        User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNome(user.getNome());
        dto.setCognome(user.getCognome());
        dto.setCitta(user.getCitta());
        dto.setIndirizzo(user.getIndirizzo());
        dto.setCodiceFiscale(user.getCodiceFiscale());
        dto.setTelefono(user.getTelefono());
        dto.setRole(user.getRole().getRoleDescription());

        return dto;
    }
    
    public List<UserProfileDTO> retrieveAllDoctors(Boolean isMed) {

        return userRepo.findAllByIsMed(isMed)
            .stream()
            .map(user -> {
                UserProfileDTO dto = new UserProfileDTO();
                dto.setUserId(user.getUserId());
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setNome(user.getNome());
                dto.setCognome(user.getCognome());
                dto.setCitta(user.getCitta());
                dto.setIndirizzo(user.getIndirizzo());
                dto.setCodiceFiscale(user.getCodiceFiscale());
                dto.setTelefono(user.getTelefono());
                dto.setTypeDoctor(user.getTypeDoctor());

                if (user.getRole() != null) {
                    dto.setRole(user.getRole().getRoleDescription());
                }

                return dto;
            }).toList();
    }
    
    public List<UserProfileDTO> retrieveAllExtUsers() {
    	Boolean isMed = false;
    	Boolean isInternal = false;
        return userRepo.findAllByIsMedAndIsInternal(isMed, isInternal)
            .stream()
            .map(user -> {
                UserProfileDTO dto = new UserProfileDTO();
                dto.setUserId(user.getUserId());
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setNome(user.getNome());
                dto.setCognome(user.getCognome());
                dto.setCitta(user.getCitta());
                dto.setIndirizzo(user.getIndirizzo());
                dto.setCodiceFiscale(user.getCodiceFiscale());
                dto.setTelefono(user.getTelefono());

                if (user.getRole() != null) {
                    dto.setRole(user.getRole().getRoleDescription());
                }

                return dto;
            }).toList();
    }

    public List<UserProfileDTO> retrieveAllIntUsers() {
        Boolean isInternal = true;
        return userRepo.findAllByIsInternal(isInternal)
            .stream()
            .map(user -> {
                UserProfileDTO dto = new UserProfileDTO();
                dto.setUserId(user.getUserId());
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
                dto.setNome(user.getNome());
                dto.setCognome(user.getCognome());
                dto.setCitta(user.getCitta());
                dto.setIndirizzo(user.getIndirizzo());
                dto.setCodiceFiscale(user.getCodiceFiscale());
                dto.setTelefono(user.getTelefono());

                if (user.getRole() != null) {
                	
                    dto.setRole(user.getRole().getRoleDescription());
                }

                return dto;
            }).toList();
    }

    public UserProfileDTO updateInternalUser(Long userId, UserUpdateRequest request) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.FALSE.equals(user.getIsInternal())) {
        	
            throw new RuntimeException("User is not internal");
        }

        user.setNome(request.getNome());
        user.setCognome(request.getCognome());
        user.setEmail(request.getEmail());
        user.setCitta(request.getCitta());
        user.setIndirizzo(request.getIndirizzo());
        user.setCodiceFiscale(request.getCodiceFiscale());
        user.setTelefono(request.getTelefono());

        User saved = userRepo.save(user);

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(saved.getUserId());
        dto.setUsername(saved.getUsername());
        dto.setEmail(saved.getEmail());
        dto.setNome(saved.getNome());
        dto.setCognome(saved.getCognome());
        dto.setCitta(saved.getCitta());
        dto.setIndirizzo(saved.getIndirizzo());
        dto.setCodiceFiscale(saved.getCodiceFiscale());
        dto.setTelefono(saved.getTelefono());
        
        if (saved.getRole() != null) {
        	
            dto.setRole(saved.getRole().getRoleDescription());
        }
        
        dto.setTypeDoctor(saved.getTypeDoctor());

        return dto;
    }

    public UserProfileDTO updateExternalUser(Long userId, UserUpdateRequest request) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (Boolean.TRUE.equals(user.getIsInternal())) {
        	
            throw new RuntimeException("User is not external");
        }

        user.setNome(request.getNome());
        user.setCognome(request.getCognome());
        user.setEmail(request.getEmail());
        user.setCitta(request.getCitta());
        user.setIndirizzo(request.getIndirizzo());
        user.setCodiceFiscale(request.getCodiceFiscale());
        user.setTelefono(request.getTelefono());

        User saved = userRepo.save(user);

        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(saved.getUserId());
        dto.setUsername(saved.getUsername());
        dto.setEmail(saved.getEmail());
        dto.setNome(saved.getNome());
        dto.setCognome(saved.getCognome());
        dto.setCitta(saved.getCitta());
        dto.setIndirizzo(saved.getIndirizzo());
        dto.setCodiceFiscale(saved.getCodiceFiscale());
        dto.setTelefono(saved.getTelefono());
        
        if (saved.getRole() != null) {
        	
            dto.setRole(saved.getRole().getRoleDescription());
        }
        
        dto.setTypeDoctor(saved.getTypeDoctor());

        return dto;
    }

    public void resetPassword(PasswordResetRequest request) {
    	
        if (request == null || request.getIdentifier() == null || request.getIdentifier().isBlank()) {
        	
            throw new IllegalArgumentException("Identifier missing");
        }

        String identifier = request.getIdentifier().trim();
        User user = userRepo.findByUsername(identifier)
            .orElseGet(() -> userRepo.findByEmail(identifier).orElseThrow(() -> new RuntimeException("User not found")));

        String rawPassword = credentialService.generateRawPassword();
        String passwordSecured = passwordEncoder.encode(rawPassword);
        user.setPassword(passwordSecured);
        userRepo.save(user);

        try {
            UserCreatedNotificationRequest req = new UserCreatedNotificationRequest();
            req.setUserId(user.getUserId());
            req.setUsername(user.getUsername());
            req.setEmail(user.getEmail());
            req.setNome(user.getNome());
            req.setCognome(user.getCognome());
            req.setIsInternal(user.getIsInternal());
            req.setIsMed(user.getIsMed());
            req.setPassword(rawPassword);
            
            if (user.getRole() != null) {
            	
                req.setRole(user.getRole().getRoleDescription());
            }
            
            notificationClient.notifyPasswordReset(req);
            
        } catch (Exception ex) {
            // ignoro
        }
    }
}
