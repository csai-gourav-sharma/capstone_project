package com.sms.inventory.repository;

import com.sms.inventory.model.StationeryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for StationeryItem CRUD and custom queries.
 */
@Repository
public interface StationeryRepository extends JpaRepository<StationeryItem, Long> {

    Page<StationeryItem> findByCategory(StationeryItem.Category category, Pageable pageable);

    List<StationeryItem> findByNameContainingIgnoreCase(String name);

    List<StationeryItem> findByAvailableQuantityLessThanEqual(int threshold);
}
