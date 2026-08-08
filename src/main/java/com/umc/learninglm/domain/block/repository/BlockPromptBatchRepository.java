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
			left join fetch b.promptTemplate
			where b.blockId in :blockIds
			""")
	List<Block> findAllWithPromptTemplateByBlockIdIn(
			@Param("blockIds") Collection<Long> blockIds
	);
}
