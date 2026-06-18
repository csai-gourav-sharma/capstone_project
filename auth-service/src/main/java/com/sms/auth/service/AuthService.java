package com.sms.auth.service;

import com.sms.auth.model.dto.AuthResponse;
import com.sms.auth.model.dto.LoginRequest;
import com.sms.auth.model.dto.RegisterRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user. Checks email uniqueness, hashes password with BCrypt,
     * saves the user, and returns a JWT token.
     *
     * @param request registration data
     * @return auth response with JWT token and user info
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password.
     * Returns a JWT token on success.
     *
     * @param request login credentials
     * @return auth response with JWT token and user info
     */
    AuthResponse login(LoginRequest request);
}
