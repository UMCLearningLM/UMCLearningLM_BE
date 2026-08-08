package com.umc.learninglm.domain.block.dto.prompt;

import com.umc.learninglm.domain.block.enums.PromptInputRole;

public record PromptArtifactValue(
		PromptInputRole role,
		Integer order,
		String label,
		Object value
) {
}
