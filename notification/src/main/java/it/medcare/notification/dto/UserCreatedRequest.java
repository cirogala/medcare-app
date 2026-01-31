package it.medcare.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserCreatedRequest {
	
    private Long userId;
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private String role;
    private Boolean isInternal;
    private Boolean isMed;
    
    @JsonProperty("password")
    private String password;
}
