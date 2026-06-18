package com.sms.request.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO for submitting a new stationery request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitRequestDTO {

    @NotNull(message = "Items list is required")
    private List<RequestItemDTO> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestItemDTO {
        @NotNull(message = "Item ID is required")
        private Long itemId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
