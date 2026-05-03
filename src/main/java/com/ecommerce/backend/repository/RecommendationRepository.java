package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Recommendation;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {

    // Tìm tất cả gợi ý hiện có của User để xử lý trong Map
    List<Recommendation> findAllByUserId(Integer userId);

    @EntityGraph(attributePaths = {"product"})
    List<Recommendation> findByUserIdOrderByScoreDesc(Integer userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Recommendation r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}
