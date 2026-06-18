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

    /**
     * Adds a new stationery item to the inventory.
     *
     * @param request the item details to add
     * @return the created item response
     */
    ItemResponse addItem(ItemRequest request);

    /**
     * Lists all stationery items in a paginated format.
     *
     * @param pageable pagination parameters
     * @return a page of item responses
     */
    Page<ItemResponse> getAllItems(Pageable pageable);

    /**
     * Retrieves a single stationery item by its unique identifier.
     *
     * @param id the ID of the item
     * @return the item details
     * @throws ItemNotFoundException if the item does not exist
     */
    ItemResponse getById(Long id);

    /**
     * Updates an existing stationery item's details.
     *
     * @param id the ID of the item to update
     * @param request the updated item details
     * @return the updated item response
     * @throws ItemNotFoundException if the item does not exist
     */
    ItemResponse updateItem(Long id, ItemRequest request);

    /**
     * Deletes a stationery item from the inventory.
     *
     * @param id the ID of the item to delete
     * @throws ItemNotFoundException if the item does not exist
     */
    void deleteItem(Long id);

    /**
     * Deducts a specific quantity from available stock for a stationery item.
     * Used upon request approval.
     *
     * @param id the ID of the item to deduct stock from
     * @param quantity the quantity to deduct
     * @throws ItemNotFoundException if the item does not exist
     * @throws InsufficientStockException if the requested quantity exceeds available stock
     */
    void deductQuantity(Long id, int quantity);

    /**
     * Searches for stationery items by name (case-insensitive substring match).
     *
     * @param query the search keyword
     * @return a list of matching item responses
     */
    List<ItemResponse> searchItems(String query);

    /**
     * Retrieves a list of items that are at or below their low-stock thresholds.
     *
     * @return a list of low stock item responses
     */
    List<ItemResponse> getLowStockItems();
}
