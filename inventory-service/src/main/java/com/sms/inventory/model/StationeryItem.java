package com.sms.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a stationery item in the inventory_db.stationery_items table.
 */
@Entity
@Table(name = "stationery_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationeryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(length = 50)
    private String unit;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "minimum_quantity")
    private Integer minimumQuantity;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Category {
        PAPER, PEN, PENCIL, NOTEBOOK, ERASER, OTHER
    }
}
