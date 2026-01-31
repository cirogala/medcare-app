package it.medcare.prenotation.common.utils;

import it.medcare.prenotation.common.rest.CustomExceptions;
import it.medcare.prenotation.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;

public final class JwtUtils {

    private JwtUtils() {}

    public static String extractToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	
            throw new CustomExceptions(Constants.MISSING_HEADER_AUTH);
        }

        return authHeader.substring(7);
    }
}

