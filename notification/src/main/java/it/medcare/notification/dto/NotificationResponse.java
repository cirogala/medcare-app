package it.medcare.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationResponse {
	
    private String id;
    private String status;
    private String message;
}
