package com.umc.learninglm.domain.block.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileResult;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.block.enums.ExecutionMode;
import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import com.umc.learninglm.domain.block.repository.BlockPromptBatchRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BlockPromptCompilerImplTest {

	@Test
	void separatesPersistenceBlockWithoutPromptTemplate() {
		BlockPromptBatchRepository repository =
				mock(BlockPromptBatchRepository.class);
		BlockPromptRenderer renderer = mock(BlockPromptRenderer.class);
		BlockPromptCompiler compiler = new BlockPromptCompilerImpl(
				repository,
				renderer
		);
		Block persistenceBlock = mock(Block.class);
		when(persistenceBlock.getBlockId()).thenReturn(21L);
		when(persistenceBlock.getBlockType()).thenReturn(BlockType.OUTPUT);
		when(persistenceBlock.getDefaultExecutionMode())
				.thenReturn(ExecutionMode.SYSTEM);
		when(repository.findAllWithPromptTemplateByBlockIdIn(List.of(21L)))
				.thenReturn(List.of(persistenceBlock));

		BlockPromptCompileResult result = compiler.compile(List.of(
				new BlockPromptCompileRequest(
						"out-009",
						21L,
						5,
						null,
						Map.of(
								"title", "review result",
								"sourceOutputKeys", List.of("developerDocument")
						),
						Map.of("tags", List.of("review")),
						Map.of(),
						List.of()
				)
		));

		assertThat(result.promptFragments()).isEmpty();
		assertThat(result.localActions())
				.singleElement()
				.satisfies(action -> {
					assertThat(action.nodeId()).isEqualTo("out-009");
					assertThat(action.blockId()).isEqualTo(21L);
					assertThat(action.blockOrder()).isEqualTo(5);
					assertThat(action.executionType())
							.isEqualTo(PromptExecutionType.PERSISTENCE);
					assertThat(action.input())
							.containsEntry("title", "review result");
					assertThat(action.options()).containsKey("tags");
				});
		verifyNoInteractions(renderer);
	}
}
