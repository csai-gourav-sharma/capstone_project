package com.sms.request.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for calling the Inventory Service.
 * Name must exactly match spring.application.name of inventory-service.
 */
@FeignClient(name = "inventory-service", configuration = FeignConfig.class)
public interface InventoryFeignClient {

    @GetMapping("/api/inventory/{id}")
    Map<String, Object> getItemById(@PathVariable Long id);

    @PutMapping("/api/inventory/{id}/deduct")
    void deductQuantity(@PathVariable Long id, @RequestParam int qty);
}
