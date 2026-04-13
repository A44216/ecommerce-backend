package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    // lấy tất cả address của user
    List<Address> findByUserId(Integer userId);

    // lấy address mặc định
    Optional<Address> findByUserIdAndIsDefaultTrue(Integer userId);

    // bỏ default của các address khác
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultByUser(Integer userId);
}