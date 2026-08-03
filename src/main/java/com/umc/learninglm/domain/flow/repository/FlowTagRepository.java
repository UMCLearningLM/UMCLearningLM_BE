package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowTagRepository extends JpaRepository<FlowTag, Long> {

	@Modifying
	@Query("delete from FlowTag ft where ft.flow.flowId = :flowId")
	void deleteByFlow_FlowId(@Param("flowId") Long flowId);
}
