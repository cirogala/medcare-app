package it.medcare.doc_repo.service.impl;

import java.security.Key;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import it.medcare.doc_repo.service.JwtService;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Claims parseToken(String token) {
    	
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
    	
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
