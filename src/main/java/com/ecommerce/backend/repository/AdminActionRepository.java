package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.AdminAction;
import com.ecommerce.backend.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionRepository extends JpaRepository<AdminAction, Integer> {

    Page<AdminAction> findByEntityType(EntityType entityType, Pageable pageable);

    Page<AdminAction> findAll(Pageable pageable);
}