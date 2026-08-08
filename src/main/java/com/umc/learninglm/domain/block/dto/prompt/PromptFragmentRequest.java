package com.umc.learninglm.domain.block.dto.prompt;

import java.util.Map;

public record PromptFragmentRequest(
		Long blockId,
		Integer blockOrder,
		Map<String, Object> input,
		Map<String, Object> options,
		Map<String, Object> resolvedContext
) {
}
