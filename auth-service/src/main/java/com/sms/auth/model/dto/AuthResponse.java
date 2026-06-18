package com.sms.auth.model.dto;

import lombok.*;

/**
 * DTO for authentication responses containing JWT token and user info.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private String role;
    private String fullName;
}
