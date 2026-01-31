package it.medcare.notification.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
	
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private String password;
}
