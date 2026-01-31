package it.medcare.billing.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    Claims parseToken(String token);
}
