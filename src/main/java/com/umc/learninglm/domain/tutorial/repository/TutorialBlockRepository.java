package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.TutorialBlock;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialBlockRepository extends JpaRepository<TutorialBlock, Long> {

	List<TutorialBlock> findByTutorialStep_TutorialStepIdOrderByBlockOrderAsc(Long tutorialStepId);

	long countByTutorialStep_TutorialStepIdIn(Collection<Long> tutorialStepIds);
}
