package com.sms.request.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing an individual item within a stationery request.
 * References inventory items by ID (no FK constraint — cross-service ref).
 */
@Entity
@Table(name = "request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @JsonIgnore
    private StationeryRequest request;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;
}
