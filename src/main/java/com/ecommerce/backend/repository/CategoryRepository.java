package com.ecommerce.backend.repository;

import com.ecommerce.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Category c WHERE c.isDeleted = false OR c.isDeleted IS NULL")
    java.util.List<Category> findAllActive();

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Category c WHERE c.id = :id AND (c.isDeleted = false OR c.isDeleted IS NULL)")
    java.util.Optional<Category> findActiveById(@org.springframework.data.repository.query.Param("id") Integer id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

}