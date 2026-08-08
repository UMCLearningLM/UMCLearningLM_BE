package com.umc.learninglm.domain.block.dto.prompt;

import com.umc.learninglm.domain.block.enums.BlockType;

public record PromptFragment(
		Long blockId,
		Long promptTemplateId,
		String templateVersion,
		BlockType stage,
		Integer blockOrder,
		String content
) {
}
