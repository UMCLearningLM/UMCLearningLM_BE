package com.umc.learninglm.domain.flow.dto.harness;

import com.umc.learninglm.domain.block.enums.PromptExecutionType;

public record LocalPostAction(
		String nodeId,
		Long blockId,
		PromptExecutionType executionType
) {
}
