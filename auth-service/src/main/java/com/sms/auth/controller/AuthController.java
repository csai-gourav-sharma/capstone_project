package com.sms.auth.controller;

import com.sms.auth.model.dto.AuthResponse;
import com.sms.auth.model.dto.LoginRequest;
import com.sms.auth.model.dto.RegisterRequest;
import com.sms.auth.security.JwtUtil;
import com.sms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication operations.
 * All endpoints are prefixed with /api/auth and are publicly accessible.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user.
     * POST /api/auth/register
     *
     * @param request registration data (email, password, fullName, role)
     * @return AuthResponse with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Login with email and password.
     * POST /api/auth/login
     *
     * @param request login credentials
     * @return AuthResponse with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate a JWT token.
     * GET /api/auth/validate
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return 200 OK if valid, 401 if invalid
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }

        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", jwtUtil.extractEmail(token),
                    "role", jwtUtil.extractRole(token)
            ));
        }
        return ResponseEntity.status(401).body(Map.of("valid", false));
    }

    /**
     * Get the current user's profile from the JWT token.
     * GET /api/auth/me
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return user profile info
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(Map.of(
                "email", jwtUtil.extractEmail(token),
                "role", jwtUtil.extractRole(token)
        ));
    }
}
