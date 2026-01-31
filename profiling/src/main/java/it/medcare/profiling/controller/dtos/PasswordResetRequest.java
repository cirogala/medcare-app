package it.medcare.profiling.controller.dtos;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String identifier;
}
