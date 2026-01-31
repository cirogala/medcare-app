package it.medcare.prenotation.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.medcare.prenotation.dto.DoctorProfileDTO;
import it.medcare.prenotation.dto.UserProfileDTO;

@Service
public class ProfilingClient {

    private static final String HEADER_ACCEPT = "application/medcare.v1+json";
    private static final String HEADER_KEY_LOGIC = "1234567";

    @Value("${profiling.doctors-url}")
    private String profilingDoctorsUrl;

    @Value("${profiling.users-url}")
    private String profilingUsersUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<DoctorProfileDTO> fetchDoctors() {
    	
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Medcare-Accept", HEADER_ACCEPT);
            headers.set("X-Medcare-KeyLogic", HEADER_KEY_LOGIC);
            headers.set("X-Medcare-TransactionID", java.util.UUID.randomUUID().toString());

            String url = UriComponentsBuilder.fromHttpUrl(profilingDoctorsUrl)
                .queryParam("isMed", "true")
                .toUriString();

            ResponseEntity<List<DoctorProfileDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<DoctorProfileDTO>>() {}
            );

            List<DoctorProfileDTO> doctors = response.getBody();
            
            return doctors == null ? List.of() : doctors;
            
        } catch (Exception ex) {
        	
            return List.of();
        }
    }

    public DoctorProfileDTO fetchDoctorById(Long doctorId) {
    	
        if (doctorId == null) {
        	
            return null;
        }

        return fetchDoctors().stream()
            .filter(doctor -> doctorId.equals(doctor.getUserId()))
            .findFirst()
            .orElse(null);
    }

    public List<UserProfileDTO> fetchExternalUsers() {
    	
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Medcare-Accept", HEADER_ACCEPT);
            headers.set("X-Medcare-KeyLogic", HEADER_KEY_LOGIC);
            headers.set("X-Medcare-TransactionID", java.util.UUID.randomUUID().toString());

            ResponseEntity<List<UserProfileDTO>> response = restTemplate.exchange(
                profilingUsersUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<UserProfileDTO>>() {}
            );

            List<UserProfileDTO> users = response.getBody();
            
            return users == null ? List.of() : users;
            
        } catch (Exception ex) {
        	
            return List.of();
        }
    }

    public UserProfileDTO fetchExternalUserById(Long userId) {
    	
        if (userId == null) {
        	
            return null;
        }

        return fetchExternalUsers().stream()
            .filter(user -> userId.equals(user.getUserId()))
            .findFirst()
            .orElse(null);
    }
}
