package com.sms.inventory.model.dto;

import lombok.*;

/**
 * DTO for returning stationery item data in API responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {

    private Long id;
    private String name;
    private String category;
    private String unit;
    private Integer availableQuantity;
    private Integer minimumQuantity;
    private boolean lowStock;
}
