package com.sms.inventory.controller;

import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.service.StationeryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for stationery item operations.
 * All endpoints are prefixed with /api/inventory.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class StationeryController {

    private final StationeryService stationeryService;

    /**
     * List all items with pagination (default 20 per page).
     * GET /api/inventory?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ItemResponse>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(stationeryService.getAllItems(pageable));
    }

    /**
     * Get a single item by ID.
     * GET /api/inventory/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(stationeryService.getById(id));
    }

    /**
     * Search items by name.
     * GET /api/inventory/search?q=pen
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemResponse>> searchItems(@RequestParam String q) {
        return ResponseEntity.ok(stationeryService.searchItems(q));
    }

    /**
     * Get items below minimum stock threshold (ADMIN only).
     * GET /api/inventory/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<ItemResponse>> getLowStockItems() {
        return ResponseEntity.ok(stationeryService.getLowStockItems());
    }

    /**
     * Add a new item (ADMIN only).
     * POST /api/inventory
     */
    @PostMapping
    public ResponseEntity<ItemResponse> addItem(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stationeryService.addItem(request));
    }

    /**
     * Update an existing item (ADMIN only).
     * PUT /api/inventory/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(stationeryService.updateItem(id, request));
    }

    /**
     * Delete an item (ADMIN only).
     * DELETE /api/inventory/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        stationeryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deduct stock quantity (Internal/Feign call).
     * PUT /api/inventory/{id}/deduct?qty=5
     */
    @PutMapping("/{id}/deduct")
    public ResponseEntity<Void> deductQuantity(
            @PathVariable Long id,
            @RequestParam int qty) {
        stationeryService.deductQuantity(id, qty);
        return ResponseEntity.ok().build();
    }
}
