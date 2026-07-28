package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.TutorialCategory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialCategoryRepository extends JpaRepository<TutorialCategory, Long> {

	// category를 함께 조회 — 응답에 카테고리 코드/이름이 항상 필요하다
	@EntityGraph(attributePaths = "category")
	List<TutorialCategory> findByTutorial_TutorialId(Long tutorialId);
}
