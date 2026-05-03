package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Recommendation;
import com.ecommerce.backend.enums.RecommendationType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {
    List<Recommendation> findByUserId(Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Recommendation r WHERE r.user.id = :userId AND r.type = :type")
    void deleteByUserIdAndType(@Param("userId") Integer userId, @Param("type") RecommendationType type);

    // THÊM MỚI: Hàm tìm kiếm để phục vụ cơ chế Upsert
    Optional<Recommendation> findByUserIdAndProductIdAndType(Integer userId, Integer productId, RecommendationType type);

}