package com.sms.request.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for returning request data in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestResponseDTO {

    private Long id;
    private Long studentId;
    private String studentEmail;
    private String status;
    private String adminComment;
    private LocalDateTime requestDate;
    private List<ItemDetail> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemDetail {
        private Long itemId;
        private String itemName;
        private Integer quantity;
    }
}
