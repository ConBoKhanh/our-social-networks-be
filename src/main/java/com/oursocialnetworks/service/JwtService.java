package com.oursocialnetworks.service;


import com.oursocialnetworks.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "a9f3c4e18d7b2bb4f8c0a1d29e4f6c12d8e79b0fe3a4c1d2f7b9a6e0c4d3f812";

    public String generateToken(User user) {
        // Determine role based on user's role or default to USER
        String role = "USER"; // Default role
        if (user.getRole() != null && user.getRole().getRole() != null) {
            role = user.getRole().getRole().toUpperCase();
        }
        
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("email", user.getUsernameLogin())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1h
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 86400_000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public Claims verify(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
