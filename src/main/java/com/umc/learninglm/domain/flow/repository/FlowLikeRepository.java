package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowLikeRepository extends JpaRepository<FlowLike, Long> {

	@Modifying
	@Query("delete from FlowLike fl where fl.flow.flowId = :flowId")
	void deleteByFlow_FlowId(@Param("flowId") Long flowId);
}
