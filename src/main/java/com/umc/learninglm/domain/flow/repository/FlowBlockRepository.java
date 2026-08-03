package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.FlowBlock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowBlockRepository extends JpaRepository<FlowBlock, Long> {

	List<FlowBlock> findByFlow_FlowIdOrderByBlockOrderAsc(Long flowId);

	void deleteByFlow_FlowId(Long flowId);
}
