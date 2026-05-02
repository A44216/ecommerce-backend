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

    @Query("""
        SELECT c FROM Complaint c
        JOIN c.user u
        WHERE (:status IS NULL OR c.status = :status)
        AND (
            :keyword IS NULL OR
            LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    Page<Complaint> adminSearchComplaints(
            @Param("status") ComplaintStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Complaint> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @Query("""
        SELECT DISTINCT u.username
        FROM Complaint c
        JOIN c.user u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY u.username
    """)
    List<String> autoCompleteComplaints(@Param("keyword") String keyword, Pageable pageable);

}