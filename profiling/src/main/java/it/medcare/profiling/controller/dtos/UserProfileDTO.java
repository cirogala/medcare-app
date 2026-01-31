package it.medcare.profiling.controller.dtos;

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
    private String telefono;
    private String role;
    private String typeDoctor;

}
