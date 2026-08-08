package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.Flow;
import java.util.List;

import com.umc.learninglm.domain.flow.enums.FlowMode;
import com.umc.learninglm.domain.home.dto.query.PopularFlowQuery;
import com.umc.learninglm.domain.home.dto.query.RecentCopiedFlowQuery;
import com.umc.learninglm.domain.storage.dto.query.MyFlowQuery;
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

    // 내 저장소의 "내가 만든 흐름" — 복사본이 아닌 원본. 가이드 모드는 학습용이라 제외
    @Query("""
            select new com.umc.learninglm.domain.storage.dto.query.MyFlowQuery(
                f.flowId,
                f.title,
                f.summary,
                cast(f.difficulty as string),
                cast(f.mode as string),
                cast(f.visibility as string),
                cast(f.status as string),
                null,
                null,
                f.updatedAt
            )
            from Flow f
            where f.user.userId = :userId
              and f.mode = com.umc.learninglm.domain.flow.enums.FlowMode.CREATE
              and f.originFlow is null
            order by f.updatedAt desc, f.flowId desc
            """)
    List<MyFlowQuery> findMyOwnFlows(@Param("userId") Long userId);

    // 내 저장소의 "복사한 흐름" — 원본 흐름과 원작자 정보를 함께 조회
    @Query("""
            select new com.umc.learninglm.domain.storage.dto.query.MyFlowQuery(
                f.flowId,
                f.title,
                f.summary,
                cast(f.difficulty as string),
                cast(f.mode as string),
                cast(f.visibility as string),
                cast(f.status as string),
                origin.flowId,
                originUser.nickname,
                f.updatedAt
            )
            from Flow f
            join f.originFlow origin
            join origin.user originUser
            where f.user.userId = :userId
              and f.mode = com.umc.learninglm.domain.flow.enums.FlowMode.CREATE
            order by f.updatedAt desc, f.flowId desc
            """)
    List<MyFlowQuery> findMyCopiedFlows(@Param("userId") Long userId);

    long countByUser_UserIdAndModeAndOriginFlowIsNull(Long userId, FlowMode mode);

    long countByUser_UserIdAndModeAndOriginFlowIsNotNull(Long userId, FlowMode mode);
}