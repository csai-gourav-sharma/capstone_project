package com.sms.auth.service;

import com.sms.auth.model.User;
import com.sms.auth.model.dto.AuthResponse;
import com.sms.auth.model.dto.LoginRequest;
import com.sms.auth.model.dto.RegisterRequest;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthService handling user registration and login.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Registers a new user:
     * 1. Checks if email is already taken → throws 409 if so
     * 2. Hashes password with BCrypt
     * 3. Saves user to auth_db
     * 4. Generates and returns JWT token
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email '" + request.getEmail() + "' is already registered"
            );
        }

        // Determine role (default to STUDENT)
        User.Role role = User.Role.STUDENT;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole()
                        + ". Allowed values: ADMIN, STUDENT");
            }
        }

        // Build and save user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(role)
                .build();

        userRepository.save(user);

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }

    /**
     * Authenticates a user:
     * 1. Finds user by email → throws 401 if not found
     * 2. Validates password with BCrypt → throws 401 if mismatch
     * 3. Generates and returns JWT token
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }

    // --- Custom exceptions (inner classes for simplicity) ---

    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class BadCredentialsException extends RuntimeException {
        public BadCredentialsException(String message) {
            super(message);
        }
    }
}
