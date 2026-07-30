package com.umc.learninglm.domain.tutorial.repository;

import com.umc.learninglm.domain.home.dto.query.RecommendedTutorialQuery;
import com.umc.learninglm.domain.tutorial.entity.Tutorial;
import com.umc.learninglm.domain.tutorial.enums.TutorialStatus;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {

	// 목록 조회는 status=PUBLISHED만 노출
	List<Tutorial> findByStatus(TutorialStatus status);

    // 게스트와 회원에게 노출할 홈 화면 추천 튜토리얼을 조회
    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.RecommendedTutorialQuery(
                t.tutorialId,
                t.title,
                t.summary,
                cast(t.difficulty as string),
                count(distinct tb.tutorialBlockId),
                t.estimatedMinutes,
                t.thumbnailUrl
            )
            from Tutorial t
            left join TutorialStep ts
                on ts.tutorial = t
            left join TutorialBlock tb
                on tb.tutorialStep = ts
            where t.status = com.umc.learninglm.domain.tutorial.enums.TutorialStatus.PUBLISHED
            group by
                t.tutorialId,
                t.title,
                t.summary,
                t.difficulty,
                t.estimatedMinutes,
                t.thumbnailUrl,
                t.createdAt
            order by t.createdAt desc, t.tutorialId desc
            """)
    List<RecommendedTutorialQuery>
    findRecommendedTutorialsForGuest(Pageable pageable);

    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.RecommendedTutorialQuery(
                t.tutorialId,
                t.title,
                t.summary,
                cast(t.difficulty as string),
                count(distinct tb.tutorialBlockId),
                t.estimatedMinutes,
                t.thumbnailUrl
            )
            from Tutorial t
            left join TutorialStep ts
                on ts.tutorial = t
            left join TutorialBlock tb
                on tb.tutorialStep = ts
            where t.status = com.umc.learninglm.domain.tutorial.enums.TutorialStatus.PUBLISHED
              and not exists (
                    select 1
                    from SavedTutorial st
                    where st.user.userId = :userId
                      and st.tutorial = t
                      and st.status = com.umc.learninglm.domain.tutorial.enums.SavedTutorialStatus.IN_PROGRESS
              )
            group by
                t.tutorialId,
                t.title,
                t.summary,
                t.difficulty,
                t.estimatedMinutes,
                t.thumbnailUrl,
                t.createdAt
            order by t.createdAt desc, t.tutorialId desc
            """)
    List<RecommendedTutorialQuery>
    findRecommendedTutorialsForUser(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
