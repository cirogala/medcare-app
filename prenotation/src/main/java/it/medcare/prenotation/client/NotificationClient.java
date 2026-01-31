package it.medcare.prenotation.client;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import it.medcare.prenotation.common.rest.Headers;
import it.medcare.prenotation.dto.NotificationPrenotationRequest;

@Service
public class NotificationClient {

    @Value("${notification.base-url}")
    private String notificationBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void notifyPrenotationCreated(NotificationPrenotationRequest request) {
    	
        if (notificationBaseUrl == null || notificationBaseUrl.isBlank()) {
        	
            return;
        }

        try {
        	
            String url = notificationBaseUrl + "/prenotations/created";
            RequestEntity<NotificationPrenotationRequest> req = RequestEntity
                .post(URI.create(url))
                .header(Headers.ACCEPT, "application/medcare.notification+v1.json")
                .header(Headers.KEY_LOGIC, "NOTIFICATION")
                .header(Headers.TRANSACTION_ID, UUID.randomUUID().toString())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request);

            ResponseEntity<String> response = restTemplate.exchange(req, String.class);
            
        } catch (Exception ex) {
            // ignoro
        }
    }
}
