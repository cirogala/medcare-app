package it.medcare.prenotation.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private String citta;
    private String indirizzo;
    private String codiceFiscale;
    private String role;
    private String typeDoctor;
    private String telefono;
}
