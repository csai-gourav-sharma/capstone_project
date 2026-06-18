package com.sms.request.repository;

import com.sms.request.model.StationeryRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<StationeryRequest, Long> {

    List<StationeryRequest> findByStudentEmailOrderByRequestDateDesc(String studentEmail);

    List<StationeryRequest> findByStatusOrderByRequestDateDesc(StationeryRequest.RequestStatus status);

    List<StationeryRequest> findAllByOrderByRequestDateDesc();
}
