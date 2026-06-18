package com.sms.request.repository;

import com.sms.request.model.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {
}
