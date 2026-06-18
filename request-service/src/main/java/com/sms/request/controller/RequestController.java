package com.sms.request.controller;

import com.sms.request.model.dto.RejectRequestDTO;
import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;
import com.sms.request.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for stationery request operations.
 * All endpoints are prefixed with /api/requests.
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    /**
     * Submit a new request (STUDENT/FACULTY only).
     * POST /api/requests
     */
    @PostMapping
    public ResponseEntity<RequestResponseDTO> submitRequest(
            @Valid @RequestBody SubmitRequestDTO dto,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestService.submitRequest(dto, principal.getName()));
    }

    /**
     * Get current user's requests.
     * GET /api/requests/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<RequestResponseDTO>> getMyRequests(Principal principal) {
        return ResponseEntity.ok(requestService.getMyRequests(principal.getName()));
    }

    /**
     * Get all requests (ADMIN only).
     * GET /api/requests/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<RequestResponseDTO>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllRequests());
    }

    /**
     * Get pending requests (ADMIN only).
     * GET /api/requests/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RequestResponseDTO>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    /**
     * Get a specific request by ID.
     * GET /api/requests/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RequestResponseDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    /**
     * Approve a request (ADMIN only). Triggers stock deduction via Feign.
     * PUT /api/requests/{id}/approve
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<RequestResponseDTO> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.approveRequest(id));
    }

    /**
     * Reject a request with comment (ADMIN only).
     * PUT /api/requests/{id}/reject
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<RequestResponseDTO> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequestDTO dto) {
        String comment = (dto != null) ? dto.getComment() : null;
        return ResponseEntity.ok(requestService.rejectRequest(id, comment));
    }
}
