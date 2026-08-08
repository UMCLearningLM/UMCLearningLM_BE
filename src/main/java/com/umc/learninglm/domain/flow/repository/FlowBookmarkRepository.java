package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowBookmarkRepository extends JpaRepository<FlowBookmark, Long> {

	@Modifying
	@Query("delete from FlowBookmark fb where fb.flow.flowId = :flowId")
	void deleteByFlow_FlowId(@Param("flowId") Long flowId);
}
