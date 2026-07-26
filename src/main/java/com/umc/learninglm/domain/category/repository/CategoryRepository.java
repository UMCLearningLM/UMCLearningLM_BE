package com.umc.learninglm.domain.category.repository;

import com.umc.learninglm.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
