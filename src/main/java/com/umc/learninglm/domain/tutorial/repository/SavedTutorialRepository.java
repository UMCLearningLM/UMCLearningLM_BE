package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.SavedTutorial;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedTutorialRepository extends JpaRepository<SavedTutorial, Long> {

	// uq_saved_tutorial (user_id, tutorial_id) 전제 — 사용자당 튜토리얼당 진행 1행
	Optional<SavedTutorial> findByUser_UserIdAndTutorial_TutorialId(Long userId, Long tutorialId);

	List<SavedTutorial> findByUser_UserId(Long userId);
}
