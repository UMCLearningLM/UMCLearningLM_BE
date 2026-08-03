package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowCategory;
import java.util.Collection;
import java.util.List;

import com.umc.learninglm.domain.home.dto.query.FlowCategoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowCategoryRepository extends JpaRepository<FlowCategory, Long> {

    List<FlowCategory> findByFlow_FlowId(Long flowId);

    void deleteByFlow_FlowId(Long flowId);

    // 홈 화면 인기 공개 흐름에 연결된 카테고리를 일괄 조회
    @Query("""
            select new com.umc.learninglm.domain.home.dto.query.FlowCategoryQuery(
                fc.flow.flowId,
                c.categoryId,
                c.name,
                c.sortOrder
            )
            from FlowCategory fc
            join fc.category c
            where fc.flow.flowId in :flowIds
            order by
                fc.flow.flowId asc,
                c.sortOrder asc,
                c.categoryId asc
            """)
    List<FlowCategoryQuery> findByFlowIds(
            @Param("flowIds") Collection<Long> flowIds
    );
}