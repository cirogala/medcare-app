package it.medcare.prenotation.dto;

import lombok.Data;

@Data
public class DoctorProfileDTO {
    private Long userId;
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private String role;
    private String typeDoctor;
}
