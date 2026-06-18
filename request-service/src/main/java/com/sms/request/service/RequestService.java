package com.sms.request.service;

import com.sms.request.model.dto.RequestResponseDTO;
import com.sms.request.model.dto.SubmitRequestDTO;

import java.util.List;

/**
 * Service interface for stationery request operations.
 */
public interface RequestService {

    RequestResponseDTO submitRequest(SubmitRequestDTO dto, String studentEmail);

    List<RequestResponseDTO> getMyRequests(String studentEmail);

    List<RequestResponseDTO> getAllRequests();

    List<RequestResponseDTO> getPendingRequests();

    RequestResponseDTO getRequestById(Long id);

    RequestResponseDTO approveRequest(Long id);

    RequestResponseDTO rejectRequest(Long id, String comment);
}
