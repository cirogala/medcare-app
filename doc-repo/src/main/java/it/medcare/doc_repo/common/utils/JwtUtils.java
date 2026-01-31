package it.medcare.doc_repo.common.utils;

import it.medcare.doc_repo.common.rest.CustomExceptions;
import it.medcare.doc_repo.constants.Constants;
import jakarta.servlet.http.HttpServletRequest;

public final class JwtUtils {

    private JwtUtils() {}

    public static String extractToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	
            throw new CustomExceptions.CustomException(Constants.MISSING_HEADER_AUTH);
        }

        return authHeader.substring(7);
    }
}
