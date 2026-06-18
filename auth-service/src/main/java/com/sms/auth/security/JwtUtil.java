package com.sms.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT utility for generating, validating, and extracting claims from tokens.
 * Secret key must be at least 256-bit (32 bytes) and is loaded from config.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs; // default 24 hours

    /**
     * Generates a JWT token containing the user's email (as subject) and role (as claim).
     *
     * @param email the user's email address
     * @param role  the user's role (ADMIN or STUDENT)
     * @return signed JWT token string
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claims(Map.of("role", role))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if valid and not expired, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the email (subject) from the JWT token.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the role claim from the JWT token.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
