package it.medcare.billing.client;

import java.time.LocalDate;
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
import it.medcare.billing.dto.PrenotationDTO;
import it.medcare.billing.dto.VisitDTO;

@Service
public class PrenotationClient {

    private static final String DEFAULT_KEY_LOGIC = "1234567";

    @Value("${prenotation.base-url}")
    private String prenotationBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<VisitDTO> fetchVisits(String authHeader, String accept, String keyLogic, String transId) {
        String url = UriComponentsBuilder.fromHttpUrl(prenotationBaseUrl)
            .path("/visits/all")
            .toUriString();

        try {
        	
            ResponseEntity<List<VisitDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(authHeader, accept, keyLogic, transId)),
                new ParameterizedTypeReference<List<VisitDTO>>() {}
            );
            
            List<VisitDTO> visits = response.getBody();
            
            return visits == null ? List.of() : visits;
            
        } catch (Exception ex) {
        	
            return List.of();
        }
    }

    public List<PrenotationDTO> fetchPrenotations(LocalDate from, LocalDate to, String authHeader, String accept, String keyLogic, String transId) {
    	
        String url = UriComponentsBuilder.fromHttpUrl(prenotationBaseUrl)
            .path("/visits/admin/prenotations")
            .queryParam("from", from)
            .queryParam("to", to)
            .toUriString();

        try {
        	
            ResponseEntity<List<PrenotationDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(authHeader, accept, keyLogic, transId)),
                new ParameterizedTypeReference<List<PrenotationDTO>>() {}
            );
            
            List<PrenotationDTO> prenotations = response.getBody();
            
            return prenotations == null ? List.of() : prenotations;
            
        } catch (Exception ex) {
        	
            return List.of();
        }
    }

    public PrenotationDTO fetchPrenotation(Long prenotationId, String authHeader, String accept, String keyLogic, String transId) {
    	
        String url = UriComponentsBuilder.fromHttpUrl(prenotationBaseUrl)
            .path("/visits/admin/prenotations/")
            .path(String.valueOf(prenotationId))
            .toUriString();

        try {
        	
            ResponseEntity<PrenotationDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(authHeader, accept, keyLogic, transId)),
                PrenotationDTO.class
            );
            
            return response.getBody();
            
        } catch (Exception ex) {
        	
            return null;
        }
    }

    private HttpHeaders buildHeaders(String authHeader, String accept, String keyLogic, String transId) {
    	
        HttpHeaders headers = new HttpHeaders();
        headers.set(Headers.ACCEPT, accept != null ? accept : MediaType.BILLING_V1);
        headers.set(Headers.KEY_LOGIC, keyLogic != null ? keyLogic : DEFAULT_KEY_LOGIC);
        headers.set(Headers.TRANSACTION_ID, transId != null ? transId : UUID.randomUUID().toString());
        
        if (authHeader != null) {
        	
            headers.set("Authorization", authHeader);
        }
        
        return headers;
    }
}
