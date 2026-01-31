package it.medcare.doc_repo.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
	
    private Long userId;
    private String email;
    private String nome;
    private String cognome;
}
