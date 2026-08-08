package com.umc.learninglm.domain.flow.dto.harness;

import com.umc.learninglm.domain.block.dto.prompt.CompiledLocalAction;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import java.util.List;
import java.util.Map;

public record HarnessCompileRequest(
		String userRequest,
		String topic,
		List<CompiledPromptFragment> fragments,
		List<CompiledLocalAction> localActions,
		Map<String, Object> responseSchema,
		Integer estimatedOutputTokens
) {
}
