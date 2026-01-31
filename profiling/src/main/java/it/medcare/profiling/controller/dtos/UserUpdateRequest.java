package it.medcare.profiling.controller.dtos;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String nome;
    private String cognome;
    private String email;
    private String citta;
    private String indirizzo;
    private String codiceFiscale;
    private String telefono;
}
