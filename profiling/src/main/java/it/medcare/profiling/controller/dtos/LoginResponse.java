package it.medcare.profiling.controller.dtos;

public record LoginResponse(
	    String accessToken,
	    String tokenType,
	    long expiresIn
	) {}

