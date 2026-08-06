package com.umc.learninglm.domain.block.service;

import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragmentRequest;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.repository.BlockPromptBatchRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockPromptCompilerImpl implements BlockPromptCompiler {

	private final BlockPromptBatchRepository blockPromptBatchRepository;
	private final BlockPromptRenderer blockPromptRenderer;

	@Override
	public List<CompiledPromptFragment> compile(
			List<BlockPromptCompileRequest> requests
	) {
		if (requests == null || requests.isEmpty()) {
			return List.of();
		}

		List<Long> blockIds = requests.stream()
				.map(BlockPromptCompileRequest::blockId)
				.distinct()
				.toList();
		Map<Long, Block> blockById = blockPromptBatchRepository
				.findAllWithActivePromptTemplateByBlockIdIn(blockIds)
				.stream()
				.collect(Collectors.toMap(
						Block::getBlockId,
						Function.identity()
				));

		return requests.stream()
				.map(request -> compile(request, blockById))
				.toList();
	}

	private CompiledPromptFragment compile(
			BlockPromptCompileRequest request,
			Map<Long, Block> blockById
	) {
		Block block = blockById.get(request.blockId());
		if (block == null) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_TEMPLATE_NOT_FOUND);
		}

		PromptFragment fragment = blockPromptRenderer.render(
				block,
				new PromptFragmentRequest(
						request.blockId(),
						request.blockOrder(),
						request.input(),
						request.options(),
						request.resolvedContext()
				)
		);

		return new CompiledPromptFragment(
				request.nodeId(),
				request.executionType(),
				fragment,
				request.artifacts() == null ? List.of() : List.copyOf(request.artifacts())
		);
	}
}
