package com.umc.learninglm.domain.block.dto.prompt;

import java.util.List;

public record BlockPromptCompileResult(
		List<CompiledPromptFragment> promptFragments,
		List<CompiledLocalAction> localActions
) {

	public BlockPromptCompileResult {
		promptFragments = promptFragments == null
				? List.of()
				: List.copyOf(promptFragments);
		localActions = localActions == null
				? List.of()
				: List.copyOf(localActions);
	}
}
