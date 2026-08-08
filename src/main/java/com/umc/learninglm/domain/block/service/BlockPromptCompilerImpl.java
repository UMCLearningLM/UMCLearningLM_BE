package com.umc.learninglm.domain.block.service;

import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileResult;
import com.umc.learninglm.domain.block.dto.prompt.CompiledLocalAction;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptArtifactValue;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragmentRequest;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.entity.PromptTemplate;
import com.umc.learninglm.domain.block.enums.ExecutionMode;
import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import com.umc.learninglm.domain.block.repository.BlockPromptBatchRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.ArrayList;
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
	public BlockPromptCompileResult compile(
			List<BlockPromptCompileRequest> requests
	) {
		if (requests == null || requests.isEmpty()) {
			return new BlockPromptCompileResult(List.of(), List.of());
		}

		List<Long> blockIds = requests.stream()
				.map(BlockPromptCompileRequest::blockId)
				.distinct()
				.toList();
		Map<Long, Block> blockById = blockPromptBatchRepository
				.findAllWithPromptTemplateByBlockIdIn(blockIds)
				.stream()
				.collect(Collectors.toMap(
						Block::getBlockId,
						Function.identity()
				));

		List<CompiledPromptFragment> promptFragments = new ArrayList<>();
		List<CompiledLocalAction> localActions = new ArrayList<>();
		requests.forEach(request -> compile(
				request,
				blockById,
				promptFragments,
				localActions
		));

		return new BlockPromptCompileResult(promptFragments, localActions);
	}

	private void compile(
			BlockPromptCompileRequest request,
			Map<Long, Block> blockById,
			List<CompiledPromptFragment> promptFragments,
			List<CompiledLocalAction> localActions
	) {
		Block block = blockById.get(request.blockId());
		if (block == null) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_TEMPLATE_NOT_FOUND);
		}

		PromptExecutionType executionType = resolveExecutionType(request, block);
		if (isLocalExecution(executionType)) {
			localActions.add(toLocalAction(request, executionType));
			return;
		}

		validateActiveTemplate(block.getPromptTemplate());
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

		promptFragments.add(new CompiledPromptFragment(
				request.nodeId(),
				executionType,
				fragment,
				normalizeArtifacts(request.artifacts())
		));
	}

	private boolean isLocalExecution(PromptExecutionType executionType) {
		return executionType == PromptExecutionType.LOCAL_TRANSFORM
				|| executionType == PromptExecutionType.PERSISTENCE;
	}

	private CompiledLocalAction toLocalAction(
			BlockPromptCompileRequest request,
			PromptExecutionType executionType
	) {
		return new CompiledLocalAction(
				request.nodeId(),
				request.blockId(),
				request.blockOrder(),
				executionType,
				normalizeMap(request.input()),
				normalizeMap(request.options()),
				normalizeMap(request.resolvedContext()),
				normalizeArtifacts(request.artifacts())
		);
	}

	private void validateActiveTemplate(PromptTemplate promptTemplate) {
		if (promptTemplate == null
				|| !Boolean.TRUE.equals(promptTemplate.getActive())) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_TEMPLATE_NOT_FOUND);
		}
	}

	private Map<String, Object> normalizeMap(Map<String, Object> value) {
		return value == null ? Map.of() : value;
	}

	private List<PromptArtifactValue> normalizeArtifacts(
			List<PromptArtifactValue> value
	) {
		return value == null ? List.of() : List.copyOf(value);
	}

	private PromptExecutionType resolveExecutionType(
			BlockPromptCompileRequest request,
			Block block
	) {
		if (request.executionType() != null) {
			return request.executionType();
		}
		if (block.getDefaultExecutionMode() == ExecutionMode.SYSTEM) {
			return PromptExecutionType.PERSISTENCE;
		}

		return switch (block.getBlockType()) {
			case INPUT, CONTEXT -> PromptExecutionType.CONFIG_ONLY;
			case PROCESS, REVIEW, OUTPUT -> PromptExecutionType.AI_INSTRUCTION;
		};
	}
}
