package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.TutorialStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialStepRepository extends JpaRepository<TutorialStep, Long> {

	List<TutorialStep> findByTutorial_TutorialIdOrderByStepOrderAsc(Long tutorialId);

	long countByTutorial_TutorialId(Long tutorialId);
}
