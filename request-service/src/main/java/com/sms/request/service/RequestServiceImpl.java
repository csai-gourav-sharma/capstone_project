package com.sms.request.service;

import com.sms.request.exception.RequestNotFoundException;
import com.sms.request.feign.InventoryFeignClient;
import com.sms.request.model.RequestItem;
import com.sms.request.model.StationeryRequest;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;
import com.sms.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of RequestService.
 * Uses Feign to communicate with inventory-service for item lookup and stock deduction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final InventoryFeignClient inventoryClient;

    @Override
    @Transactional
    public RequestResponseDTO submitRequest(SubmitRequestDTO dto, String studentEmail) {
        StationeryRequest request = StationeryRequest.builder()
                .studentEmail(studentEmail)
                .studentId(0L) // We don't have user ID from token; email is primary identifier
                .status(StationeryRequest.RequestStatus.PENDING)
                .build();

        // Build request items, fetching item names from inventory service
        List<RequestItem> items = dto.getItems().stream()
                .map(itemDto -> {
                    // Fetch item details from inventory service
                    String itemName;
                    try {
                        Map<String, Object> inventoryItem = inventoryClient.getItemById(itemDto.getItemId());
                        itemName = (String) inventoryItem.get("name");
                    } catch (Exception e) {
                        log.warn("Could not fetch item name for id={}: {}", itemDto.getItemId(), e.getMessage());
                        itemName = "Unknown Item #" + itemDto.getItemId();
                    }

                    return RequestItem.builder()
                            .request(request)
                            .itemId(itemDto.getItemId())
                            .itemName(itemName)
                            .quantity(itemDto.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        request.setItems(items);

        StationeryRequest saved = requestRepository.save(request);
        log.info("Request #{} submitted by {}", saved.getId(), studentEmail);
        return toResponse(saved);
    }

    @Override
    public List<RequestResponseDTO> getMyRequests(String studentEmail) {
        return requestRepository.findByStudentEmailOrderByRequestDateDesc(studentEmail)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestResponseDTO> getAllRequests() {
        return requestRepository.findAllByOrderByRequestDateDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestResponseDTO> getPendingRequests() {
        return requestRepository.findByStatusOrderByRequestDateDesc(StationeryRequest.RequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RequestResponseDTO getRequestById(Long id) {
        StationeryRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request not found with id: " + id));
        return toResponse(request);
    }

    @Override
    @Transactional
    public RequestResponseDTO approveRequest(Long id) {
        StationeryRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != StationeryRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Request #" + id + " is not in PENDING status. Current: " + request.getStatus());
        }

        // Deduct stock from inventory for each item via Feign
        for (RequestItem item : request.getItems()) {
            try {
                inventoryClient.deductQuantity(item.getItemId(), item.getQuantity());
                log.info("Deducted {} of item {} for request #{}", item.getQuantity(), item.getItemId(), id);
            } catch (Exception e) {
                log.error("Failed to deduct stock for item {}: {}", item.getItemId(), e.getMessage());
                throw new RuntimeException("Failed to deduct stock for '" + item.getItemName()
                        + "': " + e.getMessage());
            }
        }

        request.setStatus(StationeryRequest.RequestStatus.APPROVED);
        StationeryRequest saved = requestRepository.save(request);
        log.info("Request #{} approved", id);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RequestResponseDTO rejectRequest(Long id, String comment) {
        StationeryRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != StationeryRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Request #" + id + " is not in PENDING status. Current: " + request.getStatus());
        }

        request.setStatus(StationeryRequest.RequestStatus.REJECTED);
        request.setAdminComment(comment);
        StationeryRequest saved = requestRepository.save(request);
        log.info("Request #{} rejected with comment: {}", id, comment);
        return toResponse(saved);
    }

    // --- Helper ---

    private RequestResponseDTO toResponse(StationeryRequest request) {
        List<RequestResponseDTO.ItemDetail> itemDetails = request.getItems().stream()
                .map(item -> RequestResponseDTO.ItemDetail.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return RequestResponseDTO.builder()
                .id(request.getId())
                .studentId(request.getStudentId())
                .studentEmail(request.getStudentEmail())
                .status(request.getStatus().name())
                .adminComment(request.getAdminComment())
                .requestDate(request.getRequestDate())
                .items(itemDetails)
                .build();
    }
}
