package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Complaint;
import com.ecommerce.backend.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

    List<Complaint> findByUserId(Integer userId);

    Long countByStatus(ComplaintStatus status);

    List<Complaint> findByOrderId(Integer orderId);

    @Query(value = "SELECT c FROM Complaint c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.order WHERE (:status IS NULL OR c.status = :status)",
           countQuery = "SELECT COUNT(c) FROM Complaint c WHERE (:status IS NULL OR c.status = :status)")
    Page<Complaint> adminSearchComplaints(@Param("status") ComplaintStatus status, Pageable pageable);

}