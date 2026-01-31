package it.medcare.profiling.client.notification;

import lombok.Data;

@Data
public class UserCreatedNotificationRequest {
    private Long userId;
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private String role;
    private Boolean isInternal;
    private Boolean isMed;
    private String password;
}
