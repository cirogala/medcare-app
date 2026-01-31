package it.medcare.prenotation.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    Claims parseToken(String token);
}
