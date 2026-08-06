package com.umc.learninglm.domain.block.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragmentRequest;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.entity.PromptTemplate;
import com.umc.learninglm.domain.block.repository.BlockPromptRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockPromptConverterImpl implements BlockPromptConverter {

	private static final Pattern PLACEHOLDER_PATTERN =
			Pattern.compile("\\{([a-zA-Z][a-zA-Z0-9_.-]*)}");
	private static final String INPUT_PREFIX = "input.";
	private static final String OPTIONS_PREFIX = "options.";
	private static final String RESOLVED_CONTEXT_PREFIX = "resolvedContext.";
	private static final String INPUT_ROOT = "input";
	private static final String OPTIONS_ROOT = "options";
	private static final String RESOLVED_CONTEXT_ROOT = "resolvedContext";
	private static final String BLOCK_INSTRUCTION = "blockInstruction";

	private final BlockPromptRepository blockPromptRepository;
	private final ObjectMapper objectMapper;

	@Override
	public PromptFragment convert(PromptFragmentRequest request) {
		Block block = findBlock(request.blockId());
		PromptTemplate promptTemplate = block.getPromptTemplate();
		validateTemplateStage(block, promptTemplate);
		String content = render(promptTemplate.getPromptBody(), request, block);

		return new PromptFragment(
				block.getBlockId(),
				promptTemplate.getPromptTemplateId(),
				promptTemplate.getVersion(),
				block.getBlockType(),
				request.blockOrder(),
				content
		);
	}

	private Block findBlock(Long blockId) {
		return blockPromptRepository
				.findWithActivePromptTemplateByBlockId(blockId)
				.orElseThrow(() -> new CustomException(
						ErrorCode.BLOCK_PROMPT_TEMPLATE_NOT_FOUND
				));
	}

	private void validateTemplateStage(
			Block block,
			PromptTemplate promptTemplate
	) {
		if (block.getBlockType() != promptTemplate.getStage()) {
			throw new CustomException(
					ErrorCode.BLOCK_PROMPT_TEMPLATE_STAGE_MISMATCH
			);
		}
	}

	private String render(
			String promptBody,
			PromptFragmentRequest request,
			Block block
	) {
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(promptBody);
		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			Object value = resolveValue(matcher.group(1), request, block);
			matcher.appendReplacement(
					result,
					Matcher.quoteReplacement(toPromptText(value))
			);
		}

		matcher.appendTail(result);
		return result.toString();
	}

	private Object resolveValue(
			String placeholder,
			PromptFragmentRequest request,
			Block block
	) {
		if (INPUT_ROOT.equals(placeholder)) {
			return emptyIfNull(request.input());
		}

		if (OPTIONS_ROOT.equals(placeholder)) {
			return emptyIfNull(request.options());
		}

		if (RESOLVED_CONTEXT_ROOT.equals(placeholder)) {
			return emptyIfNull(request.resolvedContext());
		}

		if (BLOCK_INSTRUCTION.equals(placeholder)) {
			if (block.getPromptInstruction() == null
					|| block.getPromptInstruction().isBlank()) {
				throw new CustomException(
						ErrorCode.BLOCK_PROMPT_VARIABLE_MISSING
				);
			}

			return block.getPromptInstruction();
		}

		if (placeholder.startsWith(INPUT_PREFIX)) {
			return requireValue(
					request.input(),
					placeholder.substring(INPUT_PREFIX.length())
			);
		}

		if (placeholder.startsWith(OPTIONS_PREFIX)) {
			return requireValue(
					request.options(),
					placeholder.substring(OPTIONS_PREFIX.length())
			);
		}

		if (placeholder.startsWith(RESOLVED_CONTEXT_PREFIX)) {
			return requireValue(
					request.resolvedContext(),
					placeholder.substring(RESOLVED_CONTEXT_PREFIX.length())
			);
		}

		return resolveUnqualifiedValue(placeholder, request);
	}

	private Map<String, Object> emptyIfNull(Map<String, Object> value) {
		return value == null ? Map.of() : value;
	}

	private Object resolveUnqualifiedValue(
			String placeholder,
			PromptFragmentRequest request
	) {
		List<Object> values = new ArrayList<>();
		addIfPresent(values, request.input(), placeholder);
		addIfPresent(values, request.options(), placeholder);
		addIfPresent(values, request.resolvedContext(), placeholder);

		if (values.isEmpty()) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_VARIABLE_MISSING);
		}

		if (values.size() > 1) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_VARIABLE_AMBIGUOUS);
		}

		return values.get(0);
	}

	private Object requireValue(Map<String, Object> source, String path) {
		LookupResult result = findValue(source, path);

		if (!result.found()) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_VARIABLE_MISSING);
		}

		return result.value();
	}

	private void addIfPresent(
			List<Object> values,
			Map<String, Object> source,
			String path
	) {
		LookupResult result = findValue(source, path);

		if (result.found()) {
			values.add(result.value());
		}
	}

	private LookupResult findValue(Map<String, Object> source, String path) {
		if (source == null) {
			return LookupResult.notFound();
		}

		Object current = source;

		for (String key : path.split("\\.")) {
			if (!(current instanceof Map<?, ?> currentMap)
					|| !currentMap.containsKey(key)) {
				return LookupResult.notFound();
			}

			current = currentMap.get(key);
		}

		return LookupResult.found(current);
	}

	private String toPromptText(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof String
				|| value instanceof Number
				|| value instanceof Boolean) {
			return String.valueOf(value);
		}

		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException exception) {
			throw new CustomException(ErrorCode.BLOCK_PROMPT_CONVERSION_FAILED);
		}
	}

	private record LookupResult(boolean found, Object value) {

		private static LookupResult found(Object value) {
			return new LookupResult(true, value);
		}

		private static LookupResult notFound() {
			return new LookupResult(false, null);
		}
	}
}
