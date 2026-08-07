package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowBlock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowBlockRepository extends JpaRepository<FlowBlock, Long> {

	List<FlowBlock> findByFlow_FlowIdOrderByBlockOrderAsc(Long flowId);

	@Modifying
	@Query("delete from FlowBlock fb where fb.flow.flowId = :flowId")
	void deleteByFlow_FlowId(@Param("flowId") Long flowId);
}
