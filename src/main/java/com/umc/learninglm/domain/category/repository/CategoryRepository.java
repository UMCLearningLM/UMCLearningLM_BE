package com.umc.learninglm.domain.category.repository;

import com.umc.learninglm.domain.category.entity.Category;
import java.util.Collection;
import java.util.List;

import com.umc.learninglm.domain.home.dto.query.CategoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 홈 화면 카테고리 조회
    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.CategoryQuery(
                c.categoryId,
                c.name,
                c.sortOrder
            )
            from Category c
            where c.name in :codes
            """)
    List<CategoryQuery> findHomeCategories(
            @Param("codes") Collection<String> codes
    );
}