package com.umc.learninglm.domain.block.dto.prompt;

import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import java.util.List;

public record CompiledPromptFragment(
		String nodeId,
		PromptExecutionType executionType,
		PromptFragment fragment,
		List<PromptArtifactValue> artifacts
) {
}
