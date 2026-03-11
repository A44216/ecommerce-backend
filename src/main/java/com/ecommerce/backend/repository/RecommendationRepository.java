package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Integer> {

    List<Recommendation> findByUserId(Integer userId);

}