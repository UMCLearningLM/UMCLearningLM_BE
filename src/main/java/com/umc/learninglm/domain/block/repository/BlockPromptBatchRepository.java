package com.umc.learninglm.domain.block.repository;

import com.umc.learninglm.domain.block.entity.Block;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface BlockPromptBatchRepository extends Repository<Block, Long> {

	@Query("""
			select b
			from Block b
			join fetch b.promptTemplate pt
			where b.blockId in :blockIds
			  and pt.active = true
			""")
	List<Block> findAllWithActivePromptTemplateByBlockIdIn(
			@Param("blockIds") Collection<Long> blockIds
	);
}
