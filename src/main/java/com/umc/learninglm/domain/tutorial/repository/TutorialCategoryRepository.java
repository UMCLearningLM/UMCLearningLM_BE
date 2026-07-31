package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.home.dto.query.TutorialCategoryQuery;
import com.umc.learninglm.domain.tutorial.entity.TutorialCategory;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TutorialCategoryRepository extends JpaRepository<TutorialCategory, Long> {

	// category를 함께 조회 — 응답에 카테고리 코드/이름이 항상 필요하다
	@EntityGraph(attributePaths = "category")
	List<TutorialCategory> findByTutorial_TutorialId(Long tutorialId);

    // 추천 튜토리얼에 연결된 카테고리를 일괄 조회
    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.TutorialCategoryQuery(
                tc.tutorial.tutorialId,
                c.categoryId,
                c.name,
                c.sortOrder
            )
            from TutorialCategory tc
            join tc.category c
            where tc.tutorial.tutorialId in :tutorialIds
            order by
                tc.tutorial.tutorialId asc,
                c.sortOrder asc,
                c.categoryId asc
            """)
    List<TutorialCategoryQuery> findByTutorialIds(
            @Param("tutorialIds") Collection<Long> tutorialIds
    );
}
