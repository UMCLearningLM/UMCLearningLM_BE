package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowCommentRepository extends JpaRepository<FlowComment, Long> {

	void deleteByFlow_FlowId(Long flowId);
}
