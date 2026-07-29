package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.TutorialBlock;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialBlockRepository extends JpaRepository<TutorialBlock, Long> {

	// block을 함께 조회 — 응답에 블록 이름/타입이 항상 필요해 LAZY 개별 로딩을 피한다
	@EntityGraph(attributePaths = "block")
	List<TutorialBlock> findByTutorialStep_TutorialStepIdOrderByBlockOrderAsc(Long tutorialStepId);

	long countByTutorialStep_TutorialStepIdIn(Collection<Long> tutorialStepIds);
}
