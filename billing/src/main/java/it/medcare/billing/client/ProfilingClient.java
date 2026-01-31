package it.medcare.billing.client;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import it.medcare.billing.common.rest.Headers;
import it.medcare.billing.common.rest.MediaType;
import it.medcare.billing.dto.UserProfileDTO;

@Service
public class ProfilingClient {

    private static final String DEFAULT_KEY_LOGIC = "1234567";

    @Value("${profiling.base-url}")
    private String profilingBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<UserProfileDTO> fetchDoctors() {
    	
        if (profilingBaseUrl == null || profilingBaseUrl.isBlank()) {
        	
            return List.of();
        }

        String url = UriComponentsBuilder.fromHttpUrl(profilingBaseUrl)
            .path("/profiling/doctors")
            .queryParam("isMed", "true")
            .toUriString();

        try {
        	
            ResponseEntity<List<UserProfileDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(defaultHeaders()),
                new ParameterizedTypeReference<List<UserProfileDTO>>() {}
            );
            
            List<UserProfileDTO> doctors = response.getBody();
            
            return doctors == null ? List.of() : doctors;
            
        } catch (Exception ex) {
        	
            return List.of();
        }
    }

    private HttpHeaders defaultHeaders() {
    	
        HttpHeaders headers = new HttpHeaders();
        headers.set(Headers.ACCEPT, MediaType.BILLING_V1);
        headers.set(Headers.KEY_LOGIC, DEFAULT_KEY_LOGIC);
        headers.set(Headers.TRANSACTION_ID, UUID.randomUUID().toString());
        
        return headers;
    }
}
