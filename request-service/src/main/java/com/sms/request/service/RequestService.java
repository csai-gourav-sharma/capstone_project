package com.sms.request.service;

import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;

import java.util.List;

/**
 * Service interface for stationery request operations.
 */
public interface RequestService {

    /**
     * Submits a new stationery request on behalf of a student.
     * Mocks or fetches item names from the inventory service via Feign.
     *
     * @param dto the request details
     * @param studentEmail the email of the submitting student
     * @return the submitted request details
     */
    RequestResponseDTO submitRequest(SubmitRequestDTO dto, String studentEmail);

    /**
     * Retrieves all requests submitted by a specific student.
     *
     * @param studentEmail the student's email
     * @return a list of their requests
     */
    List<RequestResponseDTO> getMyRequests(String studentEmail);

    /**
     * Retrieves all requests submitted by all students. Used by admins.
     *
     * @return a list of all requests
     */
    List<RequestResponseDTO> getAllRequests();

    /**
     * Retrieves all requests with status PENDING. Used by admins.
     *
     * @return a list of pending requests
     */
    List<RequestResponseDTO> getPendingRequests();

    /**
     * Retrieves details of a specific request by its unique identifier.
     *
     * @param id the request ID
     * @return the request details
     * @throws RequestNotFoundException if the request does not exist
     */
    RequestResponseDTO getRequestById(Long id);

    /**
     * Approves a request. Calls Feign to deduct stock for each requested item.
     *
     * @param id the ID of the request to approve
     * @return the updated request details
     * @throws RequestNotFoundException if the request does not exist
     * @throws IllegalStateException if the request is not in PENDING status
     */
    RequestResponseDTO approveRequest(Long id);

    /**
     * Rejects a request with an admin justification comment.
     *
     * @param id the ID of the request to reject
     * @param comment the rejection comment
     * @return the updated request details
     * @throws RequestNotFoundException if the request does not exist
     * @throws IllegalStateException if the request is not in PENDING status
     */
    RequestResponseDTO rejectRequest(Long id, String comment);
}
