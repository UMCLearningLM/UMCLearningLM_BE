package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowTagRepository extends JpaRepository<FlowTag, Long> {

	void deleteByFlow_FlowId(Long flowId);
}
