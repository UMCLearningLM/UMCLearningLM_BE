package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.home.dto.query.ContinueLearningQuery;
import com.umc.learninglm.domain.home.dto.query.RecentTutorialQuery;
import com.umc.learninglm.domain.storage.dto.query.SavedTutorialQuery;
import com.umc.learninglm.domain.tutorial.entity.SavedTutorial;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavedTutorialRepository extends JpaRepository<SavedTutorial, Long> {

	// uq_saved_tutorial (user_id, tutorial_id) 전제 — 사용자당 튜토리얼당 진행 1행
	Optional<SavedTutorial> findByUser_UserIdAndTutorial_TutorialId(Long userId, Long tutorialId);

	List<SavedTutorial> findByUser_UserId(Long userId);

    // 저장 이후 DRAFT로 전환된 튜토리얼은 조회 API에서 404이므로 개수에서도 제외한다.
    // 목록(findSavedTutorials)과 기준을 맞춰야 개수와 실제 항목 수가 어긋나지 않는다.
    @Query("""
            select count(st)
            from SavedTutorial st
            join st.tutorial t
            where st.user.userId = :userId
              and t.status = com.umc.learninglm.domain.tutorial.enums.TutorialStatus.PUBLISHED
            """)
    long countPublishedByUserId(@Param("userId") Long userId);

    // 내 저장소의 저장한 튜토리얼 목록을 진행 정보와 함께 조회
    @Query("""
            select new com.umc.learninglm.domain.storage.dto.query.SavedTutorialQuery(
                t.tutorialId,
                f.flowId,
                t.title,
                t.summary,
                cast(t.difficulty as string),
                t.thumbnailUrl,
                cast(st.status as string),
                st.currentStepOrder,
                count(distinct ts.tutorialStepId),
                st.createdAt,
                st.updatedAt
            )
            from SavedTutorial st
            join st.tutorial t
            left join st.flow f
            left join TutorialStep ts
                on ts.tutorial = t
            where st.user.userId = :userId
              and t.status = com.umc.learninglm.domain.tutorial.enums.TutorialStatus.PUBLISHED
            group by
                st.savedTutorialId,
                t.tutorialId,
                f.flowId,
                t.title,
                t.summary,
                t.difficulty,
                t.thumbnailUrl,
                st.status,
                st.currentStepOrder,
                st.createdAt,
                st.updatedAt
            order by st.updatedAt desc, st.savedTutorialId desc
            """)
    List<SavedTutorialQuery> findSavedTutorials(@Param("userId") Long userId);

    // 홈 화면 사용자의 이어서 학습 튜토리얼과 최근 저장 튜토리얼을 조회
    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.ContinueLearningQuery(
                t.tutorialId,
                f.flowId,
                t.title,
                cast(t.difficulty as string),
                st.currentStepOrder,
                count(distinct ts.tutorialStepId),
                cast(st.status as string),
                t.thumbnailUrl,
                st.updatedAt
            )
            from SavedTutorial st
            join st.tutorial t
            left join st.flow f
            left join TutorialStep ts
                on ts.tutorial = t
            where st.user.userId = :userId
              and st.status = com.umc.learninglm.domain.tutorial.enums.SavedTutorialStatus.IN_PROGRESS
              and t.status = com.umc.learninglm.domain.tutorial.enums.TutorialStatus.PUBLISHED
            group by
                st.savedTutorialId,
                t.tutorialId,
                f.flowId,
                t.title,
                t.difficulty,
                st.currentStepOrder,
                st.status,
                t.thumbnailUrl,
                st.updatedAt
            order by st.updatedAt desc, st.savedTutorialId desc
            """)
    List<ContinueLearningQuery> findContinueLearning(
            @Param("userId") Long userId,
            Pageable pageable
    );

    default Optional<ContinueLearningQuery>
    findLatestContinueLearning(Long userId) {
        return findContinueLearning(
                userId,
                Pageable.ofSize(1)
        ).stream().findFirst();
    }

    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.RecentTutorialQuery(
                t.tutorialId,
                f.flowId,
                t.title,
                cast(t.difficulty as string),
                cast(st.status as string),
                t.thumbnailUrl,
                st.createdAt
            )
            from SavedTutorial st
            join st.tutorial t
            left join st.flow f
            where st.user.userId = :userId
              and t.status = com.umc.learninglm.domain.tutorial.enums.TutorialStatus.PUBLISHED
            order by st.createdAt desc, st.savedTutorialId desc
            """)
    List<RecentTutorialQuery> findRecentTutorials(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
