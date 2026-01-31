package it.medcare.notification.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import it.medcare.notification.enums.NotificationEventType;
import it.medcare.notification.enums.NotificationStatus;
import lombok.Data;

@Data
@Document(collection = "notifications")
public class NotificationLog {

    @Id
    private String id;
    private NotificationEventType eventType;
    private String recipientEmail;
    private String subject;
    private String body;
    private NotificationStatus status;
    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Map<String, Object> payload;
}
