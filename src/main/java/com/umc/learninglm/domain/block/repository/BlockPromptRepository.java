package com.umc.learninglm.domain.block.repository;

import com.umc.learninglm.domain.block.entity.Block;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface BlockPromptRepository extends Repository<Block, Long> {

	@Query("""
			select b
			from Block b
			join fetch b.promptTemplate pt
			where b.blockId = :blockId
			  and pt.active = true
			""")
	Optional<Block> findWithActivePromptTemplateByBlockId(
			@Param("blockId") Long blockId
	);
}
