package com.sms.request.model.dto;

import lombok.*;

/**
 * DTO for rejecting a request with an admin comment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequestDTO {
    private String comment;
}
