package com.sms.inventory.service;

import com.sms.inventory.exception.InsufficientStockException;
import com.sms.inventory.exception.ItemNotFoundException;
import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.repository.StationeryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of StationeryService handling all inventory operations.
 */
@Service
@RequiredArgsConstructor
public class StationeryServiceImpl implements StationeryService {

    private final StationeryRepository repository;

    @Override
    public ItemResponse addItem(ItemRequest request) {
        StationeryItem item = StationeryItem.builder()
                .name(request.getName())
                .category(parseCategory(request.getCategory()))
                .unit(request.getUnit())
                .availableQuantity(request.getAvailableQuantity())
                .minimumQuantity(request.getMinimumQuantity())
                .build();

        StationeryItem saved = repository.save(item);
        return toResponse(saved);
    }

    @Override
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public ItemResponse getById(Long id) {
        StationeryItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        return toResponse(item);
    }

    @Override
    public ItemResponse updateItem(Long id, ItemRequest request) {
        StationeryItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        item.setName(request.getName());
        item.setCategory(parseCategory(request.getCategory()));
        item.setUnit(request.getUnit());
        item.setAvailableQuantity(request.getAvailableQuantity());
        item.setMinimumQuantity(request.getMinimumQuantity());

        StationeryItem updated = repository.save(item);
        return toResponse(updated);
    }

    @Override
    public void deleteItem(Long id) {
        if (!repository.existsById(id)) {
            throw new ItemNotFoundException("Item not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void deductQuantity(Long id, int quantity) {
        StationeryItem item = repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));

        if (item.getAvailableQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for '" + item.getName() + "'. Available: "
                    + item.getAvailableQuantity() + ", Requested: " + quantity);
        }

        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        repository.save(item);
    }

    @Override
    public List<ItemResponse> searchItems(String query) {
        return repository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemResponse> getLowStockItems() {
        // Find items where available quantity is at or below minimum quantity
        // Using a default threshold of 10 for items without a minimum set
        return repository.findAll().stream()
                .filter(item -> {
                    int threshold = item.getMinimumQuantity() != null ? item.getMinimumQuantity() : 10;
                    return item.getAvailableQuantity() <= threshold;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --- Helper methods ---

    private ItemResponse toResponse(StationeryItem item) {
        int threshold = item.getMinimumQuantity() != null ? item.getMinimumQuantity() : 10;
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory().name())
                .unit(item.getUnit())
                .availableQuantity(item.getAvailableQuantity())
                .minimumQuantity(item.getMinimumQuantity())
                .lowStock(item.getAvailableQuantity() <= threshold)
                .build();
    }

    private StationeryItem.Category parseCategory(String category) {
        try {
            return StationeryItem.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category
                    + ". Allowed: PAPER, PEN, PENCIL, NOTEBOOK, ERASER, OTHER");
        }
    }
}
