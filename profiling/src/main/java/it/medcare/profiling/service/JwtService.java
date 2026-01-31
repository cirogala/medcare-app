package it.medcare.profiling.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Claims;
import it.medcare.profiling.entity.User;

public interface JwtService {

    String generateToken(User user);

    Claims parseToken(String token);
    
    String extractUsername (String jwt);
    
    Long extractUserId(String token);
    
    String extractRole(String token);
    
    Collection<? extends GrantedAuthority> extractAuthorities(String token);

}
