package com.sms.auth.service;

import com.sms.auth.model.User;
import com.sms.auth.model.dto.AuthResponse;
import com.sms.auth.model.dto.LoginRequest;
import com.sms.auth.model.dto.RegisterRequest;
import com.sms.auth.repository.UserRepository;
import com.sms.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User studentUser;

    @BeforeEach
    public void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("student@sms.com")
                .password("password123")
                .fullName("Student User")
                .role("STUDENT")
                .build();

        loginRequest = LoginRequest.builder()
                .email("student@sms.com")
                .password("password123")
                .build();

        studentUser = User.builder()
                .id(1L)
                .email("student@sms.com")
                .password("hashed_password")
                .fullName("Student User")
                .role(User.Role.STUDENT)
                .build();
    }

    @Test
    public void register_success_whenEmailNotTaken() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed_password");
        when(jwtUtil.generateToken(any(), any())).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("student@sms.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());
        assertEquals("mocked_jwt_token", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void register_throwsException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(AuthServiceImpl.EmailAlreadyExistsException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void login_success_withValidCredentials() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), studentUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(studentUser.getEmail(), studentUser.getRole().name())).thenReturn("mocked_jwt_token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("student@sms.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());
        assertEquals("mocked_jwt_token", response.getToken());
    }

    @Test
    public void login_throwsException_withWrongPassword() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(studentUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), studentUser.getPassword())).thenReturn(false);

        assertThrows(AuthServiceImpl.BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }
}
