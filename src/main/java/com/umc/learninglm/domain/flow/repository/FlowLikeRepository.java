package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowLikeRepository extends JpaRepository<FlowLike, Long> {

	void deleteByFlow_FlowId(Long flowId);
}
