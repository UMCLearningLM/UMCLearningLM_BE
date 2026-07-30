package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.Flow;
import java.util.List;

import com.umc.learninglm.domain.home.dto.query.PopularFlowQuery;
import com.umc.learninglm.domain.home.dto.query.RecentCopiedFlowQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowRepository extends JpaRepository<Flow, Long> {

    // 홈 화면의 인기 공개 흐름과 사용자의 최근 복사 흐름을 조회
    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.PopularFlowQuery(
                f.flowId,
                f.title,
                f.summary,
                cast(f.difficulty as string),
                u.userId,
                u.nickname,
                count(distinct fl.flowLikeId),
                count(distinct copied.flowId),
                count(distinct fc.flowCommentId)
            )
            from Flow f
            join f.user u
            left join FlowLike fl
                on fl.flow = f
            left join Flow copied
                on copied.originFlow = f
            left join FlowComment fc
                on fc.flow = f
               and fc.status = 'ACTIVE'
            where f.visibility = 'PUBLIC'
              and f.status = 'COMPLETED'
            group by
                f.flowId,
                f.title,
                f.summary,
                f.difficulty,
                u.userId,
                u.nickname
            order by
                count(distinct copied.flowId) desc,
                count(distinct fl.flowLikeId) desc,
                f.flowId desc
            """)
    List<PopularFlowQuery> findPopularFlows(
            Pageable pageable
    );

    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.RecentCopiedFlowQuery(
                copied.flowId,
                original.flowId,
                copied.title,
                cast(copied.difficulty as string),
                originalUser.userId,
                originalUser.nickname,
                copied.createdAt
            )
            from Flow copied
            join copied.originFlow original
            join original.user originalUser
            where copied.user.userId = :userId
            order by copied.createdAt desc, copied.flowId desc
            """)
    List<RecentCopiedFlowQuery> findRecentCopiedFlows(
            @Param("userId") Long userId,
            Pageable pageable
    );
}