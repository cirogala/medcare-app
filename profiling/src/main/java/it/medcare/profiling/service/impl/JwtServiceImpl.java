package it.medcare.profiling.service.impl;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import it.medcare.profiling.entity.User;
import it.medcare.profiling.service.JwtService;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public String generateToken(User user) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("role", user.getRole().getRoleType().name())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

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

    @Override
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    @Override
    public Long extractUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    @Override
    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }
    
    @Override
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {

        Claims claims = parseToken(token);

        String role = claims.get("role", String.class);

        if (role == null) {
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}