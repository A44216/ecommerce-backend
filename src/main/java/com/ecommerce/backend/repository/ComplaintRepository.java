package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {
    List<Complaint> findByUserIdOrderByCreatedAtDesc(Integer userId);
}