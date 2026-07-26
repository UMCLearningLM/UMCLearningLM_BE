package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.TutorialCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialCategoryRepository extends JpaRepository<TutorialCategory, Long> {

	List<TutorialCategory> findByTutorial_TutorialId(Long tutorialId);
}
