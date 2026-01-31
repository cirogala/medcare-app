package it.medcare.profiling.controller.dtos;

import lombok.Data;

@Data
public class LoginRequest {
	
    private String username;
    private String password;
}
