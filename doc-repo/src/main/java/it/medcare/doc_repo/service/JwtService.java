package it.medcare.doc_repo.service;

import io.jsonwebtoken.Claims;

public interface JwtService {

    Claims parseToken(String token);
}
