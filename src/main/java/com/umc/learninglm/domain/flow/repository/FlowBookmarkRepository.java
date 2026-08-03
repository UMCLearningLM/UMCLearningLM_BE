package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowBookmarkRepository extends JpaRepository<FlowBookmark, Long> {

	void deleteByFlow_FlowId(Long flowId);
}
