package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.tutorial.entity.Tutorial;
import com.umc.learninglm.domain.tutorial.enums.TutorialStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

	// 목록 조회는 status=PUBLISHED만 노출
	List<Tutorial> findByStatus(TutorialStatus status);
}
