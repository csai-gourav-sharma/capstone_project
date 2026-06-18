package com.sms.inventory.service;

import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for stationery item operations.
 */
public interface StationeryService {

    ItemResponse addItem(ItemRequest request);

    Page<ItemResponse> getAllItems(Pageable pageable);

    ItemResponse getById(Long id);

    ItemResponse updateItem(Long id, ItemRequest request);

    void deleteItem(Long id);

    void deductQuantity(Long id, int quantity);

    List<ItemResponse> searchItems(String query);

    List<ItemResponse> getLowStockItems();
}
