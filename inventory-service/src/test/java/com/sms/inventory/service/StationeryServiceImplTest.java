package com.sms.inventory.service;

import com.sms.inventory.exception.InsufficientStockException;
import com.sms.inventory.exception.ItemNotFoundException;
import com.sms.inventory.model.StationeryItem;
import com.sms.inventory.model.dto.ItemRequest;
import com.sms.inventory.model.dto.ItemResponse;
import com.sms.inventory.repository.StationeryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StationeryServiceImplTest {

    @Mock
    private StationeryRepository repository;

    @InjectMocks
    private StationeryServiceImpl service;

    private StationeryItem paperItem;
    private ItemRequest paperRequest;

    @BeforeEach
    public void setUp() {
        paperItem = StationeryItem.builder()
                .id(1L)
                .name("A4 Printing Paper")
                .category(StationeryItem.Category.PAPER)
                .unit("Pack")
                .availableQuantity(100)
                .minimumQuantity(10)
                .build();

        paperRequest = ItemRequest.builder()
                .name("A4 Printing Paper")
                .category("PAPER")
                .unit("Pack")
                .availableQuantity(100)
                .minimumQuantity(10)
                .build();
    }

    @Test
    public void addItem_savesAndReturnsItem() {
        when(repository.save(any(StationeryItem.class))).thenReturn(paperItem);

        ItemResponse response = service.addItem(paperRequest);

        assertNotNull(response);
        assertEquals("A4 Printing Paper", response.getName());
        assertEquals("PAPER", response.getCategory());
        assertEquals(100, response.getAvailableQuantity());
        verify(repository, times(1)).save(any(StationeryItem.class));
    }

    @Test
    public void getAllItems_returnsPaginatedList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<StationeryItem> page = new PageImpl<>(Arrays.asList(paperItem));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<ItemResponse> result = service.getAllItems(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("A4 Printing Paper", result.getContent().get(0).getName());
    }

    @Test
    public void getById_success_whenIdIsValid() {
        when(repository.findById(1L)).thenReturn(Optional.of(paperItem));

        ItemResponse response = service.getById(1L);

        assertNotNull(response);
        assertEquals("A4 Printing Paper", response.getName());
    }

    @Test
    public void getById_throwsNotFound_whenIdInvalid() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> {
            service.getById(99L);
        });
    }

    @Test
    public void updateItem_success_whenIdIsValid() {
        when(repository.findById(1L)).thenReturn(Optional.of(paperItem));
        when(repository.save(any(StationeryItem.class))).thenReturn(paperItem);

        ItemResponse response = service.updateItem(1L, paperRequest);

        assertNotNull(response);
        assertEquals("A4 Printing Paper", response.getName());
    }

    @Test
    public void deleteItem_success_whenIdIsValid() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(() -> service.deleteItem(1L));
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    public void deleteItem_throwsNotFound_whenIdInvalid() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () -> {
            service.deleteItem(99L);
        });
    }

    @Test
    public void deductQuantity_success_whenStockAvailable() {
        when(repository.findById(1L)).thenReturn(Optional.of(paperItem));
        when(repository.save(any(StationeryItem.class))).thenReturn(paperItem);

        assertDoesNotThrow(() -> service.deductQuantity(1L, 30));
        assertEquals(70, paperItem.getAvailableQuantity());
        verify(repository, times(1)).save(paperItem);
    }

    @Test
    public void deductQuantity_throwsException_whenInsufficientStock() {
        when(repository.findById(1L)).thenReturn(Optional.of(paperItem));

        assertThrows(InsufficientStockException.class, () -> {
            service.deductQuantity(1L, 150);
        });
    }
}
