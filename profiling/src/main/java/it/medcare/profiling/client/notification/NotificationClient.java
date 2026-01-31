package it.medcare.profiling.client.notification;

import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.medcare.profiling.common.rest.Headers;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.base-url:}")
    private String notificationBaseUrl;

    public void notifyUserCreated(UserCreatedNotificationRequest request) {
    	
        if (notificationBaseUrl == null || notificationBaseUrl.isBlank()) {
            return;
        }

        try {
            String url = notificationBaseUrl + "/users/created";
            RequestEntity<UserCreatedNotificationRequest> req = RequestEntity
                .post(URI.create(url))
                .header(Headers.ACCEPT, "application/medcare.notification+v1.json")
                .header(Headers.KEY_LOGIC, "NOTIFICATION")
                .header(Headers.TRANSACTION_ID, UUID.randomUUID().toString())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request);

            ResponseEntity<String> response = restTemplate.exchange(req, String.class);
        } catch (Exception ex) {
            // non blocco l'utente
        }
    }

    public void notifyPasswordReset(UserCreatedNotificationRequest request) {
    	
        if (notificationBaseUrl == null || notificationBaseUrl.isBlank()) {
            return;
        }

        try {
            String url = notificationBaseUrl + "/users/reset-password";
            RequestEntity<UserCreatedNotificationRequest> req = RequestEntity
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
