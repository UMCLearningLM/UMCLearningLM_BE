package com.umc.learninglm.domain.flow.dto.harness;

import com.umc.learninglm.domain.block.dto.prompt.PromptArtifactValue;
import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import java.util.List;
import java.util.Map;

public record LocalPostAction(
		String nodeId,
		Long blockId,
		Integer blockOrder,
		PromptExecutionType executionType,
		Map<String, Object> input,
		Map<String, Object> options,
		Map<String, Object> resolvedContext,
		List<PromptArtifactValue> artifacts
) {
}
