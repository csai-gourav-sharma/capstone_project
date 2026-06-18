package com.sms.inventory.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO for creating or updating a stationery item.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    private String unit;

    @NotNull(message = "Available quantity is required")
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity;

    @Min(value = 0, message = "Minimum quantity cannot be negative")
    private Integer minimumQuantity;
}
