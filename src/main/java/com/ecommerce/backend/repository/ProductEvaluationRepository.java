package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.ProductEvaluation;
import com.ecommerce.backend.enums.ProductEvaluationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductEvaluationRepository extends JpaRepository<ProductEvaluation, Integer> {

    Optional<ProductEvaluation> findByProductIdAndType(Integer productId, ProductEvaluationType type);

    @Query("""
        SELECT pe FROM ProductEvaluation pe\s
        JOIN FETCH pe.product p\s
        JOIN FETCH p.category c
        WHERE pe.type = :type AND p.status = 'APPROVED'\s
        ORDER BY pe.score DESC, p.createdAt DESC
   \s""")
    List<ProductEvaluation> findTopProductsByType(ProductEvaluationType type, Pageable pageable);

    @Query("""
        SELECT pe FROM ProductEvaluation pe\s
        WHERE pe.product.id IN :productIds AND pe.type = :type
        """)
    List<ProductEvaluation> findAllByProductIdsAndType(List<Integer> productIds, ProductEvaluationType type);

}