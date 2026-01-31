package it.medcare.profiling.common.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CustomExceptions extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public CustomExceptions(String message) {
    	
        super(message);
    }
}