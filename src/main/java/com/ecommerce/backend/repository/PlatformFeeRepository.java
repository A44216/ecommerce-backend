package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.PlatformFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformFeeRepository extends JpaRepository<PlatformFee, Integer> {
    Optional<PlatformFee> findByIsActiveTrue();
}
