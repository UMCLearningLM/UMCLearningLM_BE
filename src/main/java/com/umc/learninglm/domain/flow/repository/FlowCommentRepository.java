package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowCommentRepository extends JpaRepository<FlowComment, Long> {

	@Modifying
	@Query("delete from FlowComment fc where fc.flow.flowId = :flowId")
	void deleteByFlow_FlowId(@Param("flowId") Long flowId);
}
