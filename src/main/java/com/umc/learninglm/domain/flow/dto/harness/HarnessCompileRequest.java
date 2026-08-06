package com.umc.learninglm.domain.flow.dto.harness;

import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import java.util.List;
import java.util.Map;

public record HarnessCompileRequest(
		String userRequest,
		String topic,
		List<CompiledPromptFragment> fragments,
		Map<String, Object> responseSchema,
		Integer estimatedOutputTokens
) {
}
