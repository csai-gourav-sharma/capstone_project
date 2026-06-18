package com.sms.request.service;

import com.sms.request.exception.RequestNotFoundException;
import com.sms.request.feign.InventoryFeignClient;
import com.sms.request.model.RequestItem;
import com.sms.request.model.StationeryRequest;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;
import com.sms.request.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private InventoryFeignClient inventoryClient;

    @InjectMocks
    private RequestServiceImpl requestService;

    private SubmitRequestDTO submitRequestDTO;
    private StationeryRequest pendingRequest;
    private RequestItem requestItem;

    @BeforeEach
    public void setUp() {
        SubmitRequestDTO.RequestItemDTO itemReq = SubmitRequestDTO.RequestItemDTO.builder()
                .itemId(1L)
                .quantity(5)
                .build();

        submitRequestDTO = SubmitRequestDTO.builder()
                .items(Arrays.asList(itemReq))
                .build();

        requestItem = RequestItem.builder()
                .id(1L)
                .itemId(1L)
                .itemName("A4 Printing Paper")
                .quantity(5)
                .build();

        pendingRequest = StationeryRequest.builder()
                .id(1L)
                .studentEmail("student@sms.com")
                .studentId(0L)
                .status(StationeryRequest.RequestStatus.PENDING)
                .items(new ArrayList<>(Arrays.asList(requestItem)))
                .build();
        
        requestItem.setRequest(pendingRequest);
    }

    @Test
    public void submitRequest_success_whenItemsAvailable() {
        Map<String, Object> mockItemResponse = new HashMap<>();
        mockItemResponse.put("id", 1);
        mockItemResponse.put("name", "A4 Printing Paper");
        mockItemResponse.put("availableQuantity", 100);

        when(inventoryClient.getItemById(1L)).thenReturn(mockItemResponse);
        when(requestRepository.save(any(StationeryRequest.class))).thenReturn(pendingRequest);

        RequestResponseDTO response = requestService.submitRequest(submitRequestDTO, "student@sms.com");

        assertNotNull(response);
        assertEquals("student@sms.com", response.getStudentEmail());
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals("A4 Printing Paper", response.getItems().get(0).getItemName());
        verify(requestRepository, times(1)).save(any(StationeryRequest.class));
    }

    @Test
    public void submitRequest_fallbackItemName_whenFeignFails() {
        when(inventoryClient.getItemById(1L)).thenThrow(new RuntimeException("Service down"));
        when(requestRepository.save(any(StationeryRequest.class))).thenReturn(pendingRequest);

        RequestResponseDTO response = requestService.submitRequest(submitRequestDTO, "student@sms.com");

        assertNotNull(response);
        verify(requestRepository, times(1)).save(any(StationeryRequest.class));
    }

    @Test
    public void approveRequest_updatesStatusAndDeductsStock() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        doNothing().when(inventoryClient).deductQuantity(1L, 5);
        when(requestRepository.save(any(StationeryRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponseDTO response = requestService.approveRequest(1L);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        verify(inventoryClient, times(1)).deductQuantity(1L, 5);
        verify(requestRepository, times(1)).save(pendingRequest);
    }

    @Test
    public void approveRequest_throwsException_whenNotPending() {
        pendingRequest.setStatus(StationeryRequest.RequestStatus.APPROVED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(IllegalStateException.class, () -> {
            requestService.approveRequest(1L);
        });

        verify(inventoryClient, never()).deductQuantity(anyLong(), anyInt());
    }

    @Test
    public void rejectRequest_savesAdminComment() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any(StationeryRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestResponseDTO response = requestService.rejectRequest(1L, "Insufficient stock info");

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("Insufficient stock info", response.getAdminComment());
        verify(requestRepository, times(1)).save(pendingRequest);
    }

    @Test
    public void getRequestById_throwsNotFound_whenIdInvalid() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RequestNotFoundException.class, () -> {
            requestService.getRequestById(99L);
        });
    }
}
